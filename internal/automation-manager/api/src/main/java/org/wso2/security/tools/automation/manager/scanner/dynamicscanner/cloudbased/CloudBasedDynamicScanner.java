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

package org.wso2.security.tools.automation.manager.scanner.dynamicscanner.cloudbased;

import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.DynamicScanner;

/**
 * The interface {@code CloudBasedDynamicScanner} extends the interface {@link DynamicScanner} contains methods
 * specific to cloud based dynamic scanners
 */
public interface CloudBasedDynamicScanner extends DynamicScanner {

    /**
     * This method is to initialize the scanner
     *
     * @param userId             User id
     * @param fileUploadLocation File upload location of the host machine (This is required to get uploaded files
     *                           such as URL list file)
     * @param urlListFileName    URL list file name (URL list which needs to be scanned are defined in this file)
     * @param scannerHost        Host of the scanner
     * @param scannerPort        Port of the scanner
     */
    void init(String userId, String fileUploadLocation, String urlListFileName, String scannerHost, int scannerPort);

    /**
     * Check if the scanner is available
     */
    void checkIfScannerAvailable();
}
