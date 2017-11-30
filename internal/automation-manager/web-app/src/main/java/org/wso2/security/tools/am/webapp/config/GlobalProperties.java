/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this propertiesFile to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this propertiesFile except
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

package org.wso2.security.tools.am.webapp.config;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Read values from properties file, assign to variables, and provide getters to access variables
 */
@SuppressWarnings("unused")
public class GlobalProperties {

    private static String clientId;
    private static String clientSecret;
    private static String accessTokenUri;
    private static String automationManagerHost;
    private static int automationManagerPort;
    private static String staticScannerStartScan;
    private static String dynamicScannerStartScan;
    private static String zapType;
    private static String fsbType;
    private static String dcType;
    private static String getStaticScanners;
    private static String getDynamicScanners;

    static {
        try {
            Properties properties = new Properties();
            properties.load(new BufferedInputStream(GlobalProperties.class.getClassLoader().getResourceAsStream
                    ("global.properties")));
            clientId = properties.getProperty("webapp.client-id");
            clientSecret = properties.getProperty("webapp.client-secret");
            accessTokenUri = properties.getProperty("webapp.access-token-uri");
            automationManagerHost = properties.getProperty("automation.manager.host");
            automationManagerPort = Integer.parseInt(properties.getProperty("automation.manager.https-port"));
            dynamicScannerStartScan = properties.getProperty("dynamic-scanner.start-scan");
            zapType = properties.getProperty("scanner.dynamic.zap.type");
            staticScannerStartScan = properties.getProperty("static-scanner.start-scan");
            fsbType = properties.getProperty("scanner.static.fsb.type");
            dcType = properties.getProperty("scanner.static.dc.type");
            getStaticScanners = properties.getProperty("get.static.scanners");
            getDynamicScanners = properties.getProperty("get.dynamic.scanners");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Client id of the application
     */
    public static String getClientId() {
        return clientId;
    }

    /**
     * @return Client secret of the application
     */
    public static String getClientSecret() {
        return clientSecret;
    }

    /**
     * @return Access token URI at the API Manager end
     */
    public static String getAccessTokenUri() {
        return accessTokenUri;
    }


    /**
     * @return Automation Manager host
     */
    public static String getAutomationManagerHost() {
        return automationManagerHost;
    }

    /**
     * @return Automation Manager port
     */
    public static int getAutomationManagerPort() {
        return automationManagerPort;
    }

    /**
     * @return Dynamic scanner start scan path
     */
    public static String getDynamicScannerStartScan() {
        return dynamicScannerStartScan;
    }

    /**
     * @return Type of the zap scan
     */
    public static String getZapType() {
        return zapType;
    }

    /**
     * @return Static scanner start scan path
     */
    public static String getStaticScannerStartScan() {
        return staticScannerStartScan;
    }

    /**
     * @return Type of the FSB scan
     */
    public static String getFsbType() {
        return fsbType;
    }

    /**
     * @return Type of the DC scan
     */
    public static String getDcType() {
        return dcType;
    }

    public static String getGetStaticScanners() {
        return getStaticScanners;
    }

    public static String getGetDynamicScanners() {
        return getDynamicScanners;
    }
}
