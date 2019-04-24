/*
 *
 *   Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanmanger.scanners.qualys;

/**
 * Constants related to Qualys Scanner
 */
public class QualysScannerConstants {

    //Constants related to Qualys configurations
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String HOST = "host";
    public static final String REPORT_PATH = "reportPath";
    public static final String SCANNER_BEAN_CLASS = "scanner_bean_class";


    //Qualys API Endpoint
    public static final String QUALYS_START_SCAN_API = "/qps/rest/3.0/launch/was/wasscan";
    public static final String QUALYS_PURGE_SCAN_API = "/qps/rest/3.0/purge/was/webapp/";
    public static final String QUALYS_CANCEL_SCAN_API = "/qps/rest/3.0/cancel/was/wasscan/";
    public static final String QUALYS_GET_STATUS_API = "/qps/rest/3.0/status/was/wasscan/";
    //    public static final String QUALYS_GET_REPORT_API = "/qps/rest/3.0/download/was/report/";
    public static final String QUALYS_GET_APPLICATION_API = "/qps/rest/3.0/search/was/webapp/";
    public static final String QUALYS_GET_AUTHENTICATION_SCRIPT_API = "/qps/rest/3.0/search/was/webappauthrecord/";
    public static final String QUALYS_GET_OPTIONAL_PROFILE_API = "/qps/rest/3.0/search/was/optionprofile/";
    public static final String QUALYS_ADD_AUTH_SCRIPT_API = "/qps/rest/3.0/create/was/webappauthrecord";
    public static final String QUALYS_WEB_UPDATE_API = "/qps/rest/3.0/update/was/webapp/";
    public static final String QUALYS_WEB_APP_REPORT_CREATE_API = "/qps/rest/3.0/create/was/report";
    public static final String QUALYS_REPORT_DOWNLOAD_API = "/qps/rest/3.0/download/was/report/";


    public static final String QUALYS_SCAN_NAME_PREFIX = "New Discovery scan launch from API : ";
    public static final String INITIAL_DELAY = "initialDelay";
    public static final String SCHEDULER_DELAY = "schedulerDelay";

    //Qualys Tag Names
    public static final String WEBAPPS_TAG_NAME = "webapps";
    public static final String QUALYS_WEBAPP_TAG_NAME = "WebApp";
    public static final String QUALYS_OPTIONAL_PROFILE_TAG_NAME = "OptionProfile";
    public static final String NAME_KEYWORD = "name";
    public static final String ID_KEYWORD = "id";
    public static final String TYPE_KEYWORD = "type";
    public static final String SCANNER_APPILIANCE_TYPE_KEYWORD = "scannerApplianceType";
    public static final String PROFILE_NAME_KEYWORD = "profileName";
    public static final String PROGRESSIVE_SCAN = "isProgressiveScanningEnabled";
    public static final String ENABLED = "ENABLED";
    public static final String DISABLED = "DISABLED";
    public static final String EMAIL = "EMAIL";
    public static final String AUTHENTICATION_SCRIPTS = "authenticationScripts";
    public static final String CRAWLINGSCRIPTS = "crawlingScripts";
    public static final String SERVICE_REQUEST = "ServiceRequest";
    public static final String DATA = "data";
    public static final String WEB_APP_AUTH_RECORD = "WebAppAuthRecord";
    public static final String FORM_RECORD = "formRecord";
    public static final String SELENIUM = "SELENIUM";
    public static final String SELENIUM_SCRIPT = "seleniumScript";
    public static final String REGEX = "regex";
    public static final String AUTH_RECORDS = "authRecords";
    public static final String ADD = "add";
    public static final String WAS_SCAN = "WasScan";
    public static final String TARGET = "target";
    public static final String SCANNER_APPILIANCE = "scannerAppliance";
    public static final String PROFILE = "profile";
    public static final String PROFILE_ID = "profileId";
    public static final String INTEGER_REGEX = "[0-9]+";
    public static final String PROGRESSIVE_SCANNING = "progressiveScanning";
    public static final String XML = ".xml";
    public static final String REPORT = "Report";
    public static final String FORMAT = "format";
    public static final String DESCRIPTION = "description";
    public static final String WAS_APP_REPORT = "WAS_WEBAPP_REPORT";
    public static final String CONFIG_KEYWORD = "config";
    public static final String WEB_APP_REPORT_KEYWORD = "webAppReport";

    public static final String SCAN_STATUS_TAG = "status";
    public static final String AUTH_STATUS_TAG = "authStatus";
    public static final String RESULTS_STATUS_TAG = "resultsStatus";

    //Report Types
    public static final String PDF_TYPE = "PDF";
    public static final String HTML_BASE64_TYPE = "HTML_BASE64";
    public static final String CSV_V2_TYPE = "CSV_V2";
    public static final String XML_TYPE = "XML";

    //Qualys Scanner Status
    public static final String SUBMITTED = "SUBMITTED";
    public static final String RUNNING = "RUNNING";
    public static final String FINISHED = "FINISHED";
    public static final String TIME_LIMIT_EXCEEDED = "TIME_LIMIT_EXCEEDED";
    public static final String SCAN_NOT_LAUNCHED = "SCAN_NOT_LAUNCHED";
    public static final String SCANNER_NOT_AVAILABLE = "SCANNER_NOT_AVAILABLE";
    public static final String ERROR = "ERROR";
    public static final String CANCELLED = "CANCELLED";
    //Qualys auth status
    public static final String AUTH_SUCCESSFUL = "SUCCESSFUL";
    public static final String AUTH_FAILED = "FAILED";
    public static final String AUTH_PARTIAL = "PARTIAL";
    //Qualys Results Status
    public static final String NO_HOST_ALIVE = "NO_HOST_ALIVE";
    public static final String NO_WEB_SERVICE = "NO_WEB_SERVICE";
    public static final String SCAN_RESULTS_INVALID = "SCAN_RESULTS_INVALID";
    public static final String SERVICE_ERROR = "SERVICE_ERROR";
    public static final String SCAN_INTERNAL_ERROR = "SCAN_INTERNAL_ERROR";
    public static final String SUCCESSFUL = "SUCCESSFUL";
}
