/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.security.tools.configchecker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.wso2.security.tools.configchecker.exception.ConfigCheckerException;

import java.io.File;
import java.io.IOException;

/**
 * Configuration builder to read config checker settings.
 */
public class ConfigCheckerSettingsBuilder {

    private static final String CONFIG_CHECKER_SETTINGS_FILE = "config-checker-settings.yaml";

    /**
     * Reading the config checker settings from config-checker-settings.yaml.
     *
     * @return
     * @throws ConfigCheckerException
     */
    public static ConfigCheckerSettings getConfiguration() throws ConfigCheckerException {
        ConfigCheckerSettings configCheckerSettings = null;
        configCheckerSettings = getConfiguration(new File(CONFIG_CHECKER_SETTINGS_FILE));
        return configCheckerSettings;
    }

    /**
     * Reading the config checker settings from a given config file.
     *
     * @param configFile
     * @return
     * @throws ConfigCheckerException
     */
    public static ConfigCheckerSettings getConfiguration(File configFile) throws ConfigCheckerException {
        ConfigCheckerSettings configuration = null;
        if (configFile.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                configuration = mapper.readValue(
                        configFile, ConfigCheckerSettings.class);
            } catch (IOException e) {
                throw new ConfigCheckerException("Unable to read the configuration file " + configFile, e);
            }
        }
        return configuration;
    }
}
