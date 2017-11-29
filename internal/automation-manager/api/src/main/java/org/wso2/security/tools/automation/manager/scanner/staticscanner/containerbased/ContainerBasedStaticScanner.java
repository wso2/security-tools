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

package org.wso2.security.tools.automation.manager.scanner.staticscanner.containerbased;

import org.wso2.security.tools.automation.manager.exception.StaticScannerException;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.StaticScanner;

/**
 * The interface {@link ContainerBasedStaticScanner} extends the interface {@link StaticScanner} to define abstract
 * methods of container based static scanners
 */
public interface ContainerBasedStaticScanner extends StaticScanner {
    /**
     * Calculate static scanner container port
     *
     * @param id Auto generated database id of static scanner
     * @return Static scanner container port
     */
    static int calculateStaticScannerContainerPort(int id) {
        if (40000 + id > 65535) {
            id = 1;
        }
        return (40000 + id) % 65535;
    }

    /**
     * @param userId             User id
     * @param testName           Test name
     * @param ipAddress          Ip address where the containers spawn
     * @param productName        Product name to be scanned
     * @param wumLevel           WUM level of the product
     * @param isFileUpload       Is product uploaded via zip file. False means clone from GitHub
     * @param fileUploadLocation File upload location of the host machine (This is required to get uploaded files
     *                           such as URL list file)
     * @param zipFileName        Zip file name
     * @param gitUrl             GitHub URL
     * @param gitUsername        Username of the GitHub (If a private repository)
     * @param gitPassword        Password of the GitHub (If a private repository)
     */
    void init(String userId, String testName, String ipAddress, String productName, String wumLevel, boolean
            isFileUpload, String fileUploadLocation, String zipFileName, String gitUrl, String gitUsername, String
                      gitPassword);

    /**
     * Saves the basic meta data of the static scanner
     */
    void saveMetaData();

    /**
     * Create static scanner container
     *
     * @throws StaticScannerException The general exception type of static scanners
     */
    void createContainer() throws StaticScannerException;

    /**
     * Start static scanner container
     *
     * @throws StaticScannerException The general exception type of static scanners
     */
    void startContainer() throws StaticScannerException;

    /**
     * Check whether the static scanner host is available
     *
     * @throws StaticScannerException The general exception type of static scanners
     */
    boolean hostAvailabilityCheck() throws StaticScannerException;
}

