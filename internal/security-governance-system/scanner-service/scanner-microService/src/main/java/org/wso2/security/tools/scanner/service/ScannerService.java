/*
 *  Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.security.tools.veracode.scanner.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wso2.security.tools.veracode.scanner.ScannerConstants;
import org.wso2.security.tools.veracode.scanner.config.ConfigurationReader;
import org.wso2.security.tools.veracode.scanner.exception.InvalidRequestException;
import org.wso2.security.tools.veracode.scanner.exception.ScannerException;
import org.wso2.security.tools.veracode.scanner.scanner.Scanner;
import org.wso2.security.tools.veracode.scanner.utils.ScannerRequest;
import org.wso2.security.tools.veracode.scanner.utils.ScannerResponse;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * Service class to engage the controller method with the scannerClass.
 */
@Component("ScannerService")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ScannerService {


    private static final Logger log = Logger.getLogger(ScannerService.class);
    private static volatile boolean isScanStart = false;

    @Autowired
    Scanner scanner;
    @Autowired
    private ApplicationContext appContext;

    ScannerService() {
        if (log.isDebugEnabled()) {
            log.debug("Scanner Service is created. ");
        }
    }

    /**
     * Initiating the Scanner configurations.
     */
    public void init() throws ScannerException {
        Class<Scanner> scannerClass;
        log.info("Scanner Service is initialised in the container...");

        try {
            scannerClass = (Class<Scanner>) Class.forName(getScannerInstanceOfScanType().getName());
            scanner = scannerClass.newInstance();
            scanner.init();
        } catch (InstantiationException | IllegalAccessException | FileNotFoundException
                | UnsupportedEncodingException | ClassNotFoundException e) {
            log.error("Error occured while initiating the scannerClass.", e);
            throw new ScannerException("Error occured while creating the instance of the scanner class. ", e);
        }
    }

    /**
     * Run the scan.
     *
     * @param scannerRequest Object that represent the required information for tha scannerClass operation
     * @return the scannerResponse object.
     */
    public ScannerResponse startScan(ScannerRequest scannerRequest) throws InvalidRequestException {
        ScannerResponse scannerResponse = new ScannerResponse();
        if (!isScanStart) {
            log.info("Start scan API is being called. ");
            scannerResponse = scanner.startScan(scannerRequest);
            if (scannerResponse.getIsSuccessful()) {
                isScanStart = true;
            }
        } else {
            log.warn("You already has initialised an scan. You can't proceed with another scan now.");
            scannerResponse.setIsSuccessful(false);
        }
        return scannerResponse;
    }

    /**
     * Cancel the last scan for a given application.
     *
     * @param scannerRequest Object that represent the required information for tha scannerClass operation
     * @return whether delete scan operation success
     */
    public ScannerResponse cancelScan(ScannerRequest scannerRequest) throws ScannerException {
        ScannerResponse scannerResponse = new ScannerResponse();
        if (scanner == null) {
            log.error("First you should have started a scan to proceed with the cancel scan operation.  ");
            scannerResponse.setIsSuccessful(false);
        } else {
            log.info("Cancel scan API is being called. ");
            scannerResponse = scanner.cancelScan(scannerRequest);
        }
        return scannerResponse;
    }

    /**
     * get the Scanner class from the configuration.
     *
     * @return the scanner class
     */
    private Class getScannerInstanceOfScanType() throws FileNotFoundException {
        ConfigurationReader.loadConfiguration();
        Object beanScannerClass = appContext.getBean(ConfigurationReader.getConfigProperty(ScannerConstants.
                SCANNER_BEAN_CLASS_NAME));

        return beanScannerClass.getClass();
    }

}
