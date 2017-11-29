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
 * The main contract of the {@code AutomationManagerProperties} class is to read values from {@code automationmanager
 * .properties} file.
 */
@SuppressWarnings("unused")
public class AutomationManagerProperties {
    private static String ipAddress;
    private static String automationManagerHostRelativeToContainers;
    private static String automationManagerPort;
    private static String tempFolderPath;
    private static String statusInitiated;
    private static String statusCreated;
    private static String statusRunning;
    private static String statusCompleted;
    private static String statusFailed;
    private static String statusRemoved;
    private static String datePattern;
    private static String cloudBasedScannerType;
    private static String containerBasedScannerType;
    private static String gitUsername;
    private static String gitPassword;

    /*
      Load {@code automationmanager.properties} file and assign properties into variables
     */
    static {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(AutomationManagerProperties.class.getClassLoader().getResource
                    ("automationmanager.properties").getFile())));
            ipAddress = properties.getProperty("ip-address");
            automationManagerHostRelativeToContainers = properties.getProperty("automation.manager.host");
            automationManagerPort = properties.getProperty("automation.manager.port");
            tempFolderPath = properties.getProperty("temp.dir");
            statusInitiated = properties.getProperty("status.initiated");
            statusCreated = properties.getProperty("status.created");
            statusRunning = properties.getProperty("status.running");
            statusCompleted = properties.getProperty("status.completed");
            statusFailed = properties.getProperty("status.failed");
            statusRemoved = properties.getProperty("status.removed");
            datePattern = properties.getProperty("date.pattern");
            cloudBasedScannerType = properties.getProperty("scanner.type.cloud.based");
            containerBasedScannerType = properties.getProperty("scanner.type.container.based");
            gitUsername = properties.getProperty("app.git.username");
            gitPassword = properties.getProperty("app.git.password");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the ip address where the containers are spawn (0.0.0.0 means host machine)
     *
     * @return Ip address
     */
    public static String getIpAddress() {
        return ipAddress;
    }

    /**
     * Get Automation Manager (this application) host relative to containers
     * <p>This host along with the port are sent to containers, so that they can send back notifications after tasks
     * are completed</p>
     *
     * @return Automation Manager host relative to containers
     */
    public static String getAutomationManagerHostRelativeToContainers() {
        return automationManagerHostRelativeToContainers;
    }

    /**
     * Get Automation Manager port
     * <p>This port along with the host are sent to containers, so that they can send back notifications after tasks
     * are completed</p>
     *
     * @return Automation Manager port
     */
    public static String getAutomationManagerPort() {
        return automationManagerPort;
    }

    /**
     * Get the temporary directory location of the host to store files
     *
     * @return Temporary directory of the host
     */
    public static String getTempFolderPath() {
        return tempFolderPath;
    }

    /**
     * Get status "initiated"
     * <p>These status are required to define the current  statuses of scanners, product managers etc</p>
     *
     * @return Status initiated
     */
    public static String getStatusInitiated() {
        return statusInitiated;
    }

    /**
     * Get status "created"
     * <p>These status are required to define the current  statuses of scanners, product managers etc</p>
     *
     * @return Status created
     */
    public static String getStatusCreated() {
        return statusCreated;
    }

    /**
     * Get status "running"
     * <p>These status are required to define the current  statuses of scanners, product managers etc</p>
     *
     * @return Status running
     */
    public static String getStatusRunning() {
        return statusRunning;
    }

    /**
     * Get status "completed"
     * <p>These status are required to define the current  statuses of scanners, product managers etc</p>
     *
     * @return Status completed
     */
    public static String getStatusCompleted() {
        return statusCompleted;
    }

    /**
     * Get status "failed"
     * <p>These status are required to define the current  statuses of scanners, product managers etc</p>
     *
     * @return Status failed
     */
    public static String getStatusFailed() {
        return statusFailed;
    }

    /**
     * Get status "removed"
     * <p>These status are required to define the current  statuses of scanners, product managers etc</p>
     *
     * @return Status removed
     */
    public static String getStatusRemoved() {
        return statusRemoved;
    }

    /**
     * Get data pattern
     * <p>A common date pattern is used to store created time, file uploaded time, notified time etc.</p>
     *
     * @return Date pattern
     */
    public static String getDatePattern() {
        return datePattern;
    }

    /**
     * @return Cloud based scanner type
     */
    public static String getCloudBasedScannerType() {
        return cloudBasedScannerType;
    }

    /**
     * @return Container based scanner type
     */
    public static String getContainerBasedScannerType() {
        return containerBasedScannerType;
    }

    /**
     * Get the GitHub username for private repositories (There is a specific user defined for the application)
     *
     * @return GitHub username
     */
    public static String getGitUsername() {
        return gitUsername;
    }

    /**
     * Get the GitHub password for private repositories (There is a specific user defined for the application)
     *
     * @return GitHub password
     */
    public static String getGitPassword() {
        return gitPassword;
    }
}
