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

package org.wso2.security.tools.scanmanager.scanner.qualys.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.config.QualysScannerConfiguration;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanContext;
import org.wso2.security.tools.scanmanager.scanners.qualys.utils.RequestBodyBuilder;
import org.wso2.security.tools.scanmanger.scanners.qualys.QualysScannerConstants;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * This class is responsible to handle the required  use cases of Qualys scanner.
 */
public class QualysScanHandler {

    //    private final Log log = LogFactory.getLog(QualysScanHandler.class);
    private QualysApiInvoker qualysApiInvoker;

    public QualysScanHandler(QualysApiInvoker qualysApiInvoker) {
        this.qualysApiInvoker = qualysApiInvoker;
    }

    public QualysApiInvoker getQualysApiInvoker() {
        return qualysApiInvoker;
    }

    /**
     * Prepare the scan before launching the scan. Main tasks are Adding the authentication scripts and crawling
     * scripts.
     *
     * @param fileMap Map that contains the file paths.
     * @param appID   Application ID
     * @param jobId   Job ID
     * @param appName Web Application Name
     * @param host    host url of qualys
     * @return Authentication script id
     * @throws ScannerException Error occurred while adding authentication scripts
     */
    public String prepareScan(String appID, String jobId, String appName, Map<String, List<String>> fileMap,
            String host) throws ScannerException {
        String authScriptId;
        // Purging Scan before launching the scan.
        purgeScan(host, appID, jobId);
        // Add authentication script to Qualys scanner.
        authScriptId = addAuthenticationRecord(host, appID, jobId, fileMap);
        // Update web application with added authentication script.
        updateWebApp(host, appID, appName, authScriptId, jobId);
        return authScriptId;
    }

    /**
     * Launching the scan in qualys scan portal.
     *
     * @param scanContext Object that contains the scanner specific parameters.
     * @param host        host url of qualys
     * @return Scanner scan Id
     * @throws ScannerException Error occurred while launching the scan
     */
    public String launchScan(ScanContext scanContext, String host) throws ScannerException {
        String launchScanRequestBody;
        String scannerScanId;
        HttpResponse response;
        try {
            launchScanRequestBody = RequestBodyBuilder.buildScanLaunchRequest(scanContext);
            response = qualysApiInvoker.invokeScanLaunch(host, launchScanRequestBody);
        } catch (TransformerException | InterruptedException | IOException | ParserConfigurationException e) {
            throw new ScannerException("Error occurred while invoking launch scan API : ", e);
        }

        try {
            NodeList serviceResponseNodeList = getResponseNodeList(response);
            String responseCode = getTagValue(serviceResponseNodeList, QualysScannerConstants.RESPONSE_CODE);
            if (QualysScannerConstants.SUCCESS.equalsIgnoreCase(responseCode)) {
                scannerScanId = getTagValue(serviceResponseNodeList, QualysScannerConstants.ID_KEYWORD);
                String message =
                        " Qualys Scan for " + scanContext.getJobID() + " has successfully submitted " + scannerScanId;
                CallbackUtil.persistScanLog(scanContext.getJobID(), message, LogType.INFO);
            } else {
                String errorMessage = "Error occurred while launching the scan. " + responseCode + " : " + getTagValue(
                        serviceResponseNodeList, QualysScannerConstants.ERROR_MESSAGE);
                throw new ScannerException(errorMessage);
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new ScannerException("Error occurred while reading the service response of launching scan. ", e);
        }
        return scannerScanId;
    }

    /**
     * Cancelling Scan.
     *
     * @param host   host url
     * @param scanId scanId
     * @param jobId  JibID
     * @throws ScannerException Error occurred while cancelling scan.
     */
    public void cancelScan(String host, String scanId, String jobId) throws ScannerException {
        HttpResponse response;
        String status = retrieveScanStatus(host, scanId);
        try {
            if ((status.equalsIgnoreCase(QualysScannerConstants.RUNNING)) || status
                    .equalsIgnoreCase(QualysScannerConstants.SUBMITTED)) {
                response = qualysApiInvoker.inovkeCancelScan(host, scanId);
            } else {
                String message = "Could not find active scan for scanId : " + scanId;
                CallbackUtil.updateScanStatus(jobId, ScanStatus.ERROR, null, scanId);
                CallbackUtil.persistScanLog(jobId, message, LogType.INFO);
                return;
            }
        } catch (IOException | InterruptedException e) {
            throw new ScannerException("Error occurred while invoking cancel scan API : ", e);
        }
        try {
            NodeList serviceResponseNodeList = getResponseNodeList(response);
            String responseCode = getTagValue(serviceResponseNodeList, QualysScannerConstants.RESPONSE_CODE);
            if (QualysScannerConstants.SUCCESS.equalsIgnoreCase(responseCode)) {
                String message = "Scan id : " + scanId + " got cancelled on demand. ";
                CallbackUtil.persistScanLog(jobId, message, LogType.INFO);
                CallbackUtil.updateScanStatus(jobId, ScanStatus.CANCELED, null, scanId);
            } else {
                String errorMessage = "Error occurred while cancelling Scan. " + responseCode + " : " + getTagValue(
                        serviceResponseNodeList, QualysScannerConstants.ERROR_MESSAGE);
                throw new ScannerException(errorMessage);
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new ScannerException("Error occurred while invoking cancel scan API : ", e);
        }

    }

    /**
     * Purging Scan on given web application.
     *
     * @param host  Qualys api url
     * @param appId Application ID
     * @param jobId Job ID
     * @throws ScannerException Error occurred while purging scan
     */
    private void purgeScan(String host, String appId, String jobId) throws ScannerException {
        HttpResponse response;
        try {
            response = qualysApiInvoker.invokePurgeScan(host, appId);
        } catch (IOException | InterruptedException e1) {
            throw new ScannerException("Error occurred while invoking Qualys Purging API : ", e1);
        }
        try {
            NodeList serviceResponseNodeList = getResponseNodeList(response);
            String responseCode = getTagValue(serviceResponseNodeList, QualysScannerConstants.RESPONSE_CODE);
            if (QualysScannerConstants.SUCCESS.equalsIgnoreCase(responseCode)) {
                String message = "Application : " + appId + " is purged successfully ";
                CallbackUtil.persistScanLog(jobId, message, LogType.INFO);
            } else {
                String errorMessage = "Error occurred while purging Scan. " + responseCode + " : " + getTagValue(
                        serviceResponseNodeList, QualysScannerConstants.ERROR_MESSAGE);
                throw new ScannerException(errorMessage);
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ScannerException("Error occurred while reading the service response of purging scan. ", e);
        }
    }

    /**
     * Handle report creation task.
     *
     * @param host       Qualys URL
     * @param webId      Web application id
     * @param jobId      Job ID
     * @param reportType Report type
     * @return Report ID
     * @throws ScannerException Error occurred while creating report.
     */
    public String createReport(String host, String webId, String jobId, String reportType) throws ScannerException {
        HttpResponse response;
        String reportId;
        try {
            String createReportRequestBody = RequestBodyBuilder.buildReportCreationRequest(webId, jobId, reportType);
            response = qualysApiInvoker.invokeCreateReport(host, createReportRequestBody);
        } catch (ParserConfigurationException | InterruptedException | TransformerException | IOException e) {
            throw new ScannerException("Error occurred while invoking report creation API : ", e);
        }
        try {
            NodeList serviceResponseNodeList = getResponseNodeList(response);
            String responseCode = getTagValue(serviceResponseNodeList, QualysScannerConstants.RESPONSE_CODE);
            if (QualysScannerConstants.SUCCESS.equalsIgnoreCase(responseCode)) {
                reportId = getTagValue(serviceResponseNodeList, QualysScannerConstants.ID_KEYWORD);
                String message = "Report is successfully created for job id : " + jobId + " Report id : " + reportId;
                CallbackUtil.persistScanLog(jobId, message, LogType.INFO);
            } else {
                String errorMessage = "Error occurred while creating report. " + responseCode + " : " + getTagValue(
                        serviceResponseNodeList, QualysScannerConstants.ERROR_MESSAGE);
                throw new ScannerException(errorMessage);
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new ScannerException("Error occurred while reading the service response of report creation. ", e);
        }
        return reportId;
    }

    /**
     * Download created report.
     *
     * @param host     Qualys URL
     * @param jobId    Job ID
     * @param reportId Report ID
     * @return filePath Filepath of the report
     * @throws ScannerException Error occurred while downloading report.
     */
    public String downloadReport(String host, String jobId, String reportId) throws ScannerException {
        HttpResponse response;
        String filePath;
        try {
            response = qualysApiInvoker.invokeReportDownload(host, reportId);
        } catch (IOException | InterruptedException e) {
            throw new ScannerException("Error occurred while invoking download report API : ", e);
        }

        try {
            filePath = writeFile(response);
            if (!StringUtils.isEmpty(filePath)) {
                String message = " Report is downloaded successfully : " + reportId;
                CallbackUtil.persistScanLog(jobId, message, LogType.INFO);
            } else {
                throw new ScannerException(
                        "Error occurred while downloading report for jobId : " + jobId + " Report Id : " + reportId);
            }
        } catch (IOException e) {
            throw new ScannerException(
                    "Error occurred while downloading report for jobId : " + jobId + " Report Id : " + reportId);
        }
        return filePath;

    }

    /**
     * Add authentication script to Qualys Scanner as a web authentication record.
     *
     * @param host    Qualys url
     * @param appId   Web application id
     * @param jobId   Job ID
     * @param fileMap File map.
     * @return Authentication record id
     * @throws ScannerException Error occurred while adding authentication record
     */
    private String addAuthenticationRecord(String host, String appId, String jobId, Map<String, List<String>> fileMap)
            throws ScannerException {
        HttpResponse response;
        String authScriptId;
        try {
            //Only one authentication script can be given per single scan.
            String addAuthRecordRequestBody = RequestBodyBuilder.buildAuthScriptCreationRequest(appId,
                    fileMap.get(QualysScannerConstants.AUTHENTICATION_SCRIPTS).get(0));
            response = qualysApiInvoker.invokeAuthenticationRecordCreation(host, addAuthRecordRequestBody);
        } catch (IOException | InterruptedException | TransformerException | ParserConfigurationException e) {
            throw new ScannerException("Error occurred while invoking authentication record creation API : ", e);
        }
        try {
            NodeList serviceResponseNodeList = getResponseNodeList(response);
            String responseCode = getTagValue(serviceResponseNodeList, QualysScannerConstants.RESPONSE_CODE);
            if (QualysScannerConstants.SUCCESS.equalsIgnoreCase(responseCode)) {
                authScriptId = getTagValue(serviceResponseNodeList, QualysScannerConstants.ID_KEYWORD);
                String message = " Web Authentication Record is created successfully : " + authScriptId;
                CallbackUtil.persistScanLog(jobId, message, LogType.INFO);
            } else {
                String errorMessage =
                        "Error occurred while authentication script record creation. " + responseCode + " : "
                                + getTagValue(serviceResponseNodeList, QualysScannerConstants.ERROR_MESSAGE);
                throw new ScannerException(errorMessage);
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new ScannerException(
                    "Error occurred while reading the service response of authentication creation " + "task. ", e);
        }
        return authScriptId;
    }

    /**
     * Update Web Application with created web authentication record.
     *
     * @param host         Qulays URL
     * @param appID        Web application ID
     * @param appName      Web application name
     * @param authScriptId Authentication Record ID
     * @param jobId        Job ID
     * @throws ScannerException Error occurred while updating web application with authentication record id.
     */
    private void updateWebApp(String host, String appID, String appName, String authScriptId, String jobId)
            throws ScannerException {
        HttpResponse response;
        try {
            String updateWebAppRequestBody = RequestBodyBuilder.buildWebAppUpdateRequest(appName, authScriptId);
            response = qualysApiInvoker.updateWebApp(host, updateWebAppRequestBody, appID);
        } catch (TransformerException | InterruptedException | ParserConfigurationException | IOException e) {
            throw new ScannerException("Error occurred while invoking update Web Application API : ", e);
        }
        try {
            NodeList serviceResponseNodeList = getResponseNodeList(response);
            String responseCode = getTagValue(serviceResponseNodeList, QualysScannerConstants.RESPONSE_CODE);
            if (QualysScannerConstants.SUCCESS.equalsIgnoreCase(responseCode)) {
                String message =
                        " Web Application " + appName + " is successfully updated with authentication script " + ": "
                                + authScriptId;
                CallbackUtil.persistScanLog(jobId, message, LogType.INFO);
            } else {
                String errorMessage =
                        "Error occurred while updating web application with authentication script record" + ". "
                                + responseCode + " : " + getTagValue(serviceResponseNodeList,
                                QualysScannerConstants.ERROR_MESSAGE);
                throw new ScannerException(errorMessage);
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new ScannerException(
                    "Error occurred while reading the service response of updating web application with authentication"
                            + "script. ", e);
        }
    }

    /**
     * Retrieve Scan status. @see <a href="https://www.qualys.com/docs/qualys-was-api-user-guide.pdf</a>.
     *
     * @param host           qualys endpoint
     * @param scanId         scan id
     * @param scanStatusType type of the scan status type
     * @return scan status
     * @throws ScannerException Error occurred while retrieving the status
     */
    private String retrieveStatusByType(String host, String scanId, String scanStatusType) throws ScannerException {
        HttpResponse response;
        String status;
        try {
            response = qualysApiInvoker.invokeGetStatus(host, scanId);
        } catch (IOException | InterruptedException e) {
            throw new ScannerException("Error occurred while invoking Qualys get status API : ", e);
        }
        try {
            NodeList serviceResponseNodeList = getResponseNodeList(response);
            String responseCode = getTagValue(serviceResponseNodeList, QualysScannerConstants.RESPONSE_CODE);
            if (QualysScannerConstants.SUCCESS.equalsIgnoreCase(responseCode)) {
                status = getTagValue(serviceResponseNodeList, scanStatusType);
            } else {
                String errorMessage = "Error occurred while retrieving. " + responseCode + " : " + getTagValue(
                        serviceResponseNodeList, QualysScannerConstants.ERROR_MESSAGE);
                throw new ScannerException(errorMessage);
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ScannerException("Error occurred while reading the service response of retrieving status. ", e);
        }
        return status;
    }

    /**
     * Retrieve scan status. @see <a href="https://www.qualys.com/docs/qualys-was-api-user-guide.pdf</a>.
     *
     * @param host   qualys endpoint
     * @param scanId scan Id
     * @return scan status.
     * @throws ScannerException Error occurred while retrieving the status
     */
    public String retrieveScanStatus(String host, String scanId) throws ScannerException {
        return retrieveStatusByType(host, scanId, QualysScannerConstants.SCAN_STATUS_TAG);
    }

    /**
     * Retrieve auth status. @see <a href="https://www.qualys.com/docs/qualys-was-api-user-guide.pdf</a>.
     *
     * @param host   qualys endpoint
     * @param scanId scan id
     * @return scan status
     * @throws ScannerException Error occurred while retrieving status
     */
    public String retrieveAuthStatus(String host, String scanId) throws ScannerException {
        return retrieveStatusByType(host, scanId, QualysScannerConstants.AUTH_STATUS_TAG);
    }

    /**
     * Retrieve Scan result status. @see <a href="https://www.qualys.com/docs/qualys-was-api-user-guide.pdf</a>.
     *
     * @param host   qualys endpoint
     * @param scanId scan id
     * @return scan status
     * @throws ScannerException Error occurred while retrieving status
     */
    public String retrieveResultStatus(String host, String scanId) throws ScannerException {
        return retrieveStatusByType(host, scanId, QualysScannerConstants.RESULTS_STATUS_TAG);
    }

    public void doCleanUp(String host, String authId, String jobId) throws ScannerException {
        deleteAuthRecord(host, authId, jobId);
    }

    private void deleteAuthRecord(String host, String authId, String jobId) throws ScannerException {
        HttpResponse response;
        try {
            response = qualysApiInvoker.invokeAuthRecordDeletion(host, authId);
        } catch (IOException | InterruptedException e1) {
            throw new ScannerException("Error occurred while invoking Qualys Auth Deletion API : ", e1);
        }
        try {
            NodeList serviceResponseNodeList = getResponseNodeList(response);
            String responseCode = getTagValue(serviceResponseNodeList, QualysScannerConstants.RESPONSE_CODE);
            if (QualysScannerConstants.SUCCESS.equalsIgnoreCase(responseCode)) {
                String message = "Authentication record " + authId + " is deleted successfully";
                CallbackUtil.persistScanLog(jobId, message, LogType.INFO);
            } else {
                String errorMessage =
                        "Error occurred while deleting authentication record. " + responseCode + " : " + getTagValue(
                                serviceResponseNodeList, QualysScannerConstants.ERROR_MESSAGE);
                throw new ScannerException(errorMessage);
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ScannerException(
                    "Error occurred while reading the service response of deleting authentication" + " script. ", e);
        }
    }

    /**
     * This methods is used to create a document of Http response and return element nodes as a list.
     *
     * @param response http response of invoked api.
     * @return NodeList of document which represents the http response
     * @throws IOException                  Error occurred while converting to document
     * @throws ParserConfigurationException Error occurred while converting to document
     * @throws SAXException                 Error occurred while converting to document
     */
    private NodeList getResponseNodeList(HttpResponse response)
            throws IOException, ParserConfigurationException, SAXException {
        String result;
        StringBuilder res;
        Document doc;
        NodeList elementNodes;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), "UTF-8"))) {
            res = new StringBuilder();
            while ((result = br.readLine()) != null) {
                res.append(result);
            }
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(res.toString())));
            elementNodes = doc.getElementsByTagName(QualysScannerConstants.SERVICE_RESPONSE);
        }
        return elementNodes;
    }

    /**
     * Write file
     *
     * @param response Http response
     * @return file path
     * @throws IOException exception occurred while writing file.
     */
    private String writeFile(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        String filePath = null;
        if (entity != null) {
            Pattern contentDispositionPattern = Pattern.compile(QualysScannerConstants.CONTENT_DISPOSITION_PATTERN);
            Matcher m = contentDispositionPattern
                    .matcher(response.getFirstHeader(QualysScannerConstants.CONTENT_DISPOSITION).getValue());
            String contentDisposition = null;
            if (m.find()) {
                contentDisposition = m.group(1);
            }
            if (!StringUtils.isEmpty(contentDisposition)) {
                String filename = contentDisposition
                        .substring(contentDisposition.indexOf("\"") + 1, contentDisposition.lastIndexOf("\""));
                filePath = QualysScannerConfiguration.getInstance().getReportFilePath() + File.separator + filename;
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    entity.writeTo(fos);
                    fos.close();
                }
            }
        }
        return filePath;
    }

    /**
     * Get data based on given tag name of service response.
     *
     * @param nodeList NodeList of response
     * @param tagName  tag name
     * @return value of the tag name
     */
    private String getTagValue(NodeList nodeList, String tagName) {
        String data = null;
        if (nodeList.getLength() > 0) {
            Element element = (Element) nodeList.item(0);
            data = element.getElementsByTagName(tagName).item(0).getTextContent();
        }
        return data;
    }
}
