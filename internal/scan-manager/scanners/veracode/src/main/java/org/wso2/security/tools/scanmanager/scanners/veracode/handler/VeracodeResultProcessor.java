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
 */
package org.wso2.security.tools.scanmanager.scanners.veracode.handler;

import com.veracode.parser.util.XmlUtils;
import org.apache.log4j.Logger;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.util.XMLUtil;
import org.wso2.security.tools.scanmanager.scanners.veracode.VeracodeScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.veracode.config.VeracodeScannerConfiguration;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * class to represent the processes of veracode responses.
 */
public class VeracodeResultProcessor {

    private static final Logger log = Logger.getLogger(VeracodeResultProcessor.class);

    private VeracodeResultProcessor() {
    }

    /**
     * Check whether the provided string form Veracode is a valid XML.
     *
     * @param xmlResult XML String that need verify
     * @return whether the string is XML
     */
    public static boolean isOperationProceedWithoutError(String xmlResult) {
        boolean isValidXML = false;

        xmlResult = XmlUtils.getDecodedXmlResponse(xmlResult, true);
        String errorString = filterErrorString(xmlResult);

        if (errorString.isEmpty() || errorString.contains(VeracodeScannerConstants.NO_MODULES_SELECTED)
                || errorString.contains(VeracodeScannerConstants.SCAN_ALL_MODULE) ||
                errorString.contains(VeracodeScannerConstants.NO_BUILD)) {
            isValidXML = true;
        } else {
            log.warn("Veracode returned the following message : " + errorString);
        }
        return isValidXML;
    }

    /**
     * Get the scan status of a given application.
     *
     * @param result Result that we got from the Veracode
     * @return the Scan's status
     * @throws IOException                  when unable to retrieve scan status due to IO errors
     * @throws XPathExpressionException     when the given expression is malformed or wrong
     * @throws SAXException                 when unable to parse the XML to get status
     * @throws ParserConfigurationException when unable to create a document builder
     */
    public static String getActualScanStatus(String result) throws SAXException, ParserConfigurationException,
            XPathExpressionException, IOException {
        String xPath = VeracodeScannerConfiguration.getInstance().getConfigProperty(VeracodeScannerConstants
                .SCAN_STATUS_XPATH);
        String statusAttribute = VeracodeScannerConfiguration.getInstance().getConfigProperty(VeracodeScannerConstants
                .SCAN_STATUS_ATTRIBUTE);

        return getElementByVeracodeXMLResult(result, xPath, statusAttribute);
    }

    /**
     * Convert the Veracode scan status to generic scan status for a given Veracode response result.
     *
     * @param result Veracode API response
     * @return scan status
     * @throws IOException                  when unable to retrieve scan status due to IO errors
     * @throws XPathExpressionException     when the given expression is malformed or wrong
     * @throws SAXException                 when unable to parse the XML to get status
     * @throws ParserConfigurationException when unable to create a document builder
     */
    public static ScanStatus getScanStatus(String result) throws SAXException, ParserConfigurationException,
            XPathExpressionException, IOException {
        ScanStatus scanStatus = ScanStatus.UNKNOWN;
        String status = null;
        boolean isValidXML = VeracodeResultProcessor.isOperationProceedWithoutError(result);

        if (isValidXML) {
            status = VeracodeResultProcessor.getActualScanStatus(result);
        }

        if (status != null) {
            switch (status) {
                case VeracodeScannerConstants.SCAN_IN_PROCESS:
                    scanStatus = ScanStatus.RUNNING;
                    break;
                case VeracodeScannerConstants.PENDING_INTERVAL:
                    scanStatus = ScanStatus.RUNNING;
                    break;
                case VeracodeScannerConstants.INCOMPLETE:
                    scanStatus = ScanStatus.RUNNING;
                    break;
                case VeracodeScannerConstants.PRESCAN_SUCESS:
                    scanStatus = ScanStatus.RUNNING;
                    break;
                case VeracodeScannerConstants.PREFLIGHT_SUCESS:
                    scanStatus = ScanStatus.RUNNING;
                    break;
                case VeracodeScannerConstants.PRESCAN_SUBMITTED:
                    scanStatus = ScanStatus.SUBMITTED;
                    break;
                case VeracodeScannerConstants.PREFLIGHT_SUBMITTED:
                    scanStatus = ScanStatus.SUBMITTED;
                    break;
                case VeracodeScannerConstants.SUBMITTED_TO_ENGINE:
                    scanStatus = ScanStatus.SUBMITTED;
                    break;
                case VeracodeScannerConstants.NOT_SUBMITTED_TO_ENGINE:
                    scanStatus = ScanStatus.SUBMITTED;
                    break;
                case VeracodeScannerConstants.PRESCAN_CANCELLED:
                    scanStatus = ScanStatus.CANCELED;
                    break;
                case VeracodeScannerConstants.SCAN_CANCELED:
                    scanStatus = ScanStatus.CANCELED;
                    break;
                case VeracodeScannerConstants.PRESCAN_FAILED:
                    scanStatus = ScanStatus.ERROR;
                    break;
                case VeracodeScannerConstants.NO_MODULES_DEFINED:
                    scanStatus = ScanStatus.ERROR;
                    break;
                case VeracodeScannerConstants.RESULTS_READY:
                    scanStatus = ScanStatus.COMPLETED;
                    break;
                case VeracodeScannerConstants.SCAN_ERRORS:
                    scanStatus = ScanStatus.ERROR;
                    break;
                default:
                    scanStatus = ScanStatus.ERROR;
            }
        }
        return scanStatus;
    }

    /**
     * Get the veracode scan's build id from the veracode response.
     *
     * @param apiResult veracode response xml
     * @return build id of the scan
     * @throws IOException                  when unable to retrieve scan status due to IO errors
     * @throws XPathExpressionException     when the given expression is malformed or wrong
     * @throws SAXException                 when unable to parse the XML to get status
     * @throws ParserConfigurationException when unable to create a document builder
     */
    public static String getBuildIdByResponse(String apiResult) throws SAXException, ParserConfigurationException,
            XPathExpressionException, IOException {
        String xPath = VeracodeScannerConfiguration.getInstance().getConfigProperty(VeracodeScannerConstants
                .BUILD_ID_XPATH);
        String buildIdAttribute = VeracodeScannerConfiguration.getInstance().getConfigProperty(VeracodeScannerConstants
                .BUILD_ID_ATTRIBUTE);

        return getElementByVeracodeXMLResult(apiResult, xPath, buildIdAttribute);
    }

    /**
     * Get the required attribute from an XML string.
     *
     * @param result    XML of the Veracode response
     * @param xPath     XPath to select the required attribute
     * @param attribute tag name of the required attribute
     * @return attribute value
     * @throws IOException                  when unable to retrieve scan status due to IO errors
     * @throws XPathExpressionException     when the given expression is malformed or wrong
     * @throws SAXException                 when unable to parse the XML to get status
     * @throws ParserConfigurationException when unable to create a document builder
     */
    private static String getElementByVeracodeXMLResult(String result, String xPath, String attribute) throws
            XPathExpressionException, IOException, SAXException, ParserConfigurationException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr;

        Document doc = XMLUtil.convertStringToDocument(result);

        SimpleNamespaceContext namespaces = new SimpleNamespaceContext();
        namespaces.bindNamespaceUri(VeracodeScannerConstants.VERACODE, VeracodeScannerConfiguration.getInstance()
                .getConfigProperty(VeracodeScannerConstants.NAMESPACE));
        xpath.setNamespaceContext(namespaces);
        expr = xpath.compile(xPath);

        return XMLUtil.getValueByXmlDocument(attribute, expr, doc);
    }

    /**
     * filter error string from the Veracode response.
     *
     * @param xmlString
     * @return the error string
     */
    private static String filterErrorString(String xmlString) {
        String errorString;
        StringBuilder builder = new StringBuilder();
        Pattern pattern = Pattern.compile("<error>(.*?)</error>");
        Matcher matcher = pattern.matcher(xmlString);

        while (matcher.find()) {
            builder.append(matcher.group(1) + "\r\n");
        }

        errorString = builder.toString();

        if (errorString.contains("\r\n")) {
            errorString = errorString.substring(0, builder.lastIndexOf("\r\n"));
        }
        return errorString;
    }
}
