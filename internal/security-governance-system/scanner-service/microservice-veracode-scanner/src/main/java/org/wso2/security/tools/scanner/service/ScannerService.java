/*
 *  Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanner.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.wso2.security.tools.scanner.Constants;
import org.wso2.security.tools.scanner.config.ConfigurationReader;
import org.wso2.security.tools.scanner.exception.ScannerException;
import org.wso2.security.tools.scanner.scanner.AbstractScanner;
import org.wso2.security.tools.scanner.scanner.ScannerRequestObject;
import org.wso2.security.tools.scanner.scanner.ScannerStatus;

import java.io.File;
import java.util.Observer;

/**
 * Service class to engage the controller method with the scanner
 */
@Component
public class ScannerService {

    @Autowired
    private static Class<AbstractScanner> scannerClass = null;
    @Autowired
    AbstractScanner scanner;

    private static final Log log = LogFactory.getLog(ScannerService.class);
    private ApplicationContext appContext;

    /**
     * Initiating the Scanner configurations
     */
    private void init() {
        if (scanner == null) {
            try {
                scannerClass = (Class<AbstractScanner>) Class.forName(getScannerInstanceOfScanType().getName());
            } catch (ClassNotFoundException e) {
                log.error("Error occured while loading the Scanner class. ", e);
            }

            try {
                scanner = scannerClass.newInstance();
            } catch (InstantiationException e) {
                log.error("InstantiationException occured while initiating the scannerClass. ", e);
            } catch (IllegalAccessException e) {
                log.error("IllegalAccessException occured while initiating the scannerClass. ", e);
            }

            try {
                scanner.init();
            } catch (ScannerException e) {
                log.error("Error occured while loading the configuration", e);
            }

            Observer mainScannerObserver = observe();
            scanner.addObserver(mainScannerObserver);
        }
    }

    /**
     * Run the scan using product zip file
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return the final report location or the Scan Status if failed to send the report
     */
    public String runScanUsingProductZip(ScannerRequestObject scannerRequestObject) {
        String runScan = null;

        init();

        try {
            runScan = scanner.runScanUsingProductZip(scannerRequestObject);
        } catch (ScannerException e) {
            log.error("Error occured while starting the scan.", e);
        }

        return runScan;
    }

    private Observer observe() {
        return (o, arg) -> {
            if (new File(ConfigurationReader.getConfigProperty(Constants.REPORTS_FOLDER_PATH) +
                    File.separator + Constants.VERACODE_OUTPUT_FILE_NAME).exists()) {
                System.out.println("Scan started ........ ");
            } else {
            }

        };
    }

    /**
     * Run the scan using product github location
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return the final report location or the Scan Status if failed to send the report
     * @throws ScannerException
     */
    public String runScanUsingProductGitURL(ScannerRequestObject scannerRequestObject) {
        String runScan = null;

        init();

        try {
            runScan = scanner.runScanUsingProductGitURL(scannerRequestObject);
        } catch (ScannerException e) {
            log.error("Error occured while Initiating the scan the Scanner class", e);
        }

        return runScan;
    }

    /**
     * Returns the status of the last scan
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return Enum of the ScannerStatus
     */
    public ScannerStatus getStatus(ScannerRequestObject scannerRequestObject) {
        init();

        try {
            return scanner.getLastScanStatus(scannerRequestObject);
        } catch (ScannerException e) {
            log.error("Error occured while getting scan information of a particular application.", e);
        }

        return null;
    }

    /**
     * Cancel the last scan for a given application
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return whether delete scan operation success
     */
    public boolean cancelScan(ScannerRequestObject scannerRequestObject) {
        init();

        try {
            return scanner.deleteLastScan(scannerRequestObject);
        } catch (ScannerException e) {
            log.error("Error occured while deleting the last scan application.", e);
        }

        return false;
    }

    /**
     * Get the Scanner Java class that needs to be engaged
     *
     * @return the Java Class Name
     */
    private Class getScannerInstanceOfScanType() {

        try {
            ConfigurationReader.loadConfiguration();
        } catch (ScannerException e) {
            log.error("Error while loading configuration Properties from the property file. ", e);
        }

        System.out.println(ConfigurationReader.getConfigProperty(Constants.SCANNER_BEAN_CLASS_NAME));
        Object bean = appContext.getBean(ConfigurationReader.getConfigProperty(Constants.SCANNER_BEAN_CLASS_NAME));

        Class scannerClass = bean.getClass();
        return scannerClass;
    }

}