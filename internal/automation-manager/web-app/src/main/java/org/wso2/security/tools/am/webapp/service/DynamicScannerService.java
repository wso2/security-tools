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
import org.wso2.security.tools.am.webapp.entity.DynamicScanner;
import org.wso2.security.tools.am.webapp.exception.AutomationManagerWebException;
import org.wso2.security.tools.am.webapp.handlers.MultipartRequestHandler;
import org.wso2.security.tools.am.webapp.handlers.TokenHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Service layer methods to handle dynamic scanners.
 */
@Service
public class DynamicScannerService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * Send multiple requests to Automation Manager to start a dynamic scans based on scan types
     *
     * @param dynamicScanner     Dynamic scanner model attribute
     * @param urlListFile        URL list file
     * @param productUploadAsZip Whether the product is uploaded as a zip file. False means wso2 server is in running
     *                           state
     * @param zipFile            Zip file of the product
     * @param wso2ServerHost     WSO2 server host. If the server is in running status
     * @param wso2ServerPort     WSO2 server host. If the server is in running status
     * @throws AutomationManagerWebException The general exception for all the exceptions thrown by web app
     */
    public void sendMultipleStartScanRequests(DynamicScanner dynamicScanner, MultipartFile urlListFile, boolean
            productUploadAsZip, MultipartFile zipFile, String wso2ServerHost, int wso2ServerPort) throws
            AutomationManagerWebException {
        String scanType;
        if (dynamicScanner.isZap()) {
            scanType = GlobalProperties.getZapType();
            startScan(dynamicScanner, urlListFile, productUploadAsZip, zipFile, wso2ServerHost, wso2ServerPort,
                    scanType);
        }
    }

    private void startScan(DynamicScanner dynamicScanner, MultipartFile urlListFile, boolean productUploadAsZip,
                           MultipartFile zipFile, String wso2ServerHost, int wso2ServerPort, String scanType) throws
            AutomationManagerWebException {
        String accessToken = TokenHandler.getAccessToken();
        int i = 0;
        while (i < 5) {
            try {
                validateRequest(productUploadAsZip, zipFile, wso2ServerHost, wso2ServerPort);
                MultipartRequestHandler multipartRequest = sendRequestToStartScan(accessToken, dynamicScanner,
                        urlListFile, productUploadAsZip, zipFile, wso2ServerHost, wso2ServerPort, scanType);
                if (multipartRequest.getResponseStatus() == HttpStatus.SC_OK) {
                    break;
                }
                Thread.sleep(1000);
            } catch (URISyntaxException | IOException | InterruptedException e) {
                e.printStackTrace();
                TokenHandler.generateAccessToken();
                accessToken = TokenHandler.getAccessToken();
                i += 1;
            }
        }
    }

    private void validateRequest(boolean productUploadAsZipFile, MultipartFile zipFile,
                                 String wso2ServerHost, int wso2ServerPort) throws AutomationManagerWebException {
        if (productUploadAsZipFile) {
            if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                throw new AutomationManagerWebException("Zip file required");
            }
        } else {
            if (wso2ServerHost == null || wso2ServerPort == -1) {
                throw new AutomationManagerWebException("WSO2 host details are missing");
            }
        }
    }

    private MultipartRequestHandler sendRequestToStartScan(String accessToken, DynamicScanner dynamicScanner,
                                                           MultipartFile urlListFile, boolean productUploadAsZip,
                                                           MultipartFile zipFile, String wso2ServerHost, int
                                                                   wso2ServerPort, String scanType) throws
            URISyntaxException, IOException {
        URI uri = (new URIBuilder()).setHost(GlobalProperties.getAutomationManagerHost()).setPort(GlobalProperties
                .getAutomationManagerPort()).setScheme("https").setPath(GlobalProperties.getDynamicScannerStartScan())
                .build();
        String charset = "UTF-8";
        MultipartRequestHandler multipartRequest = new MultipartRequestHandler(uri.toString(), charset, accessToken);
        multipartRequest.addFormField("userId", dynamicScanner.getUserId());
        multipartRequest.addFormField("testName", dynamicScanner.getTestName());
        multipartRequest.addFormField("productName", dynamicScanner.getProductName());
        multipartRequest.addFormField("wumLevel", dynamicScanner.getWumLevel());
        multipartRequest.addFormField("productUploadAsZip", String.valueOf(productUploadAsZip));
        multipartRequest.addFilePart("urlListFile", urlListFile.getInputStream(), urlListFile.getOriginalFilename());
        multipartRequest.addFormField("scanType", scanType);
        if (productUploadAsZip) {
            multipartRequest.addFilePart("zipFile", zipFile.getInputStream(), zipFile.getOriginalFilename());
        } else {
            multipartRequest.addFormField("wso2ServerHost", wso2ServerHost);
            multipartRequest.addFormField("wso2ServerPort", String.valueOf(wso2ServerPort));
        }
        multipartRequest.finish();
        LOGGER.info("SERVER REPLIED:");
        return multipartRequest;
    }
}
