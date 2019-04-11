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
package org.wso2.security.tools.configchecker;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.configchecker.config.ConfigCheckerSettings;
import org.wso2.security.tools.configchecker.exception.ConfigCheckerException;
import org.wso2.security.tools.configchecker.model.ChildProperty;
import org.wso2.security.tools.configchecker.model.ConfigNode;
import org.wso2.security.tools.configchecker.model.Configuration;
import org.wso2.security.tools.configchecker.model.ParentProperty;
import org.wso2.security.tools.configchecker.model.report.ConfigElement;
import org.wso2.security.tools.configchecker.model.report.Result;
import org.wso2.security.tools.configchecker.parser.ConfigurationParser;
import org.wso2.security.tools.configchecker.report.ReportGenerator;
import org.wso2.security.tools.configchecker.utils.Constants;
import org.wso2.security.tools.configchecker.utils.Utils;
import org.wso2.security.tools.configchecker.utils.XMLParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the main class responsible of accepting input arguments and initiating config validation.
 */
public class ConfigChecker {

    private static final Logger log = LoggerFactory.getLogger(ConfigChecker.class);

    @Parameter(names = {"-path"}, description = "Product Path", required = true, order = 1)
    private static String productPath;

    @Parameter(names = {"-out"}, description = "Output Report Generator format", required = true, order = 2)
    private static String reportOutFormat;

    @Parameter(names = {"--help", "-help", "-?"}, help = true, order = 3)
    private static boolean help;

    public static void main(String[] args) {
        ConfigChecker configChecker = new ConfigChecker();
        String applicationPath = Paths.get(Constants.PRESENT_DIRECTORY).toAbsolutePath().normalize().toString();

        try {
            Utils.loadConfigCheckerSettings();

            JCommander jCommander = new JCommander(configChecker);
            jCommander.parse(args);
            jCommander.setProgramName("Configuration Checker");
            if (help) {
                jCommander.usage();
                return;
            }

            log.info("CONFIGURATION CHECK started.");
            File childPropertiesFile = new File(applicationPath + File.separator + ConfigCheckerSettings
                    .getInstance().getChildPropertiesFile());
            File parentPropertiesFile = new File(applicationPath + File.separator + ConfigCheckerSettings
                    .getInstance().getParentPropertiesFile());

            HashMap<String, List<ConfigElement>> configCheckResultsMap = validateConfigurations(childPropertiesFile,
                    parentPropertiesFile, productPath, applicationPath);
            log.info("CONFIGURATION CHECK completed.");

            ReportGenerator reportGenerator = Utils.getConfigCheckerReportGenerator(reportOutFormat);
            reportGenerator.generateReport(configCheckResultsMap, applicationPath);
        } catch (ConfigCheckerException e) {
            log.error("Error occurred while running the configuration check", e);
        }
    }

    /**
     * This method is used to parse the configurations into ConfigNode objects and compare.
     *
     * @param refConfigFilePath
     * @param productConfigFilePath
     * @param fileFormat
     * @param excludeConfigs
     */
    private static List<ConfigElement> compareConfigFiles(String refConfigFilePath, String productConfigFilePath,
                                                          String fileFormat, List<String> excludeConfigs)
            throws ConfigCheckerException {
        List<ConfigElement> configElementsResultList = new ArrayList<>();

        try {
            File refConfigFile = Utils.readFile(refConfigFilePath);
            File productConfigFile = Utils.readFile(productConfigFilePath);

            Boolean isConfigAvailable = false;
            ConfigNode refConfigNode;
            ConfigNode productConfigNode;
            ConfigurationParser configurationParser;

            List<Configuration> refConfigList = Utils.getConfigurations(refConfigFile);
            FileInputStream productConfigFileStream = new FileInputStream(productConfigFile);
            String productConfigFileString = IOUtils.toString(productConfigFileStream, StandardCharsets.UTF_8);

            //change the parser according to the file format.
            configurationParser = Utils.getConfigurationParser(fileFormat);

            productConfigNode = configurationParser.parse(productConfigFileString);
            for (Configuration refConfiguration : refConfigList) {
                if (log.isDebugEnabled()) {
                    log.debug("Comparing the config " + refConfiguration.getConfigurationId());
                }
                if (excludeConfigs.contains(refConfiguration.getConfigurationId())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Excluding the config " + refConfiguration.getConfigurationId() + " from comparing");
                    }
                    continue;
                }
                refConfigNode = configurationParser.parse(refConfiguration.getConfigurationValue());
                if (refConfigNode != null && productConfigNode != null) {
                    isConfigAvailable = Utils.compareConfigNodeTree(refConfigNode, productConfigNode);
                }
                if (isConfigAvailable) {
                    configElementsResultList.add(new ConfigElement(refConfiguration.getConfigurationId(), Result.OK));
                } else {
                    configElementsResultList.add(new ConfigElement(refConfiguration.getConfigurationId(), Result.FAIL));
                }
            }
        } catch (IOException | ConfigCheckerException e) {
            throw new ConfigCheckerException("Error occurred while parsing the configurations from file: " +
                    productConfigFilePath, e);
        }
        return configElementsResultList;
    }

    /**
     * This method is used to compare all the parent configurations.
     *
     * @param parentPropertiesHashMap
     * @param excludedConfigFilePaths
     * @param configElementsResultMap
     * @param productPath
     * @return
     * @throws ConfigCheckerException
     */
    private static HashMap<String, List<ConfigElement>> compareAllParentConfigurations(HashMap<String,
            ParentProperty> parentPropertiesHashMap, List<String> excludedConfigFilePaths, HashMap<String,
            List<ConfigElement>> configElementsResultMap, String productPath) throws ConfigCheckerException {
        List<ConfigElement> parentConfigElementsResultList;
        HashMap<String, List<ConfigElement>> allConfigElementsResultMap = configElementsResultMap;

        if (parentPropertiesHashMap != null) {
            for (Map.Entry<String, ParentProperty> parentPropertyEntry : parentPropertiesHashMap.entrySet()) {
                String parentFileId = parentPropertyEntry.getKey();
                ParentProperty parentConfigProperty = parentPropertiesHashMap.get(parentFileId);

                if (parentConfigProperty.getFileFormat() != null &&
                        parentConfigProperty.getRefParentConfigFilePath() != null) {
                    List<String> productConfigFilePaths = Utils.searchConfigFiles(parentFileId, productPath);
                    for (String productConfigFilepath : productConfigFilePaths) {
                        if (log.isDebugEnabled()) {
                            log.debug("Validating the parent configurations in " + productConfigFilepath);
                        }
                        if (excludedConfigFilePaths.contains(productConfigFilepath)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Excluding " + productConfigFilepath +
                                        " from validating parent configurations");
                            }
                            continue;
                        }
                        parentConfigElementsResultList = compareConfigFiles(parentConfigProperty
                                        .getRefParentConfigFilePath(), productConfigFilepath,
                                parentConfigProperty.getFileFormat(), Collections.emptyList());
                        if (!parentConfigElementsResultList.isEmpty()) {
                            if (!configElementsResultMap.keySet().contains(productConfigFilepath)) {
                                allConfigElementsResultMap.put(productConfigFilepath, parentConfigElementsResultList);
                            } else {
                                allConfigElementsResultMap.get(productConfigFilepath)
                                        .addAll(parentConfigElementsResultList);
                            }
                        }
                    }
                }
            }
        }
        return allConfigElementsResultMap;
    }

    /**
     * This method is used to compare child configurations.
     *
     * @param childPropertyList
     * @param excludedConfigFilePaths
     * @param parentPropertiesHashMap
     * @param productPath
     * @return
     * @throws ConfigCheckerException
     */
    private static HashMap<String, List<ConfigElement>> compareChildAndParentConfigurations(
            List<ChildProperty> childPropertyList, List<String> excludedConfigFilePaths,
            HashMap<String, ParentProperty> parentPropertiesHashMap, String productPath)
            throws ConfigCheckerException {
        List<String> excludeListForParentConfig = new ArrayList<>();
        List<ConfigElement> childConfigElementsResultList;
        List<ConfigElement> parentConfigElementsResultList = new ArrayList<>();
        List<ConfigElement> mergedConfigElementsResultList;
        HashMap<String, List<ConfigElement>> configElementsResultMap = new HashMap<>();
        HashMap<String, List<ConfigElement>> allconfigElementsResultMap;

        try {
            for (ChildProperty childProperty : childPropertyList) {
                mergedConfigElementsResultList = new ArrayList<>();
                excludeListForParentConfig.addAll(childProperty.getExcludeList());
                if (!excludedConfigFilePaths.contains(childProperty.getProductConfigFilePath())) {
                    excludedConfigFilePaths.add(childProperty.getProductConfigFilePath());
                }

                //get the child configurations from the reference config file defined in child.xml
                List<Configuration> refConfigList = Utils.getConfigurations(Utils.readFile(childProperty
                        .getRefChildConfigFilePath()));
                for (Configuration refConfig : refConfigList) {
                    excludeListForParentConfig.add(refConfig.getConfigurationId());
                }
                if (log.isDebugEnabled()) {
                    log.debug("Validating the child configurations in " + childProperty.getProductConfigFilePath());
                }

                //compare child configurations
                //passing an empty list as the exclude configs list since there are no configurations to exclude
                //when checking the child configurations.
                childConfigElementsResultList = compareConfigFiles(childProperty.getRefChildConfigFilePath(),
                        childProperty.getProductConfigFilePath(),
                        childProperty.getFileFormat(), Collections.emptyList());

                ParentProperty parentConfigProperty = parentPropertiesHashMap
                        .get(childProperty.getChildFileType());
                if (log.isDebugEnabled()) {
                    log.debug("Validating the parent configurations in " + childProperty.getProductConfigFilePath());
                }

                //compare parent configurations
                if (parentConfigProperty != null) {
                    parentConfigElementsResultList =
                            compareConfigFiles(parentConfigProperty.getRefParentConfigFilePath(),
                                    childProperty.getProductConfigFilePath(), parentConfigProperty.getFileFormat(),
                                    excludeListForParentConfig);
                }
                mergedConfigElementsResultList.addAll(parentConfigElementsResultList);
                mergedConfigElementsResultList.addAll(childConfigElementsResultList);
                if (!mergedConfigElementsResultList.isEmpty()) {
                    if (!configElementsResultMap.keySet().contains(childProperty.getProductConfigFilePath())) {
                        configElementsResultMap.put(childProperty.getProductConfigFilePath(),
                                mergedConfigElementsResultList);
                    } else {
                        configElementsResultMap.get(childProperty.getProductConfigFilePath())
                                .addAll(mergedConfigElementsResultList);
                    }
                }
                parentConfigElementsResultList.clear();
                childConfigElementsResultList.clear();
                excludeListForParentConfig.clear();
            }

            //compare parent configurations of the files that are not defined in child.xml.
            allconfigElementsResultMap = compareAllParentConfigurations(parentPropertiesHashMap,
                    excludedConfigFilePaths, configElementsResultMap, productPath);
        } catch (IOException e) {
            throw new ConfigCheckerException("Error occurred while writing comparing child configurations.", e);
        }
        return allconfigElementsResultMap;
    }

    /**
     * This method is used to compare all the configuration files in the product.
     *
     * @param childPropertiesFile
     * @param parentPropertiesFile
     * @param productPath
     * @param applicationPath
     * @return
     * @throws ConfigCheckerException
     */
    private static HashMap<String, List<ConfigElement>> validateConfigurations(File childPropertiesFile,
                                                                               File parentPropertiesFile,
                                                                               String productPath,
                                                                               String applicationPath)
            throws ConfigCheckerException {
        List<String> excludedConfigFilePaths;
        List<ChildProperty> childPropertyList;
        HashMap<String, ParentProperty> parentPropertiesHashMap = null;
        HashMap<String, List<ConfigElement>> configElementsResultMap = new HashMap<>();
        XMLParser xmlParser = new XMLParser();

        if (parentPropertiesFile.exists()) {
            NodeList parentPropertiesNodeList = xmlParser.getAllNodes(parentPropertiesFile);
            if (log.isDebugEnabled()) {
                log.debug("Reading the properties parent.xml");
            }

            //read the parent.xml
            parentPropertiesHashMap = Utils.getParentProperties(parentPropertiesNodeList, applicationPath);
            if (parentPropertiesHashMap.isEmpty()) {
                throw new ConfigCheckerException("No properties found in the parent.xml");
            }
        }

        if (childPropertiesFile.exists()) {
            Document childPropertiesDocument = xmlParser.getXMLDocument(childPropertiesFile);
            if (log.isDebugEnabled()) {
                log.debug("Reading the properties child.xml");
            }

            //read the child.xml
            excludedConfigFilePaths = Utils.getExcludeProductFiles(childPropertiesDocument, productPath);
            childPropertyList = Utils.getChildProperties(childPropertiesDocument.getChildNodes(),
                    productPath, applicationPath);
            if (childPropertyList.isEmpty()) {
                throw new ConfigCheckerException("No properties found in the child.xml");
            }
            configElementsResultMap = compareChildAndParentConfigurations(childPropertyList,
                    excludedConfigFilePaths, parentPropertiesHashMap, productPath);
        } else {

            //compare parent configurations in all the config files if child configurations does not exist.
            configElementsResultMap = compareAllParentConfigurations(parentPropertiesHashMap,
                    Collections.emptyList(), configElementsResultMap, productPath);
        }
        return configElementsResultMap;
    }
}
