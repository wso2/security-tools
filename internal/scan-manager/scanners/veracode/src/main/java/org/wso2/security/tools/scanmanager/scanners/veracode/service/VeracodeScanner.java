/*
 *  Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.security.tools.scanmanager.scanners.veracode.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.veracode.apiwrapper.cli.VeracodeCommand;
import com.veracode.apiwrapper.wrappers.UploadAPIWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.wso2.security.tools.scanmanager.common.internal.model.ScannerScanRequest;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.model.CallbackLog;
import org.wso2.security.tools.scanmanager.scanners.common.service.Scanner;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.ErrorProcessingUtil;
import org.wso2.security.tools.scanmanager.scanners.veracode.VeracodeScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.veracode.config.VeracodeScannerConfiguration;
import org.wso2.security.tools.scanmanager.scanners.veracode.handler.ScanTask;
import org.wso2.security.tools.scanmanager.scanners.veracode.handler.VeracodeResultProcessor;
import org.wso2.security.tools.scanmanager.scanners.veracode.model.ScanContext;
import org.wso2.security.tools.scanmanager.scanners.veracode.util.VeracodeAPIUtil;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Represents the Veracode Scanner.
 */
@Component("VeracodeScannerImpl") @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON) public class VeracodeScanner
        implements Scanner {

    private static final Logger log = LogManager.getLogger(VeracodeScanner.class);

    // Scan context object for a particular container.
    private ScanContext scanContext;

    /**
     * Initialising the Veracode Wrapper options.
     *
     * @throws IOException when unable to load configurations due to IO error
     */
    public VeracodeScanner() throws IOException {
        loadConfiguration();
        VeracodeCommand.Options options;
        scanContext = getScanContext();
        //        scanContext = new ScanContext();

        options = new VeracodeCommand.Options();
        options._output_folderpath = VeracodeScannerConfiguration.getInstance()
                .getConfigProperty(VeracodeScannerConstants.VERACODE_OUTPUT_FOLDER_PATH);
        options._log_filepath = VeracodeScannerConfiguration.getInstance()
                .getConfigProperty(VeracodeScannerConstants.VERACODE_LOG_FILE_PATH);
        options._vid = VeracodeScannerConfiguration.getInstance()
                .getConfigProperty(VeracodeScannerConstants.VERACODE_API_ID);
        options._vkey = String.valueOf(VeracodeScannerConfiguration.getInstance()
                .getConfigProperty(VeracodeScannerConstants.VERACODE_API_KEY));

        VeracodeAPIUtil.setCredentials(options);

        String callbackUrl =
                ScannerConstants.HTTP_PROTOCOL + System.getenv(ScannerConstants.SCAN_MANAGER_HOST) + ":" + System
                        .getenv(ScannerConstants.SCAN_MANAGER_PORT) + VeracodeScannerConfiguration.getInstance()
                        .getConfigProperty(ScannerConstants.SCAN_MANAGER_CALLBACK_URL_ENDPOINT);
        String logCallbackUrl = callbackUrl + VeracodeScannerConfiguration.getInstance()
                .getConfigProperty(ScannerConstants.SCAN_MANAGER_CALLBACK_LOG);
        String statusCallbackUrl = callbackUrl + VeracodeScannerConfiguration.getInstance()
                .getConfigProperty(ScannerConstants.SCAN_MANAGER_CALLBACK_STATUS);
        Long callbackRetryInterval = Long.parseLong(VeracodeScannerConfiguration.getInstance()
                .getConfigProperty(ScannerConstants.CALLBACK_RETRY_INCREASE_SECONDS));

        CallbackUtil.setCallbackUrls(logCallbackUrl, statusCallbackUrl, callbackRetryInterval);
    }

    /**
     * Initialising the Veracode configurations.
     *
     * @throws IOException when unable to load configurations due to IO error
     */
    private static void loadConfiguration() throws IOException {
        if (VeracodeScannerConfiguration.getInstance().getConfigs() == null) {
            VeracodeScannerConfiguration.getInstance().loadConfiguration(ScannerConstants.CONFIGURTION_FILE_NAME);
        }
    }

    /**
     * Run the scan using product zip file.
     *
     * @param scanRequest Object that represent the required information for the scanner operation
     */
    @Override public void startScan(ScannerScanRequest scanRequest) {
        scanContext.setJobId(scanRequest.getJobId());
        scanContext.setAppId(scanRequest.getAppId());
        scanContext.setArtifactLocation(scanRequest.getFileMap().get(VeracodeScannerConstants.SCAN_ARTIFACT).get(0));

        if (scanRequest.getFileMap().get(VeracodeScannerConstants.SCAN_ARTIFACT) != null) {
            if (StringUtils.isEmpty(scanContext.getAppId())) {
                String message = "Error occured while submitting the start scan request since the application "
                        + "is empty in the request. ";
                callbackErrorReport(message);
            } else {
                if (Thread.currentThread().isInterrupted()) {
                    String message = "Current thread is interrupted. ";
                    log.error(new CallbackLog(scanContext.getJobId(), message));
                } else {

                    // Write scan context object to yaml file.
                    ObjectMapper scanContextObjectMapper = new ObjectMapper(new YAMLFactory());
                    scanContextObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                    try {
                        scanContextObjectMapper.writeValue(new File(ScannerConstants.SCAN_CONTEXT_FILE_NAME),
                                scanContext);
                    } catch (IOException e) {
                        String message = "Error occured while writing scan context file. ";
                        callbackErrorReport(message);
                    }
                    ScanTask scanTask = new ScanTask(scanContext, false);
                    scanTask.run();
                }
            }
        } else {
            String message = "Error occured while submitting the start scan request since the scan artifacts "
                    + "are empty in the request. ";
            callbackErrorReport(message);
        }
    }

    @Override public boolean validateStartScan(ScannerScanRequest scannerScanRequest) {

        if (StringUtils.isEmpty(scannerScanRequest.getFileMap().get(VeracodeScannerConstants.SCAN_ARTIFACT))) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Stop the last scan for a given application.
     *
     * @param scanRequest Object that represent the required information for tha scanner operation
     */
    @Override public void cancelScan(ScannerScanRequest scanRequest) {
        String scanInfoResult;
        String deleteApiResult;
        ScanStatus currentScanStatus;

        try {
            UploadAPIWrapper uploadAPIWrapper = VeracodeAPIUtil.getUploadAPIWrapper();
            scanInfoResult = uploadAPIWrapper.getBuildInfo(scanContext.getAppId());
            currentScanStatus = VeracodeResultProcessor.getScanStatus(scanInfoResult);

            if (ScanStatus.RUNNING.equals(currentScanStatus) || ScanStatus.SUBMITTED.equals(currentScanStatus)) {
                deleteApiResult = uploadAPIWrapper.deleteBuild(scanContext.getAppId());
                if (VeracodeResultProcessor.isOperationProceedWithoutError(deleteApiResult)) {
                    String message = "Successfully cancelled the scan of the application : " + scanRequest.getAppId();
                    log.info(new CallbackLog(scanRequest.getJobId(), message));

                    CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.CANCELED, null, null);
                } else {
                    String message =
                            "Error occured while deleting the last scan of the application : " + scanRequest.getAppId();
                    callbackErrorReport(message);
                }
            } else {
                String message = "Successfully cancelled the scan of the application : " + scanRequest.getAppId();
                log.info(new CallbackLog(scanContext.getJobId(), message));

                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.CANCELED, null, null);
            }
        } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
            String message =
                    "Error occured while deleting the last scan of the application : " + scanRequest.getAppId() + " "
                            + ErrorProcessingUtil.getFullErrorMessage(e);
            callbackErrorReport(message);
        }
    }

    @Override public void resumeScan(ScannerScanRequest scanRequest) {
        System.out.println(scanContext.getAppId());
        ScanTask scanTask = new ScanTask(scanContext, true);
        log.info(new CallbackLog(scanContext.getJobId(), "Scan is to be resumed"));
        scanTask.run();
    }

    @Override public boolean validateCancelScan(ScannerScanRequest scannerScanRequest) {
        return true;
    }

    /**
     * Update the call back endpoint when error happens at the service layer.
     *
     * @param message error message
     * @return ResponseEntity with status of the updating the call back endpoint
     */
    private void callbackErrorReport(String message) {
        log.error(new CallbackLog(scanContext.getJobId(), message));

        CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
    }

    private ScanContext getScanContext() {
        File contextFile = new File(ScannerConstants.SCAN_CONTEXT_FILE_NAME);
        ScanContext scanContext = null;
        if (contextFile.exists()) {

            // Instantiating a new ObjectMapper as a YAMLFactory
            ObjectMapper scanContextObjectMapper = new ObjectMapper(new YAMLFactory());
            scanContextObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

            // Mapping the employee from the YAML file to the Employee class
            try {
                scanContext = scanContextObjectMapper.readValue(contextFile, ScanContext.class);
                String message = "Scan context is recreated for job ID: " + scanContext.getJobId();
                log.info(new CallbackLog(scanContext.getJobId(), message));
            } catch (IOException e) {
                log.error("could not read file");
            }
        } else {
            scanContext = new ScanContext();
        }
        return scanContext;
    }
}
