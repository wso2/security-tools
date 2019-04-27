/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.core.config;

import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;

import java.util.Map;

import static org.wso2.security.tools.scanmanager.core.util.Constants.DEFAULT_LOG_PAGE_SIZE;
import static org.wso2.security.tools.scanmanager.core.util.Constants.DEFAULT_SCAN_PAGE_SIZE;

/**
 * Scan Manager configuration model class.
 */
public class ScanManagerConfiguration {

    private String scanManagerHost;
    private Integer scanManagerPort;
    private Integer scannerServicePort;
    private String scannerServiceHost;
    private Integer scanPageSize;
    private Integer logPageSize;

    private static final String SCAN_MANAGER_HOST_KEY = "scanManagerHost";
    private static final String SCAN_MANAGER_PORT_KEY = "scanManagerPort";
    private static final String SCANNER_SERVICE_HOST_KEY = "scannerServiceHost";
    private static final String SCANNER_SERVICE_PORT_KEY = "scannerServicePort";
    private static final String SCAN_PAGE_SIZE = "scanPageSize";
    private static final String LOG_PAGE_SIZE = "logPageSize";

    private static final ScanManagerConfiguration scanManagerConfiguration = new ScanManagerConfiguration();

    private ScanManagerConfiguration() {
    }

    public static ScanManagerConfiguration getInstance() {
        return scanManagerConfiguration;
    }

    /**
     * Initializing the configuration.
     *
     * @param configObjectMap configuration map read from the scan-manager-config.yaml
     * @throws ScanManagerException when the required configurations are not found
     */
    public void initScanConfiguration(Map<String, Object> configObjectMap) throws ScanManagerException {
        if (configObjectMap.get(SCAN_MANAGER_HOST_KEY) != null) {
            this.scanManagerHost = (String) configObjectMap.get(SCAN_MANAGER_HOST_KEY);
        } else {
            throw new ScanManagerException("Unable to find scan manager host configuration");
        }
        if (configObjectMap.get(SCAN_MANAGER_PORT_KEY) != null) {
            this.scanManagerPort = (Integer) configObjectMap.get(SCAN_MANAGER_PORT_KEY);
        } else {
            throw new ScanManagerException("Unable to find scan manager port configuration");
        }
        if (configObjectMap.get(SCANNER_SERVICE_HOST_KEY) != null) {
            this.scannerServiceHost = (String) configObjectMap.get(SCANNER_SERVICE_HOST_KEY);
        } else {
            throw new ScanManagerException("Unable to find scaner service host configuration");
        }
        if (configObjectMap.get(SCANNER_SERVICE_PORT_KEY) != null) {
            this.scannerServicePort = (Integer) configObjectMap.get(SCANNER_SERVICE_PORT_KEY);
        } else {
            throw new ScanManagerException("Unable to find scaner service port configuration");
        }

        // Not mandatory as there are default values.
        this.scanPageSize = (Integer) configObjectMap.get(SCAN_PAGE_SIZE);
        this.logPageSize = (Integer) configObjectMap.get(LOG_PAGE_SIZE);
    }

    public String getScanManagerHost() {
        return scanManagerHost;
    }

    public Integer getScanManagerPort() {
        return scanManagerPort;
    }

    public Integer getScannerServicePort() {
        return scannerServicePort;
    }

    public String getScannerServiceHost() {
        return scannerServiceHost;
    }

    public Integer getScanPageSize() {
        if (scanPageSize == null) {
            return DEFAULT_SCAN_PAGE_SIZE;
        }
        return scanPageSize;
    }

    public Integer getLogPageSize() {
        if (logPageSize == null) {
            return DEFAULT_LOG_PAGE_SIZE;
        }
        return logPageSize;
    }
}
