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

package org.wso2.security.tools.automation.manager.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * Utility methods for handling server
 */
public class ServerHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

    /**
     * Periodically checks for a host availability by creating a socket. If {@link IOException} is occurred,
     * {@link Thread} sleeps for few seconds and again check for host availability. This loop is run for given number
     * of times
     *
     * @param host  Host to be checked
     * @param port  Port to be checked
     * @param times Number of times to check
     * @return Boolean to indicate host is available
     */
    public static boolean hostAvailabilityCheck(String host, int port, int times) throws InterruptedException {
        int i = 0;
        while (i < times) {
            LOGGER.trace("Checking host availability for the host: " + host + " and the port: " + port);
            try (Socket s = new Socket(host, port)) {
                LOGGER.info(host + ":" + port + " is available");
                return true;
            } catch (IOException e) {
                Thread.sleep(5000);
                i++;
            }
        }
        return false;
    }
}
