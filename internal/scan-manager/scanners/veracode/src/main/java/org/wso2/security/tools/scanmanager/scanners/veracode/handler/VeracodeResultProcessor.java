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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.scanmanager.common.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.config.YAMLConfigurationReader;
import org.wso2.security.tools.scanmanager.scanners.veracode.Util.FileUtil;
import org.wso2.security.tools.scanmanager.scanners.veracode.VeracodeScannerConstants;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class to represent the processes of veracode responses.
 */
public class VeracodeResultProcessor {

    private static final Logger log = Logger.getLogger(VeracodeResultProcessor.class);

    private VeracodeResultProcessor() {
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
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     * @throws IOException
     */
    public static String getActualScanStatus(String result) throws SAXException, ParserConfigurationException,
            XPathExpressionException, IOException {
        String xPath = YAMLConfigurationReader.getInstance().getConfigProperty(VeracodeScannerConstants
                .SCAN_STATUS_XPATH);
        String statusAttribute = YAMLConfigurationReader.getInstance().getConfigProperty(VeracodeScannerConstants
                .SCAN_STATUS_ATTRIBUTE);

        return getElementByXMLResult(result, xPath, statusAttribute);
    }

    /**
     * Build the Error String by Throwable.
     *
     * @param e throwable
     * @return the build error string
     */
    public static String getFullErrorMessage(Throwable e) {
        if (e.getCause() == null) {
            return e.getMessage();
        }
        return e.getMessage() + "\n\nCaused by: " + getFullErrorMessage(e.getCause());
    }

    /**
     * Get the required attribute from an XML sttring.
     *
     * @param result XML of the Veracode response
     * @param xPath XPath to select the required attribute
     * @param attribute tag name of the required attribute
     * @return attribute value
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     * @throws IOException
     */
    private static String getElementByXMLResult(String result, String xPath, String attribute) throws
            XPathExpressionException, IOException, SAXException, ParserConfigurationException {
        String status = null;
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr;
        NodeList nl;

        Document doc = convertStringToDocument(result);

        SimpleNamespaceContext namespaces = new SimpleNamespaceContext();
        namespaces.bindNamespaceUri(VeracodeScannerConstants.VERACODE, YAMLConfigurationReader.getInstance()
                .getConfigProperty(VeracodeScannerConstants.NAMESPACE));
        xpath.setNamespaceContext(namespaces);
        expr = xpath.compile(xPath);

        nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < nl.getLength(); i++) {
            Node currentItem = nl.item(i);
            status = currentItem.getAttributes().getNamedItem(attribute).getNodeValue();
        }
        return status;
    }

    /**
     * Convert a given XML to XML Document.
     *
     * @param xmlStr XML string that needs to convert
     * @return converted XML Document
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    private static Document convertStringToDocument(String xmlStr) throws ParserConfigurationException, IOException,
            SAXException {
        DocumentBuilderFactory factory = FileUtil.getSecuredDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(new InputSource(new StringReader(xmlStr)));
    }

    /**
     * Convert the Veracode scan status to generic scan status for a given Veracode response result.
     *
     * @param result Veracode API response
     * @return scan status
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     * @throws IOException
     */
    public static ScanStatus getScanStatus(String result) throws SAXException, ParserConfigurationException,
            XPathExpressionException, IOException {
        ScanStatus scanStatus = ScanStatus.COMPLETED;
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
                    scanStatus = ScanStatus.FAILED;
                    break;
                case VeracodeScannerConstants.NO_MODULES_DEFINED:
                    scanStatus = ScanStatus.FAILED;
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
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     * @throws IOException
     */
    public static String getBuildIdByResponse(String apiResult) throws SAXException, ParserConfigurationException,
            XPathExpressionException, IOException {
        String xPath = YAMLConfigurationReader.getInstance().getConfigProperty(VeracodeScannerConstants.BUILD_ID_XPATH);
        String buildIdAttribute = YAMLConfigurationReader.getInstance().getConfigProperty(VeracodeScannerConstants
                .BUILD_ID_ATTRIBUTE);

        return getElementByXMLResult(apiResult, xPath, buildIdAttribute);
    }
}
