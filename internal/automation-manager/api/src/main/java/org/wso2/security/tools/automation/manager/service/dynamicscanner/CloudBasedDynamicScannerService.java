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

package org.wso2.security.tools.automation.manager.service.dynamicscanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.cloudbased.CloudBasedDynamicScannerEntity;
import org.wso2.security.tools.automation.manager.repository.dynamicscanner.CloudBasedDynamicScannerRepository;

/**
 * Service layer methods to handle cloud based dynamic scanners
 */
@SuppressWarnings("unused")
@Service
public class CloudBasedDynamicScannerService {

    private final CloudBasedDynamicScannerRepository dynamicScannerRepository;

    @Autowired
    public CloudBasedDynamicScannerService(CloudBasedDynamicScannerRepository dynamicScannerRepository) {
        this.dynamicScannerRepository = dynamicScannerRepository;
    }

    /**
     * Get Iterable cloud based dynamic scanner entity list
     *
     * @return Iterable list of {@link CloudBasedDynamicScannerEntity}
     */
    public Iterable<CloudBasedDynamicScannerEntity> findAll() {
        return dynamicScannerRepository.findAll();
    }

    /**
     * Find a cloud based dynamic scanner entity by id
     *
     * @param id Auto generated database id of dynamic scanner
     * @return {@link CloudBasedDynamicScannerEntity}
     */
    public CloudBasedDynamicScannerEntity findOne(int id) {
        return dynamicScannerRepository.findOne(id);
    }

    /**
     * Get Iterable cloud based dynamic scanner entity list of a specific user
     *
     * @param userId User id
     * @return Iterable list of {@link CloudBasedDynamicScannerEntity}
     */
    public Iterable<CloudBasedDynamicScannerEntity> findByUserId(String userId) {
        return dynamicScannerRepository.findByUserId(userId);
    }

    /**
     * Save a dynamic scanner in cloud based dynamic scanner entity
     *
     * @param dynamicScannerEntity Dynamic scanner entity
     * @return {@link CloudBasedDynamicScannerEntity} that saves in the database
     */
    public CloudBasedDynamicScannerEntity save(CloudBasedDynamicScannerEntity dynamicScannerEntity) {
        return dynamicScannerRepository.save(dynamicScannerEntity);
    }

}
