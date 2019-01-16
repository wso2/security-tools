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

package org.wso2.security.tools.scanner.qualys.qualys_scanner.utils;

/**
 * Qualys scanner specific constants. {@link QualysScannerConstants}
 */
public class QualysScannerConstants implements ScannerConstants {

    //Qualys API URLs
    public static final String QUALYS_START_SCAN_API =
            "https://qualysapi.qg1.apps.qualys.in/qps/rest/3.0/launch/was/wasscan";
    public static final String QUALYS_CANCEL_SCAN_API =
            "https://qualysapi.qg1.apps.qualys.in/qps/rest/3.0/cancel/was/wasscan/";
    public static final String QUALYS_GET_STATUS_API =
            "https://qualysapi.qg1.apps.qualys.in/qps/rest/3.0/status/was/wasscan/";
    public static final String QUALYS_GET_REPORT_API =
            "https://qualysapi.qg1.apps.qualys.in/qps/rest/3.0/download/was/report/";
    public static final String QUALYS_GET_APPLICATION_API =
            "https://qualysapi.qg1.apps.qualys.in/qps/rest/3.0/search/was/webapp";

    //File paths
    public static final String QUALYS_APPLICATION_FILE_PATH = RESOURCE_FILE_PATH.concat("/qualysApplications.xml");

    public static final long QUALYS_SCANNER_CHECK_TIME = 15;

}
