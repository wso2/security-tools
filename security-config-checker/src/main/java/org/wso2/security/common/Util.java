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
package org.wso2.security.common;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.Attribute;
import org.wso2.security.ChildProperty;
import org.wso2.security.ConfigNode;
import org.wso2.security.Configuration;
import org.wso2.security.ParentProperty;
import org.wso2.security.exceptions.InvalidXMLElementException;
import org.wso2.security.exceptions.UnidentifiedFileFormatException;
import org.wso2.security.exceptions.XMLAttributeNotFoundException;
import org.wso2.security.parsers.json.JSONConfigurationBuilder;
import org.wso2.security.parsers.xml.XMLConfigurationBuilder;
import org.wso2.security.parsers.xml.XMLParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.security.common.Constants.CHILD_PROPERTIES_TAG;
import static org.wso2.security.common.Constants.CONFIGURATIONS_TAG;
import static org.wso2.security.common.Constants.CONFIG_ID_ATTRIBUTE;
import static org.wso2.security.common.Constants.CONFIG_TAG;
import static org.wso2.security.common.Constants.EXCLUDE_CONFIGS_TAG;
import static org.wso2.security.common.Constants.EXCLUDE_PATHS_TAG;
import static org.wso2.security.common.Constants.EXCLUDE_PATH_TAG;
import static org.wso2.security.common.Constants.FILE_FORMAT_TAG;
import static org.wso2.security.common.Constants.FILE_TAG;
import static org.wso2.security.common.Constants.FILE_TYPE_TAG;
import static org.wso2.security.common.Constants.JSON;
import static org.wso2.security.common.Constants.OUTPUT_FILE_PATH;
import static org.wso2.security.common.Constants.PARENT_PROPERTIES_TAG;
import static org.wso2.security.common.Constants.PRODUCT_PATH_TAG;
import static org.wso2.security.common.Constants.REFERENCE_PATH_TAG;
import static org.wso2.security.common.Constants.XML;

/**
 * This class includes all the utility methods.
 */
public class Util {
    private static final Logger log = LoggerFactory.getLogger(Util.class);

    public static String applicationPath;
    public static String productPath;
    public static BufferedWriter bufferedWriter;

    public static String getProductPath() {
        return productPath;
    }

    public static void setProductPath(String productPath) throws NullPointerException {
        if (productPath != null) {
            Util.productPath = productPath;
        } else {
            throw new NullPointerException("Product path is null");
        }
    }

    public static BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }

    public static void setBufferedWriter(BufferedWriter bufferedWriter) {
        Util.bufferedWriter = bufferedWriter;
    }

    public static String getApplicationPath() {
        return applicationPath;
    }

    public static void setApplicationPath(String applicationPath) throws NullPointerException {
        if (applicationPath != null) {
            Util.applicationPath = applicationPath;
        } else {
            throw new NullPointerException("Application home path is null");
        }
    }

    /**
     * This method is used to get the list of files in a given product package.
     *
     * @param filename
     * @param productDirectoryPath
     * @return a list of file paths inside the given directory path with the given file name.
     */
    private static List<String> getConfigFilePaths(String filename, String productDirectoryPath) {
        List<String> paths = new ArrayList<>();
        try {
            Files.walk(Paths.get(productDirectoryPath))
                    .filter(Files::isRegularFile)
                    .forEach((f)-> {
                        String filePath = f.toString();
                        File file = new File(filePath);
                        if (file.getName().equals(filename)) {
                            paths.add(filePath);
                        }
                    });
        } catch (IOException e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("IO Exception occurred while retrieving the configuration files", e);
        }
        return paths;
    }

    /**
     * This method is used to get the configuration file name.
     *
     * @param propertyNode The file node in the parent.xml or child.xml containing 'id' attribute
     * @return the file name of the configuration file
     * @throws XMLAttributeNotFoundException
     */
    private static String getConfigFileType(Node propertyNode) throws XMLAttributeNotFoundException {

        NamedNodeMap nodeMap;
        String configFileType = null;
        if (propertyNode.hasAttributes()) {
            nodeMap = propertyNode.getAttributes();

            for (int nodeMapIndex = 0; nodeMapIndex < nodeMap.getLength(); nodeMapIndex++) {
                Node refNode = nodeMap.item(nodeMapIndex);

                if ((refNode.getNodeName().equalsIgnoreCase(FILE_TYPE_TAG))) {
                    configFileType = refNode.getNodeValue();
                }
            }
            if (configFileType != null) {
                return configFileType;
            } else {
                throw new XMLAttributeNotFoundException("type attribute is not defined for <file> element in " +
                        propertyNode.getParentNode().getNodeName());
            }
        } else {
            throw new XMLAttributeNotFoundException("No attributes found for <file> element in " +
                    propertyNode.getParentNode().getNodeName());
        }
    }

    /**
     * This method is used to get the configuration id from the <config> element.
     *
     * @param configNode
     * @return configuration id
     * @throws XMLAttributeNotFoundException
     */
    private static String getConfigID(Node configNode) throws XMLAttributeNotFoundException {

        NamedNodeMap nodeMap;
        String configID = null;
        if (configNode.hasAttributes()) {
            nodeMap = configNode.getAttributes();

            for (int nodeMapIndex = 0; nodeMapIndex < nodeMap.getLength(); nodeMapIndex++) {
                Node refNode = nodeMap.item(nodeMapIndex);

                if ((refNode.getNodeName().equalsIgnoreCase(CONFIG_ID_ATTRIBUTE))) {
                    configID = refNode.getNodeValue();
                }
            }
            if (configID != null) {
                return configID;
            } else {
                throw new XMLAttributeNotFoundException("id attribute is not defined for <config> element in " +
                        configNode.getParentNode().getNodeName());
            }
        } else {
            throw new XMLAttributeNotFoundException("No attributes found for <config> element in " +
                    configNode.getParentNode().getNodeName());
        }
    }

    /**
     * This method is used to get the list of excluded configuration files paths in the product.
     *
     * @param childPropertiesDoc child properties file document
     * @return list of excluded configuration files.
     */
    private static List<String> getExcludeProductFiles(Document childPropertiesDoc) {

        List<String> excludePathList = new ArrayList<String>();

        NodeList excludePaths = childPropertiesDoc.getElementsByTagName(EXCLUDE_PATHS_TAG);

        for (int excludePathsIndex = 0; excludePathsIndex < excludePaths.getLength(); excludePathsIndex++) {
            NodeList excludePath = excludePaths.item(excludePathsIndex).getChildNodes();

            for (int excludePathIndex = 0; excludePathIndex < excludePath.getLength(); excludePathIndex++) {

                if (EXCLUDE_PATH_TAG.equals(excludePath.item(excludePathIndex).getNodeName())) {
                    if (excludePath.item(excludePathIndex).getTextContent() != null) {
                        excludePathList.add(productPath + excludePath.item(excludePathIndex).getTextContent());
                    }
                }
            }
        }
        return excludePathList;
    }

    /**
     * This method is used to get the list of excluded configurations for common configurations.
     *
     * @param excludeConfigsNodeList
     * @return a list of excluded configuration names
     */
    private static List<String> getExcludeConfigs(NodeList excludeConfigsNodeList)
            throws InvalidXMLElementException {

        List<String> excludeConfigList = new ArrayList<String>();
        Node excludeConfigNode;

        for (int excludeConfigNodeListIndex = 0; excludeConfigNodeListIndex <
                excludeConfigsNodeList.getLength(); excludeConfigNodeListIndex++) {

            excludeConfigNode = excludeConfigsNodeList.item(excludeConfigNodeListIndex);

            if (excludeConfigNode != null && excludeConfigNode.getNodeType() == Node.ELEMENT_NODE) {
                if (!CONFIG_TAG.equals(excludeConfigNode.getNodeName())) {
                    throw new InvalidXMLElementException("Invalid <config> element in child.xml");
                } else {
                    excludeConfigList.add(excludeConfigNode.getTextContent());
                }
            }
        }
        return excludeConfigList;
    }

    /**
     * Read the file from the given file path.
     *
     * @param filePath
     * @return
     * @throws FileNotFoundException
     */
    private static File readFile(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        if (file.exists()) {
            return file;
        } else {
            throw new FileNotFoundException("File not found in " + filePath);
        }
    }

    /**
     * This method is used to read the parent.xml file.
     *
     * @param parentPropertiesList
     * @return a hash map with a key value pair of file id and parent properties object.
     */
    private static HashMap<String, ParentProperty> getParentProperties(NodeList parentPropertiesList) {

        NodeList parentPropertyNodes = null;
        NodeList parentFileProperties;
        String parentFileType;
        HashMap<String, ParentProperty> parentPropertiesHashMap = new HashMap<String, ParentProperty>();

        for (int parentPropertiesListIndex = 0; parentPropertiesListIndex < parentPropertiesList.getLength();
             parentPropertiesListIndex++) {

            if (PARENT_PROPERTIES_TAG.equals(parentPropertiesList.item(parentPropertiesListIndex).
                    getNodeName())) {
                parentPropertyNodes = parentPropertiesList.item(parentPropertiesListIndex).getChildNodes();
            }
        }
        try {
            for (int configParentFileIndex = 0; configParentFileIndex < parentPropertyNodes.getLength();
                 configParentFileIndex++) {

                ParentProperty parentProperty = new ParentProperty();
                Node parentPropertyNode = parentPropertyNodes.item(configParentFileIndex);

                if (!FILE_TAG.equals(parentPropertyNode.getNodeName())) {
                    continue;
                }
                parentFileType = getConfigFileType(parentPropertyNode);

                if (parentPropertyNode.hasChildNodes()) {
                    parentFileProperties = parentPropertyNode.getChildNodes();

                    for (int fileIndex = 0; fileIndex < parentFileProperties.getLength();
                         fileIndex++) {

                        if (FILE_FORMAT_TAG.equals(parentFileProperties.item(fileIndex).getNodeName())) {
                            if (parentFileProperties.item(fileIndex).getTextContent() != null) {
                                parentProperty.setFileFormat(parentFileProperties.item(fileIndex)
                                        .getTextContent());
                            }
                        }
                        if (REFERENCE_PATH_TAG.equals(parentFileProperties.item(fileIndex)
                                .getNodeName())) {
                            if (parentFileProperties.item(fileIndex).getTextContent() != null) {
                                parentProperty.setRefParentConfigFilePath(applicationPath +
                                        parentFileProperties.item(fileIndex).getTextContent());
                            }
                        }
                    }
                }
                parentPropertiesHashMap.put(parentFileType, parentProperty);
            }
        } catch (XMLAttributeNotFoundException e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("XMLAttributeNotFoundException occurred while reading from parent.xml", e);
        } catch (Exception e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("Exception occurred while reading from parent.xml", e);
        }
        return parentPropertiesHashMap;
    }

    /**
     * This method is used to compareConfigFiles two ConfigNode objects.
     *
     * @param refConfigNode
     * @param productConfigNode
     * @return true if the refConfigNode contains inside productConigNode.
     */
    public static Boolean compareConfigNodeTree(ConfigNode refConfigNode, ConfigNode productConfigNode) {

        Boolean isEqual = false;
        Boolean hasAttributes = false;
        Boolean isAttributeEqual = false;
        Boolean areAllAttributesEqual = false;
        Boolean isChildEqual = false;

        if (refConfigNode.getName().equals(productConfigNode.getName())) {

            if (refConfigNode.hasAttributes()) {
                hasAttributes = true;

                if (productConfigNode.hasAttributes()) {

                    List<Attribute> refNodeAttributes = refConfigNode.getAttributes();
                    List<Attribute> productNodeAttributes = productConfigNode.getAttributes();

                    if (refNodeAttributes.size() != productNodeAttributes.size()) {
                        return false;
                    }

                    for (Attribute refNodeAttribute : refNodeAttributes) {
                        isAttributeEqual = false;

                        for (Attribute productNodeAttribute : productNodeAttributes) {
                            if (productNodeAttribute.getAttributeName().equals(refNodeAttribute
                                    .getAttributeName()) &&
                                    productNodeAttribute.getAttributeValue().equals(refNodeAttribute
                                            .getAttributeValue())) {
                                isAttributeEqual = true;
                                break;
                            }
                        }
                        if (!isAttributeEqual) {
                            break;
                        }
                    }
                    areAllAttributesEqual = isAttributeEqual;
                } else {
                    return false;
                }
            } else {
                if (productConfigNode.hasAttributes()) {
                    return false;
                }
            }
            if (!hasAttributes || areAllAttributesEqual) {
                if (refConfigNode.isLeafNode()) {
                    if (productConfigNode.isLeafNode()) {
                        if (refConfigNode.getValue().equals(productConfigNode.getValue())) {
                            isEqual = true;
                        }
                    }
                } else {
                    if (productConfigNode.isLeafNode()) {
                        return false;
                    } else {
                        List<ConfigNode> refConfigChildren = refConfigNode.getChildren();
                        List<ConfigNode> productConfigChildren = productConfigNode.getChildren();

                        for (ConfigNode refConfigChild : refConfigChildren) {
                            isChildEqual = false;
                            for (ConfigNode productConfigChild : productConfigChildren) {
                                isChildEqual = compareConfigNodeTree(refConfigChild, productConfigChild);
                                if (isChildEqual) {
                                    break;
                                }
                            }
                            if (!isChildEqual) {
                                break;
                            }
                        }
                        isEqual = isChildEqual;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
        return isEqual;
    }

    /**
     * This method is used to read the configuration file.
     *
     * @param configurationFile
     * @return List of Configuration objects containing the config id and the config value.
     */
    public static List<Configuration> getConfigurations(File configurationFile) {


        XMLParser xmlParser = new XMLParser();
        List<Configuration> configurationList = new ArrayList<Configuration>();
        NodeList configurationNodes = null;
        NodeList configProperties = null;

        NodeList configurationNodeList = xmlParser.getAllNodes(configurationFile);

        try {
            for (int configurationNodeListIndex = 0; configurationNodeListIndex <
                    configurationNodeList.getLength(); configurationNodeListIndex++) {

                if (configurationNodeList.item(configurationNodeListIndex) != null &&
                        CONFIGURATIONS_TAG.equals(configurationNodeList
                                .item(configurationNodeListIndex).getNodeName())) {
                    configurationNodes = configurationNodeList
                            .item(configurationNodeListIndex).getChildNodes();
                }
            }
            for (int configIndex = 0; configIndex <
                    configurationNodes.getLength(); configIndex++) {

                Configuration configuration = new Configuration();
                if (configurationNodes.item(configIndex) != null &&
                        !CONFIG_TAG.equals(configurationNodes.item(configIndex).getNodeName())) {
                    continue;
                }
                if (configurationNodes.item(configIndex).hasChildNodes()) {
                    configProperties = configurationNodes.item(configIndex).getChildNodes();
                }
                for (int configPropertiesIndex = 0; configPropertiesIndex <
                        configProperties.getLength(); configPropertiesIndex++) {

                    if (configProperties.item(configPropertiesIndex).getNodeType() ==
                            Node.CDATA_SECTION_NODE) {
                        configuration.setConfiguration(configProperties.item(configPropertiesIndex)
                                .getNodeValue());
                    }
                }
                configuration.setConfigurationID(getConfigID(configurationNodes.item(configIndex)));

                configurationList.add(configuration);
            }

        } catch (XMLAttributeNotFoundException e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("XMLAttributeNotFoundException occurred while reading from configuration file " +
                    configurationFile.getPath(), e);
        } catch (Exception e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("Exception occurred while reading from configuration file " +
                    configurationFile.getPath(), e);
        }
        return configurationList;
    }

    /**
     * This method is used to read the child.xml.
     * @param childPropertiesNodeList
     * @return a list of child properties objects.
     */
    private static List<ChildProperty> getChildProperties(NodeList childPropertiesNodeList) {

        List<ChildProperty> childPropertyList = new ArrayList<ChildProperty>();
        NodeList childPropertyNodes = null;
        NodeList fileProperties = null;
        NodeList excludeConfigs;

        try {
            for (int childPropertiesListIndex = 0; childPropertiesListIndex <
                    childPropertiesNodeList.getLength(); childPropertiesListIndex++) {

                if (childPropertiesNodeList.item(childPropertiesListIndex) != null &&
                        CHILD_PROPERTIES_TAG.equals(childPropertiesNodeList
                                .item(childPropertiesListIndex).getNodeName())) {
                    childPropertyNodes = childPropertiesNodeList
                            .item(childPropertiesListIndex).getChildNodes();
                }
            }
            for (int configFileIndex = 0; configFileIndex <
                    childPropertyNodes.getLength(); configFileIndex++) {

                ChildProperty childConfigProperty = new ChildProperty();
                if (childPropertyNodes.item(configFileIndex) != null &&
                        !FILE_TAG.equals(childPropertyNodes.item(configFileIndex).getNodeName())) {
                    continue;
                }
                if (childPropertyNodes.item(configFileIndex).hasChildNodes()) {
                    fileProperties = childPropertyNodes.item(configFileIndex).getChildNodes();
                }
                childConfigProperty.setChildFileType(getConfigFileType(childPropertyNodes
                        .item(configFileIndex)));

                for (int fileIndex = 0; fileIndex < fileProperties.getLength(); fileIndex++) {
                    Node filePropertyNode = fileProperties.item(fileIndex);

                    if (FILE_FORMAT_TAG.equals(filePropertyNode.getNodeName())) {
                        childConfigProperty.setFileFormat(filePropertyNode.getTextContent());
                    }
                    if (REFERENCE_PATH_TAG.equals(filePropertyNode.getNodeName())) {
                        if (filePropertyNode.getTextContent() != null) {
                            childConfigProperty.setRefChildConfigFilePath(applicationPath +
                                    filePropertyNode.getTextContent());
                        }
                    }
                    if (PRODUCT_PATH_TAG.equals(filePropertyNode.getNodeName())) {
                        if (filePropertyNode.getTextContent() != null) {
                            childConfigProperty.setProductConfigFIlePath(productPath +
                                    filePropertyNode.getTextContent());
                        }
                    }
                    if (EXCLUDE_CONFIGS_TAG.equals(filePropertyNode.getNodeName())) {
                        if (filePropertyNode.hasChildNodes()) {
                            excludeConfigs = filePropertyNode.getChildNodes();
                            childConfigProperty.getExcludeList()
                                    .addAll(getExcludeConfigs(excludeConfigs));
                        }
                    }
                }
                childPropertyList.add(childConfigProperty);
            }

        } catch (XMLAttributeNotFoundException e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("XMLAttributeNotFoundException occurred while reading from child.xml", e);
        } catch (InvalidXMLElementException e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("InvalidXMLElementException occurred while reading from child.xml", e);
        }
        return childPropertyList;
    }

    /**
     *  This method is used to parse the configurations into ConfigNode objects and compare.
     *
     * @param refConfigFilePath
     * @param productConfigFilePath
     * @param fileFormat
     * @param excludeConfigs
     */
    private static void compareConfigFiles(String refConfigFilePath, String productConfigFilePath,
                                           String fileFormat, List<String> excludeConfigs)
            throws UnidentifiedFileFormatException {

        try {
            File refConfigFile = readFile(refConfigFilePath);
            File productConfigFile = readFile(productConfigFilePath);

            Boolean isConfigAvailable = false;
            ConfigNode refConfigNode = null;
            ConfigNode productConfigNode = null;
            ConfigurationBuilder configurationBuilder;
            List<Configuration> refConfigList = Util.getConfigurations(refConfigFile);
            FileInputStream productConfigFileStream = new FileInputStream(productConfigFile);
            String productConfigFileString = IOUtils.toString(productConfigFileStream, StandardCharsets.UTF_8);

            //change the parser according to the file format.
            if (fileFormat.equals(XML)) {
                configurationBuilder = new XMLConfigurationBuilder();
            } else if (fileFormat.equals(JSON)) {
                configurationBuilder = new JSONConfigurationBuilder();
            } else {
                throw new UnidentifiedFileFormatException("File format " + fileFormat + " is not defined");
            }

            productConfigNode = configurationBuilder.parse(productConfigFileString);

            for (Configuration refConfiguration : refConfigList) {

                if (excludeConfigs.contains(refConfiguration.getConfigurationID())) {
                    Util.getBufferedWriter().write("Config ID : " + refConfiguration
                            .getConfigurationID() + " -- Excluded");
                    Util.getBufferedWriter().newLine();
                    continue;
                }
                refConfigNode = configurationBuilder.parse(refConfiguration.getConfiguration());
                if (refConfigNode != null && productConfigNode != null) {
                    isConfigAvailable = compareConfigNodeTree(refConfigNode, productConfigNode);
                }
                if (isConfigAvailable) {
                    Util.getBufferedWriter().write("Config ID : " + refConfiguration
                            .getConfigurationID() + " -- OK");
                    Util.getBufferedWriter().newLine();
                } else {
                    Util.getBufferedWriter().write("Config ID : " + refConfiguration
                            .getConfigurationID() + " -- Fail");
                    Util.getBufferedWriter().newLine();
                }
            }
        } catch (FileNotFoundException e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("FileNotFoundException occurred", e);
        } catch (IOException e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("IOException occurred", e);
        }
    }

    /**
     * This method is used to compare all the configuration files in the product.
     *
     * @param childPropertiesFile
     * @param parentPropertiesFile
     */
    public static void validateConfigurations(File childPropertiesFile, File parentPropertiesFile) {

        List<String> excludedConfigFilePaths;
        List<ChildProperty> childPropertyList;
        HashMap<String, ParentProperty> parentPropertiesHashMap = null;

        XMLParser xmlParser = new XMLParser();
        List<String> excludeListForParentConfig = new ArrayList<String>();

        try {
            if (parentPropertiesFile.exists()) {
                NodeList parentPropertiesNodeList = xmlParser.getAllNodes(parentPropertiesFile);
                //read the parent.xml
                parentPropertiesHashMap = getParentProperties(parentPropertiesNodeList);
            }
            if (childPropertiesFile.exists()) {

                Document childPropertiesDocument = xmlParser.getXMLDocument(childPropertiesFile);
                //read the child.xml
                excludedConfigFilePaths = getExcludeProductFiles(childPropertiesDocument);
                childPropertyList = getChildProperties(childPropertiesDocument.getChildNodes());

                for (ChildProperty childConfigProperty : childPropertyList) {

                    excludeListForParentConfig.addAll(childConfigProperty.getExcludeList());
                    if (!excludedConfigFilePaths.contains(childConfigProperty.getProductConfigFilePath())) {
                        excludedConfigFilePaths.add(childConfigProperty.getProductConfigFilePath());
                    }
                    //get the child config IDs to exclude them from parent configurations.
                    List<Configuration> refConfigList = Util.getConfigurations(readFile(childConfigProperty
                            .getRefChildConfigFilePath()));

                    for (Configuration refConfig : refConfigList) {
                        excludeListForParentConfig.add(refConfig.getConfigurationID());
                    }

                    getBufferedWriter().write("\n\nFile : " + childConfigProperty.getProductConfigFilePath());
                    getBufferedWriter().write("\n\nChild Configurations \n");
                    Util.getBufferedWriter().newLine();
                    //compare child configurations
                    compareConfigFiles(childConfigProperty.getRefChildConfigFilePath(),
                            childConfigProperty.getProductConfigFilePath(),
                            childConfigProperty.getFileFormat(), Collections.<String>emptyList());

                    Util.getBufferedWriter().write("-------------------------------------------" +
                            "-------------------------------------------");
                    
                    ParentProperty parentConfigProperty = parentPropertiesHashMap
                            .get(childConfigProperty.getChildFileType());

                    getBufferedWriter().write("\n\nFile : " + childConfigProperty.getProductConfigFilePath());
                    getBufferedWriter().write("\n\nParent Configurations \n");
                    getBufferedWriter().newLine();
                    //compare parent configurations
                    compareConfigFiles(parentConfigProperty.getRefParentConfigFilePath(),
                            childConfigProperty.getProductConfigFilePath(),
                            parentConfigProperty.getFileFormat(), excludeListForParentConfig);

                    Util.getBufferedWriter().write("-------------------------------------------" +
                            "-------------------------------------------");

                    excludeListForParentConfig.clear();
                }
                //compare parent configurations of the files that are not defined in child.xml.
                if (parentPropertiesHashMap != null) {
                    for (Map.Entry<String, ParentProperty> parentPropertyEntry : parentPropertiesHashMap.entrySet()) {
                        String parentFileID = parentPropertyEntry.getKey();
                        ParentProperty parentConfigProperty = parentPropertiesHashMap.get(parentFileID);

                        if (parentConfigProperty.getFileFormat() != null &&
                                parentConfigProperty.getRefParentConfigFilePath() != null) {

                            List<String> productConfigFilePaths = getConfigFilePaths(parentFileID, productPath);

                            for (String productConfigFilepath : productConfigFilePaths) {

                                if (excludedConfigFilePaths.contains(productConfigFilepath)) {
                                    continue;
                                }

                                getBufferedWriter().write("\n\nFile : " + productConfigFilepath);
                                getBufferedWriter().write("\n\nParent Configurations \n");
                                getBufferedWriter().newLine();

                                compareConfigFiles(parentConfigProperty.getRefParentConfigFilePath(),
                                        productConfigFilepath,
                                        parentConfigProperty.getFileFormat(), Collections.<String>emptyList());

                                Util.getBufferedWriter().write("-------------------------------------------" +
                                        "-------------------------------------------");
                            }
                        }
                    }
                }
            } else {
                //compare parent configurations if child configurations does not exist.
                if (parentPropertiesHashMap != null) {
                    for (Map.Entry<String, ParentProperty> parentPropertyEntry : parentPropertiesHashMap.entrySet()) {
                        String parentFileID = parentPropertyEntry.getKey();
                        ParentProperty parentConfigProperty = parentPropertiesHashMap.get(parentFileID);

                        if (parentConfigProperty.getFileFormat() != null &&
                                parentConfigProperty.getRefParentConfigFilePath() != null) {

                            List<String> productConfigFilePaths = getConfigFilePaths(parentFileID, productPath);

                            for (String productConfigFilepath : productConfigFilePaths) {

                                getBufferedWriter().write("\n\nFile : " + productConfigFilepath);
                                getBufferedWriter().write("\n\nParent Configurations \n");
                                getBufferedWriter().newLine();

                                compareConfigFiles(parentConfigProperty.getRefParentConfigFilePath(),
                                        productConfigFilepath,
                                        parentConfigProperty.getFileFormat(), Collections.<String>emptyList());

                                Util.getBufferedWriter().write("-------------------------------------------" +
                                        "-------------------------------------------");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("IOException occurred while writing to " + OUTPUT_FILE_PATH, e);
        } catch (UnidentifiedFileFormatException e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("UnidentifiedFileFormatException occurred while comparing config files", e);
        }
    }
}
