/*
 *
 *   Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.security.tools.scanner.dependency.js.utils;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.wso2.security.tools.scanner.dependency.js.constants.TicketCreatorConstants;
import org.wso2.security.tools.scanner.dependency.js.constants.JSScannerConstants;
import org.wso2.security.tools.scanner.dependency.js.exception.ConfigParserException;
import org.wso2.security.tools.scanner.dependency.js.exception.FileHandlerException;
import org.wso2.security.tools.scanner.dependency.js.model.GitUploaderProperties;
import org.wso2.security.tools.scanner.dependency.js.ticketcreator.TicketCreator;
import org.wso2.security.tools.scanner.dependency.js.ticketcreator.JIRATicketCreator;
import org.wso2.security.tools.scanner.dependency.js.ticketcreator.JIRARestClient;
import org.wso2.security.tools.scanner.dependency.js.model.Product;
import org.wso2.security.tools.scanner.dependency.js.preprocessor.AtuwaDownloader;
import org.wso2.security.tools.scanner.dependency.js.reportpublisher.GitUploader;
import org.wso2.security.tools.scanner.dependency.js.reportpublisher.ReportUploader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Util class to parse configuration properties. There are multiple configuration files related to each product and
 * credentials related configuration files. Product related configuration files are parsed during preprocessing stage
 * and credentials related config files are passed at the time where it is to be used.
 */
public class ConfigParser {

    private static final Logger log = Logger.getLogger(ConfigParser.class);
    private static Random random = new Random();

    private ConfigParser() {
    }

    /**
     * Parse the list of products which supports for weekly scanning.
     *
     * @return list of product names.
     * @throws ConfigParserException exception occurred while parsing configuration details.
     */
    public static List<String> parseProductList() throws ConfigParserException {
        List<String> products;
        Properties properties = new Properties();
        ClassLoader classLoader = ConfigParser.class.getClassLoader();
        //load a properties file from class path
        try (InputStream input = classLoader.getResourceAsStream(JSScannerConstants.PRODUCT_LIST_FILE)) {
            properties.load(input);
            products = Arrays.asList(properties.getProperty(JSScannerConstants.PRODUCTS).
                    split(JSScannerConstants.PROPERTIES_FILE_DELIMETER));
            log.info("[JS_SEC_DAILY_SCAN] Supported products are : " + String.join(",", products));
        } catch (IOException e) {
            throw new ConfigParserException("Error occurred in parsing productList config file : ", e);
        }
        return products;
    }


    /**
     * This method is responsible to parse properties regarding to upload scan reports into particular github
     * repository. THe following properties are read from config file.
     * 1.Username
     * 2.Password
     * 3.Repository url where the reports should be uploaded.
     *
     * @throws ConfigParserException exception occurred while parsing configuration details.
     */
    public static GitUploaderProperties parseGitUploaderConfigProperties() throws ConfigParserException {
        Properties properties = new Properties();
        ClassLoader classLoader = ConfigParser.class.getClassLoader();
        GitUploaderProperties gitUploaderProperties;
        try (InputStream input = classLoader.getResourceAsStream(JSScannerConstants.GIT_CONFIG_FILE)) {
            properties.load(input);
            gitUploaderProperties = new GitUploaderProperties(properties.getProperty(JSScannerConstants.USERNAME)
                    .toCharArray(), properties.getProperty(JSScannerConstants.PASSWORD).toCharArray(),
                    properties.getProperty(JSScannerConstants.SECURITY_ARTIFACT_REPO));
        } catch (IOException e) {
            throw new ConfigParserException("Error occurred in parsing github credentials : " + e);
        }
        return gitUploaderProperties;
    }

    /**
     * Parse github credential details (Access-token, Username, Password).
     *
     * @throws ConfigParserException exception occurred while parsing configuration details.
     */
    public static void parseGitAccessToken() throws ConfigParserException {

        Properties properties = new Properties();
        ClassLoader classLoader = ConfigParser.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(JSScannerConstants.GIT_ACCESS_TOKEN_CONFIG_FILE)) {
            properties.load(input);
            CommonApiInvoker.setGitToken(properties.getProperty(JSScannerConstants.ACCESSTOKEN).toCharArray());
        } catch (IOException e) {
            throw new ConfigParserException("Error occurred in parsing github credentials : ", e);
        }
    }

    /**
     * Parse jira credential details (Username, Password).
     *
     * @throws ConfigParserException exception occurred while parsing configuration details.
     */
    public static TicketCreator parseTicketCreatorCredentials() throws ConfigParserException {
        TicketCreator ticketCreatorAPI;
        Properties properties = new Properties();
        ClassLoader classLoader = ConfigParser.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(JSScannerConstants.TICKETCREATOR_CONFIG_FILE)) {
            properties.load(input);
            ticketCreatorAPI = new JIRATicketCreator(new JIRARestClient(), properties.getProperty
                    (JSScannerConstants.USERNAME).toCharArray(),
                    properties.getProperty(JSScannerConstants.PASSWORD).toCharArray(),
                    properties.getProperty(TicketCreatorConstants.WSO2_JIRA_BASE_URL));
            HashMap<String, String> ticketAssigneeMapper = new HashMap<>();
            //assignees for each products
            ticketAssigneeMapper.put(JSScannerConstants.AM, properties.getProperty(TicketCreatorConstants.APIM));
            ticketAssigneeMapper.put(JSScannerConstants.IDENTITYSERVER, properties.getProperty(
                    TicketCreatorConstants.IDENTITYSERVER));
            ticketAssigneeMapper.put(JSScannerConstants.INTEGRATION, properties.getProperty(TicketCreatorConstants
                    .INTEGRATION));
            ticketAssigneeMapper.put(JSScannerConstants.STREAMPROCESSOR, properties.getProperty(TicketCreatorConstants
                    .STREAMPROCESSOR));
            ticketAssigneeMapper.put(JSScannerConstants.OB, properties.getProperty(TicketCreatorConstants.OPENBANKING));
            ticketCreatorAPI.setAssigneeMapper(ticketAssigneeMapper);
        } catch (IOException e) {
            throw new ConfigParserException("Error occurred in parsing JIRA Credentials : ", e);
        }
        return ticketCreatorAPI;

    }

    /**
     * parse JIRA ticket information.
     *
     * @throws ConfigParserException exception occurred while parsing configuration details.
     */
    public static void parseJIRATicketInfo() throws ConfigParserException {
        Properties properties = new Properties();
        ClassLoader classLoader = ConfigParser.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(JSScannerConstants.JIRA_TICKET_INFO_FILE)) {
            properties.load(input);
            JIRATicketCreator.setTicketSubject(properties.getProperty(TicketCreatorConstants.TICKET_SUBJECT));
            JIRATicketCreator.setProjectKey(properties.getProperty(TicketCreatorConstants.PROJECT_KEY));
            JIRATicketCreator.setIssueLabel(properties.getProperty(TicketCreatorConstants.ISSUELABEL));
            JIRATicketCreator.setIssueType(properties.getProperty(TicketCreatorConstants.ISSUE_TYPE));
        } catch (IOException e) {
            throw new ConfigParserException("Error occurred in parsing JIRA Credentials : " + e);
        }

    }

    /**
     * Get atuwa URL. It reade the base url of atuwa in config file.
     *
     * @throws ConfigParserException exception occurred while parsing configuration details.
     */
    public static void parseAtuwaUrl() throws ConfigParserException {
        Properties properties = new Properties();
        ClassLoader classLoader = ConfigParser.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(JSScannerConstants.ATUWA_CONFIG)) {
            properties.load(input);
            AtuwaDownloader.setAtuwaBaseURL(properties.getProperty(JSScannerConstants.ATUWA_BASE_URL));
        } catch (IOException e) {
            throw new ConfigParserException("Error occurred in parsing Atuwa URL : " + e);
        }
    }

    /**
     * Parse each product configuration details.
     *
     * @param productName product name.
     * @return Product
     * @throws ConfigParserException exception occurred while parsing configuration details.
     */
    public static Product parseProductConfiguration(String productName) throws ConfigParserException {
        String fileName = null;
        if (productName.contains(JSScannerConstants.IDENTITYSERVER)) {
            fileName = JSScannerConstants.IS_CONFIG_FILE;
        } else if (productName.contains(JSScannerConstants.AM)) {
            fileName = JSScannerConstants.APIM_CONFIG_FILE;
        } else if (productName.contains(JSScannerConstants.INTEGRATION)) {
            fileName = JSScannerConstants.EI_CONFIG_FILE;
        } else if (productName.contains(JSScannerConstants.DAS)) {
            fileName = JSScannerConstants.DAS_CONFIG_FILE;
        } else if (productName.contains(JSScannerConstants.STREAMPROCESSOR)) {
            fileName = JSScannerConstants.SP_CONFIG_FILE;
        } else if (productName.contains(JSScannerConstants.OB)) {
            fileName = JSScannerConstants.OB_CONFIG_FILE;
        } else if (productName.contains(JSScannerConstants.APIM)) {
            fileName = JSScannerConstants.APIM_CONFIG_FILE;
        }
        Product productDto;
        Properties properties = new Properties();
        ClassLoader classLoader = ConfigParser.class.getClassLoader();
        //load a properties file from class path
        try (InputStream input = classLoader.getResourceAsStream(fileName)) {
            properties.load(input);
            productDto = new Product();
            productDto.setProductRepoName(properties.getProperty(JSScannerConstants.GIT_REPO_NAME));
            productDto.setInputSourceType(properties.getProperty(JSScannerConstants.INPUT_SOURCE_TYPE));
            List<String> componentList = Arrays.asList(properties.getProperty(JSScannerConstants.COMPONENT_REPO).
                    split(JSScannerConstants.PROPERTIES_FILE_DELIMETER));
            List<String> versionTagList = Arrays.asList(properties.getProperty(JSScannerConstants.VERSION_TAG_KEY_WORD).
                    split(JSScannerConstants.PROPERTIES_FILE_DELIMETER));
            HashMap<String, String> repoVersionMapper = new HashMap<>();
            for (int i = 0; i < componentList.size(); i++) {
                repoVersionMapper.put(componentList.get(i), versionTagList.get(i));
            }
            productDto.setRepoVersionMapper(repoVersionMapper);
        } catch (IOException e) {
            throw new ConfigParserException("Error occurred in parsing product configurations : ", e);
        }
        return productDto;
    }

    /**
     * Generate random number
     *
     * @return random number
     */
    public static int getRandomNumber() {
        //length of random string is the maximum and the 1 is our minimum
        return random.nextInt(JSScannerConstants.RANDOM_STRING.length());
    }

}
