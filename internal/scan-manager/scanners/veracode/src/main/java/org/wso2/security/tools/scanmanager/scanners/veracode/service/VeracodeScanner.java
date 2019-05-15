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

import com.veracode.apiwrapper.cli.VeracodeCommand;
import com.veracode.apiwrapper.wrappers.UploadAPIWrapper;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.wso2.security.tools.scanmanager.common.internal.model.ScannerScanRequest;
import org.wso2.security.tools.scanmanager.common.model.ErrorMessage;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
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
@Component("VeracodeScannerImpl")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class VeracodeScanner implements Scanner {

    // Scan task thread.
    Thread scanTaskThread;

    // Scan context object for a particular container.
    private ScanContext scanContext;

    /**
     * Initialising the Veracode Wrapper options.
     *
     * @throws IOException
     */
    public VeracodeScanner() throws IOException {
        loadConfiguration();
        VeracodeCommand.Options options;
        scanContext = new ScanContext();

        options = new VeracodeCommand.Options();
        options._output_folderpath = VeracodeScannerConfiguration.getInstance().getConfigProperty(
                VeracodeScannerConstants.VERACODE_OUTPUT_FOLDER_PATH);
        options._output_filepath = VeracodeScannerConfiguration.getInstance().getConfigProperty(
                VeracodeScannerConstants.VERACODE_OUTPUT_FOLDER_PATH) + File.separator + VeracodeScannerConfiguration
                .getInstance().getConfigProperty(VeracodeScannerConstants.VERACODE_OUTPUT_FILE_NAME);
        options._log_filepath = VeracodeScannerConfiguration.getInstance().getConfigProperty(VeracodeScannerConstants
                .VERACODE_LOG_FILE_PATH);
        options._vid = VeracodeScannerConfiguration.getInstance().getConfigProperty(VeracodeScannerConstants
                .VERACODE_API_ID);
        options._vkey = String.valueOf(VeracodeScannerConfiguration.getInstance().getConfigProperty(
                VeracodeScannerConstants.VERACODE_API_KEY));

        VeracodeAPIUtil.setCredentials(options);
        CallbackUtil.setCallbackUrls(VeracodeScannerConfiguration.getInstance().getConfigProperty(ScannerConstants
                        .SCAN_MANAGER_CALLBACK_URL) + VeracodeScannerConfiguration.getInstance().getConfigProperty(
                ScannerConstants.SCAN_MANAGER_CALLBACK_LOG), VeracodeScannerConfiguration.getInstance()
                        .getConfigProperty(ScannerConstants.SCAN_MANAGER_CALLBACK_URL) + VeracodeScannerConfiguration
                        .getInstance().getConfigProperty(ScannerConstants.SCAN_MANAGER_CALLBACK_STATUS),
                Long.parseLong(VeracodeScannerConfiguration.getInstance().getConfigProperty(ScannerConstants
                        .CALLBACK_RETRY_INCREASE_SECONDS)));
    }

    /**
     * Initialising the Veracode configurations.
     *
     * @throws IOException
     */
    private static void loadConfiguration() throws IOException {
        if (VeracodeScannerConfiguration.getInstance().getConfigs() == null) {
            VeracodeScannerConfiguration.getInstance().loadConfiguration(ScannerConstants.RESOURCE_FILE_PATH +
                    File.separator + ScannerConstants.CONFIGURTION_FILE_NAME);
        }
    }

    /**
     * Run the scan using product zip file.
     *
     * @param scanRequest Object that represent the required information for the scanner operation
     * @return ResponseEntity with status of the request
     */
    @Override
    public ResponseEntity startScan(ScannerScanRequest scanRequest) {
        scanContext.setJobId(scanRequest.getJobId());
        scanContext.setAppId(scanRequest.getAppId());
        scanContext.setArtifactLocation(scanRequest.getFileMap().get(VeracodeScannerConstants.SCAN_ARTIFACT).get(0));
        ResponseEntity responseEntity;

        if (scanRequest.getFileMap().get(VeracodeScannerConstants.SCAN_ARTIFACT) != null) {
            if (!StringUtils.isEmpty(scanContext.getAppId())) {
                responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
                startVeracodeScan();
            } else {
                String message = "Error occured while submitting the start scan request since the application " +
                        "is empty in the request. ";
                responseEntity = callbackErrorReport(message, HttpStatus.BAD_REQUEST);
            }
        } else {
            String message = "Error occured while submitting the start scan request since the scan artifacts " +
                    "are empty in the request. ";
            responseEntity = callbackErrorReport(message, HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    /**
     * Stop the last scan for a given application.
     *
     * @param scanRequest Object that represent the required information for tha scanner operation
     * @return ResponseEntity with status of the request
     */
    @Override
    public ResponseEntity cancelScan(ScannerScanRequest scanRequest) {
        ResponseEntity responseEntity;
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
                    CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.CANCELED, null, null);
                    CallbackUtil.persistScanLog(scanContext.getJobId(), message, LogType.INFO);
                    responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
                } else {
                    String message = "Error occured while deleting the last scan of the application : "
                            + scanRequest.getAppId();
                    responseEntity = callbackErrorReport(message, HttpStatus.BAD_REQUEST);
                }
            } else {
                String message = "Successfully cancelled the scan of the application : " + scanRequest.getAppId();
                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.CANCELED, null, null);
                CallbackUtil.persistScanLog(scanContext.getJobId(), message, LogType.INFO);
                responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
                scanTaskThread.interrupt();
            }
        } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
            String message = "Error occured while deleting the last scan of the application : "
                    + scanRequest.getAppId() + " " + ErrorProcessingUtil.getFullErrorMessage(e);
            responseEntity = callbackErrorReport(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    /**
     * Initiate the scan in the Veracode.
     */
    private void startVeracodeScan() {
        ScanTask scanTask = new ScanTask(scanContext);

        scanTaskThread = new Thread(scanTask);
        scanTaskThread.start();
    }

    /**
     * Update the call back endpoint when error happens at the service layer.
     *
     * @param message    error message
     * @param httpStatus http status code of the error
     * @return
     */
    private ResponseEntity callbackErrorReport(String message, HttpStatus httpStatus) {
        ResponseEntity responseEntity = new ResponseEntity<>(new ErrorMessage(httpStatus.value(), message),
                HttpStatus.BAD_REQUEST);

        CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
        CallbackUtil.persistScanLog(scanContext.getJobId(), message, LogType.ERROR);

        return responseEntity;
    }
}
