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

package org.wso2.security.tools.automation.manager.scanner.staticscanner.factory;

import org.wso2.security.tools.automation.manager.config.AutomationManagerProperties;

/**
 * The class {@link StaticScannerFactoryProducer} is to produce static scanner factory instances based on the
 * factory type (eg: cloud based, container based)
 */
public class StaticScannerFactoryProducer {
    /**
     * Check the factory type and returns a factory instance based on the type
     *
     * @param factoryType Factory type
     * @return {@link AbstractStaticScannerFactory}
     */
    public static AbstractStaticScannerFactory getStaticScannerFactory(String factoryType) {
        AbstractStaticScannerFactory staticScannerFactory = null;
        if (AutomationManagerProperties.getCloudBasedScannerType().equalsIgnoreCase(factoryType)) {
            staticScannerFactory = new CloudBasedStaticScannerFactory();
        }
        if (AutomationManagerProperties.getContainerBasedScannerType().equalsIgnoreCase(factoryType)) {
            staticScannerFactory = new ContainerBasedStaticScannerFactory();
        }
        return staticScannerFactory;
    }
}