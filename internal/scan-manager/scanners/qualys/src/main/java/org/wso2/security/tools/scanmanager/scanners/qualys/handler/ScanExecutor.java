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

package org.wso2.security.tools.scanmanager.scanners.qualys.handler;

import org.apache.log4j.Logger;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.ErrorProcessingUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.config.QualysScannerConfiguration;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanContext;

import java.util.List;
import java.util.Map;

/**
 * This class is responsible to initiate the Scan.
 */
public class ScanExecutor implements Runnable {

    private static final Logger log = Logger.getLogger(ScanExecutor.class);
    private ScanContext scanContext;
    private Map<String, List<String>> fileMap;
    private QualysScanHandler qualysScanHandler;

    public ScanExecutor(ScanContext scanContext, Map<String, List<String>> fileMap,
            QualysScanHandler qualysScanHandler) {
        if (log.isDebugEnabled()) {
            String logMessage =
                    "Scan Executor thread is being initialized for the application:" + scanContext.getWebAppId();
            log.debug(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobID(), logMessage, LogType.DEBUG);
        }
        this.scanContext = scanContext;
        this.fileMap = fileMap;
        this.qualysScanHandler = qualysScanHandler;
    }

    @Override
    public void run() {
        String authScriptId;
        String scannerScanId;
        // Prepare Web Application for launching scan.
        try {
            authScriptId = qualysScanHandler
                    .prepareScan(scanContext.getWebAppId(), scanContext.getJobID(), scanContext.getWebAppName(),
                            fileMap, QualysScannerConfiguration.getInstance().getHost());
            // Set ScanContext Object.
            scanContext.setAuthId(authScriptId);
            // Launch Scan.
            scannerScanId = qualysScanHandler
                    .launchScan(scanContext, QualysScannerConfiguration.getInstance().getHost());
            scanContext.setScannerScanId(scannerScanId);
            // Initiate ScheduledExecutorService to update scan status.
            StatusHandler statusChecker = new StatusHandler(qualysScanHandler, scanContext,
                    scanContext.getSchedulerDelay(), scanContext.getSchedulerDelay());
            statusChecker.activateStatusHandler();
        } catch (ScannerException e) {
            String message =
                    "Failed to start scan " + scanContext.getJobID() + ErrorProcessingUtil.getFullErrorMessage(e);
            CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null, null);
            CallbackUtil.persistScanLog(scanContext.getJobID(), message, LogType.ERROR);
        } finally {
            try {
                // Performing clean up task.
                qualysScanHandler.doCleanUp(scanContext);
            } catch (ScannerException e) {
                String message =
                        "Error occurred while doing the cleanup task. " + scanContext.getJobID() + ErrorProcessingUtil
                                .getFullErrorMessage(e);
                CallbackUtil.persistScanLog(scanContext.getJobID(), message, LogType.ERROR);

            }
        }
    }
}
