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

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.NodeList;
import org.wso2.security.scanmanager.common.exception.RetryExceededException;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.model.CallbackLog;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.ErrorProcessingUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.FileUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.XMLUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.config.QualysScannerConfiguration;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanContext;
import org.wso2.security.tools.scanmanager.scanners.qualys.util.RequestBodyBuilder;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * This class is responsible to handle the required  use cases of Qualys scanner.
 */
public class QualysScanHandler {

    private static final Logger log = (Logger) LogManager.getLogger(QualysScanHandler.class);

    private QualysApiInvoker qualysApiInvoker;

    public QualysScanHandler(QualysApiInvoker qualysApiInvoker) {
        this.qualysApiInvoker = qualysApiInvoker;
    }

    public QualysApiInvoker getQualysApiInvoker() {
        return qualysApiInvoker;
    }

    /**
     * Perform prerequisite tasks of launching new scan when authentication script is provided. Prerequisite tasks are :
     * 1. Download authentication scripts from ftp location.
     * 2. Purge scan of provided web application.
     * 3. Adding the authentication scripts to Qualys.
     * 4. Update provided web application with added authentication script.
     *
     * @param fileMap        map that contains the file paths
     * @param appID          application ID
     * @param jobId          job ID
     * @param appName        web application name
     * @param applicationUrl application SCAN_URL for scan
     * @param authRegex      regex to check whether authentication is succeeded or not
     * @return authentication script ID
     * @throws ScannerException error occurred while adding authentication scripts
     */
    public String prepareScan(String appID, String jobId, String appName, Map<String, List<String>> fileMap,
            String applicationUrl, String authRegex) throws ScannerException {
        String authScriptId;

        if (fileMap.get(QualysScannerConstants.AUTHENTICATION_SCRIPTS).size() != 0) {
            // Download authentication script form ftp location.
            File authScriptFile = downloadAuthenticationScripts(
                    fileMap.get(QualysScannerConstants.AUTHENTICATION_SCRIPTS), appID, jobId);

            // Add authentication script to Qualys scanner.
            authScriptId = addAuthenticationRecord(appID, jobId, authScriptFile, authRegex);

            // Update web application with added authentication script.
            updateWebApp(appID, appName, authScriptId, jobId, applicationUrl);
        } else {
            return null;
        }
        return authScriptId;
    }

    /**
     * Launching the scan in qualys scan portal.
     *
     * @param scanContext object that contains the scanner specific parameters
     * @return scanner scan Id
     * @throws ScannerException error occurred while launching the scan
     */
    public String launchScan(ScanContext scanContext) throws ScannerException {
        String launchScanRequestBody;
        String scannerScanId = null;
        HttpResponse response;
        try {
            launchScanRequestBody = RequestBodyBuilder.buildScanLaunchRequest(scanContext);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new ScannerException("Error occurred while building launch scan request body : ", e);
        }
        try {
            response = qualysApiInvoker.invokeScanLaunch(launchScanRequestBody);
        } catch (RetryExceededException | InterruptedException | IOException e) {
            throw new ScannerException("Error occurred while invoking launch scan API : ", e);
        }
        NodeList serviceResponseNodeList = processServiceResponse(response, QualysScannerConstants.LAUNCH_SCAN);
        if (serviceResponseNodeList != null) {
            scannerScanId = XMLUtil.getTagValue(serviceResponseNodeList, QualysScannerConstants.ID_KEYWORD);
            String message =
                    " Qualys Scan for " + scanContext.getJobID() + " has successfully submitted " + scannerScanId;
            log.info(new CallbackLog(scanContext.getJobID(), message));
        }
        return scannerScanId;
    }

    /**
     * Cancelling Scan.
     *
     * @param scanContext scan context object which holds the scan related metadata
     * @throws ScannerException error occurred while cancelling scan
     */
    public void cancelScan(ScanContext scanContext) throws ScannerException {
        HttpResponse response;
        String status = retrieveScanStatus(scanContext.getScannerScanId());
        try {
            if ((status.equalsIgnoreCase(QualysScannerConstants.RUNNING)) || status
                    .equalsIgnoreCase(QualysScannerConstants.SUBMITTED)) {
                response = qualysApiInvoker.invokeCancelScan(scanContext.getScannerScanId());
            } else {
                String message = "Could not find an active scan for scanId : " + scanContext.getScannerScanId();
                log.info(new CallbackLog(scanContext.getJobID(), message));
                CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.ERROR, null, null);
                return;
            }
        } catch (IOException | InterruptedException | RetryExceededException e) {
            throw new ScannerException("Error occurred while invoking cancel scan API : ", e);
        }
        NodeList serviceResponseNodeList = processServiceResponse(response, QualysScannerConstants.CANCEL_SCAN);
        if (serviceResponseNodeList != null) {
            String message = "Scan for Job ID : " + scanContext.getJobID() + " got cancelled ";
            log.info(new CallbackLog(scanContext.getJobID(), message));

            // Delete added authentication record before updating the status.
            doCleanUp(scanContext.getAuthId(), scanContext.getJobID());
            CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.CANCELED, null,
                    scanContext.getScannerScanId());
        }
    }

    /**
     * Purging the scan of given web application.
     *
     * @param appId application ID
     * @param jobId job ID
     * @throws ScannerException error occurred while purging scan
     */
    public void purgeScan(String appId, String jobId) throws ScannerException {
        HttpResponse response;
        try {
            response = qualysApiInvoker.invokePurgeScan(appId);
        } catch (IOException | InterruptedException | RetryExceededException e1) {
            throw new ScannerException("Error occurred while invoking Qualys Purging API : ", e1);
        }
        NodeList serviceResponseNodeList = processServiceResponse(response, QualysScannerConstants.PURGE_SCAN);
        if (serviceResponseNodeList != null) {
            String message = "Application is purged successfully and ready for scan ";
            log.info(new CallbackLog(jobId, message));
        }
    }

    /**
     * Handle report creation task.
     *
     * @param webId      web application ID
     * @param jobId      job ID
     * @param reportType report type
     * @param reportTemplateID report template ID
     * @return report ID
     * @throws ScannerException error occurred while creating report.
     */
    public String createReport(String webId, String jobId, String reportType, String reportTemplateID)
            throws ScannerException {
        HttpResponse response;
        String reportId = null;
        String createReportRequestBody = null;
        try {
            createReportRequestBody = RequestBodyBuilder
                    .buildReportCreationRequest(webId, jobId, reportType, reportTemplateID);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new ScannerException("Error occurred while building report creation API request body: ", e);
        }
        try {
            response = qualysApiInvoker.invokeCreateReport(createReportRequestBody);
        } catch (InterruptedException | RetryExceededException | IOException e) {
            throw new ScannerException("Error occurred while invoking report creation API : ", e);
        }
        NodeList serviceResponseNodeList = processServiceResponse(response, QualysScannerConstants.CREATE_REPORT);
        if (serviceResponseNodeList != null) {
            reportId = XMLUtil.getTagValue(serviceResponseNodeList, QualysScannerConstants.ID_KEYWORD);
            String message = "Report is successfully created for Report id : " + reportId;
            log.info(new CallbackLog(jobId, message));
        }
        return reportId;
    }

    /**
     * Download created report.
     *
     * @param jobId               Job ID
     * @param reportId            Report ID
     * @param reportDirectoryPath directory path where reports needs to be downloaded
     * @return filePath Filepath of the report
     * @throws ScannerException Error occurred while downloading report
     */
    public String downloadReport(String jobId, String reportId, String reportDirectoryPath) throws ScannerException {
        HttpResponse response;
        String filePath;
        try {
            response = qualysApiInvoker.invokeReportDownload(reportId);
        } catch (IOException | InterruptedException | RetryExceededException e) {
            throw new ScannerException("Error occurred while invoking download report API : ", e);
        }
        try {
            filePath = FileUtil.writeFile(response, reportDirectoryPath);
            if (!StringUtils.isEmpty(filePath)) {
                String message = " Report is downloaded successfully : " + reportId;
                log.info(new CallbackLog(jobId, message));
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
     * @param appId          Web application id
     * @param jobId          Job ID
     * @param authScriptFile authentication Script File
     * @param authRegex      regex to check whether authentication is succeeded or not
     * @return Authentication record id
     * @throws ScannerException Error occurred while adding authentication record
     */
    private String addAuthenticationRecord(String appId, String jobId, File authScriptFile, String authRegex)
            throws ScannerException {
        HttpResponse response;
        String authScriptId = null;
        String addAuthRecordRequestBody = null;
        try {
            // Only one authentication script can be provided for a scan.
            addAuthRecordRequestBody = RequestBodyBuilder
                    .buildAuthScriptCreationRequest(appId, authScriptFile.getAbsolutePath(), authRegex);
        } catch (ParserConfigurationException | TransformerException | IOException e) {
            throw new ScannerException(
                    "Error occurred while building authentication record creation API request body : ", e);
        }
        try {
            response = qualysApiInvoker.invokeAuthenticationRecordCreation(addAuthRecordRequestBody);
        } catch (IOException | InterruptedException | RetryExceededException e) {
            throw new ScannerException("Error occurred while invoking authentication record creation API : ", e);
        }
        NodeList serviceResponseNodeList = processServiceResponse(response, QualysScannerConstants.ADD_AUTH_SCRIPT);
        if (serviceResponseNodeList != null) {
            authScriptId = XMLUtil.getTagValue(serviceResponseNodeList, QualysScannerConstants.ID_KEYWORD);
            String message = " Web Authentication Record is created successfully. Web Auth Record ID : " + authScriptId;
            log.info(new CallbackLog(jobId, message));
        }
        return authScriptId;
    }

    /**
     * Update Web Application with created web authentication record.
     *
     * @param appID          web application ID
     * @param appName        web application name
     * @param authScriptId   authentication record ID
     * @param jobId          job ID
     * @param applicationUrl application SCAN_URL for scan
     * @throws ScannerException error occurred while updating web application with authentication record id
     */
    private void updateWebApp(String appID, String appName, String authScriptId, String jobId, String applicationUrl)
            throws ScannerException {
        HttpResponse response;
        String updateWebAppRequestBody = null;
        try {
            updateWebAppRequestBody = RequestBodyBuilder
                    .buildWebAppUpdateRequest(appName, authScriptId, applicationUrl);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new ScannerException("Error occurred while building update Web Application API request body: ", e);
        }
        try {
            response = qualysApiInvoker.invokeUpdateWebApp(updateWebAppRequestBody, appID);
        } catch (RetryExceededException | InterruptedException | IOException e) {
            throw new ScannerException("Error occurred while invoking update Web Application API : ", e);
        }
        NodeList serviceResponseNodeList = processServiceResponse(response, QualysScannerConstants.UPDATE_WEB_APP);
        if (serviceResponseNodeList != null) {
            String message =
                    " Web Application " + appName + " is successfully updated with web auth record: " + authScriptId;
            log.info(new CallbackLog(jobId, message));
        }
    }

    /**
     * Retrieve Scan status.
     *
     * @param scanId         scan id
     * @param scanStatusType type of the scan status type
     * @return scan status
     * @throws ScannerException error occurred while retrieving the status
     * @see <a href="https://www.qualys.com/docs/qualys-was-api-user-guide.pdf">Qualys User Quide</a>
     */
    private String retrieveStatusByType(String scanId, String scanStatusType) throws ScannerException {
        HttpResponse response;
        String status = null;
        try {
            response = qualysApiInvoker.invokeGetStatus(scanId);
        } catch (IOException | InterruptedException | RetryExceededException e) {
            throw new ScannerException("Error occurred while invoking Qualys get status API : ", e);
        }
        NodeList serviceResponseNodeList = processServiceResponse(response, QualysScannerConstants.GET_STATUS);
        if (serviceResponseNodeList != null) {
            status = XMLUtil.getTagValue(serviceResponseNodeList, scanStatusType);
        }
        return status;
    }

    /**
     * Retrieve scan status.
     *
     * @param scanId scan Id
     * @return scan status
     * @throws ScannerException error occurred while retrieving the status
     * @see <a href="https://www.qualys.com/docs/qualys-was-api-user-guide.pdf">Qualys User Quide</a>
     */
    public String retrieveScanStatus(String scanId) throws ScannerException {
        return retrieveStatusByType(scanId, QualysScannerConstants.STATUS_TAG);
    }

    /**
     * Retrieve auth status.
     *
     * @param scanId scan id
     * @return scan status
     * @throws ScannerException error occurred while retrieving status
     * @see <a href="https://www.qualys.com/docs/qualys-was-api-user-guide.pdf">Qualys User Quide</a>
     */
    public String retrieveAuthStatus(String scanId) throws ScannerException {
        return retrieveStatusByType(scanId, QualysScannerConstants.AUTH_STATUS_TAG);
    }

    /**
     * Retrieve Scan result status.
     *
     * @param scanId scan id
     * @return scan status
     * @throws ScannerException error occurred while retrieving status
     * @see <a href="https://www.qualys.com/docs/qualys-was-api-user-guide.pdf">Qualys User Quide</a>
     */
    public String retrieveResultStatus(String scanId) throws ScannerException {
        return retrieveStatusByType(scanId, QualysScannerConstants.RESULTS_STATUS_TAG);
    }

    /**
     * Retrieve report status.
     *
     * @param reportId report ID
     * @return status of the report
     * @throws ScannerException       error occurred while retrieving the report status
     * @throws IOException            error occurred while processing the http post request
     * @throws InterruptedException   error occurred while processing the http post request
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public String getReportStatus(String reportId)
            throws IOException, InterruptedException, ScannerException, RetryExceededException {
        HttpResponse response;
        String status = null;
        response = qualysApiInvoker.invokeGetReportStatus(reportId);
        NodeList serviceResponseNodeList = processServiceResponse(response, QualysScannerConstants.GET_REPORT_STATUS);
        if (serviceResponseNodeList != null) {
            status = XMLUtil.getTagValue(serviceResponseNodeList, QualysScannerConstants.STATUS_TAG);
        }
        return status;
    }

    /**
     * Delete authentication script form Qualys.
     *
     * @param authId authentication script ID
     * @param jobID  job ID
     * @throws ScannerException Error occurred while retrieving status
     */
    public void doCleanUp(String authId, String jobID) throws ScannerException {
        deleteAuthRecord(authId, jobID);
    }

    /**
     * Delete authentication record from qualys scanner.
     *
     * @param authId Scan Id
     * @param jobId  Job Id
     * @throws ScannerException Error occurred while deleting the authentication record
     * @see <a href="https://www.qualys.com/docs/qualys-was-api-user-guide.pdf">Qualys User Quide</a>
     */
    private void deleteAuthRecord(String authId, String jobId) throws ScannerException {
        HttpResponse response;
        try {
            response = qualysApiInvoker.invokeAuthRecordDeletion(authId);
        } catch (IOException | InterruptedException | RetryExceededException e1) {
            throw new ScannerException("Error occurred while invoking Qualys Auth Deletion API : ", e1);
        }
        NodeList serviceResponseNodeList = processServiceResponse(response, QualysScannerConstants.DELETE_AUTH_RECORD);
        if (serviceResponseNodeList != null) {
            String message = "Authentication record " + authId + " is deleted successfully";
            log.info(new CallbackLog(jobId, message));
        }
    }

    /**
     * Download given authentication script from FTP Location.
     *
     * @param fileList authentication file list
     * @param appId    application Id
     * @param jobId    JobId
     * @return Downloaded File
     * @throws ScannerException Error occurred while downloading authentication scripts
     */
    private File downloadAuthenticationScripts(List<String> fileList, String appId, String jobId)
            throws ScannerException {
        String authenticationScriptLocation = fileList.get(0);
        String authenticationScriptFileName = authenticationScriptLocation.substring(authenticationScriptLocation.
                lastIndexOf(File.separator) + 1, authenticationScriptLocation.length());
        String authenticationScriptFilePath = authenticationScriptLocation
                .substring(0, authenticationScriptLocation.lastIndexOf(File.separator));

        File authScriptFile = new File(QualysScannerConfiguration.getInstance()
                .getConfigProperty(QualysScannerConstants.DEFAULT_FTP_AUTH_SCRIPT_PATH) + File.separator
                + authenticationScriptFileName);
        try {
            String logMessage = "Authentication Script is downloading ....";
            log.info(new CallbackLog(jobId, logMessage));
            FileUtil.downloadFromFtp(authenticationScriptFilePath, authenticationScriptFileName, authScriptFile,
                    QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_USERNAME),
                    (QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_PASSWORD))
                            .toCharArray(),
                    QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_HOST),
                    Integer.parseInt(
                            QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_PORT)));
            logMessage = "Authentication Script is downloaded : " + authScriptFile;
            log.info(new CallbackLog(jobId, logMessage));
        } catch (IOException | JSchException | SftpException e) {
            String logMessage = "Error occurred while downloading the authentication script :  " + ErrorProcessingUtil
                    .getFullErrorMessage(e);
            throw new ScannerException(logMessage);
        }
        return authScriptFile;
    }

    /**
     * Process the service response and if it is successful this method returns the NodeList.
     *
     * @param response http response
     * @return nodeList of provided service response
     * @throws ScannerException exception occurred while processing the service response or failed to perform the given
     *                          task
     */
    private NodeList processServiceResponse(HttpResponse response, String task) throws ScannerException {
        NodeList serviceResponseNodeList;
        try {
            serviceResponseNodeList = XMLUtil.getResponseNodeList(response, QualysScannerConstants.SERVICE_RESPONSE);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ScannerException("Error occurred while processing the service response of " + task + " .", e);
        }
        String responseCode = XMLUtil.getTagValue(serviceResponseNodeList, QualysScannerConstants.RESPONSE_CODE);
        if (QualysScannerConstants.SUCCESS.equalsIgnoreCase(responseCode)) {
            return serviceResponseNodeList;
        } else {
            String errorMessage = "Error occurred while performing " + task + " due to : " + XMLUtil
                    .getTagValue(serviceResponseNodeList, QualysScannerConstants.ERROR_MESSAGE);
            throw new ScannerException(errorMessage);
        }
    }
}
