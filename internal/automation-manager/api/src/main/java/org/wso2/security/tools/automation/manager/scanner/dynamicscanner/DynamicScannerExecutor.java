/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.security.tools.automation.manager.scanner.dynamicscanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.tools.automation.manager.config.ProductManagerProperties;
import org.wso2.security.tools.automation.manager.exception.DynamicScannerException;
import org.wso2.security.tools.automation.manager.exception.ProductManagerException;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager.ProductManager;
import org.wso2.security.tools.automation.manager.service.dynamicscanner.ContainerBasedDynamicScannerService;

/**
 * The  class {@link DynamicScannerExecutor} which implements {@link Runnable}, provides methods to execute dynamic
 * scans asynchronously.
 */
public class DynamicScannerExecutor implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private ProductManager productManager;
    private DynamicScanner dynamicScanner;
    private ContainerBasedDynamicScannerService dynamicScannerService;

    /**
     * Get {@link ProductManager} and {@link DynamicScanner} instances from the constructor parameters
     *
     * @param productManager Product manager instance
     * @param dynamicScanner Dynamic scanner instance
     */
    public DynamicScannerExecutor(ProductManager productManager, DynamicScanner dynamicScanner) {
        this.productManager = productManager;
        this.dynamicScanner = dynamicScanner;
        dynamicScannerService = ApplicationContextUtils.getApplicationContext().getBean
                (ContainerBasedDynamicScannerService.class);
    }

    /**
     * Starts dynamic scanner and product manager,and start the dynamic scan asynchronously
     */
    @Override
    public void run() {
        /*
        If the scanner or / and product are docker containers, then product host relative to scanner and automation
        manager are required
         */
        String productHostRelativeToScanner;
        String productHostRelativeToAutomationManager;
        int productPort;
        try {
            dynamicScanner.startScanner();
            productManager.startProductManager(dynamicScanner.getId());
            if (productManager.startServer()) {
                productHostRelativeToScanner = productManager.getHost();
                productHostRelativeToAutomationManager = productManager.getHost();
                productPort = ProductManagerProperties.getProductManagerProductPort();
                dynamicScanner.startScan(productHostRelativeToScanner, productHostRelativeToAutomationManager,
                        productPort);
            }
        } catch (ProductManagerException | DynamicScannerException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
    }
}
