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

package org.wso2.security.tools.scanmanager.api.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.scanmanager.api.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.api.model.ScanRequest;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import static org.wso2.security.tools.scanmanager.config.StartUpInit.scanManagerConfiguration;

public class ScanManagerUtils {

    private static final Logger logger = Logger.getLogger(ScanManagerUtils.class);

    private static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String QUEUE_NAME_PREFIX = "queue.";
    private static final String CF_NAME = "qpidConnectionfactory";
    private static String CARBON_CLIENT_ID = "carbon";
    private static String CARBON_VIRTUAL_HOST_NAME = "carbon";

    public static void addToQueue(String queueName, ScanRequest scanRequest) {
        QueueConnection queueConnection = null;
        QueueSession queueSession = null;
        javax.jms.QueueSender queueSender = null;
        Properties properties = new Properties();

        try {
            properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
            properties.put(CF_NAME_PREFIX + CF_NAME, getTCPConnectionURL(scanManagerConfiguration
                    .getQueueUsername(), scanManagerConfiguration.getQueuePassword()));
            properties.put(QUEUE_NAME_PREFIX + queueName, queueName);
            InitialContext ctx = new InitialContext(properties);
            // Lookup connection factory
            QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup(CF_NAME);
            queueConnection = connFactory.createQueueConnection();
            queueConnection.start();
            queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            // Send message
            Queue queue = (Queue) ctx.lookup(queueName);
            // create the message to send
            ObjectMessage objectMessage = queueSession.createObjectMessage(scanRequest);
            queueSender = queueSession.createSender(queue);
            queueSender.send(objectMessage);
        } catch (JMSException | NamingException e) {
            logger.error("Unable to add the message to the queue " + queueName, e);
        } finally {
            try {
                if (queueSender != null) {
                    queueSender.close();
                }

                if (queueSession != null) {
                    queueSession.close();
                }

                if (queueConnection != null) {
                    queueConnection.close();
                }
            } catch (JMSException e) {
                //ignored
            }
        }
    }

    private static String getTCPConnectionURL(String username, String password) {
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        return new StringBuffer()
                .append("amqp://").append(username).append(":").append(password)
                .append("@").append(CARBON_CLIENT_ID)
                .append("/").append(CARBON_VIRTUAL_HOST_NAME)
                .append("?brokerlist='tcp://").append(scanManagerConfiguration.getBrokerHostname())
                .append(":").append(scanManagerConfiguration.getBrokerPort()).append("'")
                .toString();
    }

    public static String generateScanId() {
        Random r = new Random();
        int Low = 1000000;
        int High = 9999999;
        Integer result = r.nextInt(High - Low) + Low;
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
