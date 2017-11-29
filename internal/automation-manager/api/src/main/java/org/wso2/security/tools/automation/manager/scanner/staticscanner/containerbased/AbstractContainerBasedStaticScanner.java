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

package org.wso2.security.tools.automation.manager.scanner.staticscanner.containerbased;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.security.tools.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.tools.automation.manager.config.AutomationManagerProperties;
import org.wso2.security.tools.automation.manager.config.StaticScannerProperties;
import org.wso2.security.tools.automation.manager.entity.staticscanner.containerbased.ContainerBasedStaticScannerEntity;
import org.wso2.security.tools.automation.manager.exception.StaticScannerException;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.tools.automation.manager.handler.ServerHandler;
import org.wso2.security.tools.automation.manager.service.staticscanner.StaticScannerService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The abstract class {@link AbstractContainerBasedStaticScanner} implements the interface
 * {@link ContainerBasedStaticScanner} to implement common methods for container based static scanners
 */
public abstract class AbstractContainerBasedStaticScanner implements ContainerBasedStaticScanner {

    private String userId;
    private String testName;
    private String ipAddress;
    private String productName;
    private String wumLevel;
    private boolean isFileUpload;
    private File zipFile;
    private String gitUrl;
    private String gitUsername;
    private String gitPassword;
    private String contextPath;
    private String dockerImage;
    private StaticScannerService staticScannerService;
    private ContainerBasedStaticScannerEntity staticScannerEntity;

    public AbstractContainerBasedStaticScanner(ContainerBasedStaticScannerEntity staticScannerEntity, String
            contextPath, String dockerImage) {
        this.staticScannerEntity = staticScannerEntity;
        this.contextPath = contextPath;
        this.dockerImage = dockerImage;
        staticScannerService = ApplicationContextUtils.getApplicationContext().getBean(StaticScannerService.class);
    }

    /**
     * {@inheritDoc}
     */
    public void init(String userId, String testName, String ipAddress, String productName, String wumLevel, boolean
            isFileUpload, String uploadLocation, String zipFileName, String gitUrl, String gitUsername, String
                             gitPassword) {
        this.userId = userId;
        this.testName = testName;
        this.ipAddress = ipAddress;
        this.productName = productName;
        this.wumLevel = wumLevel;
        this.isFileUpload = isFileUpload;
        if (zipFileName != null) {
            this.zipFile = new File(uploadLocation + File.separator + zipFileName);
        }
        this.gitUrl = gitUrl;
        this.gitUsername = gitUsername;
        this.gitPassword = gitPassword;
    }

    /**
     * {@inheritDoc}
     */
    public void startScanner() throws StaticScannerException {
        saveMetaData();
        createContainer();
        startContainer();
        if (!hostAvailabilityCheck()) {
            throw new StaticScannerException("Error occurred while starting static scanner container");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void saveMetaData() {
        staticScannerEntity.setUserId(userId);
        staticScannerEntity.setTestName(testName);
        staticScannerEntity.setProductName(productName);
        staticScannerEntity.setWumLevel(wumLevel);
        staticScannerEntity.setStatus(AutomationManagerProperties.getStatusInitiated());
        staticScannerEntity.setContextPath(contextPath);
        staticScannerService.save(staticScannerEntity);
    }

    /**
     * {@inheritDoc}
     */
    public void createContainer() throws StaticScannerException {
        try {
            int port = ContainerBasedStaticScanner.calculateStaticScannerContainerPort(staticScannerEntity.getId());
            String containerId = DockerHandler.createContainer(dockerImage, ipAddress, String.valueOf(port),
                    String.valueOf(port), null, new String[]{"port=" + port});
            if (containerId == null) {
                throw new StaticScannerException("Error occurred while creating static scanner docker container");
            }
            saveContainerData(containerId, port);
        } catch (InterruptedException | DockerCertificateException | DockerException e) {
            throw new StaticScannerException("Error occurred while creating static scanner docker container", e);
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
        staticScannerEntity.setContainerId(containerId);
        staticScannerEntity.setIpAddress(ipAddress);
        staticScannerEntity.setContainerPort(port);
        staticScannerEntity.setHostPort(port);
        staticScannerEntity.setStatus(AutomationManagerProperties.getStatusCreated());
        staticScannerEntity.setCreatedTime(createdTime);
        staticScannerService.save(staticScannerEntity);
    }

    /**
     * {@inheritDoc}
     */
    public void startContainer() throws StaticScannerException {
        try {
            if (DockerHandler.startContainer(staticScannerEntity.getContainerId())) {
                staticScannerEntity.setStatus(AutomationManagerProperties.getStatusRunning());
                staticScannerEntity.setDockerIpAddress(DockerHandler.inspectContainer(staticScannerEntity
                        .getContainerId()).networkSettings().ipAddress());
                staticScannerService.save(staticScannerEntity);
            }
        } catch (InterruptedException | DockerCertificateException | DockerException e) {
            throw new StaticScannerException("Error occurred while starting static scanner docker container", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hostAvailabilityCheck() throws StaticScannerException {
        try {
            return ServerHandler.hostAvailabilityCheck(staticScannerEntity.getDockerIpAddress(), staticScannerEntity
                    .getHostPort(), 12 * 5);
        } catch (InterruptedException e) {
            throw new StaticScannerException("Error occurred while checking host availability");
        }
    }

    /**
     * Send request to static scanner micro service API to start a scan
     * {@inheritDoc}
     */
    public void startScan() throws StaticScannerException {
        try {
            URI uri = (new URIBuilder()).setHost(staticScannerEntity.getIpAddress())
                    .setPort(staticScannerEntity.getHostPort()).setScheme("http").setPath(contextPath +
                            StaticScannerProperties.getStaticScannerStartScan())
                    .addParameter("automationManagerHost", AutomationManagerProperties
                            .getAutomationManagerHostRelativeToContainers())
                    .addParameter("automationManagerPort", String.valueOf(AutomationManagerProperties
                            .getAutomationManagerPort()))
                    .addParameter("myContainerId", staticScannerEntity.getContainerId())
                    .addParameter("isFileUpload", String.valueOf(isFileUpload))
                    .addParameter("gitUrl", gitUrl)
                    .addParameter("gitUsername", gitUsername)
                    .addParameter("gitPassword", gitPassword)
                    .build();

            Map<String, File> files = new HashMap<>();
            if (zipFile != null) {
                files.put("zipFile", zipFile);
            }
            HttpResponse response = HttpRequestHandler.sendMultipartRequest(uri, files, null);
            if (response != null) {
                HttpRequestHandler.printResponse(response);
            }
        } catch (URISyntaxException | IOException e) {
            throw new StaticScannerException("Error occurred while starting static scan", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHost() {
        return staticScannerEntity.getDockerIpAddress();
    }

    /**
     * {@inheritDoc}
     */
    public int getPort() {
        return staticScannerEntity.getHostPort();
    }
}