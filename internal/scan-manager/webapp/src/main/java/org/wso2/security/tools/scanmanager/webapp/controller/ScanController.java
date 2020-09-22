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

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScanExternal;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerLogResponse;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.external.model.ScannerApp;
import org.wso2.security.tools.scanmanager.common.external.model.User;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;
import org.wso2.security.tools.scanmanager.webapp.service.LogService;
import org.wso2.security.tools.scanmanager.webapp.service.ScanService;
import org.wso2.security.tools.scanmanager.webapp.service.ScannerService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.security.tools.scanmanager.webapp.util.Constants.LOGS_VIEW;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.MAX_FILE_SIZE_PROPERTY_KEY;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCANS_VIEW;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCAN_CONFIGURATION_VIEW;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCAN_MANAGER_VIEW;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCAN_REPORT_DATA_DIRECTORY_NAME;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.URL_SEPARATOR;

/**
 * Controller methods to resolve views for scans.
 */
@Controller
@RequestMapping("scan-manager")
public class ScanController {

    private ScanService scanService;
    private ScannerService scannerService;
    private LogService logService;
    private Environment env;

    private static final String CONTENT_DISPOSITION_HEADER_NAME = "Content-Disposition";
    private static final String SCAN_LIST_RESPONSE_ATTRIBUTE_NAME = "scanListResponse";
    private static final String PREPARING_SCAN_LIST_ATTRIBUTE_NAME = "preparingScanList";
    private static final String LOG_RESPONSE_ATTRIBUTE_NAME = "logListResponse";
    private static final String SCANNER_DATA_ATTRIBUTE_NAME = "scannerData";
    private static final String PRODUCT_DATA_ATTRIBUTE_NAME = "productData";
    private static final String MAX_FILE_SIZE_ATTRIBUTE_NAME = "maxFileSize";
    private static final String SCAN_DATA_ATTRIBUTE_NAME = "scanData";
    private static final String EMAIL = "email";

    @Autowired
    public ScanController(ScanService scanService, ScannerService scannerService, LogService logService,
                          Environment env) {
        this.scanService = scanService;
        this.scannerService = scannerService;
        this.logService = logService;
        this.env = env;
    }

    @GetMapping(value = "/")
    public String scanManager() {
        return "scan-manager/index";
    }

    /**
     * Submit a scan request.
     *
     * @param httpServletRequest servlet httpServletRequest containing the scan file and parameter maps
     * @return the scans view
     * @throws ScanManagerWebappException when an error occurs while submitting the scan
     */
    @PostMapping(value = "submit-scan")
    public String startScan(HttpServletRequest httpServletRequest) throws ScanManagerWebappException {

        boolean isMultipart = ServletFileUpload.isMultipartContent(httpServletRequest);
        if (!isMultipart) {
            // Return for invalid httpServletRequests.
            return null;
        }

        ServletFileUpload upload = new ServletFileUpload();

        String username = null;
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication.getPrincipal() instanceof OidcUser) {
            OidcUser principal = ((OidcUser) authentication.getPrincipal());

            username = principal.getClaims().get(EMAIL).toString();
        }

        User user = new User(username, username);


        FileItemIterator itemIterator = null;
        try {
            itemIterator = upload.getItemIterator(httpServletRequest);
        } catch (FileUploadException | IOException e) {
            throw new ScanManagerWebappException("An error occurred while submitting the scan", e);
        }

        Scan scan = scanService.submitScan(user, itemIterator);
        if (scan != null) {
            return "redirect:scans";
        } else {
            throw new ScanManagerWebappException("An error occurred while submitting the scan");
        }
    }

    /**
     * Get the list of scans.
     *
     * @param page required page number
     * @return scans view
     * @throws ScanManagerWebappException when an error occurs while getting the list of scans
     */
    @GetMapping(value = "scans")
    public ModelAndView getScans(@RequestParam(name = "page", required = false) Integer page)
            throws ScanManagerWebappException {

        ModelAndView scansView = new ModelAndView(SCAN_MANAGER_VIEW + URL_SEPARATOR + SCANS_VIEW);

        // List of scans submitted to scan manager API.
        scansView.addObject(SCAN_LIST_RESPONSE_ATTRIBUTE_NAME, scanService.getScans(page));

        // list of scan preparing to be submitted to scan manager API.
        scansView.addObject(PREPARING_SCAN_LIST_ATTRIBUTE_NAME, scanService.getPreparingScans());
        return scansView;
    }

    /**
     * Get the logs for a scan.
     *
     * @param page  required page number
     * @param jobId scan id of the required logs
     * @return logs view
     * @throws ScanManagerWebappException when an error occurs while getting the list of logs
     */
    @GetMapping(value = "logs")
    public ModelAndView getLogs(@RequestParam(name = "page", required = false) Integer page,
                                @RequestParam("jobId") String jobId) throws ScanManagerWebappException {
        ModelAndView logsView = new ModelAndView(SCAN_MANAGER_VIEW + URL_SEPARATOR + LOGS_VIEW);
        ScanManagerLogResponse scanManagerLogResponse = logService.getLogs(jobId, page);
        logsView.addObject(LOG_RESPONSE_ATTRIBUTE_NAME, scanManagerLogResponse);
        logsView.addObject(SCAN_DATA_ATTRIBUTE_NAME, scanService.getScan(jobId));
        return logsView;
    }

    /**
     * Cancel a scan.
     *
     * @param jobId scan id of the scan to be canceled
     * @return scans view
     * @throws ScanManagerWebappException when an error occurs while cancelling the scan
     */
    @PostMapping(value = "stop")
    public String stopScan(@RequestParam("jobId") String jobId) throws ScanManagerWebappException {
        ResponseEntity<String> responseEntity = scanService.stopScan(jobId);
        if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
            return "redirect:scans";
        } else {
            throw new ScanManagerWebappException("Error occurred while cancelling the scan: " + jobId);
        }
    }

    /**
     * Clear a scan.
     *
     * @param jobId scan id of the scan to be cleared
     * @return scans view
     * @throws ScanManagerWebappException when an error occurs while clearing the scan
     */
    @PostMapping(value = "clear")
    public String clearScan(@RequestParam("jobId") String jobId) throws ScanManagerWebappException {
        if (scanService.clearScan(jobId)) {
            return "redirect:scans";
        } else {
            throw new ScanManagerWebappException("Error occurred while clearing the scan: " + jobId);
        }
    }

    /**
     * Download scan report.
     *
     * @param response HTTP servlet response
     * @param jobId    job id of the scan
     * @throws ScanManagerWebappException when an error occurs while downloading the scan report
     */
    @GetMapping(value = "report")
    public void getReport(HttpServletResponse response, @RequestParam("jobId") String jobId)
            throws ScanManagerWebappException {
        ScanExternal scan = scanService.getScan(jobId);
        if (scan != null) {
            if (StringUtils.isBlank(scan.getScanReportPath())) {
                throw new ScanManagerWebappException("Scan report path is empty");
            }

            // Download the scan report temporally into the local machine.
            File file = new File(scan.getScanReportPath());
            File reportDirectory =
                    new File(SCAN_REPORT_DATA_DIRECTORY_NAME + File.separator + scan.getJobId());
            if (!reportDirectory.exists() && !reportDirectory.mkdirs()) {
                throw new ScanManagerWebappException("Error occurred while creating the report output directory");
            }
            File outputFile = new File(SCAN_REPORT_DATA_DIRECTORY_NAME + File.separator + scan.getJobId()
                    + File.separator + file.getName());
            scanService.getScanReport(scan.getScanReportPath(),
                    outputFile.getPath());

            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null) {
                mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            response.setContentType(mimeType);
            response.setHeader(CONTENT_DISPOSITION_HEADER_NAME, String.format("inline; filename=\""
                    + outputFile.getName() + "\""));
            response.setContentLength((int) outputFile.length());

            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(outputFile))) {
                FileCopyUtils.copy(inputStream, response.getOutputStream());
            } catch (IOException e) {
                throw new ScanManagerWebappException("Error occurred while downloading scan report for " +
                        "the scan: " + jobId, e);
            }
        } else {
            throw new ScanManagerWebappException("Unable to find a scan for the given id: " + jobId);
        }
    }

    /**
     * Getting all the configurations required for the scan by scanner.
     *
     * @param scannerId scanner id
     * @return a view containing required scan configurations by scanner
     * @throws ScanManagerWebappException when an error occurs while getting scanner configuration
     */
    @GetMapping(value = "configuration")
    public ModelAndView getScannerConfig(@RequestParam("scannerId") String scannerId)
            throws ScanManagerWebappException {
        Scanner scanner = null;
        ModelAndView scannerConfigModel = new ModelAndView(SCAN_MANAGER_VIEW +
                File.separator + SCAN_CONFIGURATION_VIEW);

        scanner = scannerService.getScanner(scannerId);
        scannerConfigModel.addObject(SCANNER_DATA_ATTRIBUTE_NAME, scanner);

        // List the applicable products for the given scanner.
        List<String> productLst =
                scanner.getApps().stream().map(ScannerApp::getAssignedProduct).collect(Collectors.toList());
        Set<String> set = new HashSet<>(productLst);
        productLst.clear();
        productLst.addAll(set);
        scannerConfigModel.addObject(PRODUCT_DATA_ATTRIBUTE_NAME, productLst);
        scannerConfigModel.addObject(MAX_FILE_SIZE_ATTRIBUTE_NAME, env.getProperty(MAX_FILE_SIZE_PROPERTY_KEY));
        return scannerConfigModel;
    }
}
