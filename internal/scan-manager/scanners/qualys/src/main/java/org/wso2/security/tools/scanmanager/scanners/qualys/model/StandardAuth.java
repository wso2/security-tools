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

package org.wso2.security.tools.scanmanager.scanners.qualys.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.security.tools.scanmanager.scanners.common.util.XMLUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.util.RequestBodyBuilder;

import java.io.StringWriter;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * This class is to represent StandardAuth type of WebAppAuth for Qualys scan.
 */
public class StandardAuth implements WebAppAuth {

    // Username for the StandardAuth type.
    private char[] username;

    // Password for the StandardAuth type.
    private char[] password;

    public StandardAuth(char[] userName, char[] password) {
        this.username = Arrays.copyOf(userName, userName.length);
        this.password = Arrays.copyOf(password, password.length);
    }

    @Override public String buildAuthRequestBody(String appID)
            throws TransformerException, ParserConfigurationException {
        String standardAuthRecordRequestBody;
        DocumentBuilderFactory dbf = XMLUtil.getSecuredDocumentBuilderFactory();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement(QualysScannerConstants.SERVICE_REQUEST);
        doc.appendChild(root);

        Element data = doc.createElement(QualysScannerConstants.DATA);
        root.appendChild(data);

        Element webAppAuthRecord = doc.createElement(QualysScannerConstants.WEB_APP_AUTH_RECORD);
        data.appendChild(webAppAuthRecord);

        Element authRecordName = doc.createElement(QualysScannerConstants.NAME_KEYWORD);
        authRecordName.appendChild(
                doc.createTextNode("Standard Authentication for " + appID + " : " + RequestBodyBuilder.getDate()));
        webAppAuthRecord.appendChild(authRecordName);

        Element formRecord = doc.createElement(QualysScannerConstants.FORM_RECORD);
        webAppAuthRecord.appendChild(formRecord);

        Element type = doc.createElement(QualysScannerConstants.TYPE_KEYWORD);
        type.appendChild(doc.createTextNode(QualysScannerConstants.STANDARD_AUTH));
        formRecord.appendChild(type);

        //        Element sslOnly = doc.createElement(QualysScannerConstants.SSL_ONLY);
        //        sslOnly.appendChild(doc.createTextNode("false"));

        Element fields = doc.createElement(QualysScannerConstants.FIELD);
        formRecord.appendChild(fields);

        Element set = doc.createElement(QualysScannerConstants.SET);
        fields.appendChild(set);

        Element usernameField = doc.createElement(QualysScannerConstants.AUTH_FORM_RECORD_FIELD);
        set.appendChild(usernameField);

        Element usernameEntry = doc.createElement(QualysScannerConstants.NAME_KEYWORD);
        usernameEntry.appendChild(doc.createTextNode(QualysScannerConstants.STANDARD_AUTH_USERNAME));
        usernameField.appendChild(usernameEntry);

        Element usernameEntryValue = doc.createElement(QualysScannerConstants.VALUE);
        usernameEntryValue.appendChild(doc.createTextNode(Arrays.toString(username)));
        usernameField.appendChild(usernameEntryValue);

        Element passwordField = doc.createElement(QualysScannerConstants.AUTH_FORM_RECORD_FIELD);
        set.appendChild(passwordField);

        Element passwordEntry = doc.createElement(QualysScannerConstants.NAME_KEYWORD);
        passwordEntry.appendChild(doc.createTextNode(QualysScannerConstants.STANDARD_AUTH_PASSWORD));
        passwordField.appendChild(passwordEntry);

        Element passwordEntryValue = doc.createElement(QualysScannerConstants.VALUE);
        passwordEntryValue.appendChild(doc.createTextNode(Arrays.toString(password)));
        passwordField.appendChild(passwordEntryValue);

        StringWriter stringWriter = XMLUtil.buildSecureStringWriter(doc);
        standardAuthRecordRequestBody = stringWriter.getBuffer().toString();

        Arrays.fill(username, '0');
        Arrays.fill(password, '0');

        return standardAuthRecordRequestBody;
    }
}
