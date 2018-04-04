/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.security.parsers.xml;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.Attribute;
import org.wso2.security.ConfigNode;
import org.wso2.security.common.ExceptionSingleton;
import org.wso2.security.exceptions.IllegalOperationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.wso2.security.common.Constants.NEWLINES_REGEX;

/**
 * This class is used to parse XML configuration files.
 */
public class XMLParser {
    private static final Logger log = LoggerFactory.getLogger(XMLParser.class);

    private static final int ENTITY_EXPANSION_LIMIT = 0;
    private ExceptionSingleton exceptionSingleton = ExceptionSingleton.getInstance();

    private DocumentBuilderFactory getSecureDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX +
                    Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX +
                    Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX +
                    Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            exceptionSingleton.setHasErrors(true);
            log.error(
                    "Failed to load XML Processor Feature " +
                            Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " +
                            Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX +
                Constants.SECURITY_MANAGER_PROPERTY, securityManager);
        return dbf;
    }

    /**
     * This method used to get the XML Document from the XML file.
     *
     * @param inputXMLFile
     * @return
     */
    public Document getXMLDocument(File inputXMLFile) {
        Document doc = null;
        try {
            DocumentBuilder dBuilder = getSecureDocumentBuilder().newDocumentBuilder();
            doc = dBuilder.parse(inputXMLFile);
            doc.getDocumentElement().normalize();
        } catch (ParserConfigurationException e) {
            exceptionSingleton.setHasErrors(true);
            log.error("ParserConfigurationException occurred while parsing " + inputXMLFile.getName());
        } catch (IOException e) {
            exceptionSingleton.setHasErrors(true);
            log.error("IOException occurred while parsing " + inputXMLFile.getName());
        } catch (SAXException e) {
            exceptionSingleton.setHasErrors(true);
            log.error("SAXException occurred while parsing " + inputXMLFile.getName());
        }
        return doc;
    }

    /**
     * This method is used to get the XML document from XML string.
     *
     * @param xmlString
     * @return
     */
    public Document getXMLDocumentByXMLString(String xmlString) {
        Document doc = null;
        try {
            DocumentBuilder dBuilder = getSecureDocumentBuilder().newDocumentBuilder();
            doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));
            doc.getDocumentElement().normalize();

        } catch (ParserConfigurationException e) {
            exceptionSingleton.setHasErrors(true);
            log.error("ParserConfigurationException occurred while parsing " + xmlString);
        } catch (IOException e) {
            exceptionSingleton.setHasErrors(true);
            log.error("IOException occurred while parsing " + xmlString);
        } catch (SAXException e) {
            exceptionSingleton.setHasErrors(true);
            log.error("SAXException occurred while parsing " + xmlString);
        }
        return doc;
    }

    /**
     * This method is used to get the NodeList from the XML file.
     *
     * @param inputXMLFile
     * @return
     */
    public NodeList getAllNodes(File inputXMLFile) {
        NodeList nodeList = null;
        try {
            DocumentBuilder dBuilder = getSecureDocumentBuilder().newDocumentBuilder();
            Document doc = dBuilder.parse(inputXMLFile);
            doc.getDocumentElement().normalize();
            nodeList = doc.getChildNodes();
        } catch (ParserConfigurationException e) {
            exceptionSingleton.setHasErrors(true);
            log.error("ParserConfigurationException occurred while parsing " + inputXMLFile.getName());
        } catch (IOException e) {
            exceptionSingleton.setHasErrors(true);
            log.error("IOException occurred while parsing " + inputXMLFile.getName());
        } catch (SAXException e) {
            exceptionSingleton.setHasErrors(true);
            log.error("SAXException occurred while parsing " + inputXMLFile.getName());
        }
        return nodeList;
    }

    /**
     * This method is used to get the attribute list of a given node.
     *
     * @param node
     * @return
     */
    public static List<Attribute> getAttributeList(Node node) {

        NamedNodeMap attributesMap;
        List<Attribute> attributesList = new ArrayList<Attribute>();

        if (node.hasAttributes()) {
            attributesMap = node.getAttributes();

            for (int attributesMapIndex = 0; attributesMapIndex < attributesMap.getLength();
                 attributesMapIndex++) {
                Attribute attribute = new Attribute();
                Node attributeNode = attributesMap.item(attributesMapIndex);
                attribute.setAttributeName(attributeNode.getNodeName());
                attribute.setAttributeValue(attributeNode.getNodeValue());

                attributesList.add(attribute);
            }
        }
        return attributesList;
    }

    /**
     * This method is used to convert XML node tree into ConfigNode tree.
     *
     * @param childNode
     * @param parent
     */
    public static void setXMLConfigNodes(Node childNode, ConfigNode parent) {

        Pattern pattern = Pattern.compile(NEWLINES_REGEX);
        ConfigNode child = new ConfigNode(childNode.getNodeName());

        try {
            if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.hasChildNodes()) {
                if (childNode.hasAttributes()) {
                    child.setAttributes(getAttributeList(childNode));
                }
                parent.addChild(child);
                NodeList childNodes = childNode.getChildNodes();

                for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++) {
                    setXMLConfigNodes(childNodes.item(childNodeIndex), child);
                }
            }
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                Matcher matcher = pattern.matcher(childNode.getNodeValue());
                if (!matcher.find()) {
                    child.setValue(childNode.getNodeValue());
                    parent.addChild(child);
                }
            }
        } catch (IllegalOperationException e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("Error occurred while adding a child into " + parent.getName(), e);
        }
    }
}


