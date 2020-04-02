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

package org.wso2.security.tools.scanmanager.scanners.qualys.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wso2.security.tools.scanmanager.common.internal.model.ScannerScanRequest;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.exception.InvalidRequestException;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.model.CallbackLog;
import org.wso2.security.tools.scanmanager.scanners.common.service.Scanner;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.ErrorProcessingUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.config.QualysScannerConfiguration;
import org.wso2.security.tools.scanmanager.scanners.qualys.handler.QualysApiInvoker;
import org.wso2.security.tools.scanmanager.scanners.qualys.handler.QualysScanHandler;
import org.wso2.security.tools.scanmanager.scanners.qualys.handler.ScanExecutor;
import org.wso2.security.tools.scanmanager.scanners.qualys.handler.WebAppAuthFactory;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.CrawlingScope;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanContext;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanType;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScannerApplianceType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible to initiate the generic use cases of Qualys scanner
 */
@Component("QualysScanner") @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON) public class QualysScanner
        implements Scanner {

    private static final Logger log = LogManager.getLogger(QualysScanner.class);
    private QualysScanHandler qualysScanHandler;
    private ScanContext scanContext;

    // Scan executor thread.
    Thread scanExecutorThread;

    public QualysScanner() throws IOException, ScannerException {
        loadConfiguration();
        QualysApiInvoker qualysApiInvoker = new QualysApiInvoker();
        qualysApiInvoker.setBasicAuth(setCredentials());
        this.qualysScanHandler = new QualysScanHandler(qualysApiInvoker);
        this.scanContext = new ScanContext();

        // Setting callbackUrls.
        String callbackUrl =
                ScannerConstants.HTTP_PROTOCOL + System.getenv(ScannerConstants.SCAN_MANAGER_HOST) + ":" + System
                        .getenv(ScannerConstants.SCAN_MANAGER_PORT) + QualysScannerConfiguration.getInstance()
                        .getConfigProperty(ScannerConstants.SCAN_MANAGER_CALLBACK_URL_ENDPOINT);
        String logCallbackUrl = callbackUrl + QualysScannerConfiguration.getInstance()
                .getConfigProperty(ScannerConstants.SCAN_MANAGER_CALLBACK_LOG);
        String statusCallbackUrl = callbackUrl + QualysScannerConfiguration.getInstance()
                .getConfigProperty(ScannerConstants.SCAN_MANAGER_CALLBACK_STATUS);
        Long callbackRetryInterval = Long.parseLong(QualysScannerConfiguration.getInstance()
                .getConfigProperty(ScannerConstants.CALLBACK_RETRY_INCREASE_SECONDS));
        CallbackUtil.setCallbackUrls(logCallbackUrl, statusCallbackUrl, callbackRetryInterval);
    }

    @Override public void startScan(ScannerScanRequest scanRequest) {
        scanContext.setWebAppName(scanRequest.getPropertyMap().get(QualysScannerConstants.
                QUALYS_WEBAPP_KEYWORD).get(0));
        scanContext.setApplicationUrl(scanRequest.getPropertyMap().get(QualysScannerConstants.SCAN_URL).get(0));
        scanContext.setSchedulerDelay(QualysScannerConfiguration.getInstance().getSchedulerDelay());
        if (Thread.currentThread().isInterrupted()) {
            String message = "Current thread is interrupted. ";
            log.error(new CallbackLog(scanContext.getJobID(), message));
        } else {
            // Spawn thread to start the scan.
            startQualysScan(scanRequest.getFileMap());
            String logMessage = "Given parameters are validated and submitted for scanning.";
            log.info(new CallbackLog(scanContext.getJobID(), logMessage));
        }
    }

    @Override public boolean validateStartScan(ScannerScanRequest scannerScanRequest) {
        boolean isValidParameters = false;
        try {
            isValidParameters = isValidParameters(scannerScanRequest);
        } catch (InvalidRequestException | ScannerException e) {
            callbackErrorReport(ErrorProcessingUtil.getFullErrorMessage(e));
        }
        return isValidParameters;
    }

    @Override public void cancelScan(ScannerScanRequest scanRequest) {
        try {
            qualysScanHandler.cancelScan(scanContext);
        } catch (ScannerException e) {
            String message = "Error occurred while cancelling scan.";
            callbackErrorReport(message);
        }
    }

    @Override public boolean validateCancelScan(ScannerScanRequest scannerScanRequest) {
        return true;
    }

    /**
     * This method is responsible to spawn a new thread to execute qualys scanning process.
     */
    private void startQualysScan(Map<String, List<String>> fileMap) {
        ScanExecutor scanExecutor = new ScanExecutor(scanContext, fileMap, qualysScanHandler);
        scanExecutorThread = new Thread(scanExecutor);
        scanExecutorThread.setName("Scan Executor Thread for Qualys");
        scanExecutorThread.start();
    }

    /**
     * Validate Scan Parameters related to Qualys Scanner.
     *
     * @param scannerScanRequest Scanner Request
     * @return True if all parameters are valid
     * @throws InvalidRequestException throws if any of the parameter in not valid.
     */
    private Boolean isValidParameters(ScannerScanRequest scannerScanRequest)
            throws InvalidRequestException, ScannerException {
        String errorMessage;
        scanContext.setJobID(scannerScanRequest.getJobId());
        scanContext.setAuthId(null);

        // Validate webAppId.
        if (StringUtils.isEmpty(scannerScanRequest.getAppId()) || !scannerScanRequest.getAppId()
                .matches(QualysScannerConstants.INTEGER_REGEX)) {
            errorMessage = "Application ID is not provided or Invalid Application ID";
            throw new InvalidRequestException(errorMessage);
        } else {
            scanContext.setWebAppId(scannerScanRequest.getAppId());
        }
        Map<String, List<String>> parameterMap = scannerScanRequest.getPropertyMap();

        // Validate profile id.
        if (StringUtils.isEmpty(parameterMap.get(QualysScannerConstants.PROFILE_ID).get(0))) {
            scanContext.setProfileId(QualysScannerConfiguration.getInstance().getDefaultProfileId());
            String logMessage = "Profile ID is not provided. Default profile ID is set as profile ID value";
            log.info(new CallbackLog(scanContext.getJobID(), logMessage));
        } else if (!scannerScanRequest.getAppId().matches(QualysScannerConstants.INTEGER_REGEX)) {
            errorMessage = "Profile ID is not provided or Invalid Profile ID";
            throw new InvalidRequestException(errorMessage);
        } else {
            scanContext.setProfileId(parameterMap.get(QualysScannerConstants.PROFILE_ID).get(0));
        }

        // Validate scanner appliance type.
        if (StringUtils.isEmpty(parameterMap.get(QualysScannerConstants.SCANNER_APPILIANCE).get(0))) {
            scanContext.setScannerApplianceType(QualysScannerConfiguration.getInstance().getDefaultScannerAppliance());
            String logMessage = "Scanner appliance type for the scan is not provided. Default scanner appliance type"
                    + " is set as scanner appliance type ";
            log.info(new CallbackLog(scanContext.getJobID(), logMessage));
        } else if (!EnumUtils.isValidEnum(ScannerApplianceType.class,
                parameterMap.get(QualysScannerConstants.SCANNER_APPILIANCE).get(0))) {
            errorMessage = "Scanner Appliance Type is not provided or invalid";
            throw new InvalidRequestException(errorMessage);
        } else {
            scanContext.setScannerApplianceType(parameterMap.get(QualysScannerConstants.SCANNER_APPILIANCE).get(0));
        }

        // Validate scan type.
        if (StringUtils.isEmpty(parameterMap.get(QualysScannerConstants.TYPE_KEYWORD).get(0))) {
            scanContext.setType(QualysScannerConfiguration.getInstance().getDefaultScanType());
            String logMessage = "Scan type for the scan is not provided. Default scan type is set as scan type ";
            log.info(new CallbackLog(scanContext.getJobID(), logMessage));
        } else if (!EnumUtils
                .isValidEnum(ScanType.class, parameterMap.get(QualysScannerConstants.TYPE_KEYWORD).get(0))) {
            errorMessage = "Type of the scan is not provided or invalid.";
            throw new InvalidRequestException(errorMessage);
        } else {
            scanContext.setType(parameterMap.get(QualysScannerConstants.TYPE_KEYWORD).get(0));
        }

        // Validate progressive scan value.
        if (StringUtils.isEmpty(parameterMap.get(QualysScannerConstants.PROGRESSIVE_SCAN).get(0))) {
            scanContext
                    .setProgressiveScanning(QualysScannerConfiguration.getInstance().getDefaultProgressiveScanning());
            String logMessage = "Progressive scan option for the scan is not provided. Default progressive scan option"
                    + " is set for " + scannerScanRequest.getAppId();
            log.info(new CallbackLog(scanContext.getJobID(), logMessage));
        } else {
            scanContext.setProgressiveScanning(parameterMap.get(QualysScannerConstants.PROGRESSIVE_SCAN).get(0));
        }

        // Validate report template ID.
        if (StringUtils.isEmpty(parameterMap.get(QualysScannerConstants.PARAMETER_REPORT_TEMPLATE_ID).get(0))) {
            scanContext.setReportTemplateId(QualysScannerConfiguration.getInstance().getDefaultReportTemplateID());
            String logMessage =
                    "Report template ID for the scan is not provided. Default report template ID is set for "
                            + scannerScanRequest.getAppId();
            log.info(new CallbackLog(scanContext.getJobID(), logMessage));
        } else {
            scanContext
                    .setReportTemplateId(parameterMap.get(QualysScannerConstants.PARAMETER_REPORT_TEMPLATE_ID).get(0));
        }

        // Validate crawling scope.
        if (StringUtils.isEmpty(parameterMap.get(QualysScannerConstants.PARAMETER_CRAWLING_SCOPE).get(0))) {
            scanContext.setCrawlingScope(QualysScannerConfiguration.getInstance().getDefaultCrawlingScope());
            String logMessage = "Crawling scope for the scan is not provided. Default crawling scope (ALL) is set for "
                    + scannerScanRequest.getAppId();
            log.info(new CallbackLog(scanContext.getJobID(), logMessage));
        } else if (!EnumUtils.isValidEnum(CrawlingScope.class,
                parameterMap.get(QualysScannerConstants.PARAMETER_CRAWLING_SCOPE).get(0))) {
            scanContext.setCrawlingScope(QualysScannerConfiguration.getInstance().getDefaultCrawlingScope());
            String logMessage =
                    "Invalid crawling scope. Default crawling scope (ALL) is set for " + scannerScanRequest.getAppId();
            log.info(new CallbackLog(scanContext.getJobID(), logMessage));
        } else {
            scanContext.setCrawlingScope(parameterMap.get(QualysScannerConstants.PARAMETER_CRAWLING_SCOPE).get(0));
        }

        // Validate black list regex
        if (parameterMap.get(QualysScannerConstants.PARAMETER_BLACKLIST_REGEX).size() != 0) {
            scanContext.setBlackListRegex(QualysScannerConfiguration.getInstance().getDefaultBlackListRegex());
            String logMessage =
                    "BlackList Regex set is not provided. Default blacklist set is set for " + scannerScanRequest
                            .getAppId();
            log.info(new CallbackLog(scanContext.getJobID(), logMessage));
        } else {
            scanContext.setBlackListRegex(parameterMap.get(QualysScannerConstants.PARAMETER_BLACKLIST_REGEX));
        }

        // Validate authentication type.
        WebAppAuthFactory webAppAuthFactory = new WebAppAuthFactory();
        scanContext.setWebAppAuth(webAppAuthFactory.getWebAppAuth(scannerScanRequest));
        return true;
    }

    /**
     * Initialize the Qualys Configurations.
     *
     * @throws IOException Error occurred while initializing configurations.
     */
    private static void loadConfiguration() throws IOException {
        if (QualysScannerConfiguration.getInstance().getConfigs() == null) {
            QualysScannerConfiguration.getInstance().loadConfiguration(ScannerConstants.CONFIGURTION_FILE_NAME);
        }
        QualysScannerConfiguration.getInstance().setHost(QualysScannerConfiguration.getInstance().
                getConfigProperty(QualysScannerConstants.HOST));
        QualysScannerConfiguration.getInstance()
                .setSchedulerDelay(Long.parseLong(QualysScannerConfiguration.getInstance().
                        getConfigProperty(QualysScannerConstants.SCHEDULER_DELAY)));
        QualysScannerConfiguration.getInstance().setDefaultProfileId(
                QualysScannerConfiguration.getInstance().getConfigProperty(QualysScannerConstants.DEFAULT_PROFILE_ID));
        QualysScannerConfiguration.getInstance().setDefaultScannerAppliance(QualysScannerConfiguration.getInstance()
                .getConfigProperty(QualysScannerConstants.DEFAULT_SCANNER_APPLIANCE));
        QualysScannerConfiguration.getInstance().setDefaultScanType(QualysScannerConfiguration.getInstance().
                getConfigProperty(QualysScannerConstants.DEFAULT_SCAN_TYPE));
        QualysScannerConfiguration.getInstance().setDefaultProgressiveScanning(QualysScannerConfiguration.getInstance()
                .getConfigProperty(QualysScannerConstants.DEFAULT_PROGRESSIVE_SCANNING));
        QualysScannerConfiguration.getInstance().setDefaultReportTemplateID(QualysScannerConfiguration.getInstance().
                getConfigProperty(QualysScannerConstants.DEFAULT_REPORT_TEMPLATE_ID));
        QualysScannerConfiguration.getInstance().setDefaultCrawlingScope(QualysScannerConfiguration.getInstance().
                getConfigProperty(QualysScannerConstants.DEFAULT_CRAWLING_SCOPE));
        QualysScannerConfiguration.getInstance()
                .setDefaultBlackListRegex(Arrays.asList(QualysScannerConfiguration.getInstance().
                        getConfigProperty(QualysScannerConstants.DEFAULT_BLACKLIST_REGEX).split(",")));
    }

    /**
     * Set credentials for the basic authorization.
     *
     * @return basic authentication base 64 encoded string
     * @throws ScannerException Error occurred while encoding the credentials.
     */
    private char[] setCredentials() throws ScannerException {
        char[] basicAuth;
        char[] qualysUsername = QualysScannerConfiguration.getInstance().
                getConfigProperty(QualysScannerConstants.QUALYS_USERNAME).toCharArray();
        char[] qualysPassword = QualysScannerConfiguration.getInstance().
                getConfigProperty(QualysScannerConstants.QUALYS_PASSWORD).toCharArray();
        String credential = new String(qualysUsername) + ":" + new String(qualysPassword);
        try {
            basicAuth = new String(new Base64().encode(credential.getBytes(Charset.forName("UTF-8"))), "UTF-8")
                    .toCharArray();
            Arrays.fill(qualysUsername, '0');
            Arrays.fill(qualysPassword, '0');
        } catch (UnsupportedEncodingException e) {
            throw new ScannerException("Qualys credentials could not be encoded", e);
        }
        return basicAuth;
    }

    /**
     * Update the call back endpoint when error happens at the service layer.
     *
     * @param message error message
     */
    private void callbackErrorReport(String message) {
        log.error(new CallbackLog(scanContext.getJobID(), message));
        if (scanContext.getAuthId() != null) {
            try {
                qualysScanHandler.doCleanUp(scanContext.getAuthId(), scanContext.getJobID());
            } catch (ScannerException e) {
                String cleanupErrorMessage =
                        "Error occurred while doing the cleanup task. " + ErrorProcessingUtil.getFullErrorMessage(e);
                log.error(new CallbackLog(scanContext.getJobID(), cleanupErrorMessage));
            }
        }
        CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null, null);
    }

}
