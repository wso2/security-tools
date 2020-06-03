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
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.model.CallbackLog;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.ErrorProcessingUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanContext;

import java.util.List;
import java.util.Map;

/**
 * This class is responsible to initiate the Scan.
 */
public class ScanExecutor implements Runnable {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(ScanExecutor.class);
    private ScanContext scanContext;
    private Map<String, List<String>> fileMap;
    private QualysScanHandler qualysScanHandler;

    public ScanExecutor(ScanContext scanContext, Map<String, List<String>> fileMap,
            QualysScanHandler qualysScanHandler) {
        if (log.isDebugEnabled()) {
            String logMessage =
                    "Scan Executor thread is being initialized for the application:" + scanContext.getWebAppId();
            log.debug(new CallbackLog(scanContext.getJobID(), logMessage));
        }
        this.scanContext = scanContext;
        this.fileMap = fileMap;
        this.qualysScanHandler = qualysScanHandler;
    }

    @Override public void run() {
        String authScriptId;
        String scannerScanId;

        // Prepare Web Application for launching scan.
        try {
            // Purging Scan before launching the scan.
            qualysScanHandler.purgeScan(scanContext.getWebAppId(), scanContext.getJobID());

            authScriptId = qualysScanHandler.getAuthScriptId(scanContext.getWebAppId(), scanContext.getJobID(),
                    scanContext.getWebAppAuthenticationRecordBuilder());

            // Set ScanContext Object.
            scanContext.setAuthId(authScriptId);

            // Update web application related configurations.
            qualysScanHandler.updateWebApp(scanContext);

            // Set list of crawling script objects.
            scanContext.setListOfCrawlingScripts(fileMap.get(QualysScannerConstants.CRAWLINGSCRIPTS));

            // Add crawling script and it's configurations for scan.
            qualysScanHandler.addCrawlingSetting(scanContext.getListOfCrawlingScripts(), scanContext.getJobID(),
                    scanContext.getWebAppId(), scanContext.getWebAppName());

            // Launch Scan.
            scannerScanId = qualysScanHandler.launchScan(scanContext);
            scanContext.setScannerScanId(scannerScanId);

            // Initiate ScheduledExecutorService to update scan status.
            StatusHandler statusChecker = new StatusHandler(qualysScanHandler, scanContext,
                    scanContext.getSchedulerDelay(), scanContext.getSchedulerDelay());
            statusChecker.activateStatusHandler();
        } catch (ScannerException e) {
            String message = "Failed to start scan " + scanContext.getJobID() + ". " + ErrorProcessingUtil
                    .getFullErrorMessage(e);
            log.error(new CallbackLog(scanContext.getJobID(), message));
            try {
                // Performing clean up task.
                if (scanContext.getAuthId() != null) {
                    message = "Deleting added authentication script before update the status to scan manager";
                    log.info(new CallbackLog(scanContext.getJobID(), message));
                    qualysScanHandler.doCleanUp(scanContext.getAuthId(), scanContext.getJobID());
                }
                log.error(new CallbackLog(scanContext.getJobID(), "Scan status is updating to ERROR"));
                CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null, null);
            } catch (ScannerException e1) {
                message = "Error occurred while doing the cleanup task. " + scanContext.getJobID() + ErrorProcessingUtil
                        .getFullErrorMessage(e1);
                log.error(new CallbackLog(scanContext.getJobID(), message));
                CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null, null);
            }
        }
    }
}
