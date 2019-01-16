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

package org.wso2.security.tools.findsecbugs.scanner.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.findsecbugs.scanner.exception.FindSecBugsScannerException;
import org.wso2.security.tools.findsecbugs.scanner.service.FindSecBugsScannerService;

import javax.servlet.http.HttpServletResponse;

/**
 * The class {@code FindSecBugsScannerController} is the web controller which defines the routines for initiating
 * FindSecBugs scans
 */
@Controller
@RequestMapping("findSecBugsScanner")
public class FindSecBugsScannerController {

    private final FindSecBugsScannerService findSecBugsScannerService;

    @Autowired
    public FindSecBugsScannerController(FindSecBugsScannerService findSecBugsScannerService) {
        this.findSecBugsScannerService = findSecBugsScannerService;
    }

    /**
     * Controller method to check whether the micro service is started
     * <p>Since this micro service is started inside a Docker container, Automation Manager (which handles containers
     * such as start container, send request to container API etc.) calls this API to identify whether the micro
     * service is started</p>
     *
     * @return true
     */
    @GetMapping("isReady")
    @ResponseBody
    public boolean isReady() {
        return true;
    }

    /**
     * Controller method to start scan
     *
     * @param automationManagerHost Automation Manager host
     * @param automationManagerPort Automation Manager port
     * @param myContainerId         Container Id of this (Since this micro service is running inside a container)
     * @param isFileUpload          True means the product to be scanned is uploaded as a zip file. False means the
     *                              product should be cloned from GitHub
     * @param zipFile               ZIP file of the product source code
     * @param gitUrl                GitHub URL to clone the product. By default master branch is cloned. If a
     *                              specific branch or tag to be cloned, the specified URL for the branch or tag
     *                              should be given
     * @param gitUsername           GitHub user name if the product is in a private repository
     * @param gitPassword           GitHub password if the product is in private repository
     */
    @PostMapping("startScan")
    @ResponseBody
    public void startScan(@RequestParam String automationManagerHost,
                          @RequestParam int automationManagerPort,
                          @RequestParam String myContainerId,
                          @RequestParam boolean isFileUpload,
                          @RequestParam(required = false) MultipartFile zipFile,
                          @RequestParam(required = false) String gitUrl,
                          @RequestParam(required = false) String gitUsername,
                          @RequestParam(required = false) String gitPassword) throws FindSecBugsScannerException {
            findSecBugsScannerService.startScan(automationManagerHost, automationManagerPort, myContainerId,
                    isFileUpload, zipFile, gitUrl, gitUsername, gitPassword);
    }

    /**
     * Controller method to get generated report
     */
    @RequestMapping(value = "getReport", method = RequestMethod.GET, produces = "application/octet-stream")
    @ResponseBody
    public void getReport(HttpServletResponse response) throws FindSecBugsScannerException {
        findSecBugsScannerService.getReport(response);
    }
}

