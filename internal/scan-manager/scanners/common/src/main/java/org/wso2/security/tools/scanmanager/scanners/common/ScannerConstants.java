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
 */

package org.wso2.security.tools.scanmanager.scanners.common;

/**
 * Class to represent the Scanner constant.
 */
public final class ScannerConstants {

    public static final String ZIP_FILE_EXTENSION = ".zip";
    public static final String POM_FILE = "pom.xml";
    public static final String PDF_FILE_EXTENSION = ".pdf";
    public static final String XML_FILE_EXTENSION = ".xml";
    public static final String CALLBACK_RETRY_INCREASE_SECONDS = "callback_retry_interval_seconds";
    public static final String CONFIGURTION_FILE_NAME = "scanner-config.yaml";
    public static final String RESOURCE_FILE_PATH = "src/main/resources";
    public static final String DEFAULT_FTP_PRODUCT_PATH = "default_product_path";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    // FTP configuration.
    public static final String FTP_USERNAME = "ftp_username";
    public static final String FTP_PASSWORD = "ftp_password";
    public static final String FTP_HOST = "ftp_host";
    public static final String FTP_PORT = "ftp_port";
    public static final String SFTP = "sftp";

    // Scan manager config.
    public static final String SCAN_MANAGER_CALLBACK_URL_ENDPOINT = "scan_manager_callback_url_endpoint";
    public static final String SCAN_MANAGER_CALLBACK_STATUS = "scan_manager_callback_status";
    public static final String SCAN_MANAGER_CALLBACK_LOG = "scan_manager_callback_log";
    public static final String SCAN_MANAGER_HOST = "SCAN_MANAGER_HOST";
    public static final String SCAN_MANAGER_PORT = "SCAN_MANAGER_PORT";
    public static final String HTTP_PROTOCOL = "http://";

    // Log types.
    public static final String INFO = "info";
    public static final String WARN = "warn";
    public static final String DEBUG = "debug";
    public static final String ERROR = "error";

    // Scan reports related constants.
    public static final String CONTENT_DISPOSITION_PATTERN = "attachment;\\s*filename\\s*=\\s*\"([^\"]*)\"";
    public static final String CONTENT_DISPOSITION = "content-disposition";

    private ScannerConstants() {
    }
}
