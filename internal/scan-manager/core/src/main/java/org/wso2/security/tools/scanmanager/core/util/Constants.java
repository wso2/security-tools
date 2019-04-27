/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.security.tools.scanmanager.core.util;

/**
 * Defining the constants used by scan manager.
 */
public class Constants {

    public static final Object LOCK = new Object();

    public static final String SCAN_URL = "scanURL";
    public static final String SCAN_ARTIFACT = "scanArtifact";
    public static final String SCHEME = "http";

    public static final String CONTAINER_APP_LABEL_NAME = "appId";
    public static final String CONTAINER_SCANNER_LABEL_NAME = "scanner";
    public static final String CONTAINER_SCAN_JOB_ID_LABEL_NAME = "scanJobId";

    public static final String CONTAINER_ENV_NAME_SCAN_MANAGER_HOST = "SCAN_MANAGER_HOST";
    public static final String CONTAINER_ENV_NAME_SCAN_MANAGER_PORT = "SCAN_MANAGER_PORT";

    public static final Integer DEFAULT_SCAN_PAGE_SIZE = 10;
    public static final Integer DEFAULT_LOG_PAGE_SIZE = 10;
}
