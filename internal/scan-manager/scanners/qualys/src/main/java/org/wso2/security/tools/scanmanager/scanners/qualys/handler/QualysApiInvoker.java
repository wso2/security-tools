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

import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.security.scanmanager.common.exception.RetryExceededException;
import org.wso2.security.tools.scanmanager.common.util.HTTPUtil;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.config.QualysScannerConfiguration;

import java.io.IOException;

/**
 * This class is responsible to invoke Qualys API.
 */
public class QualysApiInvoker {

    private static final Logger log = LogManager.getLogger(QualysApiInvoker.class);

    private char[] basicAuth;
    private Long retryTimeInterval;

    // Endpoints of Qualys Scanner.
    private String startScanEndpoint;
    private String purgeScanEndpoint;
    private String cancelScanEndpoint;
    private String getStatusEndpoint;
    private String deleteAuthRecordEndpoint;
    private String addAuthScriptEndpoint;
    private String updateWebAppEndpoint;
    private String createReportEndpoint;
    private String downloadReportEndpoint;
    private String getReportStatusEndpoint;

    public QualysApiInvoker() {

        // Platform base URL of Qualys.
        String host = QualysScannerConfiguration.getInstance().getHost();

        // Retry time interval.
        this.retryTimeInterval = Long.parseLong(QualysScannerConfiguration.getInstance()
                .getConfigProperty(ScannerConstants.CALLBACK_RETRY_INCREASE_SECONDS));

        // Set required endpoints.
        this.purgeScanEndpoint = host.concat(QualysScannerConstants.QUALYS_PURGE_SCAN_API);
        this.cancelScanEndpoint = host.concat(QualysScannerConstants.QUALYS_CANCEL_SCAN_API);
        this.addAuthScriptEndpoint = host.concat(QualysScannerConstants.QUALYS_ADD_AUTH_SCRIPT_API);
        this.updateWebAppEndpoint = host.concat(QualysScannerConstants.QUALYS_WEB_UPDATE_API);
        this.createReportEndpoint = host.concat(QualysScannerConstants.QUALYS_WEB_APP_REPORT_CREATE_API);
        this.downloadReportEndpoint = host.concat(QualysScannerConstants.QUALYS_REPORT_DOWNLOAD_API);
        this.startScanEndpoint = host.concat(QualysScannerConstants.QUALYS_START_SCAN_API);
        this.getStatusEndpoint = host.concat(QualysScannerConstants.QUALYS_GET_STATUS_API);
        this.deleteAuthRecordEndpoint = host.concat(QualysScannerConstants.QUALYS_DELETE_AUTH_RECORD_API);
        this.getReportStatusEndpoint = host.concat(QualysScannerConstants.QUALYS_REPORT_STATUS_API);
    }

    public void setBasicAuth(char[] basicAuth) {
        this.basicAuth = basicAuth.clone();
    }

    /**
     * Invoke Qualys API to purge Web Application.
     *
     * @param webAppId web Application ID
     * @return returns http response if response code is 200
     * @throws IOException            error occurred while purging the application
     * @throws InterruptedException   error occurred while purging the application
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public HttpResponse invokePurgeScan(String webAppId)
            throws IOException, InterruptedException, RetryExceededException {
        String url = purgeScanEndpoint.concat(webAppId);
        return HTTPUtil.sendPOST(url, null, basicAuth, retryTimeInterval);
    }

    /**
     * Invoke Qualys API to cancel scan.
     *
     * @param scanId scanId
     * @return returns http response if response code is 200
     * @throws IOException            error occurred while cancelling the application
     * @throws InterruptedException   error occurred while cancelling the application
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public HttpResponse invokeCancelScan(String scanId)
            throws IOException, InterruptedException, RetryExceededException {
        String url = cancelScanEndpoint.concat(scanId);
        return HTTPUtil.sendPOST(url, null, basicAuth, retryTimeInterval);
    }

    /**
     * Invoke Qualys API to add authentication script in qualys end.
     *
     * @param authScriptRequestBody addAuthentication script request body
     * @return returns http response if response code is 200
     * @throws IOException            error occurred IO exception while calling the api
     * @throws InterruptedException   error occurred IO exception while calling the api
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public HttpResponse invokeAuthenticationRecordCreation(String authScriptRequestBody)
            throws IOException, InterruptedException, RetryExceededException {
        String url = addAuthScriptEndpoint;
        return HTTPUtil.sendPOST(url, authScriptRequestBody, basicAuth, retryTimeInterval);
    }

    /**
     * Invoke Qualys API to add authentication script to web app.
     *
     * @param updateWebAppRequestBody update web app request body
     * @param webId                   web id
     * @return returns http response if response code is 200
     * @throws IOException            error occurred IO exception while calling the api
     * @throws InterruptedException   error occurred IO exception while calling the api
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public HttpResponse invokeUpdateWebApp(String updateWebAppRequestBody, String webId)
            throws IOException, InterruptedException, RetryExceededException {
        String url = updateWebAppEndpoint.concat(webId);
        return HTTPUtil.sendPOST(url, updateWebAppRequestBody, basicAuth, retryTimeInterval);
    }

    /**
     * Invoke Qualys API to create scan report.
     *
     * @param createReportRequestBody create report request body
     * @return returns http response if response code is 200
     * @throws IOException            error occurred IO exception while calling the api
     * @throws InterruptedException   error occurred IO exception while calling the api
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public HttpResponse invokeCreateReport(String createReportRequestBody)
            throws IOException, InterruptedException, RetryExceededException {
        String url = createReportEndpoint;
        return HTTPUtil.sendPOST(url, createReportRequestBody, basicAuth, retryTimeInterval);
    }

    /**
     * Invoke Qualys API to download report.
     *
     * @param reportId report Id
     * @return returns http response if response code is 200
     * @throws IOException            error occurred IO exception while calling the api
     * @throws InterruptedException   error occurred IO exception while calling the api
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public HttpResponse invokeReportDownload(String reportId)
            throws IOException, InterruptedException, RetryExceededException {
        String url = downloadReportEndpoint.concat(reportId);
        return HTTPUtil.sendGET(url, basicAuth, retryTimeInterval);
    }

    /**
     * Invoke Qualys API to launch the scan.
     *
     * @param launchScanRequestBody launch scan request body
     * @return returns http response if response code is 200
     * @throws IOException            error occurred IO exception while calling the api
     * @throws InterruptedException   error occurred IO exception while calling the api
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public HttpResponse invokeScanLaunch(String launchScanRequestBody)
            throws IOException, InterruptedException, RetryExceededException {
        String url = startScanEndpoint;
        return HTTPUtil.sendPOST(url, launchScanRequestBody, basicAuth, retryTimeInterval);
    }

    /**
     * Invoke Qualys API to retrieve status based on status group type.
     *
     * @param scanId scanId
     * @return returns http response if response code is 200
     * @throws IOException            error occurred IO exception while calling the api
     * @throws InterruptedException   error occurred IO exception while calling the api
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public HttpResponse invokeGetStatus(String scanId)
            throws IOException, InterruptedException, RetryExceededException {
        String url = getStatusEndpoint.concat(scanId);
        return HTTPUtil.sendGET(url, basicAuth, retryTimeInterval);
    }

    /**
     * Invoke Qualys API to delete authentication record.
     *
     * @param authId authentication script id
     * @return Http response
     * @throws IOException            error occurred IO exception while calling the api
     * @throws InterruptedException   error occurred IO exception while calling the api
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public HttpResponse invokeAuthRecordDeletion(String authId)
            throws IOException, InterruptedException, RetryExceededException {
        String url = deleteAuthRecordEndpoint.concat(authId);
        return HTTPUtil.sendPOST(url, null, basicAuth, retryTimeInterval);
    }

    /**
     * Invoke Qualys API to get report status.
     *
     * @param reportId authentication script id
     * @return Http response
     * @throws IOException            error occurred IO exception while calling the api
     * @throws InterruptedException   error occurred IO exception while calling the api
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public HttpResponse invokeGetReportStatus(String reportId)
            throws IOException, InterruptedException, RetryExceededException {
        String url = getReportStatusEndpoint.concat(reportId);
        return HTTPUtil.sendGET(url, basicAuth, retryTimeInterval);
    }
}
