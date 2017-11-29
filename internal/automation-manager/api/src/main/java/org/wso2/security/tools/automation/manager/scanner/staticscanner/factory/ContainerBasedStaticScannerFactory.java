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

package org.wso2.security.tools.automation.manager.scanner.staticscanner.factory;

import org.wso2.security.tools.automation.manager.config.StaticScannerProperties;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.cloudbased.CloudBasedStaticScanner;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.containerbased.ContainerBasedStaticScanner;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.containerbased.dependencycheck
        .DependencyCheckScanner;
import org.wso2.security.tools.automation.manager.scanner.staticscanner.containerbased.findsecbugs.FindSecBugsScanner;

/**
 * The class {@link ContainerBasedStaticScannerFactory} implements abstract methods of
 * {@link AbstractStaticScannerFactory} to create instances of {@link ContainerBasedStaticScanner}
 */
public class ContainerBasedStaticScannerFactory extends AbstractStaticScannerFactory {
    /**
     * Check the scan type and return a scanner instance
     *
     * @param type Scanner type
     * @return {@link ContainerBasedStaticScanner} instance
     */
    @Override
    public ContainerBasedStaticScanner getContainerBasedStaticScanner(String type) {
        if (StaticScannerProperties.getDependencyCheckScannerType().equalsIgnoreCase(type)) {
            return new DependencyCheckScanner();
        }
        if (StaticScannerProperties.getFindSecBugsScannerType().equalsIgnoreCase(type)) {
            return new FindSecBugsScanner();
        }
        return null;
    }

    @Override
    public CloudBasedStaticScanner getCloudBasedStaticScanner(String type) {
        return null;
    }
}