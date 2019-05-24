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

package org.wso2.security.tools.scanmanager.scanners.qualys.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpStatus;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.config.QualysScannerConfiguration;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible to invoke qualys api.
 */
public class QualysApiInvoker {

    private static final Log log = LogFactory.getLog(QualysApiInvoker.class);

    private char[] basicAuth;
    Long retryTimeInterval = Long.valueOf(0);

    public void setBasicAuth(char[] basicAuth) {
        this.basicAuth = basicAuth.clone();
    }

    /**
     * Invoke Qualys API to purge Web Application.
     *
     * @param host     host of Qualys Scanner
     * @param webAppId Web Application Id
     * @return returns http response if response code is 200
     * @throws IOException          error occurred while purging the application
     * @throws InterruptedException error occurred while purging the application
     */
    public HttpResponse invokePurgeScan(String host, String webAppId) throws IOException, InterruptedException {
        String url = host.concat(QualysScannerConstants.QUALYS_PURGE_SCAN_API.concat(webAppId));
        return doHttpPost(url, null);
    }

    /**
     * Invoke Qualys API to cancel scan.
     *
     * @param host   host url
     * @param scanId scanId
     * @return returns http response if response code is 200
     * @throws IOException          Error occurred while cancelling the application
     * @throws InterruptedException Error occurred while cancelling the application
     */
    public HttpResponse inovkeCancelScan(String host, String scanId) throws IOException, InterruptedException {
        String url = host.concat(QualysScannerConstants.QUALYS_CANCEL_SCAN_API).concat(scanId);
        return doHttpPost(url, null);
    }

    /**
     * Call the API to add authentication script in qualys end.
     *
     * @param host                  qualys endpoint
     * @param authScriptRequestBody addAuthentication script request body
     * @return returns http response if response code is 200
     * @throws IOException          Occurred IO exception while calling the api
     * @throws InterruptedException Occurred IO exception while calling the api.
     */
    public HttpResponse invokeAuthenticationRecordCreation(String host, String authScriptRequestBody)
            throws IOException, InterruptedException {
        String url = host.concat(QualysScannerConstants.QUALYS_ADD_AUTH_SCRIPT_API);
        return doHttpPost(url, authScriptRequestBody);
    }

    /**
     * Call the API to add authentication script to web app.
     *
     * @param host                    qualys endpoint
     * @param updateWebAppRequestBody update web app request body.
     * @param webId                   web id
     * @return returns http response if response code is 200
     * @throws IOException          Occurred IO exception while calling the api
     * @throws InterruptedException Occurred IO exception while calling the api
     */
    public HttpResponse updateWebApp(String host, String updateWebAppRequestBody, String webId)
            throws IOException, InterruptedException {
        String url = host.concat(QualysScannerConstants.QUALYS_WEB_UPDATE_API).concat(webId);
        return doHttpPost(url, updateWebAppRequestBody);
    }

    /**
     * Call the API to create scan report.
     *
     * @param host                    Qualys endpoint
     * @param createReportRequestBody create report request body
     * @return returns http response if response code is 200
     * @throws IOException          Occurred IO exception while calling the api
     * @throws InterruptedException Occurred IO exception while calling the api
     */
    public HttpResponse invokeCreateReport(String host, String createReportRequestBody)
            throws IOException, InterruptedException {
        String url = host.concat(host.concat(QualysScannerConstants.QUALYS_WEB_APP_REPORT_CREATE_API));
        HttpResponse response = doHttpPost(url, createReportRequestBody);
        return response;
    }

    /**
     * Download Report.
     *
     * @param host     host ur;
     * @param reportId report Id
     * @return returns http response if response code is 200
     * @throws IOException          Occurred IO exception while calling the api
     * @throws InterruptedException Occurred IO exception while calling the api
     */
    public HttpResponse invokeReportDownload(String host, String reportId) throws IOException, InterruptedException {
        String url = host.concat(QualysScannerConstants.QUALYS_REPORT_DOWNLOAD_API.concat(reportId));
        return doHttpGet(url);
    }

    /**
     * Launch the scan.
     *
     * @param host                  qualys endpoint
     * @param launchScanRequestBody launch scan request body.
     * @return returns http response if response code is 200
     * @throws IOException          Occurred IO exception while calling the api
     * @throws InterruptedException Occurred IO exception while calling the api
     */
    public HttpResponse invokeScanLaunch(String host, String launchScanRequestBody)
            throws IOException, InterruptedException {
        String url = host.concat(QualysScannerConstants.QUALYS_START_SCAN_API);
        return doHttpPost(url, launchScanRequestBody);
    }

    /**
     * Retrieve status based on status group type.
     *
     * @param host   qualys endpoint
     * @param scanId scanId
     * @return returns http response if response code is 200
     * @throws IOException          Occurred IO exception while calling the api
     * @throws InterruptedException Occurred IO exception while calling the api
     */
    public HttpResponse invokeGetStatus(String host, String scanId) throws IOException, InterruptedException {
        String url = host.concat(QualysScannerConstants.QUALYS_GET_STATUS_API.concat(scanId));
        HttpResponse response = doHttpGet(url);
        return response;
    }

    /**
     * Invoke authentication record deletion API.
     *
     * @param host   qualys end point.
     * @param authId authentication script id.
     * @return Http response
     * @throws IOException          Occurred IO exception while calling the api
     * @throws InterruptedException Occurred IO exception while calling the api
     */
    public HttpResponse invokeAuthRecordDeletion(String host, String authId) throws IOException, InterruptedException {
        String url = host.concat(QualysScannerConstants.QUALYS_DELETE_AUTH_RECORD_API.concat(authId));
        HttpResponse response = doHttpPost(url, null);
        return response;
    }

    /**
     * Perform a http post request.
     *
     * @param url         url
     * @param requestBody http post request body
     * @return response response of HTTP Post Request
     * @throws IOException          Error occurred while processing the http post request
     * @throws InterruptedException Error occurred while processing the http post request
     */
    private HttpResponse doHttpPost(String url, String requestBody) throws IOException, InterruptedException {
        HttpResponse response;
        HttpPost postRequest = new HttpPost(url);
        postRequest.addHeader("Authorization", "Basic " + new String(basicAuth));
        HttpClient client = HttpClientBuilder.create().build();
        StringEntity entity;
        if (requestBody != null) {
            entity = new StringEntity(requestBody, ContentType.create("text/xml", Consts.UTF_8));
            postRequest.setEntity(entity);
        }
        response = client.execute(postRequest);
        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode == HttpStatus.OK.value()) {
            return response;
        } else if (HttpStatus.NOT_FOUND.value() == responseCode
                || HttpStatus.INTERNAL_SERVER_ERROR.value() == responseCode
                || HttpStatus.REQUEST_TIMEOUT.value() == responseCode
                || HttpStatus.SERVICE_UNAVAILABLE.value() == responseCode) {
            retryTimeInterval += Long.parseLong(QualysScannerConfiguration.getInstance()
                    .getConfigProperty(ScannerConstants.CALLBACK_RETRY_INCREASE_SECONDS));
            String logMessage =
                    "Qualys endpoint is not currently available and will retry after " + retryTimeInterval + " Seconds";
            log.info(logMessage);
            TimeUnit.MINUTES.sleep(retryTimeInterval);
            doHttpPost(url, requestBody);
        } else {
            throw new HttpResponseException(response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase());
        }
        return response;
    }

    /**
     * Perform a http get request.
     *
     * @param url url
     * @return response
     * @throws IOException          Error occurred while processing the http post request
     * @throws InterruptedException Error occurred while processing the http post request
     */
    private HttpResponse doHttpGet(String url) throws IOException, InterruptedException {
        HttpResponse response;
        HttpGet getRequest = new HttpGet(url);
        getRequest.addHeader("Authorization", "Basic " + new String(basicAuth));
        getRequest.addHeader("Accept", "application/xml");
        HttpClient client = HttpClientBuilder.create().build();
        response = client.execute(getRequest);
        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode == HttpStatus.OK.value()) {
            return response;
        } else if (HttpStatus.NOT_FOUND.value() == responseCode
                || HttpStatus.INTERNAL_SERVER_ERROR.value() == responseCode
                || HttpStatus.REQUEST_TIMEOUT.value() == responseCode
                || HttpStatus.SERVICE_UNAVAILABLE.value() == responseCode) {
            retryTimeInterval += Long.parseLong(QualysScannerConfiguration.getInstance()
                    .getConfigProperty(ScannerConstants.CALLBACK_RETRY_INCREASE_SECONDS));

            log.info("Qualys endpoint is not currently available and will retry after " + retryTimeInterval
                    + " Seconds");
            TimeUnit.MINUTES.sleep(retryTimeInterval);
            doHttpGet(url);
        } else {
            throw new HttpResponseException(response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase());
        }
        return response;
    }
}
