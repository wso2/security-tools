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
import org.wso2.security.tools.automation.manager.service.staticscanner.ContainerBasedStaticScannerService;
import org.wso2.security.tools.automation.manager.service.staticscanner.StaticScannerService;

/**
 * The class {@code StaticScannerController} is the web controller which defines the routines for initiating static
 * scans.
 */
@Controller
@RequestMapping("staticScanner")
@Api(value = "staticScanner", description = "APIs to initiate static scans such as ZAP and other static scanner " +
        "related controller methods")
public class StaticScannerController {

    private final StaticScannerService staticScannerService;
    private final ContainerBasedStaticScannerService containerBasedStaticScannerService;

    @Autowired
    public StaticScannerController(StaticScannerService staticScannerService, ContainerBasedStaticScannerService
            containerBasedStaticScannerService) {
        this.staticScannerService = staticScannerService;
        this.containerBasedStaticScannerService = containerBasedStaticScannerService;
    }

    /**
     * The general contract of the method is to call {@code startScan} method in {@code StaticScannerService} class.
     * Then the service level method will validate the request and execute the scanning process
     *
     * @param scanType              Indicates the dynamic scan type. Eg: ZAP, Qualis
     * @param userId                Unique identifier of a logged in user. Here email address is taken from
     *                              authenticated user
     * @param testName              User defined name for the test
     * @param productName           Name of the product to be scanned
     * @param wumLevel              WUM level of the product. Default value will be 0
     * @param sourceCodeUploadAsZip Indicates whether the product is uploaded. False means the product is already in
     *                              up and
     *                              running status
     * @param zipFile               ZIP file of the product to be scanned. This is not required if {@code isFileUpload}
     *                              parameter is false
     * @param gitUrl                GitHub URL of the product to be cloned. By default, master branch is cloned. If a
     *                              specific
     *                              branch or tag needs to be cloned, the URL should point the specified branch or tag
     * @throws AutomationManagerException The general exception thrown by Automation Manager
     */
    @PostMapping(value = "startScan")
    @ApiOperation(value = "Start static scanner, upload the product zip file or else clone product from GitHub and " +
            "start scans - FindSecBugsEntity and/or OWASP Dependency Check")
    void startScan(@RequestParam String scanType,
                   @RequestParam String userId,
                   @RequestParam String testName,
                   @RequestParam String productName,
                   @RequestParam String wumLevel,
                   @RequestParam boolean sourceCodeUploadAsZip,
                   @RequestParam(required = false) MultipartFile zipFile,
                   @RequestParam(required = false) String gitUrl) throws AutomationManagerException {
        staticScannerService.startScan(scanType, userId, testName, productName, wumLevel, sourceCodeUploadAsZip,
                zipFile, gitUrl);
    }

    /**
     * This method is to kill a running or stopped container, if any error occurs
     *
     * @param containerId Container Id of the container to be killed
     * @throws AutomationManagerException The general exception thrown by Automation Manager
     */
    @GetMapping(path = "kill")
    @ApiOperation(value = "Stop a running static scanner container")
    public @ResponseBody
    void kill(@RequestParam String containerId) throws AutomationManagerException {
        containerBasedStaticScannerService.kill(containerId);
    }
}
