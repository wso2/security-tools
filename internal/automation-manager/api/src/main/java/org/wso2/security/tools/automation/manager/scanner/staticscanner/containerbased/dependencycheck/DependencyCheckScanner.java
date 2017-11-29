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

package org.wso2.security.tools.automation.manager.scanner.staticscanner.containerbased.dependencycheck;

import org.wso2.security.tools.automation.manager.config.StaticScannerProperties;
import org.wso2.security.tools.automation.manager.entity.staticscanner.containerbased.dependencycheck
        .DependencyCheckEntity;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.containerbased
        .AbstractContainerBasedStaticScanner;

/**
 * The class {@link DependencyCheckScanner} extends the abstract class {@link AbstractContainerBasedStaticScanner}
 */
public class DependencyCheckScanner extends AbstractContainerBasedStaticScanner {

    /**
     * Calls the parent constructor with dependency check scanner specific data
     */
    public DependencyCheckScanner() {
        super(new DependencyCheckEntity(), StaticScannerProperties.getDependencyCheckScannerContextPath(),
                StaticScannerProperties.getDependencyCheckDockerImage());
    }
}
