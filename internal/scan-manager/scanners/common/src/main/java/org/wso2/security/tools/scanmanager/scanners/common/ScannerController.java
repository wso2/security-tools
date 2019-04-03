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

package org.wso2.security.tools.scanmanager.scanners.common;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.common.ErrorMessage;
import org.wso2.security.tools.scanmanager.scanners.common.service.Scanner;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.common.ScanRequest;

import java.io.IOException;

/**
 * The class {@code ScannerController} is the web controller which defines the routines for initiating
 * scanner operations.
 */
@Controller
@RequestMapping("scanner")
public class ScannerController {

    // This represents if a scan is started
    private boolean hasScanStarted = false;
    private static final Logger log = Logger.getLogger(ScannerController.class);

    @Autowired
    Scanner scanner;

    public ScannerController(Scanner scanner) throws IOException {
        log.info("Scanner Service is initialised in the container...");
        scanner.init();
    }

    /**
     * Controller method to start scan.
     *
     * @param scanRequest Object that represent the required information for tha scanner operation
     * @return Path to the scan report or status of the scan
     * @throws ScannerException
     */
    @PostMapping("scan")
    @ResponseBody
    public ResponseEntity startScan(@RequestBody ScanRequest scanRequest) {
        ResponseEntity responseEntity;

        if (!hasScanStarted) {
            log.info("Start scan API is being called. ");
            responseEntity = scanner.startScan(scanRequest);
            if (responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                hasScanStarted = true;
            }
        } else {
            String message = "You already has initialised an scan. You can't proceed with another scan now.";
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                    message), HttpStatus.BAD_REQUEST);
            log.error(message);
            CallbackUtil.persistScanLog(scanRequest.getJobId(), message, ScannerConstants.ERROR);
        }
        return responseEntity;
    }

    /**
     * Controller method to stop the last scan for a given application.
     *
     * @param scanRequest Object that represent the required information for tha scanner operation
     * @return
     */
    @DeleteMapping("scan")
    @ResponseBody
    public ResponseEntity cancelScan(@RequestBody ScanRequest scanRequest) {
        ResponseEntity responseEntity;

        if (!hasScanStarted) {
            String message = "First you should have started a scan to proceed with the cancel scan operation. ";
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.NOT_ACCEPTABLE.value(),
                    message), HttpStatus.BAD_REQUEST);
            log.error(message);
            CallbackUtil.persistScanLog(scanRequest.getJobId(), message, ScannerConstants.ERROR);
        } else {
            log.info("Cancel scan API is being called. ");
            responseEntity = scanner.cancelScan(scanRequest);
        }
        return responseEntity;
    }

}
