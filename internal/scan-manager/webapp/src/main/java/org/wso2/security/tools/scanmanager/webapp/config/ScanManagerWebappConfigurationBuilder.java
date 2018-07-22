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
package org.wso2.security.tools.scanmanager.webapp.config;

import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * Scan Manager webapp configuration builder class.
 */
public class ScanManagerWebappConfigurationBuilder {

    private static final String SCAN_MANAGER_CONFIG_FILE = "scan-manager-webapp-config.yaml";

    private ScanManagerWebappConfigurationBuilder() {
    }

    /**
     * Reading the scan manager webapp configuration from scan-manager-webapp-config.yaml
     *
     * @return
     * @throws ScanManagerWebappException
     */
    public static Map<String, Object> getConfiguration() throws ScanManagerWebappException {
        Map<String, Object> configObjectMap = null;

        URL configURL = ScanManagerWebappConfiguration.class.getClassLoader()
                .getResource(SCAN_MANAGER_CONFIG_FILE);
        if (configURL != null) {
            configObjectMap = getConfiguration(new File(configURL.getFile()));
        } else {
            throw new ScanManagerWebappException("Unable to find the scan manager webapp configuration");
        }
        return configObjectMap;
    }

    /**
     * Reading the scan manager webapp configuration from a given config file.
     *
     * @param configFile
     * @return
     * @throws ScanManagerWebappException
     */
    public static Map<String, Object> getConfiguration(File configFile) throws ScanManagerWebappException {
        Yaml yaml = new Yaml();
        Map<String, Object> configObjectMap = null;

        try (InputStream inputStream = new FileInputStream(configFile)) {
            configObjectMap = yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new ScanManagerWebappException("Unable to locate the file: " + configFile, e);
        } catch (IOException e) {
            throw new ScanManagerWebappException("Error occurred while reading the config file: " + configFile, e);
        }
        return configObjectMap;
    }
}
