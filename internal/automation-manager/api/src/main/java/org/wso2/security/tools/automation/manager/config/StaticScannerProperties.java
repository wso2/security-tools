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
 * The main contract of the {@code StaticScannerProperties} class is to read values from {@code staticscanner
 * .properties} file.
 */
@SuppressWarnings("unused")
public class StaticScannerProperties {

    private static String staticScannerIsReady;
    private static String staticScannerStartScan;
    private static String staticScannerGetReport;
    private static String findSecBugsScannerDockerImage;
    private static String findSecBugsScannerContextPath;
    private static String findSecBugsScannerType;
    private static String dependencyCheckDockerImage;
    private static String dependencyCheckScannerContextPath;
    private static String dependencyCheckScannerType;

    /*
    Load {@code staticscanner.properties} file and assign properties into variables
    */
    static {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(AutomationManagerProperties.class.getClassLoader().getResource
                    ("staticscanner.properties").getFile())));
            staticScannerIsReady = properties.getProperty("scanner.static.is-ready");
            staticScannerStartScan = properties.getProperty("scanner.static.start-scan");
            staticScannerGetReport = properties.getProperty("scanner.static.get-report");
            findSecBugsScannerDockerImage = properties.getProperty("scanner.static.find-sec-bugs.docker.image");
            findSecBugsScannerContextPath = properties.getProperty("scanner.static.find-sec-bugs.context.path");
            findSecBugsScannerType = properties.getProperty("scanner.static.find-sec-bugs.type");
            dependencyCheckDockerImage = properties.getProperty("scanner.static.dependency-check.docker.image");
            dependencyCheckScannerContextPath = properties.getProperty("scanner.static.dependency-check.context.path");
            dependencyCheckScannerType = properties.getProperty("scanner.static.dependency-check.type");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return API in static scanner container to check static scanner is ready
     */
    public static String getStaticScannerIsReady() {
        return staticScannerIsReady;
    }

    /**
     * @return API in static scanner container to start a scan
     */
    public static String getStaticScannerStartScan() {
        return staticScannerStartScan;
    }

    /**
     * @return API in static scanner container to get report
     */
    public static String getStaticScannerGetReport() {
        return staticScannerGetReport;
    }

    /**
     * @return Docker image of FindSecBugs
     */
    public static String getFindSecBugsScannerDockerImage() {
        return findSecBugsScannerDockerImage;
    }

    /**
     * @return Context path of the FindSecBugs scanner
     */
    public static String getFindSecBugsScannerContextPath() {
        return findSecBugsScannerContextPath;
    }

    /**
     * @return FindSecBugs scanner type
     */
    public static String getFindSecBugsScannerType() {
        return findSecBugsScannerType;
    }

    /**
     * @return Docker image of Dependency Check
     */
    public static String getDependencyCheckDockerImage() {
        return dependencyCheckDockerImage;
    }

    /**
     * @return Context path of the Dependency Check scanner
     */
    public static String getDependencyCheckScannerContextPath() {
        return dependencyCheckScannerContextPath;
    }

    /**
     * @return DependencyCheck scanner type
     */
    public static String getDependencyCheckScannerType() {
        return dependencyCheckScannerType;
    }
}
