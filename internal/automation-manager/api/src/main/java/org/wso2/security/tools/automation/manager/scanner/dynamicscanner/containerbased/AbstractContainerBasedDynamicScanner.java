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

package org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.wso2.security.tools.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.tools.automation.manager.config.AutomationManagerProperties;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.containerbased
        .ContainerBasedDynamicScannerEntity;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.containerbased.zap.ZapEntity;
import org.wso2.security.tools.automation.manager.exception.DynamicScannerException;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.ServerHandler;
import org.wso2.security.tools.automation.manager.service.dynamicscanner.ContainerBasedDynamicScannerService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The abstract class {@link AbstractContainerBasedDynamicScanner} implements the interface
 * {@link ContainerBasedDynamicScanner} to implement common methods for container based dynamic scanners
 */
public abstract class AbstractContainerBasedDynamicScanner implements ContainerBasedDynamicScanner {

    protected String userId;
    protected String ipAddress;
    protected String fileUploadLocation;
    protected File urlListFile;
    protected ContainerBasedDynamicScannerService dynamicScannerService;
    protected ContainerBasedDynamicScannerEntity dynamicScannerEntity;

    public AbstractContainerBasedDynamicScanner(ContainerBasedDynamicScannerEntity dynamicScannerEntity) {
        dynamicScannerService = ApplicationContextUtils.getApplicationContext().getBean
                (ContainerBasedDynamicScannerService.class);
        this.dynamicScannerEntity = dynamicScannerEntity;
    }

    /**
     * Overrides the {@code init} method to initialize instance variables, initialize {@link ZapEntity} and store
     * {@link ZapEntity} object in database
     * {@inheritDoc}
     */
    @Override
    public void init(String userId, String ipAddress, String fileUploadLocation, String urlListFileName) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.fileUploadLocation = fileUploadLocation;
        this.urlListFile = new File(fileUploadLocation + File.separator + urlListFileName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startScanner() throws DynamicScannerException {
        saveMetaData();
        createContainer();
        startContainer();
        if (!checkDynamicScannerHostAvailability()) {
            throw new DynamicScannerException("Error occurred while starting dynamic scanner container");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveMetaData() {
        dynamicScannerEntity.setUserId(userId);
        dynamicScannerEntity.setStatus(AutomationManagerProperties.getStatusInitiated());
        dynamicScannerService.save(dynamicScannerEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startContainer() throws DynamicScannerException {
        try {
            if (DockerHandler.startContainer(dynamicScannerEntity.getContainerId())) {
                dynamicScannerEntity.setStatus(AutomationManagerProperties.getStatusRunning());
                dynamicScannerEntity.setDockerIpAddress(DockerHandler.inspectContainer(dynamicScannerEntity
                        .getContainerId()).networkSettings()
                        .ipAddress());
                dynamicScannerService.save(dynamicScannerEntity);
            }
        } catch (InterruptedException | DockerCertificateException | DockerException e) {
            throw new DynamicScannerException("Error occurred while starting dynamic scanner container", e);
        }
    }

    /**
     * After container is created, the container related data are saved
     *
     * @param containerId Container id
     * @param port        Port where the container is running
     */
    protected void saveContainerData(String containerId, int port) {
        String createdTime = new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format(new Date());
        dynamicScannerEntity.setContainerId(containerId);
        dynamicScannerEntity.setIpAddress(ipAddress);
        dynamicScannerEntity.setContainerPort(port);
        dynamicScannerEntity.setHostPort(port);
        dynamicScannerEntity.setStatus(AutomationManagerProperties.getStatusCreated());
        dynamicScannerEntity.setCreatedTime(createdTime);
        dynamicScannerService.save(dynamicScannerEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkDynamicScannerHostAvailability() throws DynamicScannerException {
        try {
            return ServerHandler.hostAvailabilityCheck(dynamicScannerEntity.getDockerIpAddress(),
                    dynamicScannerEntity.getHostPort(), 12 * 5);
        } catch (InterruptedException e) {
            throw new DynamicScannerException("Error occurred while checking host availability of dynamic scanner " +
                    "container", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return dynamicScannerEntity.getId();
    }
}
