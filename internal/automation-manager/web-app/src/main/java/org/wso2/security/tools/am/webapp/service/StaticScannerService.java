/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.security.tools.am.webapp.service;

import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.am.webapp.config.GlobalProperties;
import org.wso2.security.tools.am.webapp.entity.StaticScanner;
import org.wso2.security.tools.am.webapp.exception.AutomationManagerWebException;
import org.wso2.security.tools.am.webapp.handlers.MultipartRequestHandler;
import org.wso2.security.tools.am.webapp.handlers.TokenHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Service layer methods to handle static scanners
 */
@Service
public class StaticScannerService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * Send multiple requests to Automation Manager to start a static scans based on scan types
     *
     * @param staticScanner         Static scanner model attribute
     * @param sourceCodeUploadAsZip Whether the project source code is uploaded as a zip file. False means clone from
     *                              gitHub
     * @param zipFile               Zip file of the project source code
     * @param gitUrl                GitHub URL of the project source code
     * @throws AutomationManagerWebException The general exception for all the exceptions thrown by web app
     */
    public void sendMultipleStartScanRequests(StaticScanner staticScanner, boolean sourceCodeUploadAsZip,
                                              MultipartFile zipFile, String gitUrl) throws
            AutomationManagerWebException {
        String scanType;
        if (staticScanner.isFindSecBugs()) {
            scanType = GlobalProperties.getFsbType();
            startScan(staticScanner, sourceCodeUploadAsZip, zipFile, gitUrl, scanType);
        }
        if (staticScanner.isDependencyCheck()) {
            scanType = GlobalProperties.getDcType();
            startScan(staticScanner, sourceCodeUploadAsZip, zipFile, gitUrl, scanType);
        }
    }

    private void startScan(StaticScanner staticScanner, boolean sourceCodeUploadAsZip, MultipartFile zipFile, String
            gitUrl, String scanType) throws AutomationManagerWebException {
        String accessToken = TokenHandler.getAccessToken();
        int i = 0;
        while (i < 1) {
            try {
                validateRequest(sourceCodeUploadAsZip, zipFile, gitUrl);
                MultipartRequestHandler multipartRequest = sendRequestToStartScan(accessToken, staticScanner,
                        sourceCodeUploadAsZip, zipFile, gitUrl, scanType);
                if (multipartRequest.getResponseStatus() == HttpStatus.SC_OK) {
                    multipartRequest.finish();
                    break;
                }
                multipartRequest.finish();
                Thread.sleep(1000);
            } catch (URISyntaxException | IOException | InterruptedException e) {
                e.printStackTrace();
                TokenHandler.generateAccessToken();
                accessToken = TokenHandler.getAccessToken();
                i += 1;
            }
        }
    }

    private void validateRequest(boolean sourceCodeUploadAsZip, MultipartFile zipFile, String gitUrl) throws
            AutomationManagerWebException {
        if (sourceCodeUploadAsZip) {
            if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                throw new AutomationManagerWebException("Zip file required");
            }
        } else {
            if (gitUrl == null) {
                throw new AutomationManagerWebException("Please enter a URL to clone");
            }
        }
    }

    private MultipartRequestHandler sendRequestToStartScan(String accessToken, StaticScanner staticScanner,
                                                           boolean sourceCodeUploadAsZip, MultipartFile zipFile,
                                                           String gitUrl, String scanType) throws URISyntaxException,
            IOException {
        URI uri = (new URIBuilder()).setHost(GlobalProperties.getAutomationManagerHost()).setPort(GlobalProperties
                .getAutomationManagerPort()).setScheme("https").setPath(GlobalProperties.getStaticScannerStartScan())
                .build();
        String charset = "UTF-8";
        MultipartRequestHandler multipartRequest = new MultipartRequestHandler(uri.toString(), charset, accessToken);
        multipartRequest.addFormField("userId", staticScanner.getUserId());
        multipartRequest.addFormField("testName", staticScanner.getTestName());
        multipartRequest.addFormField("productName", staticScanner.getProductName());
        multipartRequest.addFormField("wumLevel", staticScanner.getWumLevel());
        multipartRequest.addFormField("sourceCodeUploadAsZip", String.valueOf(sourceCodeUploadAsZip));
        multipartRequest.addFormField("scanType", scanType);
        if (sourceCodeUploadAsZip) {
            multipartRequest.addFilePart("zipFile", zipFile.getInputStream(), zipFile.getOriginalFilename());
        } else {
            multipartRequest.addFormField("gitUrl", gitUrl);
        }
        LOGGER.info("SERVER REPLIED:");
        return multipartRequest;
    }
}
