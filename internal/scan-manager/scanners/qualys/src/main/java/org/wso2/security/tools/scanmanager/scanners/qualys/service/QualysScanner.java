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

package org.wso2.security.tools.scanmanager.scanners.qualys.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.wso2.security.tools.scanmanager.common.internal.model.ScannerScanRequest;
import org.wso2.security.tools.scanmanager.common.model.ErrorMessage;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.exception.InvalidRequestException;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.service.Scanner;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.ErrorProcessingUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.config.QualysScannerConfiguration;
import org.wso2.security.tools.scanmanager.scanners.qualys.handler.QualysApiInvoker;
import org.wso2.security.tools.scanmanager.scanners.qualys.handler.QualysScanHandler;
import org.wso2.security.tools.scanmanager.scanners.qualys.handler.ScanExecutor;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanContext;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanType;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScannerApplianceType;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible to initiate the generic use cases of Qualys scanner
 */
@Component("QualysScanner")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class QualysScanner implements Scanner {

    private final Log log = LogFactory.getLog(QualysScanner.class);
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
        CallbackUtil.setCallbackUrls(
                QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.SCAN_MANAGER_CALLBACK_URL)
                        + QualysScannerConfiguration.getInstance()
                        .getConfigProperty(ScannerConstants.SCAN_MANAGER_CALLBACK_LOG),
                QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.SCAN_MANAGER_CALLBACK_URL)
                        + QualysScannerConfiguration.getInstance()
                        .getConfigProperty(ScannerConstants.SCAN_MANAGER_CALLBACK_STATUS), Long.parseLong(
                        QualysScannerConfiguration.getInstance()
                                .getConfigProperty(ScannerConstants.CALLBACK_RETRY_INCREASE_SECONDS)));
    }

    @Override
    public ResponseEntity startScan(ScannerScanRequest scanRequest) {
        ResponseEntity responseEntity = null;
        try {
            scanContext.setJobID(scanRequest.getJobId());
            if (isValidParameters(scanRequest)) {
                scanContext.setWebAppName(scanRequest.getPropertyMap().get(QualysScannerConstants.
                        QUALYS_WEBAPP_KEYWORD).get(0));
                scanContext.setSchedulerDelay(QualysScannerConfiguration.getInstance().getSchedulerDelay());
                startQualysScan(scanRequest.getFileMap());
                String logMessage = "Given parameters are validated and submitted for scanning";
                CallbackUtil.persistScanLog(scanRequest.getJobId(), logMessage, LogType.INFO);
                responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
            }
        } catch (InvalidRequestException e) {
            String message = "Error occurred while submitting the start scan request since given parameters are given "
                    + ErrorProcessingUtil.getFullErrorMessage(e);
            CallbackUtil.updateScanStatus(scanRequest.getJobId(), ScanStatus.ERROR, null, null);
            CallbackUtil.persistScanLog(scanRequest.getJobId(), message, LogType.ERROR);
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), message),
                    HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    @Override
    public ResponseEntity cancelScan(ScannerScanRequest scanRequest) {
        ResponseEntity responseEntity;
        try {
            qualysScanHandler
                    .cancelScan(QualysScannerConfiguration.getInstance().getHost(), scanContext.getScannerScanId(),
                            scanRequest.getJobId());
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.ACCEPTED.value(), "Scan is cancelled"),
                    HttpStatus.ACCEPTED);
        } catch (ScannerException e) {
            String message = "Error occurred while cancelling scan : " + scanRequest.getAppId();
            CallbackUtil.updateScanStatus(scanRequest.getJobId(), ScanStatus.ERROR, null, null);
            CallbackUtil.persistScanLog(scanRequest.getJobId(), message, LogType.ERROR);
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), message),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                qualysScanHandler.doCleanUp(scanContext);
            } catch (ScannerException e) {
                String message =
                        "Error occurred while doing the cleanup task. " + scanContext.getJobID() + ErrorProcessingUtil
                                .getFullErrorMessage(e);
                CallbackUtil.persistScanLog(scanContext.getJobID(), message, LogType.ERROR);
            }
        }
        return responseEntity;
    }

    /**
     * Initiate the scan in the Qualys.
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
    private Boolean isValidParameters(ScannerScanRequest scannerScanRequest) throws InvalidRequestException {
        String errorMessage;
        // Validate webAppId.
        if (!StringUtils.isEmpty(scannerScanRequest.getAppId()) && !scannerScanRequest.getAppId()
                .matches(QualysScannerConstants.INTEGER_REGEX)) {
            errorMessage = "Application ID is not provided or Invalid Application ID";
            throw new InvalidRequestException(errorMessage);
        } else {
            scanContext.setWebAppId(scannerScanRequest.getAppId());
        }
        Map<String, List<String>> parameterMap = scannerScanRequest.getPropertyMap();
        // Validate profile id.
        if (!StringUtils.isEmpty(parameterMap.get(QualysScannerConstants.PROFILE_ID).get(0))) {
            scanContext.setProfileId(QualysScannerConfiguration.getInstance().getDefaultProfileId());
            String logMessage =
                    "Profile ID for the scan is not provided. Default profile ID is set as profile ID value for"
                            + " the application " + scannerScanRequest.getAppId();
            log.info(logMessage);
            CallbackUtil.persistScanLog(scannerScanRequest.getJobId(), logMessage, LogType.INFO);
        } else if (!scannerScanRequest.getAppId().matches(QualysScannerConstants.INTEGER_REGEX)) {
            errorMessage = "Profile ID is not provided or Invalid Profile ID";
            throw new InvalidRequestException(errorMessage);
        } else {
            scanContext.setProfileId(parameterMap.get(QualysScannerConstants.PROFILE_ID).get(0));
        }
        // Validate scanner appliance type.
        if (!StringUtils.isEmpty(parameterMap.get(QualysScannerConstants.SCANNER_APPILIANCE).get(0))) {
            scanContext.setScannerApplianceType(QualysScannerConfiguration.getInstance().getDefaultScannerAppliance());
            String logMessage = "Scanner appliance type for the scan is not provided. Default scanner appliance type"
                    + " is set as scanner appliance type for the application " + scannerScanRequest.getAppId();
            log.info(logMessage);
            CallbackUtil.persistScanLog(scannerScanRequest.getJobId(), logMessage, LogType.INFO);
        } else if (!EnumUtils.isValidEnum(ScannerApplianceType.class,
                parameterMap.get(QualysScannerConstants.SCANNER_APPILIANCE).get(0))) {
            errorMessage = "Scanner Appliance Type is not provided or invalid";
            throw new InvalidRequestException(errorMessage);
        } else {
            scanContext.setScannerApplianceType(parameterMap.get(QualysScannerConstants.SCANNER_APPILIANCE).get(0));
        }
        // Validate scan type.
        if (!StringUtils.isEmpty(parameterMap.get(QualysScannerConstants.TYPE_KEYWORD).get(0))) {
            scanContext.setType(QualysScannerConfiguration.getInstance().getDefaultScanType());
            String logMessage =
                    "Scan type for the scan is not provided. Default scan type" + " is set as scan type for the "
                            + "application " + scannerScanRequest.getAppId();
            log.info(logMessage);
            CallbackUtil.persistScanLog(scannerScanRequest.getJobId(), logMessage, LogType.INFO);
        } else if (!EnumUtils
                .isValidEnum(ScanType.class, parameterMap.get(QualysScannerConstants.TYPE_KEYWORD).get(0))) {
            errorMessage = "Type of the scan is not provided or invalid.";
            throw new InvalidRequestException(errorMessage);
        } else {
            scanContext.setType(parameterMap.get(QualysScannerConstants.TYPE_KEYWORD).get(0));
        }
        // Validate progressive scan value
        if (!StringUtils.isEmpty(parameterMap.get(QualysScannerConstants.PROGRESSIVE_SCAN).get(0))) {
            scanContext
                    .setProgressiveScanning(QualysScannerConfiguration.getInstance().getDefaultProgressiveScanning());
            String logMessage = "Progressive scan option for the scan is not provided. Default progressive scan option"
                    + " is set for " + scannerScanRequest.getAppId();
            log.info(logMessage);
            CallbackUtil.persistScanLog(scannerScanRequest.getJobId(), logMessage, LogType.INFO);
        } else {
            scanContext.setProgressiveScanning(parameterMap.get(QualysScannerConstants.PROGRESSIVE_SCAN).get(0));
        }

        List<String> authFiles = scannerScanRequest.getFileMap().get(QualysScannerConstants.AUTHENTICATION_SCRIPTS);
        if (authFiles.size() != 0) {
            for (int i = 0; i < authFiles.size(); i++) {
                File file = new File(authFiles.get(0));
                if (!file.getName().endsWith(QualysScannerConstants.XML)) {
                    errorMessage = "Invalid file type for Authentication Script";
                    throw new InvalidRequestException(errorMessage);
                }
            }
        } else {
            errorMessage = "Authentication script is not provided";
            throw new InvalidRequestException(errorMessage);
        }
        scanContext.setScriptFilesLocation(authFiles.get(0).substring(0, authFiles.get(0).
                lastIndexOf(File.separator)));
        return true;
    }

    /**
     * Initialize the Qualys Configurations.
     *
     * @throws IOException Error occurred while initializing configurations.
     */
    private static void loadConfiguration() throws IOException {
        if (QualysScannerConfiguration.getInstance().getConfigs() == null) {
            QualysScannerConfiguration.getInstance().loadConfiguration(
                    ScannerConstants.RESOURCE_FILE_PATH + File.separator + ScannerConstants.CONFIGURTION_FILE_NAME);
        }
        QualysScannerConfiguration.getInstance().setHost(QualysScannerConstants.HOST);
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
                getConfigProperty(QualysScannerConstants.USERNAME).toCharArray();
        char[] qualysPassword = QualysScannerConfiguration.getInstance().
                getConfigProperty(QualysScannerConstants.PASSWORD).toCharArray();
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
}
