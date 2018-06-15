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

package org.wso2.security.tools.scanner.dependency.js.constants;

/**
 * Constants related to JSSecurityScanner
 */
public class JSScannerConstants {

    //constants related to npm
    public static final String NPM = "npm";
    public static final String INSTALL = "install";

    //constants for suffix of supported products
    public static final String IDENTITYSERVER = "wso2is";
    public static final String APIM = "wso2apim";
    public static final String AM = "wso2am";
    public static final String INTEGRATION = "wso2ei";
    public static final String STREAMPROCESSOR = "wso2sp";
    public static final String DAS = "wso2das";
    public static final String OB = "wso2-ob-solution";

    //constants related to config files
    public static final String GIT_CONFIG_FILE = "githubuploaderconfig.properties";
    public static final String GIT_ACCESS_TOKEN_CONFIG_FILE = "gitaccesstoken.properties";
    public static final String TICKETCREATOR_CONFIG_FILE = "ticketcreatorconfig.properties";
    public static final String PRODUCT_LIST_FILE = "productlist.properties";
    public static final String APIM_CONFIG_FILE = "apimconfig.properties";
    public static final String EI_CONFIG_FILE = "eiconfig.properties";
    public static final String DAS_CONFIG_FILE = "dasconfig.properties";
    public static final String SP_CONFIG_FILE = "spconfig.properties";
    public static final String IS_CONFIG_FILE = "iamconfig.properties";
    public static final String JIRA_TICKET_INFO_FILE = "jiraticketinfo.properties";
    public static final String OB_CONFIG_FILE = "obconfig.properties";
    public static final String ATUWA_CONFIG = "atuwa.properties";
    public static final String PROPERTIES_FILE_DELIMETER = ",";

    //Config files attributes
    public static final String PRODUCTS = "products";
    public static final String ACCESSTOKEN = "accessToken";
    public static final String USERNAME = "userName";
    public static final String PASSWORD = "password";
    public static final String SECURITY_ARTIFACT_REPO = "githubrepourl";
    public static final String GIT_REPO_NAME = "gitRepoName";
    public static final String INPUT_SOURCE_TYPE = "inputSourceType";
    public static final String VERSION_TAG_KEY_WORD = "componentVersionTag";
    public static final String COMPONENT_REPO = "componentRepo";

    //constants related to git api url
    public static final String GIT_BASE_URL = "https://api.github.com/repos/wso2/";
    public static final String GIT_SEARCH_URL = "https://api.github.com/search/code?q=filename:";
    public static final String POM = "pom.xml";
    public static final String PACKAGE_JSON = "package.json";
    public static final String RELEASES = "/releases";
    public static final String RAW_GIT_BASE_URL = "https://raw.githubusercontent.com/";
    public static final String ASSETS = "assets";
    public static final String NAME = "name";
    public static final String DOWNLOAD_URL = "browser_download_url";
    public static final String ATUWA_BASE_URL = "atuwabaseurl";
    public static final String OB_INDEX_FILE = "open-banking-solutions-packs.index";
    //constants related to directory
    public static final String PRODUCT_HOME = System.getProperty("user.home") + "/products";
    public static final String SECURITY_ARTIFACT_HOME = System.getProperty("user.home") + "/securityArtifact";
    public static final String WEEKLY_RELEASE = "weeklyRelease";
    public static final String GA_RELEASE = "GARelease";
    public static final String SCN_REPORT_DIRECTORY_PATH = "/RetireJSLibraryScanReports";
//            "/dependency-analysis/internal/retirejs/scan-reports";

    //constants related to github credentials
    public static final String GIT = "git";
    public static final String ATUWA = "atuwa";

    public static final String ZIP_PREFIX = ".zip";
    public static final String JAR_PREFIX = ".jar";
    public static final String WAR_PREFIX = ".war";

    public static final int FILE_SUFFIX_LENGTH = 4;
    public static final String VERSION_REGEX = "[-]\\d+[.]\\d+[.]\\d+[-]";
    public static final String POM_VERSION_REGEX = "[0-9]+[.][0-9]+[.][0-9]+";
    public static final String GA_RELEASE_VERSION_REGEX = "[-]\\d+[.]\\d+[.]\\d+[.]";

    //Random String
    public static final String RANDOM_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

}
