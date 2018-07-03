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
 *
 */
package org.wso2.security.tools.advisorytool.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.wso2.security.tools.advisorytool.exeption.AdvisoryToolException;
import org.wso2.security.tools.advisorytool.utils.Constants;

import java.io.File;
import java.io.IOException;

/**
 * Configurations builder to read advisory tool configurations.
 */
public class ConfigurationBuilder {

    private static volatile ConfigurationBuilder instance;

    public static ConfigurationBuilder getInstance() {
        if (instance == null) {
            synchronized (ConfigurationBuilder.class) {
                if (instance == null) {
                    instance = new ConfigurationBuilder();
                }
            }
        }
        return instance;
    }

    private ConfigurationBuilder() {
    }

    /**
     * Loading configurations from the security-advisory-tools.yaml
     * @return
     * @throws AdvisoryToolException
     */
    public Configuration getConfiguration() throws AdvisoryToolException {
        String advisoryToolConfigFile = "conf" + File.separator + Constants.CONFIGURATION_FILE_NAME;

        return getConfiguration(advisoryToolConfigFile);
    }

    /**
     * Load configurations from a given configuration file.
     * @param configFileLocation
     * @return
     * @throws AdvisoryToolException
     */
    public Configuration getConfiguration(String configFileLocation) throws AdvisoryToolException {
        Configuration configuration = null;
        File file = new File(configFileLocation);
        if (file.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                configuration = mapper.readValue(
                        file, Configuration.class);
            } catch (IOException e) {
                throw new AdvisoryToolException(
                        "Error while loading " + configFileLocation + " configuration file", e);
            }
        } else {
            throw new AdvisoryToolException("Advisory tool configuration file not found in: "
                    + configFileLocation);
        }
        return configuration;
    }
}
