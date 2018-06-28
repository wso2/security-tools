/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.security.tool.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tool.exception.FeedbackToolException;
import org.wso2.security.tool.util.Constants;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Reads the properties from the properties file.
 */
public class GetProperties {

    private static final Logger log = LoggerFactory.getLogger(GetProperties.class);
    private Properties properties;

    /**
     * Constructor to read the properties file and load the properties.
     *
     * @throws FeedbackToolException If an Exception is thrown inside the method
     *                               implementation.
     */
    public GetProperties() throws FeedbackToolException {
        try (FileReader reader = new FileReader(Constants.PROPERTIES_FILE_PATH)) {
            properties = new Properties();
            properties.load(reader);
        } catch (FileNotFoundException e) {
            throw new FeedbackToolException("The configuration file was not found", e);
        } catch (IOException e) {
            throw new FeedbackToolException("IOException was thrown while reading the properties file", e);
        }
    }

    /**
     * Reads the required property from the property file using the given key and returns the corresponding value.
     *
     * @param key The key mapping to the required value of the property file.
     * @return returns the value corresponding to the given key value.
     */
    public String readProperty(String key) {
        return properties.getProperty(key);
    }
}
