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
package org.wso2.security.tools.scanmanager.scanners.veracode;

/**
 * Veracode Scanner Constants of the application.
 */
public final class VeracodeScannerConstants {

    //Veracode configuration properties
    public static final String VERACODE_API_ID = "veracode_apiID";
    public static final String VERACODE_API_KEY = "veracode_apiKey";
    public static final String VERACODE_OUTPUT_FOLDER_PATH = "output_folderpath";
    public static final String VERACODE_OUTPUT_FILE_NAME = "output_filename";
    public static final String VERACODE_LOG_FILE_PATH = "log_filepath";
    public static final String SCANNER_BEAN_CLASS_NAME = "scanner_bean_class";
    public static final String DEFAULT_PRODUCT_PATH = "default_product_path";
    public static final String DEFAULT_GIT_PRODUCT_PATH = "default_git_product_path";
    public static final String JAR_FILTER_PATTERN_FILE_PATH = "jar_filter_pattern_file_path";
    public static final String PREFIX = "prefix";
    public static final String SUFFIX = "suffix";
    public static final String SCAN_ARTIFACT = "scanArtifact";
    public static final String SCAN_RESULT_WAITING_HOURS = "scan_result_waiting_interval_hours";
    public static final String SCAN_RESULT_RETRY_MINS = "scan_result_retry_interval_mins";
    public static final String SCAN_REPORT_UPLOAD_RETRY_SECONDS = "scan_report_upload_retry_interval_seconds";
    public static final String BUILD_ID_XPATH = "build_id_xpath";
    public static final String SCAN_STATUS_XPATH = "scan_status_xpath";
    public static final String BUILD_ID_ATTRIBUTE = "build_id_attribute";
    public static final String SCAN_STATUS_ATTRIBUTE = "scan_status_attribute";
    public static final String NAMESPACE = "scan_response_namespace";
    public static final String VERACODE = "veracode";
    public static final String WORK_DIRECTORY_SUFIX = "-work";
    public static final String SUMMARY = "_summary";
    public static final String THIRD_PARTY = "_third_party";

    // Veracode Scanner Status
    public static final String INCOMPLETE = "Incomplete";
    public static final String NOT_SUBMITTED_TO_ENGINE = "Not Submitted to Engine";
    public static final String SUBMITTED_TO_ENGINE = "Submitted to Engine";
    public static final String SCAN_ERRORS = "Scan Errors";
    public static final String SCAN_IN_PROCESS = "Scan In Process";
    public static final String SCAN_CANCELED = "Scan Canceled";
    public static final String RESULTS_READY = "Results Ready";
    public static final String PRESCAN_SUBMITTED = "Pre-Scan Submitted";
    public static final String PREFLIGHT_SUBMITTED = "Preflight Submitted";
    public static final String PRESCAN_FAILED = "Pre-Scan Failed";
    public static final String PRESCAN_SUCESS = "Pre-Scan Success";
    public static final String PREFLIGHT_SUCESS = "Preflight Success";
    public static final String NO_MODULES_DEFINED = "No Modules Defined";
    public static final String PRESCAN_CANCELLED = "Pre-Scan Cancelled";
    public static final String NO_BUILD = "Could not find a build for";
    public static final String NO_MODULES_SELECTED = "No modules selected";
    public static final String SCAN_ALL_MODULE = "scan_all_top_level_modules";
    public static final String PENDING_INTERVAL = "Pending Internal Review";
    public static final String JAR_FILTER_FILE = "jarFilter.xml";

    private VeracodeScannerConstants() {
    }

}
