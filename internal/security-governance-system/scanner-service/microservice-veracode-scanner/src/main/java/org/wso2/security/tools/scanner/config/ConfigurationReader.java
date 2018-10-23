/*
 *  Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanner.config;

import org.wso2.security.tools.scanner.Constants;
import org.wso2.security.tools.scanner.exception.ScannerException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationReader {
    private static Properties properties;

    private ConfigurationReader() {
    }

    /**
     * Load configuration into JVM.
     *
     * @throws ScannerException
     */
    public static void loadConfiguration() throws ScannerException {
        String configuration_file = Constants.RESOURCE_FILE_PATH + File.separator +
                Constants.VERACODE_CONFIGURTION_FILE_NAME;

        loadConfiguration(configuration_file);
    }

    /**
     * Load the properties from the property file.
     *
     * @param configFileLocation PAth to configuration file
     * @throws ScannerException
     */
    private static void loadConfiguration(String configFileLocation) throws ScannerException {
        File file = new File(configFileLocation);

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                properties = new Properties();
                properties.load(reader);

            } catch (FileNotFoundException e) {
                throw new ScannerException("The configuration file was not found", e);
            } catch (IOException e) {
                throw new ScannerException("IOException was thrown while reading the properties file", e);
            }
        } else {
            throw new ScannerException("Veracode configuration file not found in: " + configFileLocation);
        }
    }

    /**
     * Reads the required property from the property file using the given key and returns the corresponding value.
     *
     * @param key The key mapping to the required value of the property file.
     * @return returns the value corresponding to the given key value.
     */
    public static String getConfigProperty(String key) {
        return properties.getProperty(key);
    }
}
