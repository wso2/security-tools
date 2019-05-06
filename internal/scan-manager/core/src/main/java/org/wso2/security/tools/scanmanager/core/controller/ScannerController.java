/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.core.exception.InvalidRequestException;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.core.service.ScannerService;

import java.util.List;

/**
 * Web controller which defines the routines for managing scanners.
 */
@Controller
@RequestMapping("scan-manager")
public class ScannerController {

    private ScannerService scannerService;

    @Autowired
    public ScannerController(ScannerService scannerService) {
        this.scannerService = scannerService;
    }

    /**
     * Get the list  of available scanners.
     *
     * @return a list of available scanners
     */
    @GetMapping(path = "scanners")
    @ResponseBody
    public ResponseEntity<List<Scanner>> getScanners() {
        return new ResponseEntity<>(scannerService.getAll(), HttpStatus.OK);
    }

    /**
     * Add a scanner.
     *
     * @param scanner scanner details to be added or updated
     * @return inserted scanner details
     */
    @PutMapping(path = "scanners", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Scanner> addScanner(@RequestBody Scanner scanner) {
        if (scannerService.insert(scanner) != null) {
            return new ResponseEntity<>(scannerService.getById(scanner.getId()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update a scanner.
     *
     * @param scanner scanner details to be added or updated
     * @return inserted scanner details
     */
    @PostMapping(path = "scanners", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Scanner> updateScanner(@RequestBody Scanner scanner) {
        if (scannerService.update(scanner) != null) {
            return new ResponseEntity<>(scannerService.getById(scanner.getId()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Remove a scanner.
     *
     * @param scannerId scanner id of the scanner to be removed
     * @return success response if the scanner was removed successfully
     * @throws InvalidRequestException when the provided scanner id is invalid
     */
    @DeleteMapping(path = "scanners/{scannerId}")
    @ResponseBody
    public ResponseEntity removeScanner(@PathVariable("scannerId") String scannerId) throws InvalidRequestException,
            ScanManagerException {
        Scanner scanner = scannerService.getById(scannerId);
        if (scanner != null) {
            scannerService.removeByScannerId(scannerId);
            return new ResponseEntity<>(scanner, HttpStatus.OK);
        } else {
            throw new InvalidRequestException("Invalid scanner id: " + scannerId);
        }
    }
}
