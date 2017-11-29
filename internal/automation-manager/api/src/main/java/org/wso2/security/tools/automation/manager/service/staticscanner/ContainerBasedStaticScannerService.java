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

package org.wso2.security.tools.automation.manager.service.staticscanner;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.automation.manager.config.AutomationManagerProperties;
import org.wso2.security.tools.automation.manager.config.StaticScannerProperties;
import org.wso2.security.tools.automation.manager.entity.staticscanner.containerbased.ContainerBasedStaticScannerEntity;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.tools.automation.manager.handler.MailHandler;
import org.wso2.security.tools.automation.manager.repository.staticscanner.ContainerBasedStaticScannerRepository;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service layer methods to handle container based static scanners
 */
@Service
public class ContainerBasedStaticScannerService {

    private final ContainerBasedStaticScannerRepository staticScannerRepository;
    private final MailHandler mailHandler;

    @Autowired
    public ContainerBasedStaticScannerService(ContainerBasedStaticScannerRepository staticScannerRepository,
                                              MailHandler mailHandler) {
        this.staticScannerRepository = staticScannerRepository;
        this.mailHandler = mailHandler;
    }

    /**
     * Get Iterable container based static scanner entity list
     *
     * @return Iterable list of {@link ContainerBasedStaticScannerEntity}
     */
    public Iterable<ContainerBasedStaticScannerEntity> findAll() {
        return staticScannerRepository.findAll();
    }

    /**
     * Find a container based static scanner entity by a unique id
     *
     * @param id Auto generated database id of static scanner
     * @return {@link ContainerBasedStaticScannerEntity}
     */
    public ContainerBasedStaticScannerEntity findOne(int id) {
        return staticScannerRepository.findOne(id);
    }

    /**
     * Find a container based static scanner entity by container id
     *
     * @param containerId Container id of static scanner
     * @return {@link ContainerBasedStaticScannerEntity}
     */
    public ContainerBasedStaticScannerEntity findOneByContainerId(String containerId) {
        return staticScannerRepository.findOneByContainerId(containerId);
    }

    /**
     * Get Iterable container based static scanner entity list of a specific user
     *
     * @param userId User id
     * @return Iterable list of {@link ContainerBasedStaticScannerEntity}
     */
    public Iterable<ContainerBasedStaticScannerEntity> findByUserId(String userId) {
        return staticScannerRepository.findByUserId(userId);
    }

    /**
     * Save a dynamic scanner in container based static scanner entity
     *
     * @param staticScannerEntity Static scanner entity to persist
     * @return {@link ContainerBasedStaticScannerEntity} that saves in the database
     */
    public ContainerBasedStaticScannerEntity save(ContainerBasedStaticScannerEntity staticScannerEntity) {
        return staticScannerRepository.save(staticScannerEntity);
    }

    /**
     * Stop a running container and remove
     *
     * @param containerId Container id
     * @throws AutomationManagerException The general exception type of Automation Manager API
     */
    public void kill(String containerId) throws AutomationManagerException {
        try {
            ContainerBasedStaticScannerEntity staticScanner = findOneByContainerId(containerId);
            DockerHandler.killContainer(containerId);
            DockerHandler.removeContainer(containerId);
            staticScanner.setStatus(AutomationManagerProperties.getStatusRemoved());
            save(staticScanner);
        } catch (InterruptedException | DockerCertificateException | DockerException e) {
            throw new AutomationManagerException("Error occurred while removing product manager container", e);
        }
    }

    /**
     * Update that a zip file is uploaded to the container
     *
     * @param containerId Container id
     * @param status      Whether file is uploaded or not
     */
    public void updateFileUploaded(String containerId, boolean status) {
        ContainerBasedStaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setFileUploaded(status);
        staticScanner.setFileUploadedTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format
                (new Date()));
        staticScanner.setProductAvailable(true);
        save(staticScanner);
    }

    /**
     * Update that the zip file is extracted
     *
     * @param containerId Container id
     * @param status      Whether file is extracted or not
     */
    public void updateFileExtracted(String containerId, boolean status) {
        ContainerBasedStaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setFileExtracted(status);
        staticScanner.setFileExtractedTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format
                (new Date()));
        staticScanner.setProductAvailable(true);
        save(staticScanner);
    }

    /**
     * Update a product is cloned to the container
     *
     * @param containerId Container id
     * @param status      Whether the product is cloned or not
     */
    public void updateProductCloned(String containerId, boolean status) {
        ContainerBasedStaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setProductCloned(status);
        staticScanner.setProductClonedTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format
                (new Date()));
        staticScanner.setProductAvailable(true);
        save(staticScanner);
    }

    /**
     * Update the scan status (eg: running, completed)
     *
     * @param containerId Container id
     * @param status      Status of the scan
     */
    public void updateScanStatus(String containerId, String status) {
        ContainerBasedStaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setScanStatus(status);
        staticScanner.setScanStatusTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format(new
                Date()));
        save(staticScanner);
    }

    /**
     * Update that the report is ready and get the report and mail
     *
     * @param containerId Container id
     * @param status      Whether the report is ready or not
     */
    public void updateReportReady(String containerId, boolean status) {
        ContainerBasedStaticScannerEntity staticScanner = findOneByContainerId(containerId);
        staticScanner.setReportReady(status);
        staticScanner.setReportReadyTime(new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format
                (new Date()));
        save(staticScanner);
        try {
            getReportAndMail(containerId);
        } catch (AutomationManagerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the report and sent to the user
     *
     * @param containerId Container id
     * @throws AutomationManagerException The general exception type of Automation Manager API
     */
    private void getReportAndMail(String containerId) throws AutomationManagerException {
        try {
            ContainerBasedStaticScannerEntity staticScannerEntity = findOneByContainerId(containerId);
            if (staticScannerEntity != null) {
                URI uri = (new URIBuilder()).setHost(staticScannerEntity.getIpAddress())
                        .setPort(staticScannerEntity.getHostPort()).setScheme("http")
                        .setPath(staticScannerEntity.getContextPath() + StaticScannerProperties
                                .getStaticScannerGetReport()).build();
                HttpResponse response = HttpRequestHandler.sendGetRequest(uri);

                if (response != null && response.getEntity() != null) {
                    String subject = "Static Scan Report: " + staticScannerEntity.getCreatedTime();
                    mailHandler.sendMail(staticScannerEntity.getUserId(), subject, "This is auto generated message",
                            response.getEntity().getContent(), "Reports.zip");
                    staticScannerEntity.setReportSent(true);
                    staticScannerEntity.setReportSentTime(new SimpleDateFormat(AutomationManagerProperties
                            .getDatePattern()).format(new Date()));
                    kill(containerId);
                }
            }
        } catch (IOException | URISyntaxException | MessagingException e) {
            throw new AutomationManagerException("Error occurred while getting static scanner report and mail", e);
        }
    }
}
