/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.webapp.util;

/**
 * Constants class for scan manager webapp.
 */
public class Constants {

    private Constants() {
    }

    public static final String SCHEME = "http";
    public static final String SCAN_MANAGER_ENDPOINT = "scan-manager";
    public static final String SCAN_MANAGER_VIEW = "scan-manager";
    public static final String SCANNERS_VIEW_NAME = "scanners";
    public static final String SCAN_CONFIGURATION_VIEW = "scan_configuration";
    public static final String SCANS_VIEW = "scans";
    public static final String LOGS_VIEW = "logs";
    public static final String ERROR_PAGE_VIEW = "error-page";

    public static final String SCANS_URI = SCAN_MANAGER_ENDPOINT + "/scans";
    public static final String LOGS_URI = SCAN_MANAGER_ENDPOINT + "/logs";
    public static final String SCANNERS_URI = SCAN_MANAGER_ENDPOINT + "/scanners";

    public static final String URL_SEPARATOR = "/";
    public static final String DEFAULT_ERROR_PAGE_VIEW = "scan-manager/errorPage";

    public static final String FTP_SCAN_DATA_DIRECTORY_NAME = "scandata";
    public static final String SCAN_REPORT_DATA_DIRECTORY_NAME = "reports";

    public static final String PAGE_PARAM_NAME = "page";
    public static final String JOB_ID_PARAM_NAME = "jobId";
    public static final String PRE_JOB_ID_PREFIX = "pre_job_id_";
    public static final String FILES_BY_URL_SEPARATOR = "@";
    public static final String FILES_BY_URL_POSTFIX = "byURL";
}
