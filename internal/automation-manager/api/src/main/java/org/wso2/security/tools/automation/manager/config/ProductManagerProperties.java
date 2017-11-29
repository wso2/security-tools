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

package org.wso2.security.tools.automation.manager.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The main contract of the {@code ProductManagerProperties} class is to read values from {@code productmanager
 * .properties} file.
 */
@SuppressWarnings("unused")
public class ProductManagerProperties {

    private static String productManagerDockerImage;
    private static String productManagerIsReady;
    private static String productManagerStartServer;
    private static int productManagerProductPort;

    /*
           Load {@code productmanager.properties} file and assign properties into variables
       */
    static {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(AutomationManagerProperties.class.getClassLoader().getResource
                    ("productmanager.properties").getFile())));
            productManagerDockerImage = properties.getProperty("product.manager.docker.image");
            productManagerIsReady = properties.getProperty("product.manager.is-ready");
            productManagerStartServer = properties.getProperty("product.manager.start-server");
            productManagerProductPort = Integer.parseInt(properties.getProperty("product.manager.product-port"));
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * @return Docker image of product manager
     */
    public static String getProductManagerDockerImage() {
        return productManagerDockerImage;
    }

    /**
     * @return API in product manager to know that micro service is ready
     */
    public static String getProductManagerIsReady() {
        return productManagerIsReady;
    }

    /**
     * @return API in product manager to start wso2 server
     */
    public static String getProductManagerStartServer() {
        return productManagerStartServer;
    }

    /**
     * @return The port where the server should be running inside the product manager
     */
    public static int getProductManagerProductPort() {
        return productManagerProductPort;
    }
}
