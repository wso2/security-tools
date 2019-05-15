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
package org.wso2.security.tools.scanmanager.scanners.common.util;

import org.apache.log4j.Logger;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

/**
 * Utility class for XML handling.
 */
public class XMLUtil {

    private static final Logger log = Logger.getLogger(XMLUtil.class);

    /**
     * Convert a given XML to XML Document.
     *
     * @param xmlStr XML string that needs to convert
     * @return converted XML Document
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static Document convertStringToDocument(String xmlStr) throws ParserConfigurationException, IOException,
            SAXException {
        DocumentBuilderFactory factory = getSecuredDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(new InputSource(new StringReader(xmlStr)));
    }

    /**
     * Extract the required attribute from an XML document by the given Xpath.
     *
     * @param xmlDoc    XML document
     * @param xPathExpr XPath to select the required attribute
     * @param attribute tag name of the required attribute
     * @return attribute value
     * @throws XPathExpressionException
     */
    public static String getValueByXmlDocument(String attribute, XPathExpression xPathExpr, Document xmlDoc) throws
            XPathExpressionException {
        String value = null;
        NodeList nodelist = (NodeList) xPathExpr.evaluate(xmlDoc, XPathConstants.NODESET);

        for (int i = 0; i < nodelist.getLength(); i++) {
            Node currentItem = nodelist.item(i);
            value = currentItem.getAttributes().getNamedItem(attribute).getNodeValue();
        }
        return value;
    }

    /**
     * Create DocumentBuilderFactory with the XXE and XEE prevention measurements.
     *
     * @return DocumentBuilderFactory instance
     */
    public static DocumentBuilderFactory getSecuredDocumentBuilderFactory() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        int entityExpansionLimit = 0;
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or "
                    + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE +
                    " or secure-processing.");
        }
        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(entityExpansionLimit);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);
        return dbf;
    }
}
