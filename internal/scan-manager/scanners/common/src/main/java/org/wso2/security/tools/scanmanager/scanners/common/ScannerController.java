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
 */

package org.wso2.security.tools.scanmanager.scanners.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.tools.scanmanager.common.internal.model.ScannerScanRequest;
import org.wso2.security.tools.scanmanager.common.model.ErrorMessage;
import org.wso2.security.tools.scanmanager.scanners.common.service.Scanner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Web controller which defines the routines for initiating scanner operations.
 */
@Controller
@RequestMapping("scanner")
public class ScannerController {

    private static final Logger log = LogManager.getLogger(ScannerController.class);
    Scanner scanner;

    // Scan task thread.
    Thread startScanThread;

    // This represents if a scan is started.
    private boolean hasScanStarted = false;

    @Autowired
    public ScannerController(Scanner scanner) throws IOException {
        log.info("Scanner Service is initialised in the container...");
        this.scanner = scanner;
    }

    /**
     * Start a new scan.
     *
     * @param scannerScanRequest Object that represent the required information for the scanner operation
     * @return whether the start scan request is accepted
     */
    @PostMapping("scan")
    @ResponseBody
    public ResponseEntity startScan(@RequestBody ScannerScanRequest scannerScanRequest) {
        ResponseEntity responseEntity;
        responseEntity = validateStartScanReq(scannerScanRequest);
        if (responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {

            // If current scan request is to initiate new scan
            if (!Boolean.parseBoolean(scannerScanRequest.getIsResume())) {
                if (scanner.validateStartScan(scannerScanRequest)) {
                    log.info("Invoking start scan API.");
                    startScanThread = new Thread(() -> scanner.startScan(scannerScanRequest), "StartScanThread");
                    startScanThread.start();
                    hasScanStarted = true;
                } else {
                    String message = "Start scan request validation is failed.";
                    log.error(message);
                    responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), message),
                            HttpStatus.BAD_REQUEST);
                }
            } else {

                // If current scan request is to resume scan
                log.info("Invoking start scan API for resume scan.");
                startScanThread = new Thread(() -> scanner.resumeScan(scannerScanRequest), "StartResumeScanThread");
                startScanThread.start();
                hasScanStarted = true;
            }
        }
        return responseEntity;
    }

    private ResponseEntity validateStartScanReq(ScannerScanRequest scannerScanRequest) {
        ResponseEntity responseEntity;
        if (hasScanStarted) {
            String message = "Cannot start a new scan since another scan is in progress.";
            log.error(message);
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                    message), HttpStatus.BAD_REQUEST);
        } else {
            if (StringUtils.isEmpty(scannerScanRequest.getAppId())) {
                String message = "Application Id is missing in the request.";
                log.error(message);
                responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                        message), HttpStatus.BAD_REQUEST);
            } else {
                if (StringUtils.isEmpty(scannerScanRequest.getJobId())) {
                    String message = "Job Id is missing in the request.";
                    log.error(message);
                    responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                            message), HttpStatus.BAD_REQUEST);
                } else {
                    responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
                }
            }
        }
        return responseEntity;
    }

    /**
     * Stop the scan.
     *
     * @param scanRequest Object that represent the required information for the scanner operation
     * @return whether the cancel scan request is accepted
     */
    @DeleteMapping("scan")
    @ResponseBody
    public ResponseEntity cancelScan(@RequestBody ScannerScanRequest scanRequest) {
        ResponseEntity responseEntity;

        if (hasScanStarted) {
            log.info("Invoking cancel scan API.");
            if (scanner.validateCancelScan(scanRequest)) {
                if (startScanThread != null) {
                    startScanThread.interrupt();
                } else {
                    log.info("There is no running scan thread to cancel.");
                }

                new Thread(() -> {
                    stopStartScanThread();
                    scanner.cancelScan(scanRequest);
                }, "CancelScanThread").start();

                responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
            } else {
                String message = "Cancel scan request validation is failed.";
                log.error(message);
                responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                        message), HttpStatus.BAD_REQUEST);
            }
        } else {
            String message = "No scan running to perform cancellation.";
            log.error(message);
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.NOT_ACCEPTABLE.value(),
                    message), HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    private boolean stopStartScanThread() {
        while (startScanThread.isAlive()) {
            // run until the start scan thread is dead.
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                log.error("Interrupted exception occured while waiting till the start scan thread is dead. \n" +
                        e.getMessage());
            }
        }
        return true;
    }
}
