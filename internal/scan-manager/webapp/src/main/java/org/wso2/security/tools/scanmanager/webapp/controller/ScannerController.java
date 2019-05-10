/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.model.ScannerType;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;
import org.wso2.security.tools.scanmanager.webapp.service.ScannerService;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCANNERS_VIEW_NAME;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCAN_MANAGER_VIEW;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.URL_SEPARATOR;

/**
 * Controller methods to resolve views for scanners.
 */
@Controller
@RequestMapping("scan-manager")
public class ScannerController {

    private ScannerService scannerService;

    private static final String STATIC_SCANNERS_ATTRIBUTE = "staticScanners";
    private static final String DYNAMIC_SCANNERS_ATTRIBUTE = "dynamicScanners";
    private static final String DEPENDENCY_SCANNERS_ATTRIBUTE = "dependencyScanners";

    @Autowired
    public ScannerController(ScannerService scannerService) {
        this.scannerService = scannerService;
    }

    /**
     * Get the list of scanners.
     *
     * @return scanners view
     * @throws ScanManagerWebappException when an error occurs while getting the list of scanners
     */
    @GetMapping(value = "scanners")
    public ModelAndView getScanners() throws ScanManagerWebappException {
        ModelAndView scannerView = new ModelAndView(SCAN_MANAGER_VIEW + URL_SEPARATOR + SCANNERS_VIEW_NAME);
        List<Scanner> scannerList;

        scannerList = scannerService.getScanners();
        scannerView.addObject(STATIC_SCANNERS_ATTRIBUTE, getScannersByType(scannerList,
                ScannerType.STATIC));
        scannerView.addObject(DYNAMIC_SCANNERS_ATTRIBUTE, getScannersByType(scannerList,
                ScannerType.DYNAMIC));
        scannerView.addObject(DEPENDENCY_SCANNERS_ATTRIBUTE, getScannersByType(scannerList,
                ScannerType.DEPENDENCY));
        return scannerView;
    }

    /**
     * Filter the scanners by given scanner type.
     *
     * @param scannerList list of scanner objects
     * @param type        scanner type
     * @return a list of scanner objects with the given type
     */
    private List<Scanner> getScannersByType(List<Scanner> scannerList, ScannerType type) {
        List<Scanner> filteredScannerList = new ArrayList<>();

        for (Scanner scanner : scannerList) {
            if (scanner.getType().equals(type)) {
                filteredScannerList.add(scanner);
            }
        }
        return filteredScannerList;
    }
}
