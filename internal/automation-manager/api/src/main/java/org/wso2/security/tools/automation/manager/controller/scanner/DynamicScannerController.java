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

package org.wso2.security.tools.automation.manager.controller.scanner;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.service.dynamicscanner.ContainerBasedDynamicScannerService;
import org.wso2.security.tools.automation.manager.service.dynamicscanner.DynamicScannerService;

/**
 * The class {@code DynamicScannerController} is the web controller which defines the routines for initiating dynamic
 * scans.
 */
@Controller
@RequestMapping("dynamicScanner")
@Api(value = "dynamicScanner", description = "APIs to initiate dynamic scans such as ZAP and other dynamic scanner " +
        "related controller methods")
public class DynamicScannerController {

    private final DynamicScannerService dynamicScannerService;
    private final ContainerBasedDynamicScannerService containerBasedDynamicScannerService;

    @Autowired
    public DynamicScannerController(DynamicScannerService dynamicScannerService, ContainerBasedDynamicScannerService
            containerBasedDynamicScannerService) {
        this.dynamicScannerService = dynamicScannerService;
        this.containerBasedDynamicScannerService = containerBasedDynamicScannerService;
    }

    /**
     * The general contract of the method is to call {@code startScan} method in {@code DynamicScannerService} class.
     * Then the service level method will validate the request and execute the scanning process
     *
     * @param scanType           Indicates the dynamic scan type. Eg: ZAP, Qualis
     * @param userId             Unique identifier of a logged in user. Here email address is taken from
     *                           authenticated user
     * @param testName           User defined name for the test
     * @param productName        Name of the product to be scanned
     * @param wumLevel           WUM level of the product. Default value will be 0
     * @param productUploadAsZip Indicates whether the product is uploaded. False means the product is already in up and
     *                           running status
     * @param zipFile            ZIP file of the product to be scanned. This is not required if {@code isFileUpload}
     *                           parameter is false
     * @param urlListFile        URLs of the product to be scanned is sent as a file
     * @param wso2ServerHost     Host address if the product is already in up and running state
     * @param wso2ServerPort     Port of a product which is in up and running state
     * @throws AutomationManagerException The general exception thrown by Automation Manager
     */
    @PostMapping(value = "startScan")
    @ApiOperation(value = "Start ProductManager, upload the product zip file or else give IP address and " +
            "port of already running server, start dynamic scanner container or else call APIs of cloud based " +
            "dynamic scanner and start a dynamic scan")
    public @ResponseBody
    void startScan(@RequestParam String scanType, @RequestParam String userId,
                   @RequestParam String testName,
                   @RequestParam String productName,
                   @RequestParam(defaultValue = "0") String wumLevel,
                   @RequestParam boolean productUploadAsZip,
                   @RequestParam(required = false) MultipartFile zipFile,
                   @RequestParam MultipartFile urlListFile,
                   @RequestParam(required = false) String wso2ServerHost,
                   @RequestParam(required = false, defaultValue = "-1") int wso2ServerPort) throws
            AutomationManagerException {
        dynamicScannerService.startScan(scanType, userId, testName, productName, wumLevel, productUploadAsZip, zipFile,
                urlListFile, wso2ServerHost, wso2ServerPort);
    }

    /**
     * This method is to kill a running or stopped container, if any error occurs
     *
     * @param containerId Container Id of the container to be killed
     * @throws AutomationManagerException The general exception thrown by Automation Manager
     */
    @GetMapping(path = "kill")
    @ApiOperation(value = "Stop a running dynamic scanner container")
    public @ResponseBody
    void kill(@RequestParam String containerId) throws AutomationManagerException {
        containerBasedDynamicScannerService.kill(containerId);

    }
}