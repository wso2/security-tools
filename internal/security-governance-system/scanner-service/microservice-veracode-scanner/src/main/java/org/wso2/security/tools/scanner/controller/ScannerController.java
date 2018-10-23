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

package org.wso2.security.tools.scanner.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.tools.scanner.exception.ScannerException;
import org.wso2.security.tools.scanner.scanner.ScannerRequestObject;
import org.wso2.security.tools.scanner.scanner.ScannerStatus;
import org.wso2.security.tools.scanner.service.ScannerService;

/**
 * The class {@code ScannerController} is the web controller which defines the routines for initiating
 * scanner operations
 */
@Controller
@RequestMapping("Scanner")
public class ScannerController {

    private final ScannerService cloudBasedScannerService;

    @Autowired
    public ScannerController(ScannerService cloudBasedScannerService) {
        this.cloudBasedScannerService = cloudBasedScannerService;
    }

    /**
     * Controller method to start scan using product pack as a zip file
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return Path to the scan report or status of the scan
     * @throws ScannerException
     */
    @PostMapping("runScanWithZipFile")
    @ResponseBody
    public String runScanWithZipFile(@RequestBody ScannerRequestObject scannerRequestObject)
            throws ScannerException {
        return cloudBasedScannerService.runScanUsingProductZip(scannerRequestObject);
    }

    /**
     * Controller method to start scan using github product URL
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return Path to the scan report or status of the scan
     * @throws ScannerException
     */
    @PostMapping("runScanWithProductGitURL")
    @ResponseBody
    public String runScanUsingProductGitURL(@RequestBody ScannerRequestObject scannerRequestObject)
            throws ScannerException {
        return cloudBasedScannerService.runScanUsingProductGitURL(scannerRequestObject);
    }

    /**
     * Controller method to get the status of the last scan
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return Enum of the ScannerStatus
     */
    @PostMapping("getStatus")
    @ResponseBody
    public ScannerStatus getStatus(@RequestBody ScannerRequestObject scannerRequestObject) {
        System.out.println(cloudBasedScannerService);
        return cloudBasedScannerService.getStatus(scannerRequestObject);
    }

    /**
     * Controller method to stop the last scan for a given application
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return
     */
    @PostMapping("cancelScan")
    @ResponseBody
    public boolean cancelScan(@RequestBody ScannerRequestObject scannerRequestObject) {
        return cloudBasedScannerService.cancelScan(scannerRequestObject);
    }

}

