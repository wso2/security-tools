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

package org.wso2.security.tools.automation.manager.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.DynamicScannerEntity;
import org.wso2.security.tools.automation.manager.entity.productmanager.ProductManagerEntity;
import org.wso2.security.tools.automation.manager.entity.staticscanner.StaticScannerEntity;
import org.wso2.security.tools.automation.manager.service.dynamicscanner.DynamicScannerService;
import org.wso2.security.tools.automation.manager.service.productmanager.ProductManagerService;
import org.wso2.security.tools.automation.manager.service.staticscanner.StaticScannerService;

/**
 * The class {@code MainController} is the web controller which defines the routines for getting static scans,
 * dynamic scans and product managers
 */
@Controller
@RequestMapping("/")
@Api(value = "scanners", description = "Get StaticScanners, DynamicScanners and ProductManagers done by a specific " +
        "user")
public class MainController {

    private final StaticScannerService staticScannerService;
    private final DynamicScannerService dynamicScannerService;
    private final ProductManagerService productManagerService;

    @Autowired
    public MainController(StaticScannerService staticScannerService, DynamicScannerService dynamicScannerService,
                          ProductManagerService productManagerService) {
        this.staticScannerService = staticScannerService;
        this.dynamicScannerService = dynamicScannerService;
        this.productManagerService = productManagerService;
    }

    /**
     * Returns a list of {@code StaticScannerEntity} in other words, static scans done by a user
     *
     * @param userId Email address of the user
     * @return Iterable list of StaticScannerEntity
     */
    @GetMapping(value = "myStaticScanners")
    @ApiOperation(value = "Get static scans done by a user")
    @ResponseBody
    public Iterable<StaticScannerEntity> getStaticScanners(String userId) {
        return staticScannerService.findByUserId(userId);
    }

    /**
     * Returns a list of {@code DynamicScannerEntity} in other words, dynamic scans done by a user
     *
     * @param userId Email address of the user
     * @return Iterable list of DynamicScannerEntity
     */
    @GetMapping(value = "myDynamicScanners")
    @ApiOperation(value = "Get dynamic scans done by a user")
    @ResponseBody
    public Iterable<DynamicScannerEntity> getDynamicScanners(String userId) {
        return dynamicScannerService.findByUserId(userId);
    }

    /**
     * Returns a list of {@code ProductManagerEntity} in other words, product managers started by a user
     *
     * @param userId Email address of the user
     * @return Iterable list of ProductManagerEntity
     */
    @GetMapping(value = "myProductManagers")
    @ApiOperation(value = "Get product managers done by a user")
    @ResponseBody
    public Iterable<ProductManagerEntity> getProductManagers(String userId) {
        return productManagerService.findByUserId(userId);
    }
}
