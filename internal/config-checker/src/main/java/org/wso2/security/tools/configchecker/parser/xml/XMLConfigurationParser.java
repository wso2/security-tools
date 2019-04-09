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
package org.wso2.security.tools.configchecker.parser.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.configchecker.exception.ConfigCheckerException;
import org.wso2.security.tools.configchecker.model.ConfigNode;
import org.wso2.security.tools.configchecker.parser.ConfigurationParser;
import org.wso2.security.tools.configchecker.utils.Constants;
import org.wso2.security.tools.configchecker.utils.XMLParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class is used to parse XML objects into ConfigNode objects.
 */
public class XMLConfigurationParser implements ConfigurationParser {

    public ConfigNode parse(String configurationString) throws ConfigCheckerException {
        ConfigNode rootNode = null;
        try {
            Document doc = getXMLDocumentByXMLString(configurationString);
            Node root = doc.getDocumentElement();
            rootNode = new ConfigNode(root.getNodeName());
            rootNode.setAttributes(XMLParser.getAttributeList(root));
            NodeList nodeList = root.getChildNodes();
            for (int nodeListCount = 0; nodeListCount < nodeList.getLength();
                 nodeListCount++) {
                setXMLConfigNodes(nodeList.item(nodeListCount), rootNode);
            }
        } catch (ConfigCheckerException e) {
            throw new ConfigCheckerException("Error occurred while parsing.", e);
        }
        return rootNode;
    }

    /**
     * This method is used to parse an XML node tree into a ConfigNode tree.
     *
     * @param childNode
     * @param parent
     * @throws ConfigCheckerException
     */
    private static void setXMLConfigNodes(Node childNode, ConfigNode parent) throws ConfigCheckerException {
        Pattern pattern = Pattern.compile(Constants.NEWLINES_REGEX);
        ConfigNode child = new ConfigNode(childNode.getNodeName());
        try {
            if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.hasChildNodes()) {
                if (childNode.hasAttributes()) {
                    child.setAttributes(XMLParser.getAttributeList(childNode));
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
        } catch (ConfigCheckerException e) {
            throw new ConfigCheckerException("Error occurred while adding a child into " + parent.getName(), e);
        }
    }

    /**
     * This method is used to get the XML document from XML string.
     *
     * @param xmlString
     * @return
     * @throws ConfigCheckerException
     */
    public Document getXMLDocumentByXMLString(String xmlString) throws ConfigCheckerException {
        Document doc = null;
        try {
            XMLParser xmlParser = new XMLParser();
            DocumentBuilder dBuilder = xmlParser.getSecureDocumentBuilder().newDocumentBuilder();
            doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));
            doc.getDocumentElement().normalize();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ConfigCheckerException("Error occurred while parsing the xml string. " + e.getMessage(), e);
        }
        return doc;
    }
}
