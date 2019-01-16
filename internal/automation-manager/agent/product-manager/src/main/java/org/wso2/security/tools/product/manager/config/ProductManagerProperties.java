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

package org.wso2.security.tools.product.manager.config;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads productmanager.properties file and assign values for variables
 */
@SuppressWarnings({"unused"})
public class ProductManagerProperties {

    private static String productManagerProductPath;
    private static int productManagerProductPort;
    private static int productManagerPortOffset;
    private static String productManagerProductHost;
    private static String productManagerWso2ServerFile;
    private static String productManagerPortArg;

    static {
        Properties properties = new Properties();
        try {
            properties.load(new BufferedInputStream(ProductManagerProperties.class.getClassLoader().getResourceAsStream
                    ("/productmanager.properties")));
            productManagerProductPath = properties.getProperty("product.manager.product.path");
            productManagerProductPort = Integer.parseInt(properties.getProperty("product.manager.product.port"));
            productManagerPortOffset = Integer.parseInt(properties.getProperty("product.manager.product.port.offset"));
            productManagerProductHost = properties.getProperty("product.manager.product.host");
            productManagerWso2ServerFile = properties.getProperty("product.manager.wso2server.file");
            productManagerPortArg = properties.getProperty("product.manager.port.offset.arg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     * Get the product path where the zip file is uploaded
     * @return Product path
     */
    public static String getProductManagerProductPath() {
        return productManagerProductPath;
    }

    /**
     * Get the port of product server
     *
     * @return Product port
     */
    public static int getProductManagerProductPort() {
        return productManagerProductPort;
    }

    /**
     * Get port offset of the product port
     *
     * @return Product port offset
     */
    public static int getProductManagerPortOffset() {
        return productManagerPortOffset;
    }

    /**
     * Host of the product is running (localhost)
     *
     * @return Product host
     */
    public static String getProductManagerProductHost() {
        return productManagerProductHost;
    }

    /**
     * Get wso2server.sh
     *
     * @return wso2server.sh file name
     */
    public static String getProductManagerWso2ServerFile() {
        return productManagerWso2ServerFile;
    }

    /**
     * Argument to set product port offset
     *
     * @return Port offset argument
     */
    public static String getProductManagerPortArg() {
        return productManagerPortArg;
    }
}

