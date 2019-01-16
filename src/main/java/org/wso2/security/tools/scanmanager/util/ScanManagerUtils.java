/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.security.tools.scanmanager.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.scanmanager.exception.ScanManagerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import static org.wso2.security.tools.scanmanager.config.StartUpInit.scanManagerConfiguration;

/**
 * Model class to represent utilities.
 */
public class ScanManagerUtils {

    private static final Logger logger = Logger.getLogger(ScanManagerUtils.class);

    private static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String QUEUE_NAME_PREFIX = "queue.";
    private static final String CF_NAME = "qpidConnectionfactory";
    private static String carbonClientId = "carbon";
    private static String carbonVirtualHostName = "carbon";

    private static String getTCPConnectionURL(String username, String password) {
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        return new StringBuffer()
                .append("amqp://").append(username).append(":").append(password)
                .append("@").append(carbonClientId)
                .append("/").append(carbonVirtualHostName)
                .append("?brokerlist='tcp://").append(scanManagerConfiguration.getBrokerHostname())
                .append(":").append(scanManagerConfiguration.getBrokerPort()).append("'")
                .toString();
    }

    public static String generateScanId() {
        Random r = new Random();
        int low = 1000000;
        int high = 9999999;
        Integer result = r.nextInt(high - low) + low;
        return result.toString();
    }

    public static void uploadToFTP(MultipartFile file, String baseDirectory, String scanDirectory)
            throws ScanManagerException {
        FTPClient client = new FTPClient();
        FileInputStream fis = null;

        try {
            client.connect(scanManagerConfiguration.getFtpHost(), Integer
                    .parseInt(scanManagerConfiguration.getFtpPort()));
            client.login(scanManagerConfiguration.getFtpUsername(),
                    scanManagerConfiguration.getFtpPassword());
            if (client.isConnected()) {
                boolean directoryExists = client.changeWorkingDirectory(baseDirectory + File.separator +
                        scanDirectory);

                if (!directoryExists) {
                    client.makeDirectory(baseDirectory + File.separator + scanDirectory);
                    client.changeWorkingDirectory(baseDirectory + File.separator + scanDirectory);
                }

                // Create an InputStream of the file to be uploaded
                fis = (FileInputStream) file.getInputStream();
                client.setFileType(FTPClient.BINARY_FILE_TYPE);

                // Store file to FTP server
                client.storeFile(file.getOriginalFilename(), fis);
            } else {
                throw new ScanManagerException("Unable to connect to the FTP server");
            }
        } catch (IOException e) {
            throw new ScanManagerException("Error occurred while uploading the file " +
                    file.getOriginalFilename() + " to FTP server", e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                client.logout();
                client.disconnect();
            } catch (IOException e) {
                logger.error("Error occurred while closing the connection to the FTP host");
            }
        }
    }
}
