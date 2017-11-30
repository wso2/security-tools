/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.security.tools.product.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.product.manager.exception.NotificationManagerException;
import org.wso2.security.tools.product.manager.exception.ProductManagerException;
import org.wso2.security.tools.product.manager.service.ProductManagerService;

/**
 * The class {@code ProductManagerController} is the web controller which defines the routines to start a server
 */
@Controller
@RequestMapping("productManager")
public class ProductManagerController {

    private final ProductManagerService productManagerService;

    @Autowired
    public ProductManagerController(ProductManagerService productManagerService) {
        this.productManagerService = productManagerService;
    }

    /**
     * Controller method to start server
     *
     * @param automationManagerHost Automation Manager host
     * @param automationManagerPort Automation Manager port
     * @param myContainerId         Container Id of this (Since this micro service is running inside a container)
     * @param zipFile               ZIP file of the product binary
     */
    @PostMapping(value = "startServer")
    @ResponseBody
    public void startServer(@RequestParam String automationManagerHost,
                            @RequestParam int automationManagerPort,
                            @RequestParam String myContainerId,
                            @RequestParam MultipartFile zipFile) throws NotificationManagerException,
            ProductManagerException {
        productManagerService.startServer(automationManagerHost, automationManagerPort, myContainerId, zipFile);
    }
}