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

package org.wso2.security.tools.scanmanager.scanners.qualys.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanContext;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Qualys scanner accepts XML format request body which contains the data related for scan. This class is responsible to
 * create XML format request body.
 */
public class RequestBodyBuilder {

    private static final Log log = LogFactory.getLog(RequestBodyBuilder.class);
    private static final int ENTITY_EXPANSION_LIMIT = 0;

    /**
     * Build request body to add authentication script
     *
     * @param appID    Application name in qualys.
     * @param filePath Authentication script file path
     * @return Add Authentication Request Body in XML format.
     * @throws ParserConfigurationException Error occurred while parsing.
     * @throws IOException                  Error occurred while reading the XML content file.
     * @throws TransformerException         Error occurred while building secure string writer.
     */
    public static String buildAuthScriptCreationRequest(String appID, String filePath)
            throws ParserConfigurationException, IOException, TransformerException {
        String addAuthRecordRequestBody;
        DocumentBuilderFactory dbf = getSecuredDocumentBuilderFactory();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement(QualysScannerConstants.SERVICE_REQUEST);
        doc.appendChild(root);

        Element data = doc.createElement(QualysScannerConstants.DATA);
        root.appendChild(data);

        Element webAppAuthRecord = doc.createElement(QualysScannerConstants.WEB_APP_AUTH_RECORD);
        data.appendChild(webAppAuthRecord);

        File tempFile = new File(filePath);
        Element name = doc.createElement(QualysScannerConstants.NAME_KEYWORD);
        name.appendChild(doc.createTextNode("Selenium Script for " + appID + " : " + getDate()));
        webAppAuthRecord.appendChild(name);

        Element formRecord = doc.createElement(QualysScannerConstants.FORM_RECORD);
        webAppAuthRecord.appendChild(formRecord);

        Element type = doc.createElement(QualysScannerConstants.TYPE_KEYWORD);
        type.appendChild(doc.createTextNode(QualysScannerConstants.SELENIUM));
        formRecord.appendChild(type);

        Element seleniumScript = doc.createElement(QualysScannerConstants.SELENIUM_SCRIPT);
        formRecord.appendChild(seleniumScript);

        Element seleniumScriptName = doc.createElement(QualysScannerConstants.NAME_KEYWORD);
        seleniumScriptName.appendChild(doc.createTextNode("SELENIUM AUTHENTICATION SCRIPT"));
        seleniumScript.appendChild(seleniumScriptName);

        Element scriptData = doc.createElement(QualysScannerConstants.DATA);
        scriptData.appendChild(doc.createTextNode(getContentFromFile(tempFile.getAbsolutePath())));
        seleniumScript.appendChild(scriptData);

        Element regex = doc.createElement(QualysScannerConstants.REGEX);
        regex.appendChild(doc.createTextNode("selenium"));
        seleniumScript.appendChild(regex);

        StringWriter stringWriter = buildSecureStringWriter(doc);
        addAuthRecordRequestBody = stringWriter.getBuffer().toString();

        return addAuthRecordRequestBody;
    }

    /**
     * Build request body to update web app with authentication script.
     *
     * @param webAppName Web Application Name
     * @param authId     Auth Script Id
     * @return update webapp request body in XML format.
     * @throws ParserConfigurationException Error occurred while parsing.
     * @throws TransformerException         Error occurred while building secure string writer.
     */
    public static String buildWebAppUpdateRequest(String webAppName, String authId)
            throws ParserConfigurationException, TransformerException {
        String updateWebAppRequestBody;
        DocumentBuilderFactory dbf = getSecuredDocumentBuilderFactory();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement(QualysScannerConstants.SERVICE_REQUEST);
        doc.appendChild(root);

        Element data = doc.createElement(QualysScannerConstants.DATA);
        root.appendChild(data);

        Element webApp = doc.createElement(QualysScannerConstants.QUALYS_WEBAPP_KEYWORD);
        data.appendChild(webApp);

        Element name = doc.createElement(QualysScannerConstants.NAME_KEYWORD);
        name.appendChild(doc.createTextNode(webAppName));
        webApp.appendChild(name);

        Element authRecords = doc.createElement(QualysScannerConstants.AUTH_RECORDS);
        webApp.appendChild(authRecords);

        Element add = doc.createElement(QualysScannerConstants.ADD);
        authRecords.appendChild(add);

        Element webAppAuthRecord = doc.createElement(QualysScannerConstants.WEB_APP_AUTH_RECORD);
        add.appendChild(webAppAuthRecord);

        Element id = doc.createElement(QualysScannerConstants.ID_KEYWORD);
        id.appendChild(doc.createTextNode(authId));
        webAppAuthRecord.appendChild(id);

        StringWriter stringWriter = buildSecureStringWriter(doc);
        updateWebAppRequestBody = stringWriter.getBuffer().toString();

        return updateWebAppRequestBody;
    }

    /**
     * Build request body to create report.
     *
     * @param webAppId     web app id
     * @param jobId        job id
     * @param reportFormat report format
     * @return request body
     * @throws ParserConfigurationException Error occurred while parsing.
     * @throws TransformerException         Error occurred while building secure string writer.
     */
    public static String buildReportCreationRequest(String webAppId, String jobId, String reportFormat)
            throws ParserConfigurationException, TransformerException {
        String createReportRequestBody;
        DocumentBuilderFactory dbf = getSecuredDocumentBuilderFactory();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement(QualysScannerConstants.SERVICE_REQUEST);
        document.appendChild(root);

        Element data = document.createElement(QualysScannerConstants.DATA);
        root.appendChild(data);

        Element report = document.createElement(QualysScannerConstants.REPORT);
        data.appendChild(report);

        Element name = document.createElement(QualysScannerConstants.NAME_KEYWORD);
        name.appendChild(document.createTextNode("Web Application Report " + webAppId + " : "));
        report.appendChild(name);

        Element description = document.createElement(QualysScannerConstants.DESCRIPTION);
        description.appendChild(document.createTextNode("Web App Report for : " + jobId + " Date : " + getDate()));
        report.appendChild(description);

        Element format = document.createElement(QualysScannerConstants.FORMAT);
        format.appendChild(document.createTextNode(reportFormat));
        report.appendChild(format);

        Element type = document.createElement(QualysScannerConstants.TYPE_KEYWORD);
        type.appendChild(document.createTextNode(QualysScannerConstants.WAS_APP_REPORT));
        report.appendChild(type);

        Element config = document.createElement(QualysScannerConstants.CONFIG_KEYWORD);
        report.appendChild(config);

        Element webAppReport = document.createElement(QualysScannerConstants.WEB_APP_REPORT_KEYWORD);
        config.appendChild(webAppReport);

        Element target = document.createElement(QualysScannerConstants.TARGET);
        webAppReport.appendChild(target);

        Element webapps = document.createElement(QualysScannerConstants.WEBAPPS_KEYWORD);
        target.appendChild(webapps);

        Element webapp = document.createElement(QualysScannerConstants.QUALYS_WEBAPP_KEYWORD);
        webapps.appendChild(webapp);

        Element id = document.createElement(QualysScannerConstants.ID_KEYWORD);
        id.appendChild(document.createTextNode(webAppId));
        webapp.appendChild(id);

        StringWriter stringWriter = buildSecureStringWriter(document);
        createReportRequestBody = stringWriter.getBuffer().toString();

        return createReportRequestBody;

    }

    /**
     * Build launch scan request body.
     *
     * @param scanContext Qualys scanner parameters.
     * @return Launch scan request body in XML format.
     * @throws ParserConfigurationException Error occurred while parsing.
     * @throws TransformerException         Error occurred while building secure string writer.
     */
    public static String buildScanLaunchRequest(ScanContext scanContext)
            throws ParserConfigurationException, TransformerException {
        String launchScanRequestBody;
        DocumentBuilderFactory dbf = getSecuredDocumentBuilderFactory();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement(QualysScannerConstants.SERVICE_REQUEST);
        doc.appendChild(root);

        Element data = doc.createElement(QualysScannerConstants.DATA);
        root.appendChild(data);

        Element wasScan = doc.createElement(QualysScannerConstants.WAS_SCAN);
        data.appendChild(wasScan);

        Element name = doc.createElement(QualysScannerConstants.NAME_KEYWORD);
        name.appendChild(doc.createTextNode(
                QualysScannerConstants.QUALYS_SCAN_NAME_PREFIX + scanContext.getWebAppName() + " " + getDate()));
        wasScan.appendChild(name);

        Element type = doc.createElement(QualysScannerConstants.TYPE_KEYWORD);
        type.appendChild(doc.createTextNode(scanContext.getType()));
        wasScan.appendChild(type);

        Element target = doc.createElement(QualysScannerConstants.TARGET);
        wasScan.appendChild(target);

        Element webApp = doc.createElement("webApp");
        target.appendChild(webApp);

        Element webAppId = doc.createElement(QualysScannerConstants.ID_KEYWORD);
        webAppId.appendChild(doc.createTextNode(scanContext.getWebAppId()));
        webApp.appendChild(webAppId);

        Element webAppAuthRecord = doc.createElement("webAppAuthRecord");
        target.appendChild(webAppAuthRecord);

        Element webAppAuthRecordId = doc.createElement(QualysScannerConstants.ID_KEYWORD);
        webAppAuthRecordId.appendChild(doc.createTextNode(scanContext.getAuthId()));
        webAppAuthRecord.appendChild(webAppAuthRecordId);

        Element scannerAppliance = doc.createElement(QualysScannerConstants.SCANNER_APPILIANCE);
        target.appendChild(scannerAppliance);

        Element scannerApplianceType = doc.createElement(QualysScannerConstants.TYPE_KEYWORD);
        scannerApplianceType.appendChild(doc.createTextNode(scanContext.getScannerApplianceType()));
        scannerAppliance.appendChild(scannerApplianceType);

        Element profile = doc.createElement(QualysScannerConstants.PROFILE);
        wasScan.appendChild(profile);

        Element profileId = doc.createElement(QualysScannerConstants.ID_KEYWORD);
        profileId.appendChild(doc.createTextNode(scanContext.getProfileId()));
        profile.appendChild(profileId);

        Element progressiveScanning = doc.createElement(QualysScannerConstants.PROGRESSIVE_SCANNING_KEYWORD);
        progressiveScanning.appendChild(doc.createTextNode(scanContext.getProgressiveScanning()));
        wasScan.appendChild(progressiveScanning);

        StringWriter stringWriter = buildSecureStringWriter(doc);
        launchScanRequestBody = stringWriter.getBuffer().toString();

        return launchScanRequestBody;
    }

    /**
     * Read the file and get hte content.
     *
     * @param filePath file path
     * @return File content
     * @throws IOException Error occurred while reading the XML content file.
     */
    private static String getContentFromFile(String filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }

        return contentBuilder.toString();
    }

    /**
     * Build a secure String writer.
     *
     * @param doc Document that needs to be converted to String
     * @return StringWriter
     * @throws TransformerException Error occurred whilfe building secure string writer.
     */
    private static StringWriter buildSecureStringWriter(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer;
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
     * Create DocumentBuilderFactory with the XXE and XEE prevention measurements.
     *
     * @return DocumentBuilderFactory instance
     * @throws ParserConfigurationException Error occurred while creating the SecuredDocumentBuilderFactory
     */
    public static DocumentBuilderFactory getSecuredDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.
                    EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.
                    EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.
                    LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            String message =
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or "
                            + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or "
                            + Constants.LOAD_EXTERNAL_DTD_FEATURE + " or secure-processing.";
            throw e;
        }
        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);
        return dbf;
    }
}
