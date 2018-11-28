/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanmanager.api.controller;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.wso2.security.tools.scanmanager.api.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.api.model.Scanner;
import org.wso2.security.tools.scanmanager.api.service.ScannerService;

import java.util.List;

/**
 * The class {@code ScannerController} is the web controller which defines the routines for managing
 * scans.
 */
@Controller
@RequestMapping("scanManager")
public class ScanController {

    private static final Logger logger = LoggerFactory.getLogger(ScanController.class);

    private final ScannerService scannerService;

    @Autowired
    public ScanController(ScannerService scannerService) {
        this.scannerService = scannerService;
    }

    @PostMapping(value = "startScan")
    @ApiOperation(value = "Initiating a scan")
    public void startScan(MultipartHttpServletRequest multipartHttpServletRequest)
            throws ScanManagerException {
        try {
            DefaultMultipartHttpServletRequest defaultMultipartHttpServletRequest =
                    (DefaultMultipartHttpServletRequest) multipartHttpServletRequest;
            scannerService.startScan(defaultMultipartHttpServletRequest.getParameterMap(),
                    defaultMultipartHttpServletRequest.getFileMap());
        } catch (ScanManagerException e) {
            logger.error("Error occurred while initiating the scan", e);
            throw new ScanManagerException("Error occurred while initiating the scan");
        }
    }

    @GetMapping(path = "scanners")
    @ApiOperation(value = "Get the list of scanners")
    public @ResponseBody
    List<Scanner> getScanners() throws ScanManagerException {
        List<Scanner> scanners = scannerService.getScanners();
        if (scanners != null) {
            return scanners;
        } else {
            throw new ScanManagerException("Unable to retrieve the list of Scanners");
        }
    }

   /* @GetMapping(path = "currentScans")
    @ApiOperation(value = "Get the current scans")
    public @ResponseBody
    List<Scan> getCurrentScans() {
    }

    @GetMapping(path = "pastScans")
    @ApiOperation(value = "Get past scans")
    public @ResponseBody
    List<Scan> getPastScans() {
    }

    @PostMapping(value = "stop")
    void stop(@RequestParam String id) {
    }*/
}
