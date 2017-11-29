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

package org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased;

import org.wso2.security.tools.automation.manager.exception.DynamicScannerException;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.DynamicScanner;

/**
 * The interface {@code ContainerBasedDynamicScanner} extends the interface {@link DynamicScanner} contains methods
 * specific to container based dynamic scanners
 */
public interface ContainerBasedDynamicScanner extends DynamicScanner {
    /**
     * Calculate dynamic scanner container port
     *
     * @param id Auto generated database id of dynamic scanner
     * @return Dynamic scanner container port
     */
    static int calculateDynamicScannerContainerPort(int id) {
        if (5000 + id > 20000) {
            id = 1;
        }
        return (5000 + id) % 20000;
    }

    /**
     * @param userId             User id
     * @param fileUploadLocation File upload location of the host machine (This is required to get uploaded files
     *                           such as URL list file)
     * @param urlListFileName    URL list file name (URL list which needs to be scanned are defined in this file)
     */
    void init(String userId, String ipAddress, String fileUploadLocation, String urlListFileName);

    /**
     * Saves the basic meta data of the dynamic scanner
     */
    void saveMetaData();

    /**
     * Create dynamic scanner container
     *
     * @throws DynamicScannerException The general exception type of dynamic scanners
     */
    void createContainer() throws DynamicScannerException;

    /**
     * Start dynamic scanner container
     *
     * @throws DynamicScannerException The general exception type of dynamic scanners
     */
    void startContainer() throws DynamicScannerException;

    /**
     * Check whether the dynamic scanner host is available
     *
     * @throws DynamicScannerException The general exception type of dynamic scanners
     */
    boolean checkDynamicScannerHostAvailability() throws DynamicScannerException;
}
