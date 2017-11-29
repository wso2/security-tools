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
 * The main contract of the {@code DynamicScannerProperties} class is to read values from {@code dynamicscanner
 * .properties} file.
 */
@SuppressWarnings("unused")
public class DynamicScannerProperties {
    private static String zapDockerImage;
    private static String zapReport;
    private static String zapScannerType;
    private static String wso2ProductKeyUsername;
    private static String wso2ProductValueUsername;
    private static String wso2ProductKeyPassword;
    private static String wso2ProductValuePassword;
    private static String wso2ProductManagementConsoleLoginUrl;
    private static String wso2ProductManagementConsoleLogoutUrl;

    /*
        Load {@code dynamicscanner.properties} file and assign properties into variables
    */
    static {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(AutomationManagerProperties.class.getClassLoader().getResource
                    ("dynamicscanner.properties").getFile())));

            zapDockerImage = properties.getProperty("scanner.dynamic.zap.docker.image");
            zapReport = properties.getProperty("scanner.dynamic.zap.report");
            zapScannerType = properties.getProperty("scanner.dynamic.zap.type");
            wso2ProductKeyUsername = properties.getProperty("wso2.product.key.username");
            wso2ProductValueUsername = properties.getProperty("wso2.product.value.username");
            wso2ProductKeyPassword = properties.getProperty("wso2.product.key.password");
            wso2ProductValuePassword = properties.getProperty("wso2.product.value.password");
            wso2ProductManagementConsoleLoginUrl = properties.getProperty("wso2.product.login.url");
            wso2ProductManagementConsoleLogoutUrl = properties.getProperty("wso2.product.logout.url");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return ZAP docker image
     */
    public static String getZapDockerImage() {
        return zapDockerImage;
    }

    /**
     * @return ZAP report name
     */
    public static String getZapReport() {
        return zapReport;
    }

    /**
     * @return Scanner type of ZAP scanner
     */
    public static String getZapScannerType() {
        return zapScannerType;
    }

    /**
     * @return Key of username parameter to login to wso2 server
     */
    public static String getWso2ProductKeyUsername() {
        return wso2ProductKeyUsername;
    }

    /**
     * @return Value of "username" parameter to login to wso2 server
     */
    public static String getWso2ProductValueUsername() {
        return wso2ProductValueUsername;
    }

    /**
     * @return Key of "password" parameter to login to wso2 server
     */
    public static String getWso2ProductKeyPassword() {
        return wso2ProductKeyPassword;
    }

    /**
     * @return Value of "username" parameter to login to wso2 server
     */
    public static String getWso2ProductValuePassword() {
        return wso2ProductValuePassword;
    }

    /**
     * @return Login URL of wso2 management console
     */
    public static String getWso2ProductManagementConsoleLoginUrl() {
        return wso2ProductManagementConsoleLoginUrl;
    }

    /**
     * @return Logout URL of wso2 management console
     */
    public static String getWso2ProductManagementConsoleLogoutUrl() {
        return wso2ProductManagementConsoleLogoutUrl;
    }
}

