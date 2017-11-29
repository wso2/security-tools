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

package org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.automation.manager.config.ApplicationContextUtils;
import org.wso2.security.tools.automation.manager.config.AutomationManagerProperties;
import org.wso2.security.tools.automation.manager.config.ProductManagerProperties;
import org.wso2.security.tools.automation.manager.entity.productmanager.containerbased
        .ContainerBasedProductManagerEntity;
import org.wso2.security.tools.automation.manager.exception.ProductManagerException;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.tools.automation.manager.handler.ServerHandler;
import org.wso2.security.tools.automation.manager.service.productmanager.ProductManagerService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The class {@link ContainerBasedProductManager} implements the interface {@link ProductManager} methods to handle
 * container based product managers
 */
public class ContainerBasedProductManager implements ProductManager {

    private String userId;
    private String testName;
    private String ipAddress;
    private String productName;
    private String wumLevel;
    private File zipFile;
    private ProductManagerService productManagerService;
    private ContainerBasedProductManagerEntity productManagerEntity;

    public ContainerBasedProductManager() {
        productManagerService = ApplicationContextUtils.getApplicationContext().getBean(ProductManagerService.class);
    }

    /**
     * Calculates the container port of the product manager
     *
     * @param id Database generated id of the product manager
     * @return Port of the container
     */
    private static int calculateContainerPort(int id) {
        if (20000 + id > 40000) {
            id = 1;
        }
        return (20000 + id) % 40000;
    }

    /**
     * Initialize the product manager
     *
     * @param userId             User Id
     * @param testName           Test name
     * @param ipAddress          Ip address where the container spawns
     * @param productName        Product name to be scanned
     * @param wumLevel           WUM level of the product
     * @param fileUploadLocation File upload location of the host (User uploaded files are in a temporary directory
     *                           in the host machine. Therefore, uploaded location is required to get the files and
     *                           upload to containers)
     * @param zipFileName        Zip file name
     */
    public void init(String userId, String testName, String ipAddress, String productName, String wumLevel,
                     String fileUploadLocation, String zipFileName) {
        this.userId = userId;
        this.testName = testName;
        this.ipAddress = ipAddress;
        this.productName = productName;
        this.wumLevel = wumLevel;
        if (zipFileName != null) {
            this.zipFile = new File(fileUploadLocation + File.separator + zipFileName);
        }
    }

    /**
     * Save the meta data of product manager, create container, start container and check for host availability
     *
     * @param relatedDynamicScannerId Related dynamic scanner Id that the product manager belongs to
     * @throws ProductManagerException The general exception thrown by product managers
     */
    @Override
    public void startProductManager(int relatedDynamicScannerId) throws ProductManagerException {
        saveMetaData(relatedDynamicScannerId);
        createContainer();
        startContainer();
        if (!checkProductManagerHostAvailability()) {
            throw new ProductManagerException("Error occurred while starting product manager");
        }
    }

    private void saveMetaData(int relatedDynamicScannerId) {
        productManagerEntity = new ContainerBasedProductManagerEntity();
        productManagerEntity.setUserId(userId);
        productManagerEntity.setTestName(testName);
        productManagerEntity.setRelatedDynamicScannerId(relatedDynamicScannerId);
        productManagerEntity.setIpAddress(ipAddress);
        productManagerEntity.setProductName(productName);
        productManagerEntity.setWumLevel(wumLevel);
        productManagerEntity.setStatus(AutomationManagerProperties.getStatusInitiated());
        productManagerService.save(productManagerEntity);
    }

    private void createContainer() throws ProductManagerException {
        try {
            int port = calculateContainerPort(productManagerEntity.getId());
            String containerId = DockerHandler.createContainer(ProductManagerProperties
                    .getProductManagerDockerImage(), ipAddress, String.valueOf(port), String.valueOf(port), null, new
                    String[]{"port=" + port});

            if (containerId == null) {
                throw new ProductManagerException("Error occurred while creating product manager");
            }
            String createdTime = new SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format(new Date());
            productManagerEntity.setContainerId(containerId);
            productManagerEntity.setDockerIpAddress(ipAddress);
            productManagerEntity.setContainerPort(port);
            productManagerEntity.setHostPort(port);
            productManagerEntity.setStatus(AutomationManagerProperties.getStatusCreated());
            productManagerEntity.setCreatedTime(createdTime);
            productManagerService.save(productManagerEntity);

        } catch (InterruptedException | DockerCertificateException | DockerException e) {
            throw new ProductManagerException("Error occurred while creating product manager container", e);
        }
    }

    private void startContainer() throws ProductManagerException {
        try {
            if (DockerHandler.startContainer(productManagerEntity.getContainerId())) {
                productManagerEntity.setStatus(AutomationManagerProperties.getStatusRunning());
                productManagerEntity.setDockerIpAddress(DockerHandler.inspectContainer(productManagerEntity
                        .getContainerId()).networkSettings().ipAddress());
                productManagerService.save(productManagerEntity);
            }
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new ProductManagerException("Error occurred while starting product manager container", e);
        }
    }

    private boolean checkProductManagerHostAvailability() throws ProductManagerException {
        try {
            return ServerHandler.hostAvailabilityCheck(productManagerEntity.getDockerIpAddress(), productManagerEntity
                    .getHostPort(), 12 * 5);
        } catch (InterruptedException e) {
            throw new ProductManagerException("Error occurred while checking host availability of product manager", e);
        }
    }

    /**
     * Sends HTTP request to the micro service which runs inside product manager containers
     *
     * @return Boolean to indicate server is started
     * @throws ProductManagerException The general exception thrown by product managers
     */
    @Override
    public boolean startServer() throws ProductManagerException {
        boolean serverStarted = false;
        try {
            URI uri = (new URIBuilder()).setHost(productManagerEntity.getDockerIpAddress())
                    .setPort(productManagerEntity.getHostPort()).setScheme("http")
                    .setPath(ProductManagerProperties.getProductManagerStartServer())
                    .addParameter("automationManagerHost", AutomationManagerProperties
                            .getAutomationManagerHostRelativeToContainers())
                    .addParameter("automationManagerPort", String.valueOf(AutomationManagerProperties
                            .getAutomationManagerPort()))
                    .addParameter("myContainerId", productManagerEntity.getContainerId())
                    .build();
            Map<String, File> files = new HashMap<>();
            files.put("zipFile", zipFile);

            HttpResponse response = HttpRequestHandler.sendMultipartRequest(uri, files, null);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                serverStarted = checkWso2ServerHostAvailability();
            }
        } catch (URISyntaxException | IOException e) {
            throw new ProductManagerException("Error occurred while starting wso2 server", e);
        }
        return serverStarted;
    }

    private boolean checkWso2ServerHostAvailability() throws ProductManagerException {
        try {
            return ServerHandler.hostAvailabilityCheck(productManagerEntity.getDockerIpAddress(),
                    ProductManagerProperties.getProductManagerProductPort(), 12 * 5);
        } catch (InterruptedException e) {
            throw new ProductManagerException("Error occurred while checking host availability of product manager", e);
        }
    }

    /**
     * Get the docker host of the product manager (since product manager is a container)
     *
     * @return Product Manager docker host
     */
    @Override
    public String getHost() {
        return productManagerEntity.getDockerIpAddress();
    }

    /**
     * @return Port of the product manager container
     */
    @Override
    public int getPort() {
        return productManagerEntity.getHostPort();
    }
}
