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
import org.wso2.security.tools.scanmanager.common.internal.model.ScannerScanRequest;
import org.wso2.security.tools.scanmanager.common.model.ErrorMessage;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.scanners.common.service.Scanner;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;

import java.io.IOException;

/**
 * Web controller which defines the routines for initiating scanner operations.
 */
@Controller
@RequestMapping("scanner")
public class ScannerController {

    private static final Logger log = Logger.getLogger(ScannerController.class);
    Scanner scanner;
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
     * @param scanRequest Object that represent the required information for the scanner operation
     * @return whether the start scan request is accepted
     */
    @PostMapping("scan")
    @ResponseBody
    public ResponseEntity startScan(@RequestBody ScannerScanRequest scanRequest) {
        ResponseEntity responseEntity;

        if (!hasScanStarted) {
            log.info("Invoking start scan API.");
            responseEntity = scanner.startScan(scanRequest);
            if (responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                hasScanStarted = true;
            }
        } else {
            String message = "Cannot start a new scan since another scan is in progress.";
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                    message), HttpStatus.BAD_REQUEST);
            log.error(message);
            CallbackUtil.persistScanLog(scanRequest.getJobId(), message, LogType.ERROR);
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

        if (!hasScanStarted) {
            String message = "No scan running to perform cancellation.";
            responseEntity = new ResponseEntity<>(new ErrorMessage(HttpStatus.NOT_ACCEPTABLE.value(),
                    message), HttpStatus.BAD_REQUEST);
            log.error(message);
            CallbackUtil.persistScanLog(scanRequest.getJobId(), message, LogType.ERROR);
        } else {
            log.info("Invoking cancel scan API.");
            responseEntity = scanner.cancelScan(scanRequest);
        }
        return responseEntity;
    }
}
