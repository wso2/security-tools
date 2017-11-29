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

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.automation.manager.config.AutomationManagerProperties;
import org.wso2.security.tools.automation.manager.entity.productmanager.containerbased
        .ContainerBasedProductManagerEntity;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.repository.productmanager.ContainerBasedProductManagerRepository;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service layer methods to handle container based product managers
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Service
public class ContainerBasedProductManagerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerBasedProductManagerService.class);
    private final ContainerBasedProductManagerRepository productManagerRepository;

    @Autowired
    public ContainerBasedProductManagerService(ContainerBasedProductManagerRepository productManagerRepository) {
        this.productManagerRepository = productManagerRepository;
    }

    /**
     * Get Iterable container based product manager entity list
     *
     * @return Iterable list of {@link ContainerBasedProductManagerEntity}
     */
    public Iterable<ContainerBasedProductManagerEntity> findAll() {
        return productManagerRepository.findAll();
    }

    /**
     * Find a container based product manager entity by id
     *
     * @param id Auto generated database id of product manager
     * @return {@link ContainerBasedProductManagerEntity}
     */
    public ContainerBasedProductManagerEntity findOne(int id) {
        return productManagerRepository.findOne(id);
    }

    /**
     * Find a container based product manager entity by container id
     *
     * @param containerId Container id of product manager
     * @return {@link ContainerBasedProductManagerEntity}
     */
    public ContainerBasedProductManagerEntity findOneByContainerId(String containerId) {
        return productManagerRepository.findOneByContainerId(containerId);
    }

    /**
     * Get Iterable container based product manager entity list of a specific user
     *
     * @param userId User id
     * @return Iterable list of {@link ContainerBasedProductManagerEntity}
     */
    public Iterable<ContainerBasedProductManagerEntity> findByUserId(String userId) {
        return productManagerRepository.findByUserId(userId);
    }

    /**
     * Save a container based product manager
     *
     * @param productManagerEntity Product manager entity
     * @return {@link ContainerBasedProductManagerEntity} that saves in the database
     */
    public ContainerBasedProductManagerEntity save(ContainerBasedProductManagerEntity productManagerEntity) {
        return productManagerRepository.save(productManagerEntity);
    }

    /**
     * Update that a zip file is uploaded to the container
     *
     * @param containerId Container id
     * @param status      Whether file is uploaded or not
     */
    public void updateFileUploaded(String containerId, boolean status) {
        ContainerBasedProductManagerEntity productManagerEntity = findOneByContainerId(containerId);
        productManagerEntity.setFileUploaded(status);
        productManagerEntity.setFileUploadedTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern())
                .format(new
                Date()));
        save(productManagerEntity);
    }

    /**
     * Update that the zip file is extracted
     *
     * @param containerId Container id
     * @param status      Whether file is extracted or not
     */
    public void updateFileExtracted(String containerId, boolean status) {
        ContainerBasedProductManagerEntity productManagerEntity = findOneByContainerId(containerId);
        productManagerEntity.setFileExtracted(status);
        productManagerEntity.setFileExtractedTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern())
                .format(new
                Date()));
        save(productManagerEntity);
    }

    /**
     * Update a server is started in the container
     *
     * @param containerId Container id
     * @param status      Whether the server is started or not
     */
    public void updateServerStarted(String containerId, boolean status) {
        ContainerBasedProductManagerEntity productManagerEntity = findOneByContainerId(containerId);
        productManagerEntity.setServerStarted(status);
        productManagerEntity.setServerStartedTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern())
                .format(new
                Date()));
        save(productManagerEntity);
    }

    /**
     * Stop a running container and remove
     *
     * @param containerId Container id
     * @throws AutomationManagerException The general exception type of Automation Manager API
     */
    public void kill(String containerId) throws AutomationManagerException {
        try {
            ContainerBasedProductManagerEntity productManagerEntity = findOneByContainerId(containerId);
            DockerHandler.killContainer(containerId);
            DockerHandler.removeContainer(containerId);
            productManagerEntity.setStatus(AutomationManagerProperties.getStatusRemoved());
            save(productManagerEntity);
        } catch (InterruptedException | DockerCertificateException | DockerException e) {
            throw new AutomationManagerException("Error occurred while removing product manager container");

        }
    }
}
