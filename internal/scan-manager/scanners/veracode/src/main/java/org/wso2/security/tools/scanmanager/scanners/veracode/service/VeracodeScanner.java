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

import com.veracode.apiwrapper.AbstractAPIWrapper;
import com.veracode.apiwrapper.cli.VeracodeCommand;
import com.veracode.apiwrapper.wrappers.ResultsAPIWrapper;
import com.veracode.apiwrapper.wrappers.UploadAPIWrapper;
import com.veracode.util.lang.StringUtility;
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
import org.wso2.security.tools.scanmanager.scanners.common.config.YAMLConfigurationReader;
import org.wso2.security.tools.scanmanager.scanners.common.service.Scanner;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.veracode.VeracodeScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.veracode.config.VeracodeScannerConfiguration;
import org.wso2.security.tools.scanmanager.scanners.veracode.handler.ScanTask;
import org.wso2.security.tools.scanmanager.scanners.veracode.handler.VeracodeResultProcessor;
import org.wso2.security.tools.scanmanager.scanners.veracode.model.ScanContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.DatatypeConverter;

/**
 * Represents the Veracode Scanner.
 */
@Component("VeracodeScannerImpl")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class VeracodeScanner implements Scanner {

    // Veracode upload API wrapper.
    private UploadAPIWrapper uploadAPIWrapper = null;

    // Veracode results API wrapper.
    private ResultsAPIWrapper resultsAPIWrapper = null;

    // Scan context object for a particular container.
    private ScanContext scanContext;

    // Veracode specific log writer.
    private PrintStream logWriter;

    /**
     * Initialising the Veracode Wrapper options.
     *
     * @throws IOException
     */
    public VeracodeScanner() throws IOException {
        loadConfiguration();
        VeracodeCommand.Options options;

        options = new VeracodeCommand.Options();
        options._output_folderpath = VeracodeScannerConfiguration.getInstance().getOutputFolderPath();
        options._output_filepath = VeracodeScannerConfiguration.getInstance().getOutputFilePath();
        options._log_filepath = VeracodeScannerConfiguration.getInstance().getLogFilePath();
        options._vid = VeracodeScannerConfiguration.getInstance().getApiId();
        options._vkey = String.valueOf(VeracodeScannerConfiguration.getInstance().getApiKey());
        createLogWriter(options);
        getUploadAPIWrapper(options);
        getResultAPIWrapper(options);
    }

    /**
     * Initialising the Veracode configurations.
     *
     * @throws IOException
     */
    private static void loadConfiguration() throws IOException {
        if (YAMLConfigurationReader.getInstance().getConfigs() == null) {
            YAMLConfigurationReader.getInstance().loadConfiguration();
        }

        VeracodeScannerConfiguration.getInstance().setApiId(YAMLConfigurationReader.getInstance()
                .getConfigProperty(VeracodeScannerConstants.VERACODE_API_ID));
        VeracodeScannerConfiguration.getInstance().setApiKey((YAMLConfigurationReader.getInstance()
                .getConfigProperty(VeracodeScannerConstants.VERACODE_API_KEY)).toCharArray());
        VeracodeScannerConfiguration.getInstance().setOutputFolderPath(YAMLConfigurationReader.getInstance()
                .getConfigProperty(VeracodeScannerConstants.VERACODE_OUTPUT_FOLDER_PATH));
        VeracodeScannerConfiguration.getInstance().setOutputFilePath(YAMLConfigurationReader.getInstance()
                .getConfigProperty(VeracodeScannerConstants.VERACODE_OUTPUT_FOLDER_PATH) + File.separator +
                YAMLConfigurationReader.getInstance().getConfigProperty(VeracodeScannerConstants
                        .VERACODE_OUTPUT_FILE_NAME));
        VeracodeScannerConfiguration.getInstance().setLogFilePath(YAMLConfigurationReader.getInstance().
                getConfigProperty(VeracodeScannerConstants.VERACODE_LOG_FILE_PATH));
        VeracodeScannerConfiguration.getInstance().setScannerClass(YAMLConfigurationReader.getInstance().
                getConfigProperty(VeracodeScannerConstants.SCANNER_BEAN_CLASS_NAME));
    }

    /**
     * Run the scan using product zip file.
     *
     * @param scanRequest Object that represent the required information for the scanner operation
     * @return ResponseEntity with status of the request
     */
    @Override
    public ResponseEntity startScan(ScannerScanRequest scanRequest) {
        scanContext = new ScanContext();
        scanContext.setJobId(scanRequest.getJobId());
        scanContext.setAppId(scanRequest.getAppId());
        scanContext.setArtifactLocation(scanRequest.getFileMap().get(VeracodeScannerConstants.SCAN_ARTIFACT).get(0));
        ResponseEntity responseEntity;

        if (scanRequest.getFileMap().get(VeracodeScannerConstants.SCAN_ARTIFACT) != null) {
            if (!StringUtils.isEmpty(scanContext.getAppId())) {
                responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
                startVeracodeScan();
            } else {
                String message = "Error occured while submitting the start scan request since " +
                        "the application is empty in the request. ";
                responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                        message), HttpStatus.BAD_REQUEST);
                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null,
                        null);
                CallbackUtil.persistScanLog(scanContext.getJobId(), message, LogType.ERROR);
            }
        } else {
            String message = "Error occured while submitting the start scan request since " +
                    "the scan artifacts are empty in the request. ";
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                    message), HttpStatus.BAD_REQUEST);
            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null,
                    null);
            CallbackUtil.persistScanLog(scanContext.getJobId(), message, LogType.ERROR);
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
        String result;

        try {
            result = uploadAPIWrapper.deleteBuild(scanContext.getAppId());
            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.CANCELED, null,
                    null);
            if (VeracodeResultProcessor.isOperationProceedWithoutError(result)) {
                responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
            } else {
                String message = "Error occured while deleting the last scan of the application : "
                        + scanRequest.getAppId();
                responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                        message), HttpStatus.BAD_REQUEST);
                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null,
                        null);
                CallbackUtil.persistScanLog(scanContext.getJobId(), message, LogType.ERROR);
            }
        } catch (IOException e) {
            String message = "Error occured while deleting the last scan of the application : "
                    + scanRequest.getAppId() + VeracodeResultProcessor.getFullErrorMessage(e);
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message), HttpStatus.INTERNAL_SERVER_ERROR);
            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null,
                    null);
            CallbackUtil.persistScanLog(scanContext.getJobId(), message, LogType.ERROR);
        }
        return responseEntity;
    }

    /**
     * Build the upload wrap for Upload API.
     *
     * @param options represents the Veracode credentials and other required configurations
     * @return Veracode Upload API Wrapper
     * @throws UnsupportedEncodingException
     */
    private UploadAPIWrapper getUploadAPIWrapper(VeracodeCommand.Options options) throws UnsupportedEncodingException {
        uploadAPIWrapper = new UploadAPIWrapper();
        this.setUpWrapperCredentials(uploadAPIWrapper, options);

        return uploadAPIWrapper;
    }

    /**
     * Build the upload wrap for Result API.
     *
     * @param options represents the Veracode credentials and other required configurations
     * @return Veracode Result API Wrapper
     * @throws UnsupportedEncodingException
     */
    private ResultsAPIWrapper getResultAPIWrapper(VeracodeCommand.Options options) throws UnsupportedEncodingException {
        resultsAPIWrapper = new ResultsAPIWrapper();
        this.setUpWrapperCredentials(resultsAPIWrapper, options);

        return resultsAPIWrapper;
    }

    /**
     * Initiate the scan in the Veracode.
     */
    private void startVeracodeScan() {
        ScanTask scanTask = new ScanTask(uploadAPIWrapper, scanContext, resultsAPIWrapper);

        Thread scanTaskThread = new Thread(scanTask);
        scanTaskThread.start();
    }

    /**
     * Set the Veracode credentials to the Veracode API wrappers.
     *
     * @param wrapper Warpper that needs to be set credentials
     * @param options represents the Veracode credentials and other required configurations
     * @throws UnsupportedEncodingException
     */
    private void setUpWrapperCredentials(AbstractAPIWrapper wrapper, VeracodeCommand.Options options) throws
            UnsupportedEncodingException {
        String apiID = options._vid;
        String user;
        String pass;

        if (StringUtility.isNullOrEmpty(apiID)) {
            user = options._vuser;
            pass = options._vpassword;

            if (StringUtility.isNullOrEmpty(user) && !StringUtility.isNullOrEmpty(options._api1)) {
                user = this.decodeB64(options._api1);
            }

            if (StringUtility.isNullOrEmpty(pass) && !StringUtility.isNullOrEmpty(options._api2)) {
                pass = this.decodeB64(options._api2);
            }
            wrapper.setUpCredentials(user, pass);
        } else {
            wrapper.setUpApiCredentials(apiID, options._vkey);
            VeracodeScannerConfiguration.getInstance().zeroingVeracodeApiKeyInMemory();
        }
    }

    /**
     * Do base64 decode for a given encoded string.
     *
     * @param b64EncodedString encoded string that need to decode
     * @return decoded string
     * @throws UnsupportedEncodingException
     */
    private String decodeB64(String b64EncodedString) throws UnsupportedEncodingException {
        return new String(DatatypeConverter.parseBase64Binary(b64EncodedString), StandardCharsets.UTF_8.name());
    }

    /**
     * Create log writer.
     *
     * @param options represents the Veracode configurations
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     */
    private void createLogWriter(VeracodeCommand.Options options) throws FileNotFoundException,
            UnsupportedEncodingException {
        logWriter = new PrintStream(new FileOutputStream(options._log_filepath), true,
                StandardCharsets.UTF_8.name());

        logWriteLine(StringUtility.repeatChar('-', 80));
        logWriteLine(VeracodeCommand.getVersionString());
        logWriteLine(StringUtility.repeatChar('-', 80));
    }

    /**
     * Write logs.
     *
     * @param text line to be written
     */
    private void logWriteLine(String text) {
        if (logWriter != null && !StringUtility.isNullOrEmpty(text)) {
            logWriter.println(text);
            logWriter.flush();
        }
    }
}
