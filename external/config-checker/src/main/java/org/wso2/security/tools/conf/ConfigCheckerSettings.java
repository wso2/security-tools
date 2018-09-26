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

package org.wso2.security.tools.conf;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class to store Config Checker settings.
 */
public class ConfigCheckerSettings {

    private Map<String, String> parsers = new HashMap<>();
    private Map<String, String> reportGenerators = new HashMap<>();
    private String parentPropertiesFile;
    private String childPropertiesFile;

    private ConfigCheckerSettings() {
    }

    private static volatile ConfigCheckerSettings instance;

    public static ConfigCheckerSettings getInstance() {
        if (instance == null) {
            synchronized (ConfigCheckerSettings.class) {
                if (instance == null) {
                    instance = new ConfigCheckerSettings();
                }
            }
        }
        return instance;
    }

    public Map<String, String> getParsers() {
        return parsers;
    }

    public void setParsers(Map<String, String> parsers) {
        this.parsers = parsers;
    }

    public Map<String, String> getReportGenerators() {
        return reportGenerators;
    }

    public void setReportGenerators(Map<String, String> reportGenerators) {
        this.reportGenerators = reportGenerators;
    }

    public String getParentPropertiesFile() {
        return parentPropertiesFile;
    }

    public void setParentPropertiesFile(String parentPropertiesFile) {
        this.parentPropertiesFile = parentPropertiesFile;
    }

    public String getChildPropertiesFile() {
        return childPropertiesFile;
    }

    public void setChildPropertiesFile(String childPropertiesFile) {
        this.childPropertiesFile = childPropertiesFile;
    }
}
