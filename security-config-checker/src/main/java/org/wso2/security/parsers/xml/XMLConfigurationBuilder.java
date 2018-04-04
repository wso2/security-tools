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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.ConfigNode;
import org.wso2.security.common.ConfigurationBuilder;

/**
 * This class is used to parse XML objects into ConfigNode objects.
 */
public class XMLConfigurationBuilder implements ConfigurationBuilder {
    public ConfigNode parse(String configurationString) {

        XMLParser xmlParser = new XMLParser();
        Document doc = xmlParser.getXMLDocumentByXMLString(configurationString);
        Node root = doc.getDocumentElement();
        ConfigNode rootNode = new ConfigNode(root.getNodeName());

        rootNode.setAttributes(xmlParser.getAttributeList(root));
        NodeList nodeList = root.getChildNodes();

        for (int nodeListCount = 0; nodeListCount < nodeList.getLength();
             nodeListCount++) {
            xmlParser.setXMLConfigNodes(nodeList.item(nodeListCount), rootNode);
        }
        return rootNode;
    }
}
