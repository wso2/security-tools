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
import org.wso2.security.tools.automation.manager.service.productmanager.ContainerBasedProductManagerService;

/**
 * The main contract of the {@code ProductManagerNotificationController} class is to provide an API to be called by
 * ProductManager Docker containers.
 * <p>When a {@code ProductManager} Docker container is initialized to run a task, it will run asynchronously. Therefore
 * in order to track whether a task is completed or not, Docker container is configured to send back notifications to
 * APIs defined here.</p>
 */

@Controller
@RequestMapping("productManager/notify")
@Api(value = "productManagerNotifications", description = "Product Manager Docker container will use this API to " +
        "notify the status of the container such as file uploaded, file extracted and server started")
public class ProductManagerNotificationController {

    private final ContainerBasedProductManagerService containerBasedProductManagerService;

    @Autowired
    public ProductManagerNotificationController(ContainerBasedProductManagerService
                                                        containerBasedProductManagerService) {
        this.containerBasedProductManagerService = containerBasedProductManagerService;
    }

    /**
     * Calls by {@code ProductManager} Docker container to notify that the product zip file is uploaded
     *
     * @param containerId Container Id of the container
     * @param status      boolean status to indicate the file is uploaded or not
     */
    @GetMapping(value = "fileUploaded")
    @ApiOperation(value = "Update that a zip file is uploaded to the container")
    public @ResponseBody
    void updateFileUploaded(@RequestParam String containerId, @RequestParam boolean status) {
        containerBasedProductManagerService.updateFileUploaded(containerId, status);
    }

    /**
     * Calls by {@code ProductManager} Docker container to notify that the product zip file is extracted
     *
     * @param containerId Container Id of the container
     * @param status      boolean status to indicate the file is extracted or not
     */
    @GetMapping(value = "fileExtracted")
    @ApiOperation(value = "Update that a zip file is extracted to the container")
    public @ResponseBody
    void updateFileExtracted(@RequestParam String containerId, @RequestParam boolean status) {
        containerBasedProductManagerService.updateFileExtracted(containerId, status);
    }

    /**
     * Calls by {@code ProductManager} Docker container to notify that the server is started
     *
     * @param containerId Container Id of the container
     * @param status      boolean status to indicate the server is started or not
     */
    @GetMapping(value = "serverStarted")
    @ApiOperation(value = "Update that a server is started inside the container")
    public @ResponseBody
    void updateServerStarted(@RequestParam String containerId, @RequestParam boolean status) {
        containerBasedProductManagerService.updateServerStarted(containerId, status);
    }
}
