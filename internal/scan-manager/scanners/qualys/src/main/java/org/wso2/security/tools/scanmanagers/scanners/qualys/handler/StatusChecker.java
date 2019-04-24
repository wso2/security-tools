/*
 *
 *   Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.security.tools.scanmanagers.scanners.qualys.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanContext;
import org.wso2.security.tools.scanmanager.scanners.qualys.service.QualysScanner;
import org.wso2.security.tools.scanmanager.scanners.qualys.utils.RequestBodyBuilder;
import org.wso2.security.tools.scanmanger.scanners.qualys.QualysScannerConstants;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Responsible to check the scan status.
 */
public class StatusChecker {

    private static final Log log = LogFactory.getLog(StatusChecker.class);
    private static final int NUM_THREADS = 1;
    private ScheduledExecutorService scheduler;
    private final long initialDelay;
    private final long delayBetweenRuns;
    private static AtomicReference<ScanStatus> currentStatus = new AtomicReference<>();
    private static AtomicReference<String> currentScannerStatus = new AtomicReference<>();
    private QualysApiInvoker qualysApiInvoker;
    private ScanContext scanContext;

    StatusChecker(QualysApiInvoker qualysApiInvoker, ScanContext scanContext, long initialDelay,
            long delayBetweenRuns) {
        this.qualysApiInvoker = qualysApiInvoker;
        this.scanContext = scanContext;
        this.initialDelay = initialDelay;
        this.delayBetweenRuns = delayBetweenRuns;
        this.scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
        currentScannerStatus.set(QualysScannerConstants.SUBMITTED);
        currentStatus.set(ScanStatus.SUBMITTED);
    }

    /**
     * Activate status checker when Qualys scan id is generated.
     */
    public void activateStatusChecker() {
        log.info("-------------------");
        Runnable checkStatusTask = new CheckStatusTask();
        scheduler.scheduleWithFixedDelay(checkStatusTask, initialDelay, delayBetweenRuns, TimeUnit.MINUTES);
        log.info("activated status checker");
    }

    /**
     * Runnable class to check status task.
     */
    private final class CheckStatusTask implements Runnable {
        @Override public void run() {
            if (log.isDebugEnabled()) {
                CallbackUtil.persistScanLog(scanContext.getJobID(),
                        "Start scheduled Executor Service to check scan status periodically", LogType.DEBUG);
            }
            String status;
            try {
                status = qualysApiInvoker.retrieveStatus(QualysScanner.host, scanContext.getScannerScanId());
                if (currentScannerStatus.get().equalsIgnoreCase(status)) {
                    currentScannerStatus = new AtomicReference<>(status);
                    updateStatus(status);
                }
            } catch (IOException | ParserConfigurationException | SAXException e) {
                CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null,
                        scanContext.getScannerScanId());
                CallbackUtil.persistScanLog(scanContext.getJobID(), "Could not retrieve the status",
                        LogType.ERROR);
                scheduler.shutdown();
            }
        }

        /**
         * Update current status to scan manager.
         *
         * @param status status
         * @throws IOException                  Occurred IO exception while calling the api
         * @throws ParserConfigurationException Occurred while retrieving the status
         * @throws SAXException                 Occurred while retrieving the status
         */
        private void updateStatus(String status) throws ParserConfigurationException, SAXException, IOException {
            if (status != null) {
                switch (status) {
                case QualysScannerConstants.SUBMITTED:
                    currentStatus.set(ScanStatus.RUNNING);
                    CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.RUNNING, null,
                            scanContext.getScannerScanId());
                    break;
                case QualysScannerConstants.RUNNING:
                    currentStatus.set(ScanStatus.RUNNING);
                    CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.RUNNING, null,
                            scanContext.getScannerScanId());
                    break;
                case QualysScannerConstants.FINISHED:
                    String authStatus = qualysApiInvoker
                            .retrieveAuthStatus(QualysScanner.host, scanContext.getScannerScanId());
                    String resultsStatus = qualysApiInvoker
                            .retrieveResultStatus(QualysScanner.host, scanContext.getScannerScanId());
                    Boolean isScanAuthenticationSuccessfull = false;
                    Boolean isScanSuccessFull = false;
                    if (authStatus != null) {
                        switch (authStatus) {
                        case QualysScannerConstants.AUTH_PARTIAL:
                            currentStatus.set(ScanStatus.FAILED);
                            isScanAuthenticationSuccessfull = false;
                            CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.FAILED, null,
                                    scanContext.getScannerScanId());
                            CallbackUtil.persistScanLog(scanContext.getJobID(),
                                    "Scan is failed due to authentication failure", LogType.ERROR);
                            scheduler.shutdown();
                            break;
                        case QualysScannerConstants.AUTH_FAILED:
                            currentStatus.set(ScanStatus.FAILED);
                            isScanAuthenticationSuccessfull = false;
                            CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.FAILED, null,
                                    scanContext.getScannerScanId());
                            CallbackUtil.persistScanLog(scanContext.getJobID(),
                                    "Scan is failed due to authentication failure", LogType.ERROR);
                            scheduler.shutdown();
                            break;
                        case QualysScannerConstants.AUTH_SUCCESSFUL:
                            isScanAuthenticationSuccessfull = true;
                            CallbackUtil.persistScanLog(scanContext.getJobID(), "Authentication is succeed.",
                                    LogType.INFO);
                            break;
                        default:
                            isScanAuthenticationSuccessfull = false;
                        }
                    }
                    if (resultsStatus != null) {
                        switch (resultsStatus) {
                        case QualysScannerConstants.NO_HOST_ALIVE:
                            isScanSuccessFull = false;
                            currentStatus.set(ScanStatus.FAILED);
                            CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.FAILED, null,
                                    scanContext.getScannerScanId());
                            CallbackUtil.persistScanLog(scanContext.getJobID(),
                                    "Scan is failed due NO_HOST_LIVE. Please check qualys documentation for"
                                            + " more information", LogType.ERROR);
                            scheduler.shutdown();
                            break;
                        case QualysScannerConstants.NO_WEB_SERVICE:
                            isScanSuccessFull = false;
                            currentStatus.set(ScanStatus.FAILED);
                            CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.FAILED, null,
                                    scanContext.getScannerScanId());
                            CallbackUtil.persistScanLog(scanContext.getJobID(),
                                    "Scan is failed due NO_WEB_SERVICE. Please check qualys documentation "
                                            + "for more information",
                                    LogType.ERROR);
                            scheduler.shutdown();
                            break;
                        case QualysScannerConstants.SCAN_RESULTS_INVALID:
                            isScanSuccessFull = false;
                            currentStatus.set(ScanStatus.FAILED);
                            CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.FAILED, null,
                                    scanContext.getScannerScanId());
                            CallbackUtil.persistScanLog(scanContext.getJobID(),
                                    "Scan is finished but scan result is invalid. Please check qualys "
                                            + "documentation for more information",
                                    LogType.ERROR);
                            scheduler.shutdown();
                            break;
                        case QualysScannerConstants.TIME_LIMIT_EXCEEDED:
                            isScanSuccessFull = false;
                            Runnable scanRelauncher = new ScanReLauncher();
                            scanRelauncher.run();
                            CallbackUtil.persistScanLog(scanContext.getJobID(),
                                    "Scan is relaunched due to TIME LIMIT EXCEED", LogType.ERROR);
                            scheduler.shutdown();
                            break;
                        case QualysScannerConstants.SERVICE_ERROR:
                            isScanSuccessFull = false;
                            currentStatus.set(ScanStatus.ERROR);
                            CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null,
                                    scanContext.getScannerScanId());
                            CallbackUtil.persistScanLog(scanContext.getJobID(),
                                    "Scan is failed due to service error. Please check qualys documentation"
                                            + " for more information",
                                    LogType.ERROR);
                            scheduler.shutdown();
                            break;
                        case QualysScannerConstants.SCAN_INTERNAL_ERROR:
                            isScanSuccessFull = false;
                            currentStatus.set(ScanStatus.ERROR);
                            CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null,
                                    scanContext.getScannerScanId());
                            CallbackUtil.persistScanLog(scanContext.getJobID(),
                                    "Scan is failed due to scan internal error. Please check qualys "
                                            + "documentation for more information",
                                    LogType.ERROR);
                            scheduler.shutdown();
                            break;
                        case QualysScannerConstants.SUCCESSFUL:
                            isScanSuccessFull = true;
                            currentStatus.set(ScanStatus.COMPLETED);
                            CallbackUtil.persistScanLog(scanContext.getJobID(), "Scan is completed in Qualys"
                                            + " end", LogType.INFO);
                            break;
                        }
                    }
                    if ((isScanAuthenticationSuccessfull) && (isScanSuccessFull)) {
                        try {
                            String[] reportTypes = { QualysScannerConstants.PDF_TYPE, QualysScannerConstants.XML_TYPE,
                                    QualysScannerConstants.HTML_BASE64_TYPE, QualysScannerConstants.CSV_V2_TYPE };
                            for (String types : reportTypes) {
                                String createReportRequestBody = RequestBodyBuilder
                                        .buildCreateReportRequestBody(scanContext.getWebAppId(), scanContext.getJobID(),
                                                types);
                                String reportId = qualysApiInvoker
                                        .createReport(QualysScanner.host, createReportRequestBody);
                                if (reportId != null) {
                                    CallbackUtil.persistScanLog(scanContext.getJobID(),
                                            types + " Report is successfully created : " + scanContext.getJobID(),
                                            LogType.INFO);
                                    qualysApiInvoker.downloadReport(QualysScanner.host, reportId);
                                    CallbackUtil.persistScanLog(scanContext.getJobID(),
                                            types + " Report is successfully downloaded : " + scanContext.getJobID(),
                                            LogType.INFO);
                                }
                            }
                            // TODO: 4/23/19 Find a way to add report path
                            CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.COMPLETED, null,
                                    scanContext.getScannerScanId());
                            scheduler.shutdown();
                        } catch (TransformerException e) {
                            CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null,
                                    scanContext.getScannerScanId());
                            CallbackUtil.persistScanLog(scanContext.getJobID(),
                                    "Failed to create report : " + scanContext.getJobID(), LogType.ERROR);
                            scheduler.shutdown();
                        }
                    }
                    break;
                case QualysScannerConstants.ERROR:
                    CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null,
                            scanContext.getScannerScanId());
                    CallbackUtil.persistScanLog(scanContext.getJobID(), "SCAN FAILED", LogType.ERROR);
                    scheduler.shutdown();
                    break;
                case QualysScannerConstants.TIME_LIMIT_EXCEEDED:
                    Runnable scanRelauncher = new ScanReLauncher();
                    scanRelauncher.run();
                    CallbackUtil.persistScanLog(scanContext.getJobID(), "Scan is relaunched due to TIME LIMIT "
                                    + "EXCEED", LogType.INFO);
                    scheduler.shutdown();
                    break;
                case QualysScannerConstants.SCANNER_NOT_AVAILABLE:
                    CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null,
                            scanContext.getScannerScanId());
                    CallbackUtil.persistScanLog(scanContext.getJobID(),
                            "Scan is failed due to due to scanner not available"
                                    + ". Please check qualys documentation for more information\" ",
                            LogType.ERROR);
                    scheduler.shutdown();
                    break;
                case QualysScannerConstants.CANCELLED:
                    CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.CANCELED, null,
                            scanContext.getScannerScanId());
                    CallbackUtil.persistScanLog(scanContext.getJobID(), "SCAN IS CANCELLED", LogType.INFO);
                    scheduler.shutdown();
                    break;
                }
            }
        }
    }

    /**
     * ReLaunch Scan.
     */
    private final class ScanReLauncher implements Runnable {
        @Override public void run() {
            try {
                qualysApiInvoker
                        .launchScan(QualysScanner.host, RequestBodyBuilder.buildLaunchScanRequestBody(scanContext));
            } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
                CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null,
                        scanContext.getScannerScanId());
                CallbackUtil
                        .persistScanLog(scanContext.getJobID(), "Failed to relaunch teh scan", LogType.ERROR);
            }
        }
    }
}
