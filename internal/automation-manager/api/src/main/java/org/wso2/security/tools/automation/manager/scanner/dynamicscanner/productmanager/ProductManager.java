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

package org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager;

import org.wso2.security.tools.automation.manager.exception.ProductManagerException;

/**
 * The interface {@link ProductManager} provides abstraction to product manager related methods
 */
@SuppressWarnings("WeakerAccess")
public interface ProductManager {

    /**
     * Start a product manager
     *
     * @param relatedDynamicScannerId Related dynamic scanner id that product manager belongs to
     * @throws ProductManagerException The general exception thrown by product managers
     */
    void startProductManager(int relatedDynamicScannerId) throws ProductManagerException;

    /**
     * Start wso2 server (Container based product manager will unzip a file and start a server, and cloud based
     * product manager will check whether the host is available)
     *
     * @return Boolean to indicate server is started
     * @throws ProductManagerException The general exception thrown by product managers
     */
    boolean startServer() throws ProductManagerException;

    /**
     * @return Product manager host
     */
    String getHost();

    /**
     * @return Product manager port
     */
    int getPort();
}
