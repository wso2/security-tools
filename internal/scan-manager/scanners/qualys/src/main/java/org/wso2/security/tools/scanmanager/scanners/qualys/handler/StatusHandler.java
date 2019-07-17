/*
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
 */

package org.wso2.security.tools.scanmanager.scanners.qualys.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.model.CallbackLog;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.ErrorProcessingUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Responsible to check the scan status.
 */
public class StatusHandler {

    private static final Logger log = LogManager.getLogger(StatusHandler.class);
    private static final int NUM_THREADS = 1;
    private ScheduledExecutorService scheduler;
    private final long initialDelay;
    private final long delayBetweenRuns;

    // Current status in Scan Manager perspective.
    private ScanStatus currentStatus;

    // Current status in Qualys scanner perspective.
    private String currentScannerStatus;
    private QualysScanHandler qualysScanHandler;
    private ScanContext scanContext;

    public StatusHandler(QualysScanHandler qualysScanHandler, ScanContext scanContext, long initialDelay,
            long delayBetweenRuns) {
        this.qualysScanHandler = qualysScanHandler;
        this.scanContext = scanContext;
        this.initialDelay = initialDelay;
        this.delayBetweenRuns = delayBetweenRuns;
        this.scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
        currentScannerStatus = QualysScannerConstants.SUBMITTED;
        currentStatus = ScanStatus.SUBMITTED;
    }

    /**
     * Activate status checker when Qualys scan id is generated.
     */
    public void activateStatusHandler() {
        Runnable checkStatusTask = new CheckStatusTask();
        scheduler.scheduleWithFixedDelay(checkStatusTask, initialDelay, delayBetweenRuns, TimeUnit.MINUTES);
        if (log.isDebugEnabled()) {
            String message = "Status Checker scheduler service is activated";
            log.debug(new CallbackLog(scanContext.getJobID(), message));
        }
    }

    /**
     * Runnable class to check status task.
     */
    private final class CheckStatusTask implements Runnable {

        @Override public void run() {
            if (log.isDebugEnabled()) {
                String message = " Scheduled Executor Service is started to check scan status periodically";
                log.debug(new CallbackLog(scanContext.getJobID(), message));
            }
            String status;
            try {
                // Retrieve Qualys Scan status from Qualys Scanner.
                status = qualysScanHandler.retrieveScanStatus(scanContext.getScannerScanId());
                if (!currentScannerStatus.equalsIgnoreCase(status)) {
                    currentScannerStatus = status;

                    // Map qualys scanner status with scan manager status.
                    ScanStatus tempScanManagerStatus = mapStatus(status);
                    if (currentStatus.compareTo(tempScanManagerStatus) == 0) {
                        currentStatus = tempScanManagerStatus;
                        updateStatus(currentStatus);
                    }
                }
            } catch (ScannerException e) {
                log.error(new CallbackLog(scanContext.getJobID(), ErrorProcessingUtil.getFullErrorMessage(e)));
                CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null,
                        scanContext.getScannerScanId());
                try {
                    qualysScanHandler.doCleanUp(scanContext);
                } catch (ScannerException e1) {
                    String message = "Error occurred while doing the cleanup task. " + scanContext.getJobID()
                            + ErrorProcessingUtil.getFullErrorMessage(e);
                    log.error(new CallbackLog(scanContext.getJobID(), message));
                }
                scheduler.shutdown();
            }
        }

        /**
         * Map the Qualys Scanner status with Scan Manager Status.
         *
         * @param status Qualys Scanner Status
         * @return Scan Manager Status
         * @throws ScannerException Error occurred while doing mapping task
         */
        private synchronized ScanStatus mapStatus(String status) throws ScannerException {
            Runnable scanRelauncher;
            ScanStatus tempScanStatus = null;
            switch (status) {
            case QualysScannerConstants.SUBMITTED:
            case QualysScannerConstants.RUNNING:
                tempScanStatus = ScanStatus.RUNNING;
                break;
            case QualysScannerConstants.ERROR:
                tempScanStatus = ScanStatus.ERROR;
                break;
            case QualysScannerConstants.TIME_LIMIT_EXCEEDED:
                scanRelauncher = new ScanReLauncher();
                scanRelauncher.run();
                log.info(new CallbackLog(scanContext.getJobID(), "Scan is relaunched due to TIME LIMIT EXCEED"));
                scheduler.shutdown();
                break;
            case QualysScannerConstants.SCANNER_NOT_AVAILABLE:
                log.info(new CallbackLog(scanContext.getJobID(),
                        "Scan is relaunched due to Scanner" + " is not available"));
                scanRelauncher = new ScanReLauncher();
                scanRelauncher.run();
                scheduler.shutdown();
                break;
            case QualysScannerConstants.CANCELLED:
                tempScanStatus = ScanStatus.CANCELED;
                break;
            case QualysScannerConstants.FINISHED:
                String authStatus = qualysScanHandler.retrieveAuthStatus(scanContext.getScannerScanId());
                String resultsStatus = qualysScanHandler.retrieveResultStatus(scanContext.getScannerScanId());
                if ((isScanAuthenticationSucceeded(authStatus)) && (isResultSucceeded(resultsStatus))) {
                    String logMessage =
                            "Scan is finished. Authentication status : " + authStatus + " and result status : "
                                    + resultsStatus;
                    log.info(new CallbackLog(scanContext.getJobID(), logMessage));
                    tempScanStatus = ScanStatus.COMPLETED;
                } else {
                    String logMessage = "Scan is finished with error. Authentication status : " + authStatus
                            + " and result status : " + resultsStatus;
                    log.info(new CallbackLog(scanContext.getJobID(), logMessage));
                    tempScanStatus = ScanStatus.ERROR;
                }
                break;
            default:
                break;
            }
            return tempScanStatus;
        }

        /**
         * Update scan status in Scan Manager.
         *
         * @param scanStatus Scan status of Scan Manager
         * @throws ScannerException Error occurred while updating the scan status
         */
        private synchronized void updateStatus(ScanStatus scanStatus) throws ScannerException {
            currentStatus = scanStatus;
            String logMessage;
            switch (scanStatus) {
            case COMPLETED:
                logMessage = "Scan results are ready for the application: " + scanContext.getWebAppId();
                log.info(new CallbackLog(scanContext.getJobID(), logMessage));
                ReportHandler reportHandler = new ReportHandler(qualysScanHandler);
                if (reportHandler.execute(scanContext)) {
                    log.info(new CallbackLog(scanContext.getJobID(), "Scan is successfully completed"));
                    CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.COMPLETED,
                            scanContext.getScriptFilesLocation(), scanContext.getScannerScanId());
                }
                scheduler.shutdown();
                break;
            case RUNNING:
            case SUBMITTED:
                logMessage =
                        "Scan status for the application: " + scanContext.getWebAppId() + " is updated to " + scanStatus
                                .name();
                log.info(new CallbackLog(scanContext.getJobID(), logMessage));
                CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.RUNNING, null,
                        scanContext.getScannerScanId());
                break;
            case CANCELED:
            case ERROR:
                logMessage =
                        "Scan status for the application: " + scanContext.getWebAppId() + " is updated to " + scanStatus
                                .name();
                log.info(new CallbackLog(scanContext.getJobID(), logMessage));
                CallbackUtil.updateScanStatus(scanContext.getJobID(), scanStatus, null,
                 scanContext.getScannerScanId());
                scheduler.shutdown();
                break;
            default:
                break;
            }
        }

        /**
         * Retrive whether authentication is succeed or not based on auth status.
         *
         * @param authStatus authentication status
         * @return Return true if authentication is successful
         */
        private synchronized boolean isScanAuthenticationSucceeded(String authStatus) {
            boolean isScanAuthenticationSuccessful = false;
            switch (authStatus) {
            case QualysScannerConstants.AUTH_PARTIAL:
                log.error(new CallbackLog(scanContext.getJobID(),
                        "Scan is failed since authentication is partially " + "successful"));
                break;
            case QualysScannerConstants.AUTH_FAILED:
                log.error(new CallbackLog(scanContext.getJobID(), "Scan is failed due to authentication failure"));
                break;
            case QualysScannerConstants.AUTH_SUCCESSFUL:
                isScanAuthenticationSuccessful = true;
                log.info(new CallbackLog(scanContext.getJobID(),
                        "Authentication of scanning web application" + " is got succeeded."));
                break;
            default:
                isScanAuthenticationSuccessful = false;
            }
            return isScanAuthenticationSuccessful;
        }

        /**
         * Retrive whether result is succeed or not based on result status.
         *
         * @param resultStatus result status
         * @return Return true if result is successful
         */
        private synchronized boolean isResultSucceeded(String resultStatus) {
            boolean isScanSuccessFull = false;
            switch (resultStatus) {
            case QualysScannerConstants.NO_HOST_ALIVE:
            case QualysScannerConstants.NO_WEB_SERVICE:
                log.error(new CallbackLog(scanContext.getJobID(),
                        "Scan is failed " + resultStatus + " ." + " Please check qualys documentation for"
                                + " more information"));
                break;
            case QualysScannerConstants.SCAN_RESULTS_INVALID:
                log.error(new CallbackLog(scanContext.getJobID(),
                        "Scan is finished but scan result is invalid. " + "Please check qualys "
                                + "documentation for more information"));
                break;
            case QualysScannerConstants.TIME_LIMIT_EXCEEDED:
                log.error(new CallbackLog(scanContext.getJobID(), "Scan is relaunched due to TIME LIMIT EXCEED"));
                Runnable scanRelauncher = new ScanReLauncher();
                scanRelauncher.run();
                scheduler.shutdown();
                break;
            case QualysScannerConstants.SERVICE_ERROR:
                log.error(new CallbackLog(scanContext.getJobID(), "Scan is failed due to service error. "
                        + "Please check qualys documentation for more information"));
                break;
            case QualysScannerConstants.SCAN_INTERNAL_ERROR:
                log.error(new CallbackLog(scanContext.getJobID(), "Scan is failed due to scan internal error. "
                        + "Please check qualys documentation for more information"));
                break;
            case QualysScannerConstants.SUCCESSFUL:
                isScanSuccessFull = true;
                log.info(new CallbackLog(scanContext.getJobID(),
                        "Scan is completed in Qualys end. Please wait to" + " create and download reports"));
                break;
            default:
            }
            return isScanSuccessFull;
        }

    }

    /**
     * ReLaunch Scan.
     */
    private final class ScanReLauncher implements Runnable {
        @Override public void run() {
            try {
                qualysScanHandler.launchScan(scanContext);
            } catch (ScannerException e) {
                log.error(new CallbackLog(scanContext.getJobID(),
                        "Failed to relaunch teh scan. " + ErrorProcessingUtil.getFullErrorMessage(e)));
                CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null,
                        scanContext.getScannerScanId());
            } finally {
                try {
                    qualysScanHandler.doCleanUp(scanContext);
                } catch (ScannerException e) {
                    String message = "Error occurred while doing the cleanup task. " + scanContext.getJobID()
                            + ErrorProcessingUtil.getFullErrorMessage(e);
                    log.error(new CallbackLog(scanContext.getJobID(), message));
                    CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null,
                            scanContext.getScannerScanId());
                }
            }
        }
    }
}
