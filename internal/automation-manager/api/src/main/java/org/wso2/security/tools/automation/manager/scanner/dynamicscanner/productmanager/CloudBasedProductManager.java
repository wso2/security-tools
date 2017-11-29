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

package org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager;

import org.wso2.security.tools.automation.manager.entity.productmanager.cloudbased.CloudBasedProductManagerEntity;
import org.wso2.security.tools.automation.manager.service.productmanager.ProductManagerService;

/**
 * The class {@link CloudBasedProductManager} implements the interface {@link ProductManager} methods to handle cloud
 * based product managers
 */
@SuppressWarnings("unused")
public class CloudBasedProductManager implements ProductManager {

    private String userId;
    private String testName;
    private String ipAddress;
    private String productName;
    private String wumLevel;
    private String wso2ServerHost;
    private int wso2ServerPort;
    private int relatedDynamicScannerId;
    private ProductManagerService productManagerService;
    private CloudBasedProductManagerEntity productManagerEntity;

    public void init(String userId, String testName, String ipAddress, String productName, String wumLevel, String
            wso2ServerHost, int wso2ServerPort) {

        this.userId = userId;
        this.testName = testName;
        this.ipAddress = ipAddress;
        this.productName = productName;
        this.wumLevel = wumLevel;
        this.wso2ServerHost = wso2ServerHost;
        this.wso2ServerPort = wso2ServerPort;
    }

    @Override
    public void startProductManager(int relatedDynamicScannerId) {
    }

    @Override
    public boolean startServer() {
        return false;
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public int getPort() {
        return 0;
    }
}
