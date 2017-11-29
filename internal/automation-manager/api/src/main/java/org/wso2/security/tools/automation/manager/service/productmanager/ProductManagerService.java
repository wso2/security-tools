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

package org.wso2.security.tools.automation.manager.service.productmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.automation.manager.entity.productmanager.ProductManagerEntity;
import org.wso2.security.tools.automation.manager.repository.productmanager.ProductManagerRepository;

/**
 * Service layer methods to handle product managers
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Service
public class ProductManagerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductManagerService.class);
    private final ProductManagerRepository productManagerRepository;

    @Autowired
    public ProductManagerService(ProductManagerRepository productManagerRepository) {
        this.productManagerRepository = productManagerRepository;
    }

    /**
     * Get Iterable product manager entity list
     *
     * @return Iterable list of {@link ProductManagerEntity}
     */
    public Iterable<ProductManagerEntity> findAll() {
        return productManagerRepository.findAll();
    }

    /**
     * Find a product manager entity by id
     *
     * @param id Auto generated database id of product manager
     * @return {@link ProductManagerEntity}
     */
    public ProductManagerEntity findOne(int id) {
        return productManagerRepository.findOne(id);
    }

    /**
     * Get Iterable product manager entity list of a specific user
     *
     * @param userId User id
     * @return Iterable list of {@link ProductManagerEntity}
     */
    public Iterable<ProductManagerEntity> findByUserId(String userId) {
        return productManagerRepository.findByUserId(userId);
    }

    /**
     * Save a product manager
     *
     * @param dynamicScanner Product manager entity
     * @return {@link ProductManagerEntity} that saves in the database
     */
    public ProductManagerEntity save(ProductManagerEntity dynamicScanner) {
        return productManagerRepository.save(dynamicScanner);
    }
}
