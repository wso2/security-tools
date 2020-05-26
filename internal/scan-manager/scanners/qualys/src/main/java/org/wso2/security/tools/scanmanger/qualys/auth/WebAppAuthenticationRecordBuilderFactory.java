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

package org.wso2.security.tools.scanmanger.qualys.auth;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.security.tools.scanmanager.common.internal.model.ScannerScanRequest;
import org.wso2.security.tools.scanmanager.scanners.common.exception.InvalidRequestException;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.model.CallbackLog;
import org.wso2.security.tools.scanmanager.scanners.common.util.FileUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;

import java.util.List;

/**
 * Factory class to generate web web application authentication type objects.
 * This class is will get the type of the authentication type and based on that it will create the objects.
 */
public class WebAppAuthenticationRecordBuilderFactory {

    private static final Logger log = LogManager.getLogger(WebAppAuthenticationRecordBuilderFactory.class);

    /**
     * Create web application authentication objects based on given type. Currently it supports,
     * 01. NONE
     * 02. STANDARD AUTHENTICATION (Basic authentication)
     * 03. SELENIUM SCRIPT AUTHENTICATION
     *
     * @param scannerScanRequest Scan Request object which holds the scan related parameters
     * @return web application authentication type object
     * @throws InvalidRequestException error occurred due to invalid values for authentication type related parameters.
     * @throws ScannerException        error occurred while cancelling scan
     */
    public WebAppAuthenticationRecordBuilder getWebAppAuth(ScannerScanRequest scannerScanRequest)
            throws InvalidRequestException, ScannerException {
        String errorMessage;
        String webAppAuthType = scannerScanRequest.getPropertyMap().get(QualysScannerConstants.WEBAPP_AUTH_TYPE).get(0);
        String logMessage = null;
        // Check whether web app auth type is provided or not, if not provided, it will be set to NONE type.
        if (StringUtils.isEmpty(webAppAuthType)) {
            errorMessage = "Authentication type is not provided. Please select NONE/STANDARD/SELENIUM";
            throw new InvalidRequestException(errorMessage);
        }

        switch (webAppAuthType) {
        case QualysScannerConstants.NONE:
            logMessage = "NONE type is set for WebApp Authentication type";
            log.warn(new CallbackLog(scannerScanRequest.getJobId(), logMessage));
            return null;
        case QualysScannerConstants.STANDARD_AUTH:
            // If auth type is standard and credential is not provided by the user.
            if (!scannerScanRequest.getPropertyMap().containsKey(QualysScannerConstants.STANDARD_AUTH_USERNAME)
                    || !scannerScanRequest.getPropertyMap()
                    .containsKey(QualysScannerConstants.STANDARD_AUTH_PASSWORD)) {
                errorMessage = "Credential for STANDARD Authentication is not provided";
                throw new InvalidRequestException(errorMessage);
            } else {
                return new StandardAuthenticationRecordBuilder(scannerScanRequest.getPropertyMap().
                        get(QualysScannerConstants.STANDARD_AUTH_USERNAME).get(0).toCharArray(),
                        scannerScanRequest.getPropertyMap().
                                get(QualysScannerConstants.STANDARD_AUTH_PASSWORD).get(0).toCharArray());
            }
        case QualysScannerConstants.SELENIUM:
            if (scannerScanRequest.getFileMap().containsKey(QualysScannerConstants.AUTHENTICATION_SCRIPTS)) {
                List<String> authFiles = scannerScanRequest.getFileMap().
                        get(QualysScannerConstants.AUTHENTICATION_SCRIPTS);
                if (FileUtil.validateFileType(authFiles, QualysScannerConstants.XML)) {
                    log.info(new CallbackLog(scannerScanRequest.getJobId(),
                            "Authentication Script file type is validated"));
                } else {
                    errorMessage = "Invalid file type for Selenium Authentication Scripts";
                    log.info(new CallbackLog(scannerScanRequest.getJobId(), errorMessage));
                    throw new InvalidRequestException(errorMessage);
                }
                // If authentication script is provided, authentication status checker regex should be provided.
                if (scannerScanRequest.getPropertyMap().containsKey(QualysScannerConstants.AUTH_REGEX_KEYWORD)) {
                    errorMessage = "Authentication checker regex is not provided for authentication script";
                    throw new InvalidRequestException(errorMessage);
                } else {
                    SeleniumAuthenticationRecordBuilder authenticationRecordBuilder =
                            new SeleniumAuthenticationRecordBuilder(
                            scannerScanRequest.getPropertyMap().
                                    get(QualysScannerConstants.AUTH_REGEX_KEYWORD).get(0), authFiles.get(0),
                            scannerScanRequest.getJobId());
                    return authenticationRecordBuilder;
                }
            } else {
                logMessage = "Authentication script for the scan is not provided. Default authentication script will"
                        + " be used. ";
                log.info(new CallbackLog(scannerScanRequest.getJobId(), logMessage));
                return null;
            }
        default:
            return null;
        }
    }
}
