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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.product.manager.exception.NotificationManagerException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The class {@code NotificationManager} is to notify back the status to Automation Manager
 */
public class NotificationManager {

    private final static String NOTIFY = "automationManager/productManager/notify";
    private final static String FILE_UPLOADED = NOTIFY + "/fileUploaded";
    private final static String FILE_EXTRACTED = NOTIFY + "/fileExtracted";
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationManager.class);
    private static String myContainerId;
    private static String automationManagerHost;
    private static int automationManagerPort;

    /**
     * Configure the {@code NotificationManager} with Automation Manager details and container details.
     * <p>Since this micro service runs inside a container, the container id is given, because when notifying back, the
     * Automation Manager has to know which is the container</p>
     *
     * @param myContainerId         Container id which this micro service belongs to
     * @param automationManagerHost Automation Manager host
     * @param automationManagerPort Automation Manager port
     */
    public static void config(String automationManagerHost, int automationManagerPort, String myContainerId) {
        NotificationManager.automationManagerHost = automationManagerHost;
        NotificationManager.automationManagerPort = automationManagerPort;
        NotificationManager.myContainerId = myContainerId;
    }

    private static void notifyStatus(String path, boolean status) throws NotificationManagerException {
        int i = 0;
        while (i < 10) {
            try {
                URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme
                        ("http").setPath(path)
                        .addParameter("containerId", myContainerId)
                        .addParameter("status", String.valueOf(status))
                        .build();
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpGet get = new HttpGet(uri);

                HttpResponse response = httpClient.execute(get);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    LOGGER.info("Notified successfully");
                    return;
                } else {
                    i++;
                }
                Thread.sleep(2000);
            } catch (URISyntaxException | InterruptedException | IOException e) {
                e.printStackTrace();
                LOGGER.error(e.toString());
                i++;
            }
        }
        throw new NotificationManagerException("Error occurred while notifying status to Automation Manager");
    }

    /**
     * Notify file is uploaded or not
     *
     * @param status Boolean status
     */
    public static void notifyFileUploaded(boolean status) throws NotificationManagerException {
        LOGGER.trace("Notifying file uploaded: " + status);
        notifyStatus(FILE_UPLOADED, status);
    }

    /**
     * Notify file is extracted or not
     *
     * @param status Boolean status
     */
    public static void notifyFileExtracted(boolean status) throws NotificationManagerException {
        LOGGER.trace("Notifying file extracted: " + status);
        notifyStatus(FILE_EXTRACTED, status);
    }

    /**
     * Notify server is started or not
     *
     * @param status Boolean status
     */
    public static void notifyServerStarted(boolean status) throws NotificationManagerException {
        LOGGER.trace("Notifying server started: " + status);
        notifyStatus(FILE_EXTRACTED, status);
    }

    /**
     * Whether the {@link NotificationManager} is configured or not
     *
     * @return Boolean status
     */
    public static boolean isConfigured() {
        return automationManagerHost != null && automationManagerPort != -1 && myContainerId != null;
    }
}
