/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.security.tools.findsecbugs.scanner.handler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * The class {@code XMLHandler} contains methods to modify a pom.xml file and add the FindSecBugs maven plugin
 */
public class XMLHandler {

    //FindSecBugs plugin related
    private static final String BUILD_ELEMENT = "build";
    private static final String PLUGINS_ELEMENT = "plugins";
    private static final String PLUGIN_ELEMENT = "plugin";
    private static final String GROUP_ID_ELEMENT = "groupId";
    private static final String GROUP_ID_TEXT = "org.codehaus.mojo";
    private static final String ARTIFACT_ID_ELEMENT = "artifactId";
    private static final String ARTIFACT_ID_TEXT = "findbugs-maven-plugin";
    private static final String VERSION_ELEMENT = "version";
    private static final String VERSION_TEXT = "3.0.1";
    private static final String CONFIGURATION_ELEMENT = "configuration";
    private static final String EFFORT_ELEMENT = "effort";
    private static final String EFFORT_TEXT = "Max";
    private static final String THRESHOLD_ELEMENT = "threshold";
    private static final String THRESHOLD_TEXT = "Low";
    private static final String FAIL_ON_ERROR_ELEMENT = "failOnError";
    private static final String FAIL_ON_ERROR_TEXT = "true";
    private static final String INCLUDE_FILTER_FILE_ELEMENT = "includeFilterFile";
    private static final String INCLUDE_FILTER_FILE_TEXT = "${session" +
            ".executionRootDirectory}/findbugs-security-include.xml";
    private static final String EXCLUDE_FILTER_FILE_ELEMENT = "excludeFilterFile";
    private static final String EXCLUDE_FILTER_FILE_TEXT = "${session" +
            ".executionRootDirectory}/findbugs-security-exclude.xml";
    private static final String GROUP_ID_TEXT_2 = "com.h3xstream.findsecbugs";
    private static final String ARTIFACT_ID_TEXT_2 = "findsecbugs-plugin";
    private static final String VERSION_TEXT_2 = "LATEST";
    //FindSecBugs include, exclude files related
    private static final String FIND_BUGS_FILTER_ELEMENT = "FindBugsFilter";
    private static final String MATCH_ELEMENT = "match";
    private static final String BUG_ELEMENT = "Bug";
    private static final String CATEGORY_ATTRIBUTE = "category";
    private static final String CATEGORY_ATTRIBUTE_VALUE = "SECURITY";

    /**
     * Iterate through a pom.xml file and find the {@code <build>} node. Then append {@code FindBugs} plugin included
     * with {@code FindSecBugs} plugin
     *
     * @param node     Node to iterate and find
     * @param document XML document
     * @return XML document
     * @throws TransformerException
     */
    public static Document appendFindBugsPlugin(Node node, Document document) throws TransformerException {
        Element rootElement = iterateAndFindElement(node);
        createFindBugsPluginNode(document, rootElement);
        return document;
    }

    private static Element iterateAndFindElement(Node node) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                //calls this method for all the children which is Element
                if (currentNode.getNodeName().equals(BUILD_ELEMENT)) {
                    return (Element) currentNode;
                }
                iterateAndFindElement(currentNode);
            }
        }
        return null;
    }

    private static void createFindBugsPluginNode(Document document, Element rootElement) throws TransformerException {
        //Get the <plugins> element that available under <build> element
        Element pluginsElement = (Element) rootElement.getElementsByTagName(PLUGINS_ELEMENT).item(0);
        //Create new element <plugin>
        Element pluginElement = document.createElement(PLUGIN_ELEMENT);
        //Create <groupId> element and add text
        Element groupIdElement = document.createElement(GROUP_ID_ELEMENT);
        groupIdElement.appendChild(document.createTextNode(GROUP_ID_TEXT));
        //Create <artifactId> element and add text
        Element artifactIdElement = document.createElement(ARTIFACT_ID_ELEMENT);
        artifactIdElement.appendChild(document.createTextNode(ARTIFACT_ID_TEXT));
        //Create <version> element and add text
        Element versionElement = document.createElement(VERSION_ELEMENT);
        versionElement.appendChild(document.createTextNode(VERSION_TEXT));
        //Create <configuration> element
        Element configurationElement = createConfigurationNode(document);
        //Create findSecBugs <plugin> element and append to <configuration> element
        Element findSecBugsPlugin = createFindSecBugsPlugin(document);
        configurationElement.appendChild(findSecBugsPlugin);
        //Append child elements to <plugin> element
        pluginElement.appendChild(groupIdElement);
        pluginElement.appendChild(artifactIdElement);
        pluginElement.appendChild(versionElement);
        pluginElement.appendChild(configurationElement);
        //Append child elements to <plugins> element
        pluginsElement.appendChild(pluginElement);
    }

    private static Element createConfigurationNode(Document document) {
        //Create <configuration> element
        Element configurationElement = document.createElement(CONFIGURATION_ELEMENT);
        Element effortElement = document.createElement(EFFORT_ELEMENT);
        effortElement.appendChild(document.createTextNode(EFFORT_TEXT));
        //Create <threshold> element and set text
        Element thresholdElement = document.createElement(THRESHOLD_ELEMENT);
        thresholdElement.appendChild(document.createTextNode(THRESHOLD_TEXT));
        //Create <failOnError> element and set text
        Element failOnErrorElement = document.createElement(FAIL_ON_ERROR_ELEMENT);
        failOnErrorElement.appendChild(document.createTextNode(FAIL_ON_ERROR_TEXT));
        //Create <includeFilterFile> element and add text
        Element includeFilterFileElement = document.createElement(INCLUDE_FILTER_FILE_ELEMENT);
        includeFilterFileElement.appendChild(document.createTextNode(INCLUDE_FILTER_FILE_TEXT));
        //Create <excludeFilterFile> element and add text
        Element excludeFilterFileElement = document.createElement(EXCLUDE_FILTER_FILE_ELEMENT);
        excludeFilterFileElement.appendChild(document.createTextNode(EXCLUDE_FILTER_FILE_TEXT));
        //Add child nodes to <configuration> element
        configurationElement.appendChild(effortElement);
        configurationElement.appendChild(thresholdElement);
        configurationElement.appendChild(failOnErrorElement);
        configurationElement.appendChild(includeFilterFileElement);
        configurationElement.appendChild(excludeFilterFileElement);
        return configurationElement;
    }

    private static Element createFindSecBugsPlugin(Document document) {
        //Creates <plugins> and <plugin> elements
        Element pluginsElement = document.createElement(PLUGINS_ELEMENT);
        Element pluginElement = document.createElement(PLUGIN_ELEMENT);
        //Creates child elements
        Element groupIdElement = document.createElement(GROUP_ID_ELEMENT);
        groupIdElement.appendChild(document.createTextNode(GROUP_ID_TEXT_2));
        Element artifactIdElement = document.createElement(ARTIFACT_ID_ELEMENT);
        artifactIdElement.appendChild(document.createTextNode(ARTIFACT_ID_TEXT_2));
        Element versionElement = document.createElement(VERSION_ELEMENT);
        versionElement.appendChild(document.createTextNode(VERSION_TEXT_2));
        //append children to <plugin>
        pluginElement.appendChild(groupIdElement);
        pluginElement.appendChild(artifactIdElement);
        pluginElement.appendChild(versionElement);
        //append <plugin> child to <plugins>
        pluginsElement.appendChild(pluginElement);
        return pluginsElement;
    }

    public static void writeFindSecBugsExcludeFile(Document document) throws ParserConfigurationException {
        // create the root element
        Element rootEle = document.createElement(FIND_BUGS_FILTER_ELEMENT);
        document.appendChild(rootEle);
    }

    public static void writeFindSecBugsIncludeFile(Document document) throws ParserConfigurationException {
        Element rootEle = document.createElement(FIND_BUGS_FILTER_ELEMENT);
        Element matchElement = document.createElement(MATCH_ELEMENT);
        Element bugElement = document.createElement(BUG_ELEMENT);
        bugElement.setAttribute(CATEGORY_ATTRIBUTE, CATEGORY_ATTRIBUTE_VALUE);
        matchElement.appendChild(bugElement);
        rootEle.appendChild(matchElement);
        document.appendChild(rootEle);
    }
}