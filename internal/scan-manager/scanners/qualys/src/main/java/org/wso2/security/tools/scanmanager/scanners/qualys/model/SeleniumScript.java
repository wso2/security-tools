/*
 *
 *   Copyright (c) 2020, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.security.tools.scanmanager.scanners.qualys.model;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.model.CallbackLog;
import org.wso2.security.tools.scanmanager.scanners.common.util.ErrorProcessingUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.FileUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.config.QualysScannerConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Model class to represent Selenium Script Object.
 */
public abstract class SeleniumScript {

    private static final Logger log = LogManager.getLogger(SeleniumScript.class);

    // Script File
    File scriptFile;

    // Name of the script file
    String scriptFileName;

    public File getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(File scriptFile) {
        this.scriptFile = scriptFile;
    }

    public String getScriptFileName() {
        return scriptFileName;
    }

    public void setScriptFileName(String scriptFileName) {
        this.scriptFileName = scriptFileName;
    }

    /**
     * Download given authentication script from FTP Location.
     *
     * @param scriptFileLocation Authentication script file location
     * @param jobId              JobId
     * @throws ScannerException Error occurred while downloading authentication scripts
     */
    public void downloadAuthenticationScripts(String scriptFileLocation, String jobId) throws ScannerException {
        this.scriptFileName = scriptFileLocation.substring(scriptFileLocation.
                lastIndexOf(File.separator) + 1, scriptFileLocation.length());
        String scriptFilePath = scriptFileLocation.substring(0, scriptFileLocation.lastIndexOf(File.separator));

        this.scriptFile = new File(QualysScannerConfiguration.getInstance()
                .getConfigProperty(QualysScannerConstants.DEFAULT_FTP_SCRIPT_PATH) + File.separator + scriptFileName);
        try {
            String logMessage = scriptFileName + " Script is downloading ....";
            log.info(new CallbackLog(jobId, logMessage));
            FileUtil.downloadFromFtp(scriptFilePath, scriptFileName, scriptFile,
                    QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_USERNAME),
                    (QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_PASSWORD))
                            .toCharArray(),
                    QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_HOST),
                    Integer.parseInt(
                            QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_PORT)));
            logMessage = scriptFileName + " Script is downloaded";
            log.info(new CallbackLog(jobId, logMessage));
        } catch (IOException | JSchException | SftpException e) {
            String logMessage = "Error occurred while downloading the authentication script :  " + ErrorProcessingUtil
                    .getFullErrorMessage(e);
            throw new ScannerException(logMessage);
        }
    }
}
