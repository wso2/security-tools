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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.scanmanager.common.internal.model.ScannerScanRequest;
import org.wso2.security.tools.scanmanager.common.model.ErrorMessage;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.config.YAMLConfigurationReader;
import org.wso2.security.tools.scanmanager.scanners.common.exception.InvalidRequestException;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.service.Scanner;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.config.QualysScannerConfiguration;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanContext;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanType;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScannerApplianceType;
import org.wso2.security.tools.scanmanagers.scanner.qualys.handler.QualysApiInvoker;
import org.wso2.security.tools.scanmanagers.scanner.qualys.handler.QualysScanHandler;
import org.wso2.security.tools.scanmanger.scanners.qualys.QualysScannerConstants;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.wso2.security.tools.scanmanager.scanners.qualys.utils.RequestBodyBuilder.getSecuredDocumentBuilderFactory;

/**
 * This class is responsible to initiate the generic use cases of Qualys scanner
 */
@Component("QualysScannerImpl") @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON) public class QualysScanner
        implements Scanner {

    private final Log log = LogFactory.getLog(QualysScanner.class);
    public static String host;
    private QualysScanHandler qualysScanHandler;
    private ScanContext scanContext;

    public QualysScanner() throws IOException, ScannerException {
        loadConfiguration();
        host = QualysScannerConfiguration.getInstance().getHost();
        QualysApiInvoker qualysApiInvoker = new QualysApiInvoker();
        qualysApiInvoker.setBasicAuth(setCredentials());
        this.qualysScanHandler = new QualysScanHandler(qualysApiInvoker);
    }

    @Override public ResponseEntity startScan(ScannerScanRequest scanRequest) {
        ResponseEntity responseEntity = null;
        String authScriptId;
        String scannerScanId;
        try {
            if (isValidateParameters(scanRequest)) {
                CallbackUtil.persistScanLog(scanRequest.getJobId(), "Parameters are validated", LogType.ERROR);
                // Prepare Web Application for launching scan.
                authScriptId = qualysScanHandler.prepareScan(scanRequest.getAppId(), scanRequest.getJobId(),
                        scanRequest.getPropertyMap().get(QualysScannerConstants.QUALYS_WEBAPP_TAG_NAME).get(0),
                        scanRequest.getFileMap(), host);
                // Set ScanContext Object.
                scanContext = new ScanContext();
                scanContext.setJobID(scanRequest.getJobId());
                scanContext.setWebAppId(scanRequest.getAppId());
                scanContext.setAuthId(authScriptId);
                scanContext.setWebAppName(scanRequest.getPropertyMap().get(QualysScannerConstants.
                        QUALYS_WEBAPP_TAG_NAME).get(0));
                scanContext.setProfileId(scanRequest.getPropertyMap().get(QualysScannerConstants.PROFILE_ID).get(0));
                scanContext.setType(scanRequest.getPropertyMap().get(QualysScannerConstants.TYPE_KEYWORD).get(0));
                scanContext.setScannerApplianceType(
                        scanRequest.getPropertyMap().get(QualysScannerConstants.SCANNER_APPILIANCE_TYPE_KEYWORD)
                                .get(0));
                scanContext.setProgressiveScanning(
                        scanRequest.getPropertyMap().get(QualysScannerConstants.PROGRESSIVE_SCAN).get(0));
                scanContext.setInitialDelay(QualysScannerConfiguration.getInstance().getInitialDelay());
                scanContext.setSchedulerDelay(QualysScannerConfiguration.getInstance().getSchedulerDelay());
                // Launch Scan
                scannerScanId = qualysScanHandler.launchScan(scanContext, host);
                if (scannerScanId != null) {
                    responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
                }
            }
        } catch (ScannerException e) {
            String message = "Failed to start scan " + scanRequest.getJobId() + e.getMessage();
            responseEntity = new ResponseEntity<>(
                    new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to start scan"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
            CallbackUtil.updateScanStatus(scanRequest.getJobId(), ScanStatus.ERROR, null, null);
            CallbackUtil.persistScanLog(scanRequest.getJobId(), message, LogType.ERROR);
        } catch (InvalidRequestException e) {
            String message = "Error occurred while submitting the start scan request since invalid parameters given";
            CallbackUtil.updateScanStatus(scanRequest.getJobId(), ScanStatus.ERROR, null, null);
            CallbackUtil.persistScanLog(scanRequest.getJobId(), message, LogType.ERROR);
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), message),
                    HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    @Override public ResponseEntity cancelScan(ScannerScanRequest scanRequest) {
        ResponseEntity responseEntity;
        try {
            qualysScanHandler.cancelScan(host, scanContext.getScannerScanId(), scanRequest.getJobId());
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.ACCEPTED.value(), "Scan is cancelled"),
                    HttpStatus.ACCEPTED);
        } catch (ScannerException e) {
            String message = "Error occurred while cancelling scan : " + scanRequest.getAppId();

            CallbackUtil.updateScanStatus(scanRequest.getJobId(), ScanStatus.ERROR, null, null);
            CallbackUtil.persistScanLog(scanRequest.getJobId(), message, LogType.ERROR);
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), message),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    /**
     * Validate Scan Parameters related to Qualys Scanner.
     *
     * @param scannerScanRequest Scanner Request
     * @return True if all parameters are valid
     * @throws InvalidRequestException throws if any of the parameter in not valid.
     */
    private Boolean isValidateParameters(ScannerScanRequest scannerScanRequest) throws InvalidRequestException {
        String errorMessage;
        // Validate webAppId.
        if (!StringUtils.isEmpty(scannerScanRequest.getAppId()) && !scannerScanRequest.getAppId()
                .matches(QualysScannerConstants.INTEGER_REGEX)) {
            errorMessage = "Application Id is not provided or Invalid Application ID";
            throw new InvalidRequestException(errorMessage);
        }
        Map<String, List<String>> parameterMap = scannerScanRequest.getPropertyMap();
        // Validate profile id.
        if (!StringUtils.isEmpty(parameterMap.get(QualysScannerConstants.PROFILE_ID).get(0)) && !scannerScanRequest
                .getAppId().matches(QualysScannerConstants.INTEGER_REGEX)) {
            errorMessage = "Profile Id is not provided or Invalid Profile Id";
            throw new InvalidRequestException(errorMessage);
        }

        if (!EnumUtils.isValidEnum(ScannerApplianceType.class,
                parameterMap.get(QualysScannerConstants.SCANNER_APPILIANCE).get(0))) {
            errorMessage = "Scanner Appliance Type is not provided or invalid";
            throw new InvalidRequestException(errorMessage);
        }

        if (!EnumUtils.isValidEnum(ScanType.class, parameterMap.get(QualysScannerConstants.TYPE_KEYWORD).get(0))) {
            errorMessage = "Type of the scan is not provided or invalid";
            throw new InvalidRequestException(errorMessage);
        }

        List<String> authFiles = scannerScanRequest.getFileMap().get(QualysScannerConstants.AUTHENTICATION_SCRIPTS);
        if (authFiles.size() != 0) {
            for (int i = 0; i < authFiles.size(); i++) {
                File file = new File(authFiles.get(0));
                if (!file.exists()) {
                    errorMessage = "Authentication script is not exists";
                    throw new InvalidRequestException(errorMessage);
                } else {
                    if (!file.getName().endsWith(QualysScannerConstants.XML)) {
                        errorMessage = "Invalid file type for Authentication Script";
                        throw new InvalidRequestException(errorMessage);
                    }
                }
            }
        } else {
            errorMessage = "Authentication script is not provided";
            throw new InvalidRequestException(errorMessage);
        }
        return true;
    }

    /**
     * Initialize the Qualys Configurations.
     *
     * @throws IOException Error occurred while initializing configurations.
     */
    private static void loadConfiguration() throws IOException {
        if (YAMLConfigurationReader.getInstance().getConfigs() == null) {
            YAMLConfigurationReader.getInstance().loadConfiguration();
        }

        QualysScannerConfiguration.getInstance().setUsername(
                YAMLConfigurationReader.getInstance().getConfigProperty(QualysScannerConstants.USERNAME).toCharArray());
        QualysScannerConfiguration.getInstance().setPassword(
                YAMLConfigurationReader.getInstance().getConfigProperty(QualysScannerConstants.PASSWORD).toCharArray());
        QualysScannerConfiguration.getInstance().setHost(QualysScannerConstants.HOST);
        QualysScannerConfiguration.getInstance().setReportFilePath(QualysScannerConstants.REPORT_PATH);
        QualysScannerConfiguration.getInstance().setScannerClass(
                YAMLConfigurationReader.getInstance().getConfigProperty(QualysScannerConstants.SCANNER_BEAN_CLASS));
        QualysScannerConfiguration.getInstance().setInitialDelay(Long.parseLong(YAMLConfigurationReader.getInstance().
                getConfigProperty(QualysScannerConstants.INITIAL_DELAY)));
        QualysScannerConfiguration.getInstance().setSchedulerDelay(Long.parseLong(YAMLConfigurationReader.getInstance().
                getConfigProperty(QualysScannerConstants.SCHEDULER_DELAY)));
    }

    /**
     * Set credentials for the basic authorization.
     *
     * @return basic authentication base 64 encoded string
     * @throws ScannerException Error occurred while encoding the credentials.
     */
    private char[] setCredentials() throws ScannerException {
        char[] basicAuth;
        char[] qualysUsername = QualysScannerConfiguration.getInstance().getUsername();
        char[] qualysPassword = QualysScannerConfiguration.getInstance().getPassword();
        String credential = new String(qualysUsername) + ":" + new String(qualysPassword);
        try {
            basicAuth = new String(new Base64().encode(credential.getBytes()), "UTF-8").toCharArray();
            Arrays.fill(qualysUsername, '0');
            Arrays.fill(qualysPassword, '0');
        } catch (UnsupportedEncodingException e) {
            throw new ScannerException("Qualys credentials could not be encoded\"", e);
        }
        return basicAuth;
    }

    /**
     * Get the current date.
     *
     * @return formatted date and time
     */
    private static String getDate() {
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss");
        return ft.format(date);
    }

    /**
     * Get value for given tag.
     *
     * @param filePath filePath file path of the list of data
     * @param tagName  tag name
     * @param name     Name
     * @return id
     * @throws ParserConfigurationException Error occurred while parsing
     * @throws IOException                  IOException
     * @throws SAXException                 Error occurred while parsing
     */
    private String getIdForGivenTag(String filePath, String tagName, String name)
            throws ParserConfigurationException, IOException, SAXException {
        File file = new File(filePath);
        String id = null;
        NodeList nodeList;
        DocumentBuilder builder;

        DocumentBuilderFactory dbFactory = getSecuredDocumentBuilderFactory();
        builder = dbFactory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        nodeList = doc.getElementsByTagName(tagName);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                if (eElement.getElementsByTagName(QualysScannerConstants.NAME_KEYWORD).item(0).getTextContent()
                        .equals(name)) {
                    id = eElement.getElementsByTagName(QualysScannerConstants.ID_KEYWORD).item(0).getTextContent();
                }
            }
        }
        return id;
    }
}
