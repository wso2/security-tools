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

package org.wso2.security.tools.product.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.product.manager.config.ProductManagerProperties;
import org.wso2.security.tools.product.manager.exception.NotificationManagerException;
import org.wso2.security.tools.product.manager.exception.ProductManagerException;
import org.wso2.security.tools.product.manager.handler.FileHandler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

/**
 * Methods to extract a zip file of wso2 product and run wso2server.sh asynchronously
 */
@SuppressWarnings({"unused"})
public class ProductManagerExecutor implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(ProductManagerExecutor.class);
    private String zipFileName;

    public ProductManagerExecutor(String zipFileName) {
        this.zipFileName = zipFileName;
    }

    /**
     * Extract the uploaded zip file and command to start server.
     */
    @Override
    public void run() {
        try {
            extractZipFileAndStartServer();
        } catch (NotificationManagerException | ProductManagerException e) {
            LOGGER.error(e.toString());
        }
    }

    private void extractZipFileAndStartServer() throws NotificationManagerException, ProductManagerException {
        String folderName;
        String productPath = ProductManagerProperties.getProductManagerProductPath();
        try {
            folderName = FileHandler.extractZipFile(productPath + File.separator + zipFileName);
            NotificationManager.notifyFileExtracted(true);
            LOGGER.info("File successfully extracted");
        } catch (IOException e) {
            NotificationManager.notifyFileExtracted(false);
            throw new ProductManagerException("Error occurred while extracting zip file");
        }
        FileHandler.findFile(new File(productPath + File.separator + folderName), ProductManagerProperties
                .getProductManagerWso2ServerFile());
        try {
            runShellScript(new String[]{"chmod", "+x", FileHandler.getWso2serverFileAbsolutePath()});
            Thread.sleep(1000);
            runShellScript(new String[]{FileHandler.getWso2serverFileAbsolutePath(), ProductManagerProperties
                    .getProductManagerPortArg() +
                    String.valueOf(ProductManagerProperties.getProductManagerPortOffset())});
            LOGGER.info("Successfully commanded to start server");
        } catch (IOException | InterruptedException e) {
            NotificationManager.notifyServerStarted(false);
            throw new ProductManagerException("Error occurred while executing sh file");
        }
    }

    private void runShellScript(String[] command) throws IOException {
        Runtime.getRuntime().exec(command);
    }

    private boolean hostAvailabilityCheck(String host, int port, int times) {
        int i = 0;
        while (i < times) {
            LOGGER.info("Checking host availability...");
            try (Socket s = new Socket(host, port)) {
                LOGGER.info(host + ":" + port + " is available");
                return true;
            } catch (IOException e) {
                LOGGER.error(e.toString());
                try {
                    Thread.sleep(5000);
                    i++;
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }
}
