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

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.configchecker.config.ConfigCheckerSettings;
import org.wso2.security.tools.configchecker.config.ConfigCheckerSettingsBuilder;
import org.wso2.security.tools.configchecker.exception.ConfigCheckerException;
import org.wso2.security.tools.configchecker.model.Attribute;
import org.wso2.security.tools.configchecker.model.ChildProperty;
import org.wso2.security.tools.configchecker.model.ConfigNode;
import org.wso2.security.tools.configchecker.model.Configuration;
import org.wso2.security.tools.configchecker.model.ParentProperty;
import org.wso2.security.tools.configchecker.parser.ConfigurationParser;
import org.wso2.security.tools.configchecker.report.ReportGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.wso2.security.tools.configchecker.utils.Constants.CHILD_PROPERTIES_TAG;
import static org.wso2.security.tools.configchecker.utils.Constants.CONFIGURATIONS_TAG;
import static org.wso2.security.tools.configchecker.utils.Constants.CONFIG_ID_ATTRIBUTE;
import static org.wso2.security.tools.configchecker.utils.Constants.CONFIG_TAG;
import static org.wso2.security.tools.configchecker.utils.Constants.EXCLUDE_CONFIGS_TAG;
import static org.wso2.security.tools.configchecker.utils.Constants.EXCLUDE_PATHS_TAG;
import static org.wso2.security.tools.configchecker.utils.Constants.EXCLUDE_PATH_TAG;
import static org.wso2.security.tools.configchecker.utils.Constants.FILE_FORMAT_TAG;
import static org.wso2.security.tools.configchecker.utils.Constants.FILE_TAG;
import static org.wso2.security.tools.configchecker.utils.Constants.FILE_TYPE_TAG;
import static org.wso2.security.tools.configchecker.utils.Constants.PARENT_PROPERTIES_TAG;
import static org.wso2.security.tools.configchecker.utils.Constants.PRODUCT_PATH_TAG;
import static org.wso2.security.tools.configchecker.utils.Constants.REFERENCE_PATH_TAG;

/**
 * This class includes the utility methods.
 */
public class Utils {

    private Utils() {
    }

    /**
     * Loading the configuration checker settings.
     *
     * @throws ConfigCheckerException when an error occurs while reading config checker settings
     */
    public static void loadConfigCheckerSettings() throws ConfigCheckerException {
        try {
            ConfigCheckerSettings.getInstance().setParentPropertiesFile(ConfigCheckerSettingsBuilder
                    .getConfiguration().getParentPropertiesFile());
            ConfigCheckerSettings.getInstance().setChildPropertiesFile(ConfigCheckerSettingsBuilder
                    .getConfiguration().getChildPropertiesFile());
            ConfigCheckerSettings.getInstance().setParsers(ConfigCheckerSettingsBuilder
                    .getConfiguration().getParsers());
            ConfigCheckerSettings.getInstance().setReportGenerators(ConfigCheckerSettingsBuilder
                    .getConfiguration().getReportGenerators());
        } catch (ConfigCheckerException e) {
            throw new ConfigCheckerException("Error occurred while reading config checker settings", e);
        }
    }

    /**
     * Get the appropriate report generator according to the given output format.
     *
     * @param configCheckerReportOutFormat output format of the report (e.g. text)
     * @return instance of the requested output report format generator class
     * @throws ConfigCheckerException when an error occurs while creating the report generation instance
     */
    public static ReportGenerator getConfigCheckerReportGenerator(String configCheckerReportOutFormat)
            throws ConfigCheckerException {
        ReportGenerator configCheckerReportGenerator = null;
        configCheckerReportGenerator = (ReportGenerator) getClassInstance(configCheckerReportOutFormat,
                ConfigCheckerSettings.getInstance().getReportGenerators());
        if (configCheckerReportGenerator == null) {
            throw new ConfigCheckerException("unable to find a report generator for the given report format: "
                    + configCheckerReportOutFormat);
        }
        return configCheckerReportGenerator;
    }

    /**
     * Get the appropriate parser according to the given file format.
     *
     * @param fileFormat format of the configuration file (e.g. xml, json, etc)
     * @return a configuration parser instance of the requested configuration file type
     * @throws ConfigCheckerException when an error occurs while creating the configuration parser instance.
     */
    public static ConfigurationParser getConfigurationParser(String fileFormat) throws ConfigCheckerException {
        ConfigurationParser configurationParser = null;
        configurationParser = (ConfigurationParser) getClassInstance(fileFormat,
                ConfigCheckerSettings.getInstance().getParsers());
        if (configurationParser == null) {
            throw new ConfigCheckerException("unable to find an parser for the given file format: "
                    + fileFormat);
        }
        return configurationParser;
    }

    private static Object getClassInstance(String type, Map<String, String> configurationMap)
            throws ConfigCheckerException {
        Object classInstance = null;
        for (Map.Entry<String, String> entry : configurationMap.entrySet()) {
            try {
                if (entry.getKey().equals(type)) {
                    classInstance = Class.forName(entry.getValue()).newInstance();
                }
            } catch (ClassNotFoundException e) {
                throw new ConfigCheckerException("Unable to find the class: " + entry.getValue(), e);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ConfigCheckerException("Unable to create an instance of the class: " + entry.getValue(), e);
            }
        }
        return classInstance;
    }

    /**
     * This method is used to get the list of files in a given product package.
     *
     * @param filename             file name to be searched
     * @param productDirectoryPath directory to be searched
     * @return a list of file paths inside the given directory path with the given file name
     * @throws ConfigCheckerException when an error occurs while iterating through the file paths
     */
    public static List<String> searchConfigFiles(String filename, String productDirectoryPath)
            throws ConfigCheckerException {
        List<String> paths = new ArrayList<>();
        try (Stream<Path> path = Files.walk(Paths.get(productDirectoryPath))) {
            path.filter(Files::isRegularFile)
                    .forEach((f) -> {
                        String filePath = f.toString();
                        File file = new File(filePath);
                        if (file.getName().equals(filename)) {
                            paths.add(filePath);
                        }
                    });
        } catch (IOException e) {
            throw new ConfigCheckerException("IO Exception occurred while retrieving the configuration files", e);
        }
        return paths;
    }

    /**
     * This method is used to get the configuration file type (e.g web.xml, jaggery.conf, etc.).
     *
     * @param propertyNode the file node in the parent.xml or child.xml containing 'id' attribute
     * @return the file name of the configuration file
     * @throws ConfigCheckerException when the given attribute is not found in the node
     */
    private static String getConfigFileType(Node propertyNode) throws ConfigCheckerException {
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
                throw new ConfigCheckerException("type attribute is not defined for <file> element in " +
                        propertyNode.getParentNode().getNodeName());
            }
        } else {
            throw new ConfigCheckerException("No attributes found for <file> element in " +
                    propertyNode.getParentNode().getNodeName());
        }
    }

    private static String getConfigId(Node configNode) throws ConfigCheckerException {
        NamedNodeMap nodeMap;
        String configId = null;
        if (configNode.hasAttributes()) {
            nodeMap = configNode.getAttributes();

            for (int nodeMapIndex = 0; nodeMapIndex < nodeMap.getLength(); nodeMapIndex++) {
                Node refNode = nodeMap.item(nodeMapIndex);
                if ((refNode.getNodeName().equalsIgnoreCase(CONFIG_ID_ATTRIBUTE))) {
                    configId = refNode.getNodeValue();
                }
            }
            if (configId != null) {
                return configId;
            } else {
                throw new ConfigCheckerException("id attribute is not defined for <config> element in " +
                        configNode.getParentNode().getNodeName());
            }
        } else {
            throw new ConfigCheckerException("No attributes found for <config> element in " +
                    configNode.getParentNode().getNodeName());
        }
    }

    /**
     * This method is used to get the list of excluded configuration files paths in the product.
     *
     * @param childPropertiesDoc xml document object with the child.xml properties
     * @param productPath        base path for the product pack
     * @return list of excluded configuration files
     */
    public static List<String> getExcludeProductFiles(Document childPropertiesDoc, String productPath) {
        List<String> excludePathList = new ArrayList<String>();
        NodeList excludePaths = childPropertiesDoc.getElementsByTagName(EXCLUDE_PATHS_TAG);

        for (int excludePathsIndex = 0; excludePathsIndex < excludePaths.getLength(); excludePathsIndex++) {
            NodeList excludePath = excludePaths.item(excludePathsIndex).getChildNodes();
            for (int excludePathIndex = 0; excludePathIndex < excludePath.getLength(); excludePathIndex++) {
                if (EXCLUDE_PATH_TAG.equals(excludePath.item(excludePathIndex).getNodeName())
                        && excludePath.item(excludePathIndex).getTextContent() != null) {
                    excludePathList.add(productPath + excludePath.item(excludePathIndex).getTextContent());
                }
            }
        }
        return excludePathList;
    }

    /**
     * This method is used to get the list of excluded configurations form child.xml.
     *
     * @param excludeConfigsNodeList node list containing the exclude configurations
     * @return a list of excluded configuration names
     * @throws ConfigCheckerException when an invalid node is parsed
     */
    public static List<String> getExcludeConfigs(NodeList excludeConfigsNodeList)
            throws ConfigCheckerException {
        List<String> excludeConfigList = new ArrayList<>();
        Node excludeConfigNode;

        for (int excludeConfigNodeListIndex = 0; excludeConfigNodeListIndex <
                excludeConfigsNodeList.getLength(); excludeConfigNodeListIndex++) {
            excludeConfigNode = excludeConfigsNodeList.item(excludeConfigNodeListIndex);
            if (excludeConfigNode != null && excludeConfigNode.getNodeType() == Node.ELEMENT_NODE) {
                if (!CONFIG_TAG.equals(excludeConfigNode.getNodeName())) {
                    throw new ConfigCheckerException("Invalid <config> element in child.xml");
                } else {
                    excludeConfigList.add(excludeConfigNode.getTextContent());
                }
            }
        }
        return excludeConfigList;
    }

    /**
     * Read a file from a given file path.
     *
     * @param filePath file path for the file
     * @return File object
     * @throws FileNotFoundException when requested file cannot be found
     */
    public static File readFile(String filePath) throws FileNotFoundException {
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
     * @param parentPropertiesList xml node list containing parent properties
     * @param applicationPath      base path for the reference config files defined in parent.xml
     * @return a hash map with a key value pair of file id and parent properties object
     * @throws ConfigCheckerException when an error occurs while reading the parent properties
     */
    public static HashMap<String, ParentProperty> getParentProperties(NodeList parentPropertiesList,
                                                                      String applicationPath)
            throws ConfigCheckerException {
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
        if (parentPropertyNodes == null) {
            throw new ConfigCheckerException("Unable to find parent properties");
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
                        if (FILE_FORMAT_TAG.equals(parentFileProperties.item(fileIndex).getNodeName())
                                && parentFileProperties.item(fileIndex).getTextContent() != null) {
                            parentProperty.setFileFormat(parentFileProperties.item(fileIndex)
                                    .getTextContent());
                        }
                        if (REFERENCE_PATH_TAG.equals(parentFileProperties
                                .item(fileIndex).getNodeName())
                                && parentFileProperties.item(fileIndex).getTextContent() != null) {
                            parentProperty.setRefParentConfigFilePath(applicationPath +
                                    parentFileProperties.item(fileIndex).getTextContent());
                        }
                    }
                }
                parentPropertiesHashMap.put(parentFileType, parentProperty);
            }
        } catch (ConfigCheckerException e) {
            throw new ConfigCheckerException("Error occurred while reading from parent.xml", e);
        }

        return parentPropertiesHashMap;
    }

    /**
     * This method is used to compare two ConfigNode objects.
     *
     * @param refConfigNode     xml node containing the reference configuration
     * @param productConfigNode configuration xml node in the product that needs to be validated with the reference
     *                          config node
     * @return true if the refConfigNode contains inside productConfigNode.
     */
    public static Boolean compareConfigNodeTree(ConfigNode refConfigNode, ConfigNode productConfigNode) {
        Boolean isEqual = false;
        Boolean hasAttributes = false;
        Boolean isAttributeEqual = false;
        Boolean areAllAttributesEqual = false;
        Boolean isChildEqual = false;
        List<Attribute> refNodeAttributes;
        List<Attribute> productNodeAttributes;

        if (refConfigNode.getName().equals(productConfigNode.getName())) {
            if (refConfigNode.hasAttributes()) {
                hasAttributes = true;

                if (productConfigNode.hasAttributes()) {
                    refNodeAttributes = refConfigNode.getAttributes();
                    productNodeAttributes = productConfigNode.getAttributes();

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
            }

            if (!hasAttributes || areAllAttributesEqual) {
                if (refConfigNode.isLeafNode()) {
                    if (productConfigNode.isLeafNode() && refConfigNode.getValue()
                            .equals(productConfigNode.getValue())) {
                        isEqual = true;
                    }
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
     * @param configurationFile configuration file object that needs to be mapped into configuration objects
     * @return a list of Configuration objects containing the config id and the config value
     * @throws ConfigCheckerException when an error occurs when reading from the config file
     */
    public static List<Configuration> getConfigurations(File configurationFile) throws ConfigCheckerException {
        XMLParser xmlParser = new XMLParser();
        List<Configuration> configurationList = new ArrayList<Configuration>();
        NodeList configurationNodes = null;
        NodeList configProperties = null;

        NodeList configurationNodeList = xmlParser.getAllNodes(configurationFile);
        try {
            for (int configurationNodeListIndex = 0; configurationNodeListIndex < configurationNodeList
                    .getLength(); configurationNodeListIndex++) {
                if (configurationNodeList.item(configurationNodeListIndex) != null &&
                        CONFIGURATIONS_TAG.equals(configurationNodeList
                                .item(configurationNodeListIndex).getNodeName())) {
                    configurationNodes = configurationNodeList
                            .item(configurationNodeListIndex).getChildNodes();
                }
            }

            for (int configIndex = 0; configIndex < configurationNodes.getLength(); configIndex++) {
                Configuration configuration = new Configuration();
                if (configurationNodes.item(configIndex) != null &&
                        !CONFIG_TAG.equals(configurationNodes.item(configIndex).getNodeName())) {
                    continue;
                }
                if (configurationNodes.item(configIndex).hasChildNodes()) {
                    configProperties = configurationNodes.item(configIndex).getChildNodes();
                }
                if (configProperties != null) {
                    for (int configPropertiesIndex = 0; configPropertiesIndex <
                            configProperties.getLength(); configPropertiesIndex++) {

                        if (configProperties.item(configPropertiesIndex).getNodeType() ==
                                Node.CDATA_SECTION_NODE) {
                            configuration.setConfigurationValue(configProperties.item(configPropertiesIndex)
                                    .getNodeValue());
                        }
                    }
                }
                configuration.setConfigurationId(getConfigId(configurationNodes.item(configIndex)));
                configurationList.add(configuration);
            }
        } catch (ConfigCheckerException e) {
            throw new ConfigCheckerException("Error occurred while reading from configuration file: " +
                    configurationFile.getPath(), e);
        }
        return configurationList;
    }

    /**
     * This method is used to read the child.xml.
     *
     * @param childPropertiesNodeList xml node list containing the child config properties
     * @param productPath             base path containing the product pack
     * @param applicationPath         base path for the reference config files defined in child.xml
     * @return a list of child properties objects
     * @throws ConfigCheckerException when invalid configurations found in the child.xml
     */
    public static List<ChildProperty> getChildProperties(NodeList childPropertiesNodeList, String productPath,
                                                         String applicationPath) throws ConfigCheckerException {
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
                    if (FILE_FORMAT_TAG.equals(filePropertyNode.getNodeName())
                            && filePropertyNode.getTextContent() != null) {
                        if (filePropertyNode.getTextContent().isEmpty()) {
                            throw new ConfigCheckerException("Empty value for the " +
                                    "format element in child.xml");
                        } else {
                            childConfigProperty.setFileFormat(filePropertyNode.getTextContent());
                        }
                    }
                    if (REFERENCE_PATH_TAG.equals(filePropertyNode.getNodeName())
                            && filePropertyNode.getTextContent() != null) {
                        if (filePropertyNode.getTextContent().isEmpty()) {
                            throw new ConfigCheckerException("Empty value for the " +
                                    "reference-path element in child.xml");
                        } else {
                            childConfigProperty.setRefChildConfigFilePath(applicationPath +
                                    filePropertyNode.getTextContent());
                        }
                    }
                    if (PRODUCT_PATH_TAG.equals(filePropertyNode.getNodeName())
                            && filePropertyNode.getTextContent() != null) {
                        if (filePropertyNode.getTextContent().isEmpty()) {
                            throw new ConfigCheckerException("Empty value for the" +
                                    " product-path element in child.xml");
                        } else {
                            childConfigProperty.setProductConfigFilePath(productPath +
                                    filePropertyNode.getTextContent());
                        }
                    }
                    if (EXCLUDE_CONFIGS_TAG.equals(filePropertyNode.getNodeName())
                            && filePropertyNode.hasChildNodes()) {
                        excludeConfigs = filePropertyNode.getChildNodes();
                        childConfigProperty.getExcludeList()
                                .addAll(getExcludeConfigs(excludeConfigs));
                    }
                }
                childPropertyList.add(childConfigProperty);
            }
        } catch (ConfigCheckerException e) {
            throw new ConfigCheckerException("Error occurred while reading from child.xml", e);
        }
        return childPropertyList;
    }
}
