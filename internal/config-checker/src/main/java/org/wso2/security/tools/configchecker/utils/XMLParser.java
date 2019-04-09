/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.configchecker.utils;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.configchecker.exception.ConfigCheckerException;
import org.wso2.security.tools.configchecker.model.Attribute;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class is used to parse XML configuration files.
 */
public class XMLParser {

    private static final int ENTITY_EXPANSION_LIMIT = 0;

    /**
     * This method is used to get the secure document builder.
     *
     * @return a secured document builder factory
     * @throws ConfigCheckerException when an error occurs while setting parser configuration
     */
    public DocumentBuilderFactory getSecureDocumentBuilder() throws ConfigCheckerException {
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
            throw new ConfigCheckerException(
                    "Failed to load XML Processor Feature " +
                            Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " +
                            Constants.LOAD_EXTERNAL_DTD_FEATURE, e);
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
     * @param inputXMLFile xml file object
     * @return xml document from the given file
     * @throws ConfigCheckerException when an error occurs while parsing
     */
    public Document getXMLDocument(File inputXMLFile) throws ConfigCheckerException {
        Document doc = null;
        try {
            DocumentBuilder dBuilder = getSecureDocumentBuilder().newDocumentBuilder();
            doc = dBuilder.parse(inputXMLFile);
            doc.getDocumentElement().normalize();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ConfigCheckerException("Error occurred while parsing " + inputXMLFile.getName(), e);
        }
        return doc;
    }

    /**
     * This method is used to get the NodeList from the XML file.
     *
     * @param inputXMLFile xml file object
     * @return a node list of the given xml file
     * @throws ConfigCheckerException when an error occurs while parsing
     */
    public NodeList getAllNodes(File inputXMLFile) throws ConfigCheckerException {
        NodeList nodeList = null;
        try {
            DocumentBuilder dBuilder = getSecureDocumentBuilder().newDocumentBuilder();
            Document doc = dBuilder.parse(inputXMLFile);
            doc.getDocumentElement().normalize();
            nodeList = doc.getChildNodes();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ConfigCheckerException("Error occurred while parsing " + inputXMLFile.getName(), e);
        }
        return nodeList;
    }

    /**
     * This method is used to get the attribute list of a given node.
     *
     * @param node xml node
     * @return list of attribute objects inside the given xml node
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
}
