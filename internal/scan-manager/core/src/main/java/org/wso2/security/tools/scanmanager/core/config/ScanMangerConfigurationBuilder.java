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
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * Scan manager configuration builder class.
 */
public class ScanMangerConfigurationBuilder {

    private static final String SCAN_MANAGER_CONFIG_FILE = "scan-manager-config.yaml";

    private ScanMangerConfigurationBuilder() {
    }

    /**
     * Reading the scan manager configuration from scanner-config.yaml.
     *
     * @return configuration map
     * @throws ScanManagerException when an error occurs when reading the scan manager config file
     */
    public static Map<String, Object> getConfiguration() throws ScanManagerException {
        Map<String, Object> configObjectMap = null;

        URL scanManagerConfigURL = ScanMangerConfigurationBuilder.class.getClassLoader()
                .getResource(SCAN_MANAGER_CONFIG_FILE);
        if (scanManagerConfigURL != null) {
            configObjectMap = getConfiguration(new File(scanManagerConfigURL.getFile()));
        } else {
            throw new ScanManagerException("Unable to find the scan manager configuration");
        }
        return configObjectMap;
    }

    /**
     * Reading the scan manager configuration from a given config file.
     *
     * @param configFile config file name
     * @return configuration map
     * @throws ScanManagerException when an error occurs while reading the config file
     */
    public static Map<String, Object> getConfiguration(File configFile) throws ScanManagerException {
        Yaml yaml = new Yaml();
        Map<String, Object> configObjectMap = null;

        try (InputStream inputStream = new FileInputStream(configFile)) {
            configObjectMap = yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new ScanManagerException("Unable to locate the file: " + configFile, e);
        } catch (IOException e) {
            throw new ScanManagerException("Error occurred while reading the config file: " + configFile, e);
        }
        return configObjectMap;
    }
}
