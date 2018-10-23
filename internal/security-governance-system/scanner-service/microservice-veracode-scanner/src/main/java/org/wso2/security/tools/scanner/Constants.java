/*
 *  Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.security.tools.scanner;

/**
 * Constants of the application
 */
public final class Constants {
    public static final String FIND_SEC_BUGS_REPORTS_FOLDER = "Find-Sec-Bugs-Reports";
    //Files
    public static final String ZIP_FILE_EXTENSION = ".zip";
    public static final String POM_FILE = "pom.xml";
    public static final String JAR_FILTER_PATTERN_FILE_PATH = "jar_filter_pattern_file_path";
    public static final String VERACODE_CONFIGURTION_FILE_NAME = "conf.properties";
    public static final String RESOURCE_FILE_PATH = "src/main/resources";

    //Symbols
    public static final String UNDERSCORE = "_";
    public static final String NULL_STRING = "";
    public static final String APP_ID = "app_id";
    public static final String PREFIX = "prefix";
    public static final String SUFFIX = "suffix";
    public static final String ZIP_FILE = "zipFile";
    public static final String GIT_URL = "gitUrl";
    public static final String SFTP = "sftp";
    public static final String WORK_DIRECTORY_SUFIX = "-work";

    public static final String MVN_BUILD_COMMAND = "clean install -Dmaven.test.skip=true";

    //Veracode configuration properties
    public static final String VERACODE_USERNAME = "vuser";
    public static final String VERACODE_PASSWORD = "vpassword";
    public static final String VERACODE_OUTPUT_FOLDER_PATH = "output_folderpath";
    public static final String VERACODE_OUTPUT_FILE_NAME = "output_filepath";
    public static final String VERACODE_LOG_FILE_PATH = "log_filepath";
    public static final String GIT_USERNAME = "git_username";
    public static final String GIT_PASSWORD = "git_password";
    public static final String SCANNER_BEAN_CLASS_NAME = "scanner_bean_class";
    public static final String DEFAULT_PRODUCT_PATH = "default_product_path";
    public static final String DEFAULT_GIT_PRODUCT_PATH = "default_git_product_path";
    public static final String REPORTS_FOLDER_PATH = "report_folder_path";
    public static final String DEFAULT_FTP_PRODUCT_PATH = "default_ftp_product_path";

    //ftp configuration
    public static final String FTP_USERNAME = "ftp_username";
    public static final String FTP_PASSWORD = "ftp_password";
    public static final String FTP_HOST = "ftp_host";
    public static final String FTP_PORT = "ftp_port";

    //maven configuration
    public static final String MVN_M2_HOME = "maven_m2_home";
}
