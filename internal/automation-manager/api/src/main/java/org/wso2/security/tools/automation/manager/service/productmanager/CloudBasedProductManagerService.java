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

package org.wso2.security.tools.automation.manager.service.productmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.automation.manager.entity.productmanager.cloudbased.CloudBasedProductManagerEntity;
import org.wso2.security.tools.automation.manager.repository.productmanager.CloudBasedProductManagerRepository;

/**
 * Service layer methods to handle cloud based product managers
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Service
public class CloudBasedProductManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerBasedProductManagerService.class);
    private final CloudBasedProductManagerRepository productManagerRepository;

    @Autowired
    public CloudBasedProductManagerService(CloudBasedProductManagerRepository productManagerRepository) {
        this.productManagerRepository = productManagerRepository;
    }

    /**
     * Get Iterable cloud based product manager entity list
     *
     * @return Iterable list of {@link CloudBasedProductManagerEntity}
     */
    public Iterable<CloudBasedProductManagerEntity> findAll() {
        return productManagerRepository.findAll();
    }

    /**
     * Find a cloud based product manager entity by id
     *
     * @param id Auto generated database id of product manager
     * @return {@link CloudBasedProductManagerEntity}
     */
    public CloudBasedProductManagerEntity findOne(int id) {
        return productManagerRepository.findOne(id);
    }

    /**
     * Get Iterable cloud based product manager entity list of a specific user
     *
     * @param userId User id
     * @return Iterable list of {@link CloudBasedProductManagerEntity}
     */
    public Iterable<CloudBasedProductManagerEntity> findByUserId(String userId) {
        return productManagerRepository.findByUserId(userId);
    }

    /**
     * Save a cloud based product manager
     *
     * @param productManagerEntity Product manager entity
     * @return {@link CloudBasedProductManagerEntity} that saves in the database
     */
    public CloudBasedProductManagerEntity save(CloudBasedProductManagerEntity productManagerEntity) {
        return productManagerRepository.save(productManagerEntity);
    }
}
