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

package org.wso2.security.tools.veracode.scanner.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.tools.veracode.scanner.exception.InvalidRequestException;
import org.wso2.security.tools.veracode.scanner.exception.ScannerException;
import org.wso2.security.tools.veracode.scanner.service.ScannerService;
import org.wso2.security.tools.veracode.scanner.utils.ScannerRequest;
import org.wso2.security.tools.veracode.scanner.utils.ScannerResponse;

/**
 * The class {@code ScannerController} is the web controller which defines the routines for initiating
 * scanner operations.
 */
@Controller
@RequestMapping("scanner")
public class ScannerController {

    private final ScannerService scannerService;

    @Autowired
    public ScannerController(ScannerService scannerService) throws ScannerException {
        this.scannerService = scannerService;
        scannerService.init();
    }

    /**
     * Controller method to start scan.
     *
     * @param scannerRequest Object that represent the required information for tha scanner operation
     * @return Path to the scan report or status of the scan
     * @throws ScannerException
     */
    @PostMapping("start-scan")
    @ResponseBody
    public ResponseEntity<ScannerResponse> startScan(@RequestBody ScannerRequest scannerRequest)
            throws InvalidRequestException {
        ScannerResponse scannerResponse;
        scannerResponse = scannerService.startScan(scannerRequest);

        return new ResponseEntity<>(scannerResponse, HttpStatus.OK);
    }

    /**
     * Controller method to stop the last scan for a given application.
     *
     * @param scannerRequest Object that represent the required information for tha scanner operation
     * @return
     */
    @PostMapping("cancel-scan")
    @ResponseBody
    public ResponseEntity<ScannerResponse> cancelScan(@RequestBody ScannerRequest scannerRequest)
            throws ScannerException {
        ScannerResponse scannerResponse;
        scannerResponse = scannerService.cancelScan(scannerRequest);

        return new ResponseEntity<>(scannerResponse, HttpStatus.OK);
    }

}
