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

package org.wso2.security.tools.automation.manager.controller.notification;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.tools.automation.manager.service.staticscanner.ContainerBasedStaticScannerService;

/**
 * The main contract of the {@code StaticScannerNotificationController} class is to provide an API to be called by
 * {@code StaticScanner} Docker containers. (eg: findsecbugs_scanner, dependency_check_scanner)
 * <p>When a {@code StaticScanner} Docker container is initialized to run a task, it will run asynchronously. Therefore
 * in order to track whether a task is completed or not, Docker container is configured to send back notifications to
 * APIs defined here.
 */
@Controller
@RequestMapping("staticScanner/notify")
@Api(value = "staticScannerNotifications", description = "Container based static scanners such as FindSecBugs " +
        "scanner, DependencyCheck scanner notify status after a task is completed")
public class StaticScannerNotificationController {

    private final ContainerBasedStaticScannerService containerBasedStaticScannerService;

    @Autowired
    public StaticScannerNotificationController(ContainerBasedStaticScannerService containerBasedStaticScannerService) {
        this.containerBasedStaticScannerService = containerBasedStaticScannerService;
    }

    /**
     * Calls by {@code StaticScanner} Docker containers to notify the product zip file is uploaded
     *
     * @param containerId Container Id of the container
     * @param status      boolean status to indicate the file is uploaded or not
     */
    @GetMapping(value = "fileUploaded")
    @ApiOperation(value = "Update that a zip file is uploaded to the container")
    public @ResponseBody
    void updateFileUploaded(@RequestParam String containerId, @RequestParam boolean status) {
        containerBasedStaticScannerService.updateFileUploaded(containerId, status);
    }

    /**
     * Calls by {@code StaticScanner} Docker containers to notify the product zip file is extracted
     *
     * @param containerId Container Id of the container
     * @param status      boolean status to indicate the file is extracted or not
     */
    @GetMapping(value = "fileExtracted")
    @ApiOperation(value = "Update that a zip file is extracted to the container")
    public @ResponseBody
    void updateFileExtracted(@RequestParam String containerId, @RequestParam boolean status) {
        containerBasedStaticScannerService.updateFileExtracted(containerId, status);
    }

    /**
     * Calls by {@code StaticScanner} Docker containers to notify the product is cloned
     *
     * @param containerId Container Id of the container
     * @param status      boolean status to indicate the product is cloned to the container
     */
    @GetMapping(value = "productCloned")
    @ApiOperation(value = "Update that a product is cloned to the container")
    public @ResponseBody
    void updateProductCloned(@RequestParam String containerId, @RequestParam boolean status) {
        containerBasedStaticScannerService.updateProductCloned(containerId, status);
    }

    /**
     * Calls by {@code StaticScanner} Docker containers to notify the status of the scan
     *
     * @param containerId Container Id of the container
     * @param status      status of the scan such as initiated, running, completed,failed
     */
    @GetMapping(value = "scanStatus")
    public @ResponseBody
    @ApiOperation(value = "Update the status of the scan. eg: running, failed, completed")
    void updateScanStatus(@RequestParam String containerId, @RequestParam String status) {
        containerBasedStaticScannerService.updateScanStatus(containerId, status);
    }

    /**
     * Calls by {@code StaticScanner} Docker containers to notify the scan report is generated
     *
     * @param containerId Container Id of the container
     * @param status      boolean status to indicate the scan report is generated
     */
    @GetMapping(value = "reportReady")
    public @ResponseBody
    @ApiOperation(value = "Update that the scan report is ready")
    void updateReportReady(@RequestParam String containerId, @RequestParam boolean status) {
        containerBasedStaticScannerService.updateReportReady(containerId, status);
    }
}
