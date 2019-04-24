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

package org.wso2.security.tools.scanmanagers.scanners.qualys.handler;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.scanmanger.scanners.qualys.QualysScannerConstants;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class is responsible to invoke qualys api
 */
public class QualysApiInvoker {


    //    private static final Log log = LogFactory.getLog(QualysApiInvoker.class);

    //    public char[] getBasicAuth() {
    //        return basicAuth;
    //    }

    public void setBasicAuth(char[] basicAuth) {
        this.basicAuth = basicAuth;
    }

    private char[] basicAuth;

    /**
     * Purge Web Application.
     *
     * @param host     host of Qualys Scanner
     * @param webAppId Web Application Id
     * @return true if application is purged successfully
     * @throws IOException                  error occurred while purging the application
     * @throws ParserConfigurationException Occurred while retrieving the web id.
     * @throws SAXException                 Occurred while retrieving the web id.
     */
    public Boolean purgeScan(String host, String webAppId)
            throws IOException, ParserConfigurationException, SAXException {
        Boolean isPurgedScan = null;
        String url = host.concat(QualysScannerConstants.QUALYS_PURGE_SCAN_API.concat(webAppId));
        HttpResponse response;
        response = doHttpPost(url, null);
        if (response.getStatusLine().getStatusCode() == 200) {
            if (webAppId.equalsIgnoreCase(getTagData(response, "id"))) {
                isPurgedScan = true;
            }
        }
        return isPurgedScan;
    }

    /**
     * Invoke Qualys Api
     * @param host host url
     * @param scanId scanId
     * @return return true if cancelled
     * @throws IOException                  error occurred while cancelling the application
     * @throws ParserConfigurationException Occurred while retrieving the cancel Scan.
     * @throws SAXException                 Occurred while retrieving the cancel Scan.
     */
    public Boolean cancelScan(String host, String scanId) throws IOException, ParserConfigurationException,
            SAXException {
        Boolean isScanCancelled = null;
        String url = host.concat(QualysScannerConstants.QUALYS_CANCEL_SCAN_API).concat(scanId);
        HttpResponse response = doHttpPost(url, null);
        if (response.getStatusLine().getStatusCode() == 200) {
            if (scanId.equalsIgnoreCase(getTagData(response, "id"))) {
                isScanCancelled = true;
            }
        }
        return isScanCancelled;
    }

    /**
     * Call the Api to add authentication script in qualys end.
     *
     * @param host                  qualys endpoint
     * @param authScriptRequestBody addAuthentication script request body.
     * @return auth script id
     * @throws IOException                  Occurred IO exception while calling the api
     * @throws ParserConfigurationException Occurred while retrieving the script id.
     * @throws SAXException                 Occurred while retrieving the script id.
     */
    public String addAuthenticationScript(String host, String authScriptRequestBody)
            throws IOException, ParserConfigurationException, SAXException {
        String authScriptId = null;
        String url = host.concat(QualysScannerConstants.QUALYS_ADD_AUTH_SCRIPT_API);
        HttpResponse response = doHttpPost(url, authScriptRequestBody);
        if (response.getStatusLine().getStatusCode() == 200) {
            authScriptId = getTagData(response, "id");
        }
        return authScriptId;
    }

    /**
     * Call the Api to add authentication script to web app.
     *
     * @param host                    qualys endpoint
     * @param updateWebAppRequestBody update web app request body.
     * @param webId                   web id
     * @return web id
     * @throws IOException                  Occurred IO exception while calling the api
     * @throws ParserConfigurationException Occurred while retrieving the web id.
     * @throws SAXException                 Occurred while retrieving the web id.
     */
    public String updateWebApp(String host, String updateWebAppRequestBody, String webId)
            throws IOException, ParserConfigurationException, SAXException {
        String retrievedWebId = null;
        String url = host.concat(QualysScannerConstants.QUALYS_WEB_UPDATE_API).concat(webId);
        HttpResponse response = doHttpPost(url, updateWebAppRequestBody);
        if (response.getStatusLine().getStatusCode() == 200) {
            retrievedWebId = getTagData(response, "id");
        }
        return retrievedWebId;
    }

    /**
     * Call the Api to create scan report
     *
     * @param host                    Qualys endpoint
     * @param createReportRequestBody create report request body
     * @return report id
     * @throws IOException                  Occurred IO exception while calling the api
     * @throws ParserConfigurationException Occurred while retrieving the web id.
     * @throws SAXException                 Occurred while retrieving the web id.
     */
    public String createReport(String host, String createReportRequestBody)
            throws IOException, ParserConfigurationException, SAXException {
        String reportId = null;
        String url = host.concat(host.concat(QualysScannerConstants.QUALYS_WEB_APP_REPORT_CREATE_API));
        HttpResponse response = doHttpPost(url, createReportRequestBody);
        if (response.getStatusLine().getStatusCode() == 200) {
            reportId = getTagData(response, "id");
        }
        return reportId;
    }

    /**
     * Download Report
     *
     * @param host     host ur;
     * @param reportId report Id
     * @throws IOException Occurred IO exception while calling the api
     */
    public Boolean downloadReport(String host, String reportId) throws IOException {
        Boolean isDownloadSuccessfull = null;
        String url = host.concat(QualysScannerConstants.QUALYS_REPORT_DOWNLOAD_API.concat(reportId));
        HttpResponse response = doHttpGet(url);
        if (response.getStatusLine().getStatusCode() == 200) {
            isDownloadSuccessfull = true;
        }
        return isDownloadSuccessfull;
    }

    /**
     * Launch the scan.
     *
     * @param host                  qualys endpoint
     * @param launchScanRequestBody launch scan request body.
     * @return scannerScanId
     * @throws IOException                  Occurred IO exception while calling the api
     * @throws ParserConfigurationException Occurred while retrieving the scan id
     * @throws SAXException                 Occurred while retrieving the scan id
     */
    public String launchScan(String host, String launchScanRequestBody)
            throws IOException, ParserConfigurationException, SAXException {
        String scanId = null;
        String url = host.concat(QualysScannerConstants.QUALYS_START_SCAN_API);
        HttpResponse response = doHttpPost(url, launchScanRequestBody);
        if (response.getStatusLine().getStatusCode() == 200) {
            scanId = getTagData(response, "id");
        }
        return scanId;
    }

    /**
     * Retrieve Scan status. @see <a href="https://www.qualys.com/docs/qualys-was-api-user-guide.pdf</a>.
     *
     * @param host   qualys endpoint
     * @param scanId scan id
     * @return scan status
     * @throws IOException                  Occurred IO exception while calling the api
     * @throws ParserConfigurationException Occurred while retrieving the status
     * @throws SAXException                 Occurred while retrieving the status
     */
    public String retrieveStatus(String host, String scanId)
            throws IOException, ParserConfigurationException, SAXException {
        return getStatus(host, scanId, QualysScannerConstants.SCAN_STATUS_TAG);

    }

    /**
     * Retrieve auth status. @see <a href="https://www.qualys.com/docs/qualys-was-api-user-guide.pdf</a>.
     *
     * @param host   qualys endpoint
     * @param scanId scan id
     * @return scan status
     * @throws IOException                  Occurred IO exception while calling the api
     * @throws ParserConfigurationException Occurred while retrieving the status
     * @throws SAXException                 Occurred while retrieving the status
     */
    public String retrieveAuthStatus(String host, String scanId)
            throws IOException, ParserConfigurationException, SAXException {
        return getStatus(host, scanId, QualysScannerConstants.AUTH_STATUS_TAG);
    }

    /**
     * Retrieve Scan result status. @see <a href="https://www.qualys.com/docs/qualys-was-api-user-guide.pdf</a>.
     *
     * @param host   qualys endpoint
     * @param scanId scan id
     * @return scan status
     * @throws IOException                  Occurred IO exception while calling the api
     * @throws ParserConfigurationException Occurred while retrieving the status
     * @throws SAXException                 Occurred while retrieving the status
     */
    public String retrieveResultStatus(String host, String scanId)
            throws IOException, ParserConfigurationException, SAXException {
        return getStatus(host, scanId, QualysScannerConstants.RESULTS_STATUS_TAG);
    }

    /**
     * Retrieve status based on status group type.
     *
     * @param host       qualys endpoint
     * @param scanId     scanId
     * @param statusType Status Type
     * @return status
     * @throws IOException                  Occurred IO exception while calling the api
     * @throws ParserConfigurationException Occurred while retrieving the status
     * @throws SAXException                 Occurred while retrieving the status
     */
    private String getStatus(String host, String scanId, String statusType)
            throws IOException, ParserConfigurationException, SAXException {
        String resultStatus = null;
        String url = host.concat(QualysScannerConstants.QUALYS_GET_STATUS_API.concat(scanId));
        HttpResponse response = doHttpGet(url);
        if (response.getStatusLine().getStatusCode() == 200) {
            resultStatus = getTagData(response, statusType);
        }
        return resultStatus;
    }

    /**
     * get the value of given tag name from http response.
     *
     * @param response http response
     * @param tagName  tag name
     * @return value
     * @throws IOException                  Occurred IO exception while calling the api
     * @throws ParserConfigurationException Occurred while retrieving the data
     * @throws SAXException                 Occurred while retrieving the data
     */
    // TODO: 4/3/19 rename to getTagValue and use xpath
    private String getTagData(HttpResponse response, String tagName)
            throws IOException, ParserConfigurationException, SAXException {
        String result;
        String requiredData = null;
        String responseCode;
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
            elementNodes = doc.getElementsByTagName("ServiceResponse");
            if (elementNodes.getLength() > 0) {
                Element element = (Element) elementNodes.item(0);
                responseCode = element.getElementsByTagName("responseCode").item(0).getTextContent();
                if (responseCode != null) {
                    if (responseCode.equals("SUCCESS")) {
                        requiredData = element.getElementsByTagName(tagName).item(0).getTextContent();
                    }
                }
            }
        }
        return requiredData;
    }

    /**
     * Perform a http post request.
     *
     * @param url         url
     * @param requestBody http post request body
     * @return response response of HTTP Post Request
     * @throws IOException Error occurred while processing the http post request
     */
    private HttpResponse doHttpPost(String url, String requestBody) throws IOException {
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
        return response;
    }

    /**
     * Perform a http get request.
     *
     * @param url url
     * @return response
     * @throws IOException Error occurred while processing the http post request
     */
    private HttpResponse doHttpGet(String url) throws IOException {
        HttpGet getRequest = new HttpGet(url);
        getRequest.addHeader("Authorization", "Basic " + new String(basicAuth));
        getRequest.addHeader("Accept", "application/xml");
        HttpClient client = HttpClientBuilder.create().build();
        return client.execute(getRequest);
    }
}
