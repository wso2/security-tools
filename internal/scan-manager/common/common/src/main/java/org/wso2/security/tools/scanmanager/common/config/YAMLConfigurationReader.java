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
package org.wso2.security.tools.scanmanager.common.config;

import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Class to get the configurations from the configuration file.
 */
public class YAMLConfigurationReader implements ConfigurationReader {

    private Map<String, Object> configObjectMap = null;

    /**
     * Load the properties from the property file.
     *
     * @param configFile configuration file
     * @throws IOException when the required file is not found
     */
    public void loadConfiguration(String configFile) throws IOException {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = new ClassPathResource(configFile).getInputStream()) {
            configObjectMap = yaml.load(inputStream);
        }
    }

    /**
     * Reads the required property from the property file using the given key and returns the corresponding value.
     *
     * @param key The key mapping to the required value of the property file.
     * @return returns the value corresponding to the given key value.
     */
    public String getConfigProperty(String key) {
        return String.valueOf(configObjectMap.get(key));
    }

    /**
     * Reads the required property from the property file using the given key and returns the corresponding value.
     *
     * @return returns the config object.
     */
    public Map getConfigs() {
        return configObjectMap;
    }
}
