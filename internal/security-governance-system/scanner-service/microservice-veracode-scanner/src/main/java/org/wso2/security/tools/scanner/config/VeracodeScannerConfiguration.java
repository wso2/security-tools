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

import com.veracode.apiwrapper.cli.VeracodeCommand;

/**
 * Veracode specific configuration class
 */
public class VeracodeScannerConfiguration extends ScannerConfiguration {

    private static volatile VeracodeScannerConfiguration instance;
    private VeracodeCommand.Options options;
    private String veracodeUsername;
    private String veracodePassword;
    private String outputFolderPath;
    private String gitUsername;
    private String gitPassword;
    private String outputFilePath;
    private String logFilePath;

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

    public String getGitUsername() {
        return gitUsername;
    }

    public void setGitUsername(String gitUsername) {
        this.gitUsername = gitUsername;
    }

    public String getGitPassword() {
        return gitPassword;
    }

    public void setGitPassword(String gitPassword) {
        this.gitPassword = gitPassword;
    }

    public String getVeracodeUsername() {
        return veracodeUsername;
    }

    public void setVeracodeUsername(String veracodeUsername) {
        this.veracodeUsername = veracodeUsername;
    }

    public String getVeracodePassword() {
        return veracodePassword;
    }

    public void setVeracodePassword(String veracodePassword) {
        this.veracodePassword = veracodePassword;
    }

    public String getOutputFolderPath() {
        return outputFolderPath;
    }

    public void setOutputFolderPath(String outputFolderPath) {
        this.outputFolderPath = outputFolderPath;
    }

    public String getOutputFilePath() {
        return outputFilePath;
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
}



