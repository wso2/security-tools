/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.webapp.controller;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.wso2.security.tools.scanmanager.webapp.model.Scanner;
import org.wso2.security.tools.scanmanager.webapp.service.ScanService;
import org.wso2.security.tools.scanmanager.webapp.util.Constants;
import org.wso2.security.tools.scanmanager.webapp.util.Utils;

import java.io.File;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;

/**
 * Controller methods to resolve views.
 */
@Controller
@RequestMapping("scanManager")
public class ScanController {

    private ServletContext context;
    private final ScanService scanService;

    private static final String SCAN_MANAGER_VIEW = "scanManager";
    private static final String SCANNERS_VIEW_NAME = "scanners";
    private static final String SCAN_CONFIGURATION_VIEW = "scanConfiguration";
    private static final String SCANS_VIEW = "scans";
    private static final String ERROR_PAGE_VIEW = "errorPage";

    private static final String SCAN_LIST_ATTRIBUTE_NAME = "scansList";
    private static final String SCANNERS_CONTEXT_ATTRIBUTE = "scanners";
    private static final String STATIC_SCANNERS_ATTRIBUTE = "staticScanners";
    private static final String DYNAMIC_SCANNERS_ATTRIBUTE = "dynamicScanners";
    private static final String DEPENDENCY_SCANNERS_ATTRIBUTE = "dependencyScanners";
    private static final String MESSAGE_ATTRIBUTE = "message";

    @Autowired
    public ScanController(ScanService scanService, ServletContext context) {
        this.scanService = scanService;
        this.context = context;
    }

    @GetMapping(value = "/")
    public String scanManager() {
        return "scanManager/index";
    }

    @RequestMapping(value = "/startScan", method = RequestMethod.POST)
    public ModelAndView startScan(MultipartHttpServletRequest multipartHttpServletRequest) {

        Map<String, String[]> parameterMap = ((DefaultMultipartHttpServletRequest) multipartHttpServletRequest)
                .getParameterMap();
        Map<String, MultipartFile> fileMap = multipartHttpServletRequest.getFileMap();
        ModelAndView successView = new ModelAndView(SCAN_MANAGER_VIEW + File.separator + SCANS_VIEW);
        ModelAndView failureView = new ModelAndView(SCAN_MANAGER_VIEW + File.separator + ERROR_PAGE_VIEW);

        int status = scanService.startScan(fileMap, parameterMap);
        if (status == HttpStatus.SC_OK) {
            return successView;
        } else {
            failureView.addObject(MESSAGE_ATTRIBUTE, "An error occurred while initiating the scan.");
            return failureView;
        }
    }

    @GetMapping(value = "/scanners")
    public ModelAndView getScanners() {
        ModelAndView scannerModel = new ModelAndView(SCAN_MANAGER_VIEW + File.separator + SCANNERS_VIEW_NAME);
        List<Scanner> scannerList;

        if (context.getAttribute(SCANNERS_CONTEXT_ATTRIBUTE) != null) {
            scannerList = (List<Scanner>) context.getAttribute(SCANNERS_CONTEXT_ATTRIBUTE);
        } else {
            scannerList = scanService.getScanners();
            context.setAttribute(SCANNERS_CONTEXT_ATTRIBUTE, scannerList);
        }

        scannerModel.addObject(STATIC_SCANNERS_ATTRIBUTE, Utils.getScannersByType(scannerList,
                Constants.STATIC_SCANNER_TYPE));
        scannerModel.addObject(DYNAMIC_SCANNERS_ATTRIBUTE, Utils.getScannersByType(scannerList,
                Constants.DYNAMIC_SCANNER_TYPE));
        scannerModel.addObject(DEPENDENCY_SCANNERS_ATTRIBUTE, Utils.getScannersByType(scannerList,
                Constants.DEPENDENCY_SCANNER_TYPE));
        return scannerModel;
    }

    @GetMapping(value = "/scans")
    public ModelAndView getScans() {
        ModelAndView scansModel = new ModelAndView(SCAN_MANAGER_VIEW + File.separator + SCANS_VIEW);
        scansModel.addObject(SCAN_LIST_ATTRIBUTE_NAME, scanService.getScans());
        return scansModel;
    }

    @PostMapping(value = "/stop")
    public String stopScan(@RequestParam String id) {
        ResponseEntity<String> responseEntity = scanService.stopScan(id);
        if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
            return "redirect:scans";
        } else {
            return SCAN_MANAGER_VIEW + File.separator + ERROR_PAGE_VIEW;
        }
    }

    @PostMapping(value = "/scanConfiguration")
    public ModelAndView upload(@RequestParam("scannerId") String scannerId) {
        List<Scanner> scannerList = null;
        Scanner selectedScanner = new Scanner();

        ModelAndView scannerConfigModel = new ModelAndView(SCAN_MANAGER_VIEW +
                File.separator + SCAN_CONFIGURATION_VIEW);
        if (context.getAttribute(SCANNERS_CONTEXT_ATTRIBUTE) != null) {
            scannerList = (List<Scanner>) context.getAttribute(SCANNERS_CONTEXT_ATTRIBUTE);
        } else {
            scannerList = scanService.getScanners();
            context.setAttribute(SCANNERS_CONTEXT_ATTRIBUTE, scannerList);
        }
        for (Scanner scanner : scannerList) {
            if (scanner.getId().equals(scannerId)) {
                selectedScanner.setId(scanner.getId());
                selectedScanner.setName(scanner.getName());
                selectedScanner.setType(scanner.getType());
                selectedScanner.setFields(scanner.getFields());
            }
        }
        scannerConfigModel.addObject(MESSAGE_ATTRIBUTE, selectedScanner);
        return scannerConfigModel;
    }
}
