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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.model.CallbackLog;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.ErrorProcessingUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.QualysScanContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible to initiate the Scan.
 */
public class ScanExecutor implements Runnable {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(ScanExecutor.class);
    private QualysScanContext qualysScanContext;
    private Map<String, List<String>> fileMap;
    private QualysScanHandler qualysScanHandler;

    public ScanExecutor(QualysScanContext qualysScanContext, Map<String, List<String>> fileMap,
            QualysScanHandler qualysScanHandler) {
        if (log.isDebugEnabled()) {
            String logMessage =
                    "Scan Executor thread is being initialized for the application:" + qualysScanContext.getWebAppId();
            log.debug(new CallbackLog(qualysScanContext.getJobID(), logMessage));
        }
        this.qualysScanContext = qualysScanContext;
        this.fileMap = fileMap;
        this.qualysScanHandler = qualysScanHandler;
    }

    @Override public void run() {
        String authScriptId;
        String scannerScanId;

        // Prepare Web Application for launching scan.
        try {

            // If scanner scan id is empty, then current request is to initiate either new scan or
            // pre process for the current request is not completed successfully prior to container unavailability
            if (StringUtils.isEmpty(qualysScanContext.getScannerScanId())) {

                // Purging Scan before launching the scan.
                qualysScanHandler.purgeScan(qualysScanContext.getWebAppId(), qualysScanContext.getJobID());

                authScriptId = qualysScanHandler
                        .getAuthScriptId(qualysScanContext.getWebAppId(), qualysScanContext.getJobID(),
                                qualysScanContext.getWebAppAuthenticationRecordBuilder());

                if (StringUtils.isEmpty(qualysScanContext.getAuthId())) {

                    // Set authentication script id.
                    qualysScanContext.setAuthId(authScriptId);
                }

                // Update web application related configurations.
                qualysScanHandler.updateWebApp(qualysScanContext);

                if (fileMap.containsKey(QualysScannerConstants.CRAWLINGSCRIPTS)
                        && fileMap.get(QualysScannerConstants.CRAWLINGSCRIPTS).size() != 0) {
                    // Set list of crawling script objects.
                    qualysScanContext.setListOfCrawlingScripts(fileMap.get(QualysScannerConstants.CRAWLINGSCRIPTS));

                    // Add crawling script and it's configurations for scan.
                    qualysScanHandler.addCrawlingSetting(qualysScanContext.getListOfCrawlingScripts(),
                            qualysScanContext.getJobID(), qualysScanContext.getWebAppId(),
                            qualysScanContext.getWebAppName());
                }

                // Launch Scan.
                scannerScanId = qualysScanHandler.launchScan(qualysScanContext);
                qualysScanContext.setScannerScanId(scannerScanId);

                // Persist Scan Context.
                ObjectMapper scanContextObjectMapper = new ObjectMapper();
                scanContextObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                String scanContextJsonString = scanContextObjectMapper.writeValueAsString(qualysScanContext);
                CallbackUtil.updateScanContext(qualysScanContext.getJobID(), scanContextJsonString, Long.valueOf(0));
            }

            // Initiate ScheduledExecutorService to update scan status.
            initiateStatusChecker();
        } catch (ScannerException e) {
            String message = "Failed to start scan " + qualysScanContext.getJobID() + ". " + ErrorProcessingUtil
                    .getFullErrorMessage(e);
            log.error(new CallbackLog(qualysScanContext.getJobID(), message));
            try {

                // Performing clean up task.
                if (!StringUtils.isEmpty(qualysScanContext.getAuthId())) {
                    message = "Deleting added authentication script before update the status to scan manager";
                    log.info(new CallbackLog(qualysScanContext.getJobID(), message));
                    qualysScanHandler.doCleanUp(qualysScanContext.getAuthId(), qualysScanContext.getJobID());
                }
                log.error(new CallbackLog(qualysScanContext.getJobID(), "Scan status is updating to ERROR"));
                CallbackUtil.updateScanStatus(qualysScanContext.getJobID(), ScanStatus.ERROR, null, null);
            } catch (ScannerException e1) {
                message = "Error occurred while doing the cleanup task. " + qualysScanContext.getJobID()
                        + ErrorProcessingUtil.getFullErrorMessage(e1);
                log.error(new CallbackLog(qualysScanContext.getJobID(), message));
                CallbackUtil.updateScanStatus(qualysScanContext.getJobID(), ScanStatus.ERROR, null, null);
            }
        } catch (IOException e) {
            String message = "Error occured while generating scan context file";
            log.error(new CallbackLog(qualysScanContext.getJobID(), message));
        }
    }

    private void initiateStatusChecker() {
        StatusHandler statusChecker = new StatusHandler(qualysScanHandler, qualysScanContext,
                qualysScanContext.getSchedulerDelay(), qualysScanContext.getSchedulerDelay());
        statusChecker.activateStatusHandler();
    }
}
