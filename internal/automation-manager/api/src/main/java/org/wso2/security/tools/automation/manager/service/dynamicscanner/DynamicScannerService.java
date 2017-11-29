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

package org.wso2.security.tools.automation.manager.service.dynamicscanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.automation.manager.config.AutomationManagerProperties;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.DynamicScannerEntity;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.handler.FileHandler;
import org.wso2.security.tools.automation.manager.repository.dynamicscanner.DynamicScannerRepository;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.DynamicScanner;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.DynamicScannerExecutor;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.cloudbased.CloudBasedDynamicScanner;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.cloudbased.CloudBasedDynamicScannerEnum;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased.ContainerBasedDynamicScanner;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased
        .ContainerBasedDynamicScannerEnum;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.factory.AbstractDynamicScannerFactory;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.factory.DynamicScannerFactoryProducer;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager.CloudBasedProductManager;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager.ContainerBasedProductManager;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager.ProductManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service layer methods to handle container based dynamic scanners
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Service
public class DynamicScannerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicScannerService.class);

    private final DynamicScannerRepository dynamicScannerRepository;

    @Autowired
    public DynamicScannerService(DynamicScannerRepository dynamicScannerRepository) {
        this.dynamicScannerRepository = dynamicScannerRepository;
    }

    /**
     * Get Iterable dynamic scanner entity list
     *
     * @return Iterable list of {@link DynamicScannerEntity}
     */
    public Iterable<DynamicScannerEntity> findAll() {
        return dynamicScannerRepository.findAll();
    }

    /**
     * Find a dynamic scanner entity by a unique id
     *
     * @param id Auto generated database id of dynamic scanner
     * @return {@link DynamicScannerEntity}
     */
    public DynamicScannerEntity findOne(int id) {
        return dynamicScannerRepository.findOne(id);
    }

    /**
     * Get Iterable dynamic scanner entity list of a specific user
     *
     * @param userId User id
     * @return Iterable list of {@link DynamicScannerEntity}
     */
    public Iterable<DynamicScannerEntity> findByUserId(String userId) {
        return dynamicScannerRepository.findByUserId(userId);
    }

    /**
     * Save a dynamic scanner entity
     *
     * @param dynamicScannerEntity Dynamic scanner entity
     * @return {@link DynamicScannerEntity} that saves in the database
     */
    public DynamicScannerEntity save(DynamicScannerEntity dynamicScannerEntity) {
        return dynamicScannerRepository.save(dynamicScannerEntity);
    }

    /**
     * {@inheritDoc}
     */
    public void startScan(String scanType, String userId, String testName, String productName, String wumLevel,
                          boolean productUploadAsZipFile, MultipartFile zipFile, MultipartFile urlListFile, String
                                  wso2ServerHost, int wso2ServerPort) throws
            AutomationManagerException {

        try {
            String fileUploadLocation = AutomationManagerProperties.getTempFolderPath() + File.separator + userId + new
                    SimpleDateFormat(AutomationManagerProperties.getDatePattern()).format(new Date());
            String urlListFileName = urlListFile.getOriginalFilename();
            DynamicScanner dynamicScanner = null;
            ProductManager productManager;
            if (productUploadAsZipFile) {
                if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                    throw new AutomationManagerException("Please upload a zip file");
                }
                uploadFileToTempDirectory(fileUploadLocation, zipFile);
            }
            uploadFileToTempDirectory(fileUploadLocation, urlListFile);
            if (isCloudBasedDynamicScanner(scanType)) {
                dynamicScanner = createAndInitCloudBasedDynamicScanner(scanType, userId, fileUploadLocation,
                        urlListFileName);
            } else if (isContainerBasedDynamicScanner(scanType)) {
                dynamicScanner = createAndInitContainerBasedDynamicScanner(scanType, userId, fileUploadLocation,
                        urlListFileName);
            }

            if (dynamicScanner == null) {
                throw new AutomationManagerException("Error occurred while creating dynamic scanner");
            }
            if (productUploadAsZipFile) {
                productManager = createAndInitContainerBasedProductManager(userId, testName, productName, wumLevel,
                        fileUploadLocation, zipFile.getOriginalFilename());
            } else {
                productManager = createAndInitCloudBasedProductManager(userId, testName, productName, wumLevel,
                        wso2ServerHost, wso2ServerPort);
            }
            if (productManager == null) {
                throw new AutomationManagerException("Error occurred while creating product manager");
            }
            DynamicScannerExecutor dynamicScannerExecutor = new DynamicScannerExecutor(productManager, dynamicScanner);
            new Thread(dynamicScannerExecutor).start();
        } catch (IOException e) {
            throw new AutomationManagerException("I/O error occurred while uploading file", e);
        }
    }

    private void uploadFileToTempDirectory(String fileUploadLocation, MultipartFile file) throws IOException {
        File tempDirectory = new File(AutomationManagerProperties.getTempFolderPath());
        File uploadDirectory = new File(fileUploadLocation);

        if (tempDirectory.exists() || tempDirectory.mkdir()) {
            if (uploadDirectory.exists() || uploadDirectory.mkdir()) {
                String filename = file.getOriginalFilename();
                FileHandler.uploadFile(file, fileUploadLocation + File.separator + filename);
            }
        }
    }

    /**
     * Check if a given scan type is a cloud based one.
     *
     * @param scanType Scan type
     * @return Boolean to indicate the scanner is a cloud based
     */
    private boolean isCloudBasedDynamicScanner(String scanType) {
        for (CloudBasedDynamicScannerEnum e : CloudBasedDynamicScannerEnum.values()) {
            if (e.name().equalsIgnoreCase(scanType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a given scan type is a container based one.
     *
     * @param scanType Scan type
     * @return Boolean to indicate the scanner is a cloud based
     */
    private boolean isContainerBasedDynamicScanner(String scanType) {
        for (ContainerBasedDynamicScannerEnum e : ContainerBasedDynamicScannerEnum.values()) {
            if (e.name().equalsIgnoreCase(scanType)) {
                return true;
            }
        }
        return false;
    }

    private CloudBasedDynamicScanner createAndInitCloudBasedDynamicScanner(String scanType, String userId, String
            fileUploadLocation, String urlListFileName) throws
            AutomationManagerException {
        String factoryType = "cloud";
        String scannerHost = "";
        int scannerPort = 0;
        AbstractDynamicScannerFactory dynamicScannerFactory = DynamicScannerFactoryProducer.getDynamicScannerFactory
                (factoryType);
        if (dynamicScannerFactory == null) {
            throw new AutomationManagerException("Cannot create dynamic scanner factory");
        }
        CloudBasedDynamicScanner dynamicScanner = dynamicScannerFactory.getCloudBasedDynamicScanner(scanType);
        if (dynamicScanner == null) {
            throw new AutomationManagerException("Dynamic scanner cannot be created");
        }
        dynamicScanner.init(userId, fileUploadLocation, urlListFileName, scannerHost, scannerPort);
        return dynamicScanner;
    }

    private ContainerBasedDynamicScanner createAndInitContainerBasedDynamicScanner(String scanType, String userId,
                                                                                   String fileUploadLocation, String
                                                                                           urlListFileName) throws
            AutomationManagerException {
        String factoryType = "container";
        AbstractDynamicScannerFactory dynamicScannerFactory = DynamicScannerFactoryProducer.getDynamicScannerFactory
                (factoryType);
        if (dynamicScannerFactory == null) {
            throw new AutomationManagerException("Cannot create dynamic scanner factory");
        }
        ContainerBasedDynamicScanner dynamicScanner = dynamicScannerFactory.getContainerBasedDynamicScanner(scanType);
        if (dynamicScanner == null) {
            throw new AutomationManagerException("Dynamic scanner cannot be created");
        }
        dynamicScanner.init(userId, AutomationManagerProperties.getIpAddress(), fileUploadLocation, urlListFileName);
        return dynamicScanner;
    }

    private ContainerBasedProductManager createAndInitContainerBasedProductManager(String userId, String testName,
                                                                                   String productName, String wumLevel,
                                                                                   String fileUploadLocation, String
                                                                                           zipFileName) {
        ContainerBasedProductManager productManager = new ContainerBasedProductManager();
        productManager.init(userId, testName, AutomationManagerProperties.getIpAddress(), productName, wumLevel,
                fileUploadLocation, zipFileName);
        return productManager;
    }

    private CloudBasedProductManager createAndInitCloudBasedProductManager(String userId, String testName,
                                                                           String productName, String wumLevel,
                                                                           String wso2serverHost, int wso2ServerPort) {
        CloudBasedProductManager productManager = new CloudBasedProductManager();
        productManager.init(userId, testName, AutomationManagerProperties.getIpAddress(), productName, wumLevel,
                wso2serverHost, wso2ServerPort);
        return productManager;
    }
}