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
 *
 *
 */

package org.wso2.security.tools.scanmanager.scanners.veracode.config;

import org.wso2.security.tools.scanmanager.scanners.common.config.Configuration;
import org.wso2.security.tools.scanmanager.scanners.common.config.ScannerConfiguration;

/**
 * Veracode specific configuration class.
 */
public class VeracodeScannerConfiguration extends ScannerConfiguration {
    private static volatile VeracodeScannerConfiguration instance;
    private String outputFolderPath;
    private String outputFilePath;
    private String logFilePath;
    private char[] apiKey;
    private String apiID;

    private VeracodeScannerConfiguration() {
    }

    public static VeracodeScannerConfiguration getInstance() {
        if (instance == null) {
            synchronized (Configuration.class) {
                if (instance == null) {
                    instance = new VeracodeScannerConfiguration();
                }
            }
        }
        return instance;
    }

    public void zeroingVeracodeApiKeyInMemory() {
        for (int i = 0; i < apiKey.length - 1; i++) {
            apiKey[i] = '\0';
        }
    }

    public String getOutputFolderPath() {
        return outputFolderPath;
    }

    public void setOutputFolderPath(String outputFolderPath) {
        this.outputFolderPath = outputFolderPath;
    }

    public String getOutputFilePath() {
        return this.outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getApiID() {
        return apiID;
    }

    public void setApiID(String apiID) {
        this.apiID = apiID;
    }

    public char[] getApiKey() {
        return apiKey;
    }

    public void setApiKey(char[] apiKey) {
        this.apiKey = apiKey;
    }

}
