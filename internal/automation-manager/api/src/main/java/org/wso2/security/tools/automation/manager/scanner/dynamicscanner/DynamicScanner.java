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

import org.wso2.security.tools.automation.manager.exception.DynamicScannerException;

/**
 * The interface {@link DynamicScanner} provides abstraction to dynamic scanner related methods
 */
public interface DynamicScanner {

    /**
     * Start a dynamic scanner
     *
     * @throws DynamicScannerException The general exception thrown by the dynamic scanners
     */
    void startScanner() throws DynamicScannerException;

    /**
     * Start a dynamic scan
     *
     * @param productHostRelativeToScanner           Product host relative to scanner (If dynamic scanner or/and product
     *                                               manager are container based, scanner needs the product host
     *                                               relative to
     *                                               it as it has to communicate)
     * @param productHostRelativeToAutomationManager Product host relative to automation manager (If dynamic scanner
     *                                               or/and product manager are container based, automation manager
     *                                               needs the product host relative to it as it has to communicate)
     * @param productPort                            Product port
     * @throws DynamicScannerException The general exception thrown by the dynamic scanners
     */
    void startScan(String productHostRelativeToScanner, String productHostRelativeToAutomationManager, int
            productPort) throws DynamicScannerException;

    /**
     * Get the auto generated id of the dynamic scanner, as it is needed to product manager
     *
     * @return Dynamic scanner database id
     */
    int getId();
}
