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

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.automation.manager.config.AutomationManagerProperties;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.containerbased
        .ContainerBasedDynamicScannerEntity;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.MailHandler;
import org.wso2.security.tools.automation.manager.repository.dynamicscanner.ContainerBasedDynamicScannerRepository;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service layer methods to handle container based dynamic scanners
 */
@SuppressWarnings("unused")
@Service
public class ContainerBasedDynamicScannerService {

    private final ContainerBasedDynamicScannerRepository dynamicScannerRepository;
    private final MailHandler mailHandler;

    @Autowired
    public ContainerBasedDynamicScannerService(ContainerBasedDynamicScannerRepository dynamicScannerRepository,
                                               MailHandler mailHandler) {
        this.dynamicScannerRepository = dynamicScannerRepository;
        this.mailHandler = mailHandler;
    }

    /**
     * Get Iterable container based dynamic scanner entity list
     *
     * @return Iterable list of {@link ContainerBasedDynamicScannerEntity}
     */
    public Iterable<ContainerBasedDynamicScannerEntity> findAll() {
        return dynamicScannerRepository.findAll();
    }

    /**
     * Find a container based dynamic scanner entity by a unique id
     *
     * @param id Auto generated database id of dynamic scanner
     * @return {@link ContainerBasedDynamicScannerEntity}
     */
    public ContainerBasedDynamicScannerEntity findOne(int id) {
        return dynamicScannerRepository.findOne(id);
    }

    /**
     * Find a container based dynamic scanner entity by container id
     *
     * @param containerId Container id of dynamic scanner
     * @return {@link ContainerBasedDynamicScannerEntity}
     */
    public ContainerBasedDynamicScannerEntity findOneByContainerId(String containerId) {
        return dynamicScannerRepository.findOneByContainerId(containerId);
    }

    /**
     * Get Iterable container based dynamic scanner entity list of a specific user
     *
     * @param userId User id
     * @return Iterable list of {@link ContainerBasedDynamicScannerEntity}
     */
    public Iterable<ContainerBasedDynamicScannerEntity> findByUserId(String userId) {
        return dynamicScannerRepository.findByUserId(userId);
    }

    /**
     * Save a dynamic scanner in container based dynamic scanner entity
     *
     * @param dynamicScannerEntity Dynamic scanner entity to persist
     * @return {@link ContainerBasedDynamicScannerEntity} that saves in the database
     */
    public ContainerBasedDynamicScannerEntity save(ContainerBasedDynamicScannerEntity dynamicScannerEntity) {
        return dynamicScannerRepository.save(dynamicScannerEntity);
    }

    /**
     * Stop a running container and remove
     *
     * @param containerId Container id
     * @throws AutomationManagerException The general exception type of Automation Manager API
     */
    public void kill(String containerId) throws AutomationManagerException {
        try {
            ContainerBasedDynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
            DockerHandler.killContainer(containerId);
            DockerHandler.removeContainer(containerId);
            dynamicScanner.setStatus(AutomationManagerProperties.getStatusRemoved());
            save(dynamicScanner);
        } catch (InterruptedException | DockerCertificateException | DockerException e) {
            throw new AutomationManagerException("Error occurred while removing dynamic scanner container");
        }
    }

    /**
     * Update the dynamic scan status
     *
     * @param containerId Container id
     * @param status      Scan status (eg: running, completed)
     * @param progress    Progress of the scan
     */
    public void updateScanStatus(String containerId, String status, int progress) {
        ContainerBasedDynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        dynamicScanner.setScanStatus(status);
        dynamicScanner.setScanProgress(progress);
        dynamicScanner.setScanProgressTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format
                (new Date()));
        save(dynamicScanner);
    }

    /**
     * Update that the report is ready by dynamic scanner, get the report and mail
     *
     * @param containerId    Container id
     * @param status         Status whether the report is ready
     * @param reportFilePath Reports file path
     * @throws AutomationManagerException The general exception type of Automation Manager API
     */
    public void updateReportReady(String containerId, boolean status, String reportFilePath) throws
            AutomationManagerException {
        ContainerBasedDynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        dynamicScanner.setReportReady(status);
        dynamicScanner.setReportReadyTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format
                (new Date()));
        save(dynamicScanner);
        getReportAndMail(containerId, reportFilePath);
    }

    /**
     * Update a message
     *
     * @param containerId Container id
     * @param message     Message to be updated
     */
    public void updateMessage(String containerId, String message) {
        ContainerBasedDynamicScannerEntity dynamicScanner = findOneByContainerId(containerId);
        dynamicScanner.setMessage(message);
        save(dynamicScanner);
    }

    /**
     * Get the report and sent to the user
     *
     * @param containerId    Container id
     * @param reportFilePath Reports file path
     * @throws AutomationManagerException The general exception type of Automation Manager API
     */
    private void getReportAndMail(String containerId, String reportFilePath) throws AutomationManagerException {
        try {
            ContainerBasedDynamicScannerEntity dynamicScannerEntity = findOneByContainerId(containerId);
            String subject = "Dynamic Scan Report: ";
            mailHandler.sendMail(dynamicScannerEntity.getUserId(), subject, "This is auto generated message",
                    new FileInputStream(new File(reportFilePath)), "ZapReport.html");
            dynamicScannerEntity.setReportSent(true);
            dynamicScannerEntity.setReportSentTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern())
                    .format(new Date()));
            kill(containerId);
        } catch (Exception e) {
            throw new AutomationManagerException("Error occurred while getting dynamic scanner report and mail");
        }
    }
}

