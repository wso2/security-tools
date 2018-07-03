/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.security.tools.advisorytool;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.wso2.security.tools.advisorytool.builders.SecurityAdvisoryBuilder;
import org.wso2.security.tools.advisorytool.builders.SecurityAdvisoryDirector;
import org.wso2.security.tools.advisorytool.config.Configuration;
import org.wso2.security.tools.advisorytool.config.ConfigurationBuilder;
import org.wso2.security.tools.advisorytool.data.ProductDataHolder;
import org.wso2.security.tools.advisorytool.data.SecurityAdvisoryDataHolder;
import org.wso2.security.tools.advisorytool.exeption.AdvisoryToolException;
import org.wso2.security.tools.advisorytool.model.Extension;
import org.wso2.security.tools.advisorytool.model.Product;
import org.wso2.security.tools.advisorytool.model.SecurityAdvisory;
import org.wso2.security.tools.advisorytool.model.SecurityAdvisoryData;
import org.wso2.security.tools.advisorytool.model.Version;
import org.wso2.security.tools.advisorytool.output.SecurityAdvisoryOutputGenerator;
import org.wso2.security.tools.advisorytool.utils.Constants;

import java.io.File;
import java.io.IOException;

/**
 * Security Advisory Tool
 */
public class AdvisoryTool {
    private static final Logger logger = Logger.getLogger(AdvisoryTool.class);

    @Parameter(names = {"-type"}, description = "Security Advisory type", required = true, order = 1)
    private static String advisoryType;

    @Parameter(names = {"-out"}, description = "Required output format", required = true, order = 2)
    private static String advisoryOutFormat;

    @Parameter(names = {"--help", "-help", "-?"}, help = true, order = 3)
    private static boolean help;

    public static void main(String[] args) {
        AdvisoryTool advisoryTool = new AdvisoryTool();
        SecurityAdvisoryBuilder builder = null;
        SecurityAdvisoryOutputGenerator securityAdvisoryOutputGenerator = null;
        SecurityAdvisoryDirector securityAdvisoryDirector = new SecurityAdvisoryDirector();

        try {
            logger.info("-------------------------------------------------");
            logger.info("-----                                       -----");
            logger.info("-----      Security Advisory Generator      -----");
            logger.info("-----                                       -----");
            logger.info("-------------------------------------------------");

            //parsing the main arguments.
            JCommander jCommander = new JCommander(advisoryTool);
            jCommander.parse(args);
            jCommander.setProgramName("Security Advisory Generator");

            if (help) {
                jCommander.usage();
                return;
            }

            loadConfiguration();
            loadReleasedProductsList(Constants.RELEASED_PRODUCTS_FILE_PATH);
            loadSecurityAdvisoryListFromFile();

            //get the appropriate security advisory builder according to the given advisory type.
            builder = getSecurityAdvisoryBuilder(advisoryType);

            //get the appropriate security advisory output generator according to the given output format.
            securityAdvisoryOutputGenerator = getSecurityAdvisoryOutputGenerator(advisoryOutFormat);

            //reading the input security advisory list along with user defined data
            SecurityAdvisoryData securityAdvisoryData = SecurityAdvisoryDataHolder.getInstance()
                    .getSecurityAdvisoryData();

            for (SecurityAdvisory securityAdvisory : securityAdvisoryData.getAdvisories()) {

                securityAdvisoryDirector.createSecurityAdvisory(builder, securityAdvisory,
                        securityAdvisoryOutputGenerator);

            }
        } catch (ParameterException e) {
            logger.error("Error occurred while parsing the tool parameters", e);
        } catch (AdvisoryToolException e) {
            logger.error("Error occurred while generating the security advisories", e);
        }
    }

    /**
     * Get the appropriate security advisory builder according to the given advisory type.
     *
     * @param advisoryType
     * @return
     */
    private static SecurityAdvisoryBuilder getSecurityAdvisoryBuilder(String advisoryType)
            throws AdvisoryToolException {
        SecurityAdvisoryBuilder securityAdvisoryBuilder = null;

        for (Extension advisoryBuilder : Configuration.getInstance().getAdvisoryBuilders()) {
            try {
                if (advisoryType.equals(advisoryBuilder.getId())) {
                    securityAdvisoryBuilder = (SecurityAdvisoryBuilder) Class
                            .forName(advisoryBuilder.getClassName()).newInstance();
                }
            } catch (ClassNotFoundException e) {
                throw new AdvisoryToolException("Unable to find the class " + advisoryBuilder
                        .getClassName(), e);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new AdvisoryToolException("Unable to create an instance of the class "
                        + advisoryBuilder.getClassName(), e);
            }
        }
        if (securityAdvisoryBuilder == null) {
            throw new AdvisoryToolException("unable to find an output generator for the given advisory type "
                    + advisoryType);
        }

        return securityAdvisoryBuilder;
    }

    /**
     * Get the appropriate security advisory output generator according to the given output format
     *
     * @param advisoryOutFormat
     * @return
     * @throws AdvisoryToolException
     */
    private static SecurityAdvisoryOutputGenerator getSecurityAdvisoryOutputGenerator(String advisoryOutFormat)
            throws AdvisoryToolException {
        SecurityAdvisoryOutputGenerator securityAdvisoryOutputGenerator = null;

        for (Extension outputGenerator : Configuration.getInstance().getOutputGenerators()) {
            try {
                if (advisoryOutFormat.equals(outputGenerator.getId())) {
                    securityAdvisoryOutputGenerator = (SecurityAdvisoryOutputGenerator) Class
                            .forName(outputGenerator.getClassName()).newInstance();
                }
            } catch (ClassNotFoundException e) {
                throw new AdvisoryToolException("Unable to find the class " + outputGenerator.getClassName(), e);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new AdvisoryToolException("Unable to create an instance of the class " + outputGenerator.getClassName(), e);
            }
        }
        if (securityAdvisoryOutputGenerator == null) {
            throw new AdvisoryToolException("unable to find an output generator for the given advisory format " + advisoryOutFormat);
        }
        return securityAdvisoryOutputGenerator;
    }

    /**
     * Loading the released products list.
     *
     * @param releasedProductFilePath
     * @throws AdvisoryToolException
     */
    private static void loadReleasedProductsList(String releasedProductFilePath) throws AdvisoryToolException {
        ProductDataHolder releasedProductDataHolder = null;
        File file = new File(releasedProductFilePath);
        if (file.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                releasedProductDataHolder = mapper.readValue(
                        file, ProductDataHolder.class);

            } catch (IOException e) {
                throw new AdvisoryToolException(
                        "Error while loading " + releasedProductFilePath + " products file", e);
            }

            for (Product product : releasedProductDataHolder.getProductList()) {

                if (StringUtils.isEmpty(product.getName())) {
                    throw new AdvisoryToolException("Missing product name in Products list");
                }

                if (StringUtils.isEmpty(product.getCodeName())) {
                    throw new AdvisoryToolException("Missing product codename in Products list");
                }

                for (Version version : product.getVersionList()) {
                    if (StringUtils.isEmpty(version.getVersionNumber())) {
                        throw new AdvisoryToolException("Missing version number for the product "
                                + product.getName() + "in Products list");
                    }

                    if (StringUtils.isEmpty(version.getKernelVersionNumber())) {
                        throw new AdvisoryToolException("Missing kernel version number for " +
                                "the product" + product.getName() + "in Products list");
                    }

                    if (StringUtils.isEmpty(version.getPlatformVersionNumber())) {
                        throw new AdvisoryToolException("Missing platform version number for " +
                                " the product" + product.getName() + "in Products list");
                    }

                    if (StringUtils.isEmpty(version.getReleasedDate().toString())) {
                        throw new AdvisoryToolException("Missing released date for the product "
                                + product.getName() + "in Products list");
                    }
                }
            }

        } else {
            throw new AdvisoryToolException("Products list file not found in: " + releasedProductFilePath);
        }

        ProductDataHolder.getInstance().setProductList(releasedProductDataHolder.getProductList());
    }

    /**
     * Reading the security-advisory-data.yaml
     *
     * @throws AdvisoryToolException
     */
    private static void loadSecurityAdvisoryListFromFile() throws AdvisoryToolException {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            SecurityAdvisoryData securityAdvisoryData = mapper.readValue(
                    new File(Constants.SECURITY_ADVISORY_DATA_FILE), SecurityAdvisoryData.class);

            for (SecurityAdvisory securityAdvisory : securityAdvisoryData.getAdvisories()) {
                if (StringUtils.isEmpty(securityAdvisory.getName())) {
                    throw new AdvisoryToolException("Security Advisory name cannot be empty.");
                }

                if (StringUtils.isEmpty(securityAdvisory.getTitle())) {
                    if (StringUtils.isEmpty(securityAdvisoryData.getTitle())) {
                       // throw new AdvisoryToolException("Security Advisory title cannot be empty");
                    } else {
                        securityAdvisory.setTitle(securityAdvisoryData.getTitle());
                    }
                }

                if (StringUtils.isEmpty(securityAdvisory.getThanks())) {
                    if (StringUtils.isEmpty(securityAdvisoryData.getThanks())) {
                        throw new AdvisoryToolException("Security Advisory thanks cannot be empty");
                    } else {
                        securityAdvisory.setThanks(securityAdvisoryData.getThanks());
                    }
                }

                if (StringUtils.isEmpty(securityAdvisory.getDate())) {
                    if (StringUtils.isEmpty(securityAdvisoryData.getDate())) {
                        throw new AdvisoryToolException("Security Advisory date cannot be empty");
                    } else {
                        securityAdvisory.setDate(securityAdvisoryData.getDate());
                    }
                }

                if (StringUtils.isEmpty(securityAdvisory.getDescription())) {
                    securityAdvisory.setDescription(securityAdvisoryData.getDescription());
                }

                if (StringUtils.isEmpty(securityAdvisory.getOverview())) {
                    securityAdvisory.setOverview(securityAdvisoryData.getOverview());
                }

                if (StringUtils.isEmpty(securityAdvisory.getImpact())) {
                    securityAdvisory.setImpact(securityAdvisoryData.getImpact());
                }

                if (StringUtils.isEmpty(securityAdvisory.getCredits())) {
                    securityAdvisory.setCredits(securityAdvisoryData.getCredits());
                }

                if (StringUtils.isEmpty(securityAdvisory.getSolution())) {
                    securityAdvisory.setSolution(securityAdvisoryData.getSolution());
                }

                if (StringUtils.isEmpty(securityAdvisory.getPublicDisclosure())) {
                    securityAdvisory.setPublicDisclosure(securityAdvisoryData.getPublicDisclosure());
                }

                if (StringUtils.isEmpty(securityAdvisory.getNotes())) {
                    securityAdvisory.setNotes(securityAdvisoryData.getNotes());
                }

                if (StringUtils.isEmpty(securityAdvisory.getScore())) {
                    securityAdvisory.setScore(securityAdvisoryData.getScore());
                }

                if (StringUtils.isEmpty(securityAdvisory.getSeverity())) {
                    securityAdvisory.setSeverity(securityAdvisoryData.getSeverity());
                }
            }
            SecurityAdvisoryDataHolder.getInstance().setSecurityAdvisoryData(securityAdvisoryData);

        } catch (IOException e) {
            throw new AdvisoryToolException("Error occurred while reading " + Constants.SECURITY_ADVISORY_DATA_FILE, e);
        }
    }

    /**
     * Reading the advisory tool configuration
     *
     * @throws AdvisoryToolException
     */
    private static void loadConfiguration() throws AdvisoryToolException {
        Configuration configuration = ConfigurationBuilder.getInstance().getConfiguration();

        if (!StringUtils.isEmpty(configuration.getPatchListAPI())) {
            Configuration.getInstance().setPatchListAPI(configuration.getPatchListAPI());
            if (logger.isDebugEnabled()) {
                logger.debug("Patch List API : " + configuration.getPatchListAPI());
            }
        } else {
            throw new AdvisoryToolException("Patch List API URL cannot be empty");
        }

        if (!StringUtils.isEmpty(configuration.getPatchDetailsAPI())) {
            Configuration.getInstance().setPatchDetailsAPI(configuration.getPatchDetailsAPI());
            if (logger.isDebugEnabled()) {
                logger.debug("Patch Detail API : " + configuration.getPatchDetailsAPI());
            }
        } else {
            throw new AdvisoryToolException("Patch Details API URL cannot be empty");
        }

        if (!StringUtils.isEmpty(configuration.getAdvisoryDetailsAPI())) {
            Configuration.getInstance().setAdvisoryDetailsAPI(configuration.getAdvisoryDetailsAPI());
            if (logger.isDebugEnabled()) {
                logger.debug("Advisory Detail API : " + configuration.getAdvisoryDetailsAPI());
            }
        } else {
            throw new AdvisoryToolException("Advisory Details API URL cannot be empty");
        }

        if (configuration.getPatchSupportPeriod() > 0) {
            Configuration.getInstance().setPatchSupportPeriod(configuration.getPatchSupportPeriod());
            if (logger.isDebugEnabled()) {
                logger.debug("Patch Support Period : " + configuration.getPatchSupportPeriod());
            }
        } else {
            throw new AdvisoryToolException("Patch Support Period cannot be empty");
        }

        if (configuration.getPatchListAPIUsername() != null) {
            Configuration.getInstance().setPatchListAPIUsername(configuration.getPatchListAPIUsername());
        } else {
            throw new AdvisoryToolException("Patch List API username cannot be empty");
        }

        if (configuration.getPatchListAPIPassword() != null) {
            Configuration.getInstance().setPatchListAPIPassword(configuration.getPatchListAPIPassword());
        } else {
            throw new AdvisoryToolException("Patch List API password cannot be empty");
        }

        if (!StringUtils.isEmpty(configuration.getAdvisoryDetailsAPIAuthToken())) {
            Configuration.getInstance().setAdvisoryDetailsAPIAuthToken(configuration.getAdvisoryDetailsAPIAuthToken());
        } else {
            throw new AdvisoryToolException("Advisory Details API Token cannot be empty");
        }

        if (!StringUtils.isEmpty(configuration.getPatchDetailsAPIAuthToken())) {
            Configuration.getInstance().setPatchDetailsAPIAuthToken(configuration.getPatchDetailsAPIAuthToken());
        } else {
            throw new AdvisoryToolException("Patch Details API Token cannot be empty");
        }

        if (!StringUtils.isEmpty(configuration.getPatchZIPCustomerLocation())) {
            Configuration.getInstance().setPatchZIPCustomerLocation(configuration.getPatchZIPCustomerLocation());
            if (logger.isDebugEnabled()) {
                logger.debug("Customer patch zip location : " + configuration.getPatchZIPCustomerLocation());
            }
        }

        if (!StringUtils.isEmpty(configuration.getPatchZIPPublicLocation())) {
            Configuration.getInstance().setPatchZIPCustomerLocation(configuration.getPatchZIPPublicLocation());
            if (logger.isDebugEnabled()) {
                logger.debug("Public patch zip location : " + configuration.getPatchZIPPublicLocation());
            }
        }

        if (configuration.getPlatforms() != null) {
            Configuration.getInstance().setPlatforms(configuration.getPlatforms());
        } else {
            throw new AdvisoryToolException("Platform list cannot be empty");
        }

        if (configuration.getAdvisoryBuilders() != null) {
            Configuration.getInstance().setAdvisoryBuilders(configuration.getAdvisoryBuilders());
        } else {
            throw new AdvisoryToolException("Advisory builder list cannot be empty");
        }

        if (configuration.getOutputGenerators() != null) {
            Configuration.getInstance().setOutputGenerators(configuration.getOutputGenerators());
        } else {
            throw new AdvisoryToolException("Advisory output generator list cannot be empty");
        }
    }
}
