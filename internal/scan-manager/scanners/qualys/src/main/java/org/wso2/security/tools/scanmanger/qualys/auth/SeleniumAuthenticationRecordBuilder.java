/*
 *
 *   Copyright (c) 2020, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanmanger.qualys.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.util.FileUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.XMLUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.SeleniumScript;
import org.wso2.security.tools.scanmanager.scanners.qualys.util.RequestBodyBuilder;

import java.io.IOException;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * This class is to represent Selenium Authentication type of WebAppAuthenticationRecordBuilder for Qualys scan.
 */
public class SeleniumAuthenticationRecordBuilder extends SeleniumScript implements WebAppAuthenticationRecordBuilder {

    private static final Logger log = LogManager.getLogger(SeleniumAuthenticationRecordBuilder.class);

    // Regex to check whether authentication status is success or not
    private String authRegex;

    public SeleniumAuthenticationRecordBuilder(String authRegex, String scriptFileLocation, String jobId)
            throws ScannerException {
        super(scriptFileLocation, jobId);
        this.authRegex = authRegex;
    }

    public String getAuthRegex() {
        return authRegex;
    }

    @Override public String buildAuthRequestBody(String appID)
            throws ParserConfigurationException, IOException, TransformerException {
        String addAuthRecordRequestBody;
        DocumentBuilderFactory dbf = XMLUtil.getSecuredDocumentBuilderFactory();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement(QualysScannerConstants.SERVICE_REQUEST);
        doc.appendChild(root);

        Element data = doc.createElement(QualysScannerConstants.DATA);
        root.appendChild(data);

        Element webAppAuthRecord = doc.createElement(QualysScannerConstants.WEB_APP_AUTH_RECORD);
        data.appendChild(webAppAuthRecord);

        Element name = doc.createElement(QualysScannerConstants.NAME_KEYWORD);
        name.appendChild(doc.createCDATASection("Selenium Script for " + appID + " : " + RequestBodyBuilder.getDate()));
        webAppAuthRecord.appendChild(name);

        Element formRecord = doc.createElement(QualysScannerConstants.FORM_RECORD);
        webAppAuthRecord.appendChild(formRecord);

        Element type = doc.createElement(QualysScannerConstants.TYPE_KEYWORD);
        type.appendChild(doc.createTextNode(QualysScannerConstants.SELENIUM));
        formRecord.appendChild(type);

        Element seleniumScript = doc.createElement(QualysScannerConstants.SELENIUM_SCRIPT);
        formRecord.appendChild(seleniumScript);

        Element seleniumScriptName = doc.createElement(QualysScannerConstants.NAME_KEYWORD);
        seleniumScriptName.appendChild(doc.createCDATASection("SELENIUM AUTHENTICATION SCRIPT"));
        seleniumScript.appendChild(seleniumScriptName);

        Element scriptData = doc.createElement(QualysScannerConstants.DATA);
        scriptData.appendChild(
                doc.createCDATASection(FileUtil.getContentFromFile(this.getScriptFile().getAbsolutePath())));
        seleniumScript.appendChild(scriptData);

        Element regex = doc.createElement(QualysScannerConstants.REGEX);
        regex.appendChild(doc.createCDATASection(authRegex));
        seleniumScript.appendChild(regex);

        StringWriter stringWriter = XMLUtil.buildSecureStringWriter(doc);
        addAuthRecordRequestBody = stringWriter.getBuffer().toString();

        return addAuthRecordRequestBody;
    }
}
