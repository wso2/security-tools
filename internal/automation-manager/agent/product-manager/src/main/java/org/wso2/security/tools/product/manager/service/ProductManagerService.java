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

package org.wso2.security.tools.product.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.product.manager.NotificationManager;
import org.wso2.security.tools.product.manager.ProductManagerExecutor;
import org.wso2.security.tools.product.manager.config.ProductManagerProperties;
import org.wso2.security.tools.product.manager.exception.NotificationManagerException;
import org.wso2.security.tools.product.manager.exception.ProductManagerException;
import org.wso2.security.tools.product.manager.handler.FileHandler;

import java.io.File;
import java.io.IOException;

/**
 * Service methods for the application
 */
@Service
public class ProductManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductManagerService.class);

    /**
     * Configure {@link NotificationManager}, validate the request, upload file to directory and start a separate
     * thread to start server
     *
     * @param automationManagerHost Automation Manager host
     * @param automationManagerPort Automation Manager port
     * @param containerId           Container id
     * @param zipFile               Zip file to be uploaded
     * @throws ProductManagerException
     */
    public void startServer(String automationManagerHost, int automationManagerPort, String containerId,
                            MultipartFile zipFile) throws ProductManagerException, NotificationManagerException {
        String zipFileName;
        File productFolder = new File(ProductManagerProperties.getProductManagerProductPath());
        if (!zipFile.getOriginalFilename().endsWith(".zip")) {
            throw new ProductManagerException("Please upload a zip file");
        }
        if (!configureNotificationManager(automationManagerHost, automationManagerPort, containerId)) {
            throw new ProductManagerException("Error occurred while configuring Notification Manager");
        }
        zipFileName = zipFile.getOriginalFilename();
        if (productFolder.exists() || productFolder.mkdir()) {
            String fileUploadPath = ProductManagerProperties.getProductManagerProductPath() + File.separator +
                    zipFile.getOriginalFilename();
            try {
                FileHandler.uploadFile(zipFile, fileUploadPath);
                LOGGER.info("File successfully uploaded");
                NotificationManager.notifyFileUploaded(true);
            } catch (IOException e) {
                NotificationManager.notifyFileUploaded(false);
                throw new ProductManagerException("Error occurred while uploading zip file");
            }
        }
        ProductManagerExecutor productManagerExecutor = new ProductManagerExecutor(zipFileName);
        new Thread(productManagerExecutor).start();
    }

    private boolean configureNotificationManager(String automationManagerHost, int automationManagerPort, String
            myContainerId) {
        NotificationManager.config(automationManagerHost, automationManagerPort, myContainerId);
        return NotificationManager.isConfigured();
    }
}
