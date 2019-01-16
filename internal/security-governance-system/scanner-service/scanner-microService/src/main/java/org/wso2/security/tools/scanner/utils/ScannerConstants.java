/*
 *  Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanner.utils;

/**
 * Common scanner specific constants. {@link ScannerConstants}
 */
public interface ScannerConstants {

    String ZIP_FILE_EXTENSION = ".zip";
    String PDF_FILE_EXTENSION = ".pdf";
    String POM_FILE = "pom.xml";
    String JAR_FILTER_PATTERN_FILE_PATH = "jar_filter_pattern_file_path";
    String CONFIGURTION_FILE_NAME = "conf.properties";
    String RESOURCE_FILE_PATH = "src/main/resources";
    String OUTPUT_FILE_NAME = "output_filepath";

    // ftp configuration
    String FTP_USERNAME = "ftp_username";
    String FTP_PASSWORD = "ftp_password";
    String FTP_HOST = "ftp_host";
    String FTP_PORT = "ftp_port";
    String SFTP = "sftp";

    String SCANNER_BEAN_CLASS_NAME = "scanner_bean_class";
    String DEFAULT_PRODUCT_PATH = "default_product_path";
    String DEFAULT_GIT_PRODUCT_PATH = "default_git_product_path";
    String REPORTS_FOLDER_PATH = "report_folder_path";
    String DEFAULT_FTP_PRODUCT_PATH = "default_ftp_product_path";

    // maven
    String MVN_M2_HOME = "maven_m2_home";
    String MVN_BUILD_COMMAND = "clean install -Dmaven.test.skip=true";

    // git
    String GIT_USERNAME = "git_username";
    String GIT_PASSWORD = "git_password";
}
