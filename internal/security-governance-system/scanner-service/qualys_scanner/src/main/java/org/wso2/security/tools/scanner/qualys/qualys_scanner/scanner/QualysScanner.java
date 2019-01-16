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
 *
 *
 */

package org.wso2.security.tools.scanner.qualys.qualys_scanner.scanner;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.scanner.qualys.qualys_scanner.utils.QualysScannerConstants;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.wso2.security.tools.scanner.qualys.qualys_scanner.handler.XmlFileHandler.xmlFileBuilder;

public class QualysScanner extends org.wso2.security.tools.scanner.scanner.AbstractScanner {
    private boolean appsInitialized = false;
    private String basicAuth = null;
    private final Log log = LogFactory.getLog(QualysScanner.class);
    private HttpClient httpClient = buildSecureHttpClient();

    /**
     * Overrides the {@code init} method to initialize qualys username and password and calls the
     * {@code getApplicationIds} method to load all the available applications from Qualys scanner.
     */
    @Override
    public void init() throws ScannerException {
        String qualysUsername = ConfigurationReader.getConfigProperty("qualysUsername");
        String qualysPassword = ConfigurationReader.getConfigProperty("qualysPassword");
        basicAuth = setCredentials(qualysUsername, qualysPassword);

        getApplicationIds(); //to get the applications from Qualys backend
    }

    /**
     * Overrides the {@code startScan} method to start a scan.
     *
     * @param scannerRequestObject Request that comes from the user
     * @return Scanner Response Object
     * @throws ScannerException
     */
    @Override
    public ScannerResponseObject startScan(ScannerRequestObject scannerRequestObject) throws ScannerException {
        String appID, responseCode = null;
        String scanID = null;
        String result;
        ScannerResponseObject responseObject = new ScannerResponseObject();

        if (!appsInitialized) {
            init();
        }

        //to get the corresponding application ID from the given product name
        appID = getApplicationId(QualysScannerConstants.QUALYS_APPLICATION_FILE_PATH,
                scannerRequestObject.getProductName());

        //to build the xml request
        String xmlRequest = xmlFileBuilder(appID);

        try { //http post request to Qualys start scan API
            HttpPost postRequest = new HttpPost(QualysScannerConstants.QUALYS_START_SCAN_API);
            postRequest.addHeader("Authorization", "Basic " + basicAuth);

            StringEntity entity = new StringEntity(xmlRequest, ContentType.create("text/xml", Consts.UTF_8));
            postRequest.setEntity(entity);

            HttpResponse response = httpClient.execute(postRequest);

            //if the response if 200 OK
            if (response.getStatusLine().getStatusCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
                StringBuffer res = new StringBuffer();

                while ((result = br.readLine()) != null) {
                    res.append(result);
                }

                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource
                        (new StringReader(res.toString())));
                NodeList errNodes = doc.getElementsByTagName("ServiceResponse");

                if (errNodes.getLength() > 0) {
                    Element err = (Element) errNodes.item(0);
                    scanID = err.getElementsByTagName("id").item(0).getTextContent();
                    responseCode = err.getElementsByTagName("responseCode").item(0).getTextContent();
                }

                responseObject.setScanID(scanID);
                responseObject.setIsSuccessful(responseCode);

                if (responseCode != null) {
                    if (responseCode.equals("SUCCESS")) {
                        log.info("Scan started with the scan ID :" + scanID);
                        return responseObject;
                    } else if (responseCode.equals("INVALID_XML")) {
                        log.error("Invalid XML format in the XML request");
                        return responseObject;
                    } else if (responseCode.equals("UNAUTHORIZED")) {
                        log.error("Unauthorized to access the Start Scan API");
                        return responseObject;
                    }
                }
            } else if (response.getStatusLine().getStatusCode() == 400) {
                log.error("The API request did not contain one or more parameters which are required.");
            } else if (response.getStatusLine().getStatusCode() == 202) {
                log.error("Request is being processed. The API request is for a business operation which is " +
                        "already underway.");
            } else if (response.getStatusLine().getStatusCode() == 501) {
                log.error("The API request failed due to a problem with QWEB.");
            } else if (response.getStatusLine().getStatusCode() == 401) {
                log.error("The API request failed because of an authentication failure");
            } else {
                log.error("Unable to retrieve data");
            }
        } catch (Exception e) {
            throw new ScannerException("Error occurred : API request failed!"); //log
        }
        return null;
    }

    /**
     * Overrides the {@code getStatus} method to check the status of a scan for every 15 minutes.
     *
     * @param scannerRequestObject Request that comes from the user
     * @return Enum of the ScannerStatus
     * @throws ScannerException
     */
    @Override
    public ScannerStatus getStatus(ScannerRequestObject scannerRequestObject) throws ScannerException {
        String status = null;
        String result;
        String scanId = scannerRequestObject.getScanId();

        if (!appsInitialized) {
            throw new ScannerException("Applications not initialized.");
        }

        try { //http GET request to Qualys get status API
            HttpGet getRequest = new HttpGet (
                    QualysScannerConstants.QUALYS_GET_STATUS_API + scanId
            );
            getRequest.addHeader("accept", "application/xml;q=0.9, */*;q=0.8");
            getRequest.addHeader("Authorization", "Basic " + basicAuth);

            HttpResponse response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() == 200) {

                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
                StringBuffer res = new StringBuffer();

                while ((result = br.readLine()) != null) {
                    res.append(result);
                }

                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource
                        (new StringReader(res.toString())));
                NodeList errNodes = doc.getElementsByTagName("ServiceResponse");

                if (errNodes.getLength() > 0) {
                    Element err = (Element) errNodes.item(0);
                    status = err.getElementsByTagName("status").item(0).getTextContent();
                }

                br.close();

                if (status != null) {
                    if (status.equals("RUNNING")) {
                        return ScannerStatus.RUNNING;
                    } else if (status.equals("CANCELED")) {
                        return ScannerStatus.CANCELED;
                    } else if (status.equals("SUBMITTED")) {
                        return ScannerStatus.SUBMITTED;
                    } else if (status.equals("FAILED")) {
                        return ScannerStatus.FAILED;
                    } else if (status.equals("FINISHED")) {
                        return ScannerStatus.FINISHED;
                    } else {
                        return ScannerStatus.ERROR;
                    }
                }
            } else if (response.getStatusLine().getStatusCode() == 400) {
                log.error("The API request did not contain one or more parameters which are required.");
            } else if (response.getStatusLine().getStatusCode() == 202) {
                log.error("Request is being processed. The API request is for a business operation which is " +
                        "already underway.");
            } else if (response.getStatusLine().getStatusCode() == 501) {
                log.error("The API request failed due to a problem with QWEB.");
            } else if (response.getStatusLine().getStatusCode() == 401) {
                log.error("The API request failed because of an authentication failure");
            } else {
                log.error("Unable to retrieve data");
            }

        } catch (ParserConfigurationException e) {
            throw new ScannerException("Error in configuration!", e);
        } catch (SAXException e) {
            throw new ScannerException("Error in the XML parser!", e);
        } catch (ClientProtocolException e) {
            throw new ScannerException("Error when handling the request!", e);
        } catch (IOException e) {
            throw new ScannerException("interrupted IO operation!", e);
        }
        return null;
    }

    /**
     * Overrides the {@code cancelScan} method to cancel a scan.
     *
     * @param scannerRequestObject Request that comes from the user
     * @return a boolean value to indicate the operation success
     * @throws ScannerException
     */
    @Override
    public boolean cancelScan(ScannerRequestObject scannerRequestObject) throws ScannerException {
        String scanId = scannerRequestObject.getScanId();

        if (!appsInitialized) {
            throw new ScannerException("Applications not initialized.");
        }

        try { //http POST request to Qualys cancel scan API
            HttpPost postRequest = new HttpPost(QualysScannerConstants.QUALYS_CANCEL_SCAN_API + scanId);
            postRequest.addHeader("Authorization", "Basic " + basicAuth);
            postRequest.addHeader("Accept", "application/xml");

            HttpResponse response = httpClient.execute(postRequest);

            //if the response is 200 OK
            if (response.getStatusLine().getStatusCode() == 200) {
                log.info("Scan canceled with the scan ID :" + scanId);
                return true;
            } else if (response.getStatusLine().getStatusCode() == 400) {
                log.error("The API request did not contain one or more parameters which are required.");
            } else if (response.getStatusLine().getStatusCode() == 202) {
                log.error("Request is being processed. The API request is for a business operation which is " +
                        "already underway.");
            } else if (response.getStatusLine().getStatusCode() == 501) {
                log.error("The API request failed due to a problem with QWEB.");
            } else if (response.getStatusLine().getStatusCode() == 401) {
                log.error("The API request failed because of an authentication failure");
            } else {
                log.error("Unable to retrieve data");
            }

        } catch (RuntimeException e) {
            throw new ScannerException("Could not cancel scan!", e);

        } catch (ClientProtocolException e) {
            throw new ScannerException("Error when handling the request", e);

        } catch (IOException e) {
            throw new ScannerException("Error when cancelling the scan!", e);

        }
        return false;
    }

    /**
     * Overrides the {@code getReport} method to get the report of a scan.
     *
     * @param scannerRequestObject Request that comes from the user
     * @return a boolean value to indicate the operation success
     * @throws ScannerException
     */
    @Override
    public boolean getReport(ScannerRequestObject scannerRequestObject) throws ScannerException {
        boolean printStatus;
        String path = ScannerConstants.REPORTS_FOLDER_PATH; //default path to download reports
        String scanId = scannerRequestObject.getScanId();
        String date = getDate();

        if (!appsInitialized) {
            throw new ScannerException("Applications not initialized.");
        }

        try { //http GET request to Qualys report API
            HttpGet getRequest = new HttpGet(
                    QualysScannerConstants.QUALYS_GET_REPORT_API + scanId
            );
            getRequest.addHeader("Authorization", "Basic " + basicAuth);

            HttpResponse response = httpClient.execute(getRequest);

            //if the response is 200 OK
            if (response.getStatusLine().getStatusCode() == 200) {
                if (!path.isEmpty()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    response.getEntity().writeTo(baos);
                    byte[] bytes = baos.toByteArray();
                    String outputFilePath = path + scanId + date + ScannerConstants.PDF_FILE_EXTENSION;

                    OutputStream out = new FileOutputStream(outputFilePath);
                    out.write(bytes);
                    out.close();
                    printStatus = true;

                    return printStatus;
                } else {
                    log.error("Resulted in the following errors: Output file path missing");
                }
            } else if (response.getStatusLine().getStatusCode() == 400) {
                log.error("The API request did not contain one or more parameters which are required.");
            } else if (response.getStatusLine().getStatusCode() == 202) {
                log.error("Request is being processed. The API request is for a business operation which is " +
                        "already underway.");
            } else if (response.getStatusLine().getStatusCode() == 501) {
                log.error("The API request failed due to a problem with QWEB.");
            } else if (response.getStatusLine().getStatusCode() == 401) {
                log.error("The API request failed because of an authentication failure");
            } else {
                log.error("Unable to retrieve data");
            }
        } catch (RuntimeException e) {
            throw new ScannerException("Error when handling the request!", e);
        } catch (IOException e) {
            throw new ScannerException("Error while downloading the report", e);
        }
        return false;
    }

    /**
     * Check the status of a scan.
     *
     * @param scannerRequestObject Request that comes from the user
     * @return Enum of the ScannerStatus
     * @throws ScannerException
     */
    private ScannerStatus checkStatus(ScannerRequestObject scannerRequestObject) throws ScannerException {
        try {
            TimeUnit.MINUTES.sleep(QualysScannerConstants.QUALYS_SCANNER_CHECK_TIME);
            scannerRequestObject.setScanId(scannerRequestObject.getScanId());
            ScannerStatus scannerStatus = getStatus(scannerRequestObject);

            while (!scannerStatus.equals(ScannerStatus.FINISHED)) {
                TimeUnit.MINUTES.sleep(5);
                scannerStatus = getStatus(scannerRequestObject);
            }
            return scannerStatus;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    /**
     * Build a secure Http client.
     *
     * @return a DefaultHttpClient
     */
    private DefaultHttpClient buildSecureHttpClient() {
        DefaultHttpClient httpClient = new DefaultHttpClient();

        return httpClient;
    }

    /**
     * Set credentials for the basic authorization.
     *
     * @param qualysUsername Username of the Qualys user
     * @param qualysPassword Password of the Qualys user
     * @return basic authentication base 64 encoded string
     * @throws ScannerException
     */
    private String setCredentials(String qualysUsername, String qualysPassword) throws ScannerException {

        String userpass = qualysUsername + ":" + qualysPassword;
        String basicAuth;
        try {
            basicAuth = new String (new Base64().encode(userpass.getBytes()),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ScannerException("Unsupported Enconding exception", e);
        }

        return basicAuth;
    }

    /**
     * Get the application ID of a given URL.
     *
     * @param filePath File path of the application file
     * @param productName Name of the product selected
     * @return application ID
     * @throws ScannerException
     */
    private String getApplicationId(String filePath, String productName) throws ScannerException {
        File file = new File(filePath);
        String id = null;
        NodeList nodeList;
        DocumentBuilder builder;

        DocumentBuilderFactory dbFactory = getSecuredDocumentBuilderFactory();

        try {
            builder = dbFactory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            nodeList = doc.getElementsByTagName("WebApp");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;
                    if (eElement.getElementsByTagName("name").item(0).getTextContent().equals(productName)) {
                        id = eElement.getElementsByTagName("id").item(0).getTextContent();
                    }
                }
            }
            return id;

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ScannerException("Error when handling the request!", e);
        }

    }

    /**
     * Get all the applications from Qualys backend.
     *
     * @return a boolean value to indicate the operation success
     * @throws ScannerException
     */
    private boolean getApplicationIds() throws ScannerException {
        String result;

        try {
            HttpPost postRequest = new HttpPost(QualysScannerConstants.QUALYS_GET_APPLICATION_API);
            postRequest.addHeader("Authorization", "Basic " + basicAuth);
            postRequest.addHeader("Accept", "application/xml");

            HttpResponse response = httpClient.execute(postRequest);

            if (response.getStatusLine().getStatusCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
                StringBuffer res = new StringBuffer();

                while ((result = br.readLine()) != null) {
                    res.append(result);
                }

                BufferedWriter bwr = new BufferedWriter(new FileWriter(new File
                        (QualysScannerConstants.QUALYS_APPLICATION_FILE_PATH)));
                bwr.write(res.toString());
                bwr.flush();
                bwr.close();
                br.close();

                appsInitialized = true;
            } else if (response.getStatusLine().getStatusCode() == 400) {
                log.error("The API request did not contain one or more parameters which are required.");
            } else if (response.getStatusLine().getStatusCode() == 202) {
                log.error("Request is being processed. The API request is for a business operation which is " +
                        "already underway.");
            } else if (response.getStatusLine().getStatusCode() == 501) {
                log.error("The API request failed due to a problem with QWEB.");
            } else if (response.getStatusLine().getStatusCode() == 401) {
                log.error("The API request failed because of an authentication failure");
            } else {
                log.error("Unable to retrieve data");
            }
            return appsInitialized;

        } catch (ClientProtocolException e) {
            throw new ScannerException("Error when handling the request!", e);
        } catch (IOException e) {
            throw new ScannerException("Interrupted IO operation!", e);
        }
    }

    /**
     * Get the current date and time.
     *
     * @return formatted date and time
     */
    private String getDate() {
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss");

        return ft.format(date);
    }

}
