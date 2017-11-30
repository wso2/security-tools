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

package org.wso2.security.tools.am.webapp.controller;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.am.webapp.entity.DynamicScanner;
import org.wso2.security.tools.am.webapp.entity.StaticScanner;
import org.wso2.security.tools.am.webapp.entity.User;
import org.wso2.security.tools.am.webapp.exception.AutomationManagerWebException;
import org.wso2.security.tools.am.webapp.service.DynamicScannerService;
import org.wso2.security.tools.am.webapp.service.StaticScannerService;
import org.wso2.security.tools.am.webapp.service.MainService;

/**
 * Controller methods to resolve views
 */
@SessionAttributes({"user", "staticScanner", "dynamicScanner"})
@Controller
public class ViewController {

    private final MainService mainService;
    private final DynamicScannerService dynamicScannerService;
    private final StaticScannerService staticScannerService;

    @Autowired
    public ViewController(MainService mainService, DynamicScannerService dynamicScannerService, StaticScannerService
            staticScannerService) {
        this.staticScannerService = staticScannerService;
        this.dynamicScannerService = dynamicScannerService;
        this.mainService = mainService;
    }

    /**
     * Initiate a model attribute to keep user email
     *
     * @return user model attribute
     */
    @ModelAttribute("user")
    public User getUser() {
        return new User();
    }


    /**
     * Initiate a model attribute to keep dynamic scanner related user inputs
     *
     * @return dynamicScanner model attribute
     */
    @ModelAttribute("dynamicScanner")
    public DynamicScanner getDynamicScanner() {
        return new DynamicScanner();
    }

    /**
     * Initiate a model attribute to keep static scanner related user inputs
     *
     * @return staticScanner model attribute
     */
    @ModelAttribute("staticScanner")
    public StaticScanner getStaticScanner() {
        return new StaticScanner();
    }

    /**
     * Render signing page
     *
     * @return Path to signin UI
     */
    @GetMapping(value = "/")
    public String signIn() {
        return "common/signin";
    }

    /**
     * Set user id to model attribute and render UI of main scanners
     *
     * @param user   Model attribute
     * @param userId User id
     * @return Path to Main Scanners UI
     */
    @PostMapping(value = "signin")
    public String authUser(@ModelAttribute("user") User user, @RequestParam String userId) {
        user.setEmail(userId);
        return "common/mainScanners";
    }

    /**
     * Set data to dynamic scanner model attribute and render UI of dynamic scanner types
     *
     * @param dynamicScanner Dynamic scanner model attribute
     * @param user           User model attribute
     * @param testName       Test name
     * @param productName    Product name
     * @param wumLevel       WUM level
     * @return Scanners UI of dynamic scanner
     */
    @PostMapping(value = "dynamicScanner/scanners")
    public String getDynamicScanners(@ModelAttribute("dynamicScanner") DynamicScanner dynamicScanner,
                                     @ModelAttribute("user") User user, @RequestParam String testName,
                                     @RequestParam String productName, @RequestParam String wumLevel) {
        dynamicScanner.setUserId(user.getEmail());
        dynamicScanner.setTestName(testName);
        dynamicScanner.setProductName(productName);
        dynamicScanner.setWumLevel(wumLevel);
        return "dynamicScanner/scanners";
    }

    /**
     * Renders UI of dynamic scanner types
     *
     * @param dynamicScanner Dynamic scanner model attribute
     * @return Scanners UI of dynamic scanner
     */
    @GetMapping(value = "dynamicScanner/scanners")
    public String getDynamicScanners(@ModelAttribute("dynamicScanner") DynamicScanner dynamicScanner) {
        return "dynamicScanner/scanners";
    }

    /**
     * Set scan types to dynamic scanner model attribute and render UI of uploading product
     *
     * @param dynamicScanner Dynamic scanner model attribute
     * @param isZap          Boolean to indicate whether to run zap scan
     * @return UI of uploading product to dynamic scanner
     */
    @PostMapping(value = "dynamicScanner/productUploader")
    public String getDynamicScannerProductUploader(@ModelAttribute("dynamicScanner") DynamicScanner dynamicScanner,
                                                   @RequestParam boolean isZap) {
        dynamicScanner.setZap(isZap);
        return "dynamicScanner/productUploader";
    }

    /**
     * Render UI of uploading product
     *
     * @param dynamicScanner Dynamic scanner model attribute
     * @return UI of uploading product to dynamic scanner
     */
    @GetMapping(value = "dynamicScanner/productUploader")
    public String getDynamicScannerProductUploader(@ModelAttribute("dynamicScanner") DynamicScanner dynamicScanner) {
        return "dynamicScanner/productUploader";
    }

    /**
     * Sends request to start dynamic scan
     *
     * @param dynamicScanner     Dynamic scanner model attribute
     * @param urlListFile        URL list file to be scanned
     * @param productUploadAsZip Product uploaded as a zip file. False means product is in up and running state
     * @param zipFile            zip file of the product binary
     * @param wso2ServerHost     Wso2 server host, if the product is in up and running state
     * @param wso2ServerPort     Wso2 server port, if the product is in up and running state
     * @return UI to view user initiated scans
     */
    @PostMapping(value = "dynamicScanner/startScan")
    public String dynamicScannerStartScan(@ModelAttribute("dynamicScanner") DynamicScanner dynamicScanner,
                                          @RequestParam MultipartFile urlListFile,
                                          @RequestParam boolean productUploadAsZip,
                                          @RequestParam(required = false) MultipartFile zipFile,
                                          @RequestParam(required = false) String wso2ServerHost,
                                          @RequestParam(required = false, defaultValue = "-1") int wso2ServerPort)
            throws AutomationManagerWebException {
        dynamicScannerService.sendMultipleStartScanRequests(dynamicScanner, urlListFile, productUploadAsZip, zipFile,
                wso2ServerHost, wso2ServerPort);
        return "dynamicScanner/scanners";
    }

    /**
     * Set data to static scanner model attribute and render UI of static scanner types
     *
     * @param staticScanner Static scanner model attribute
     * @param user          User model attribute
     * @param testName      Test name
     * @param productName   Product name
     * @param wumLevel      WUM level
     * @return Scanners UI of static scanner
     */
    @PostMapping(value = "staticScanner/scanners")
    public String getStaticScanners(@ModelAttribute("staticScanner") StaticScanner staticScanner,
                                    @ModelAttribute("user") User user,
                                    @RequestParam String testName,
                                    @RequestParam String productName, String wumLevel) {
        staticScanner.setUserId(user.getEmail());
        staticScanner.setTestName(testName);
        staticScanner.setProductName(productName);
        staticScanner.setWumLevel(wumLevel);
        return "staticScanner/scanners";
    }

    /**
     * Renders UI of static scanner types
     *
     * @param staticScanner Static scanner model attribute
     * @return Scanners UI of static scanner
     */
    @GetMapping(value = "staticScanner/scanners")
    public String getStaticScanners(@ModelAttribute("staticScanner") StaticScanner staticScanner) {
        return "staticScanner/scanners";
    }

    /**
     * Set scan types to dynamic scanner model attribute and render UI of uploading product
     *
     * @param staticScanner     Static scanner model attribute
     * @param isFindSecBugs     Boolean to indicate whether to run FindSecBugs scan
     * @param isDependencyCheck Boolean to indicate whether to run Dependency Check scan
     * @return UI of uploading product to dynamic scanner
     */
    @PostMapping(value = "staticScanner/productUploader")
    public String getStaticScannerProductUploader(@ModelAttribute("staticScanner") StaticScanner staticScanner,
                                                  @RequestParam boolean isFindSecBugs,
                                                  @RequestParam boolean isDependencyCheck) {
        staticScanner.setFindSecBugs(isFindSecBugs);
        staticScanner.setDependencyCheck(isDependencyCheck);
        return "staticScanner/productUploader";
    }

    /**
     * Render UI of uploading project source code
     *
     * @param staticScanner Static scanner model attribute
     * @return UI of uploading project source code to static scanner
     */
    @GetMapping(value = "staticScanner/productUploader")
    public String getStaticScannerProductUploader(@ModelAttribute("staticScanner") StaticScanner staticScanner) {
        return "staticScanner/productUploader";
    }

    /**
     * Sends request to start static scan
     *
     * @param staticScanner         Static scanner model attribute
     * @param sourceCodeUploadAsZip Project source code is uploaded as a zip file. False means clone from GitHub
     * @param zipFile               zip file of the product binary
     * @param gitUrl                GitHub url of the project source code
     * @return UI to view user initiated scans
     */
    @PostMapping(value = "staticScanner/startScan")
    public String staticScannerStartScan(@ModelAttribute("staticScanner") StaticScanner staticScanner,
                                         @RequestParam boolean sourceCodeUploadAsZip,
                                         @RequestParam(required = false) MultipartFile zipFile,
                                         @RequestParam(required = false) String gitUrl) throws AutomationManagerWebException {
        staticScannerService.sendMultipleStartScanRequests(staticScanner, sourceCodeUploadAsZip, zipFile, gitUrl);
        return "common/myScans";
    }

    /**
     * Get scans initiated by a user, and render UI to view scans
     *
     * @param userId User id
     * @param model  Model to set attributes
     * @return UI to view scans
     */
    @GetMapping(value = "myScanners")
    public String getMyScans(@RequestParam String userId, Model model) {
        JSONArray[] scanners = mainService.getMyScanners(userId);
        if (scanners != null) {
            model.addAttribute("staticScanners", scanners[0]);
            model.addAttribute("dynamicScanners", scanners[1]);
        }
        return "common/myScans";
    }
}
