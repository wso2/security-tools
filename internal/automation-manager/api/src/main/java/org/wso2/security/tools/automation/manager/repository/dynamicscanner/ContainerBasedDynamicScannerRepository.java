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

package org.wso2.security.tools.automation.manager.repository.dynamicscanner;

import org.springframework.data.repository.CrudRepository;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.containerbased
        .ContainerBasedDynamicScannerEntity;

/**
 * Repository methods for accessing persistent {@link ContainerBasedDynamicScannerEntity}
 */
public interface ContainerBasedDynamicScannerRepository extends CrudRepository<ContainerBasedDynamicScannerEntity,
        Integer> {
    /**
     * Find a container by containerId
     *
     * @param containerId Container Id
     * @return a {@code ContainerBasedDynamicScannerEntity} object
     */
    ContainerBasedDynamicScannerEntity findOneByContainerId(String containerId);

    /**
     * Find a list of container based dynamic scanners by user id
     *
     * @param userId User id
     * @return List of {@code ContainerBasedDynamicScannerEntity}
     */
    Iterable<ContainerBasedDynamicScannerEntity> findByUserId(String userId);
}
