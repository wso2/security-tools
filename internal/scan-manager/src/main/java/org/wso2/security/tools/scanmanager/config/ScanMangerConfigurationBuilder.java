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

package org.wso2.security.tools.scanmanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.wso2.security.tools.scanmanager.exception.ScanManagerException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Scan manager configuration builder class.
 */
public class ScanMangerConfigurationBuilder {

    private static final String SCAN_MANAGER_CONFIG_FILE = "scan-manager-config.yaml";

    /**
     * Reading the scan manager configuration from scanner-config.yaml.
     *
     * @return configurations of scan manager.
     * @throws ScanManagerException
     */
    public static ScanManagerConfiguration getConfiguration() throws ScanManagerException {
        ScanManagerConfiguration scanManagerConfiguration = null;

        URL scanManagerConfigURL = ScanManagerConfiguration.class.getClassLoader()
                .getResource(SCAN_MANAGER_CONFIG_FILE);
        if (scanManagerConfigURL != null) {
            scanManagerConfiguration = getConfiguration(new File(scanManagerConfigURL.getFile()));
        } else {
            throw new ScanManagerException("Unable to find the scan manager configuration");
        }
        return scanManagerConfiguration;
    }

    /**
     * Reading the scan manager configuration from a given config file.
     *
     * @param configFile
     * @return configurations of scan manager.
     * @throws ScanManagerException.
     */
    public static ScanManagerConfiguration getConfiguration(File configFile) throws ScanManagerException {
        ScanManagerConfiguration configuration = null;

        if (configFile.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                configuration = mapper.readValue(
                        configFile, ScanManagerConfiguration.class);
            } catch (IOException e) {
                throw new ScanManagerException("Unable to read the configuration file "
                        + configFile + e.getMessage(), e);
            }
        }
        return configuration;
    }
}
