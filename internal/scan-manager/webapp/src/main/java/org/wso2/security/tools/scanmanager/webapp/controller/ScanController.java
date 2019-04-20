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
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.wso2.security.tools.scanmanager.common.external.model.ScanExternal;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerLogResponse;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.model.ScannerType;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;
import org.wso2.security.tools.scanmanager.webapp.service.LogService;
import org.wso2.security.tools.scanmanager.webapp.service.ScanService;
import org.wso2.security.tools.scanmanager.webapp.service.ScannerService;
import org.wso2.security.tools.scanmanager.webapp.util.FTPUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.security.tools.scanmanager.webapp.util.Constants.LOGS_VIEW;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCANNERS_VIEW_NAME;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCANS_VIEW;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCAN_CONFIGURATION_VIEW;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCAN_MANAGER_VIEW;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.URL_SEPARATOR;

/**
 * Controller methods to resolve views.
 */
@Controller
@RequestMapping("scan-manager")
public class ScanController {

    private ServletContext context;
    private ScanService scanService;
    private ScannerService scannerService;
    private LogService logService;

    private static final String CONTENT_DISPOSITION_HEADER_NAME = "Content-Disposition";
    private static final String SCAN_LIST_RESPONSE_ATTRIBUTE_NAME = "scanListResponse";
    private static final String LOG_RESPONSE_ATTRIBUTE_NAME = "logListResponse";
    private static final String SCANNERS_CONTEXT_ATTRIBUTE = "scanners";
    private static final String STATIC_SCANNERS_ATTRIBUTE = "staticScanners";
    private static final String DYNAMIC_SCANNERS_ATTRIBUTE = "dynamicScanners";
    private static final String DEPENDENCY_SCANNERS_ATTRIBUTE = "dependencyScanners";
    private static final String MESSAGE_ATTRIBUTE = "message";
    private static final String SCANNER_DATA_ATTRIBUTE_NAME = "scannerData";
    private static final String SCAN_DATA_ATTRIBUTE_NAME = "scanData";
    private static final String SCAN_NAME_PARAMETER_KEY = "scanName";

    private static final Logger logger = LoggerFactory.getLogger(ScanController.class);

    @Autowired
    public ScanController(ScanService scanService, ServletContext context, ScannerService scannerService,
                          LogService logService) {
        this.scanService = scanService;
        this.scannerService = scannerService;
        this.context = context;
        this.logService = logService;
    }

    @GetMapping(value = "/")
    public String scanManager() {
        return "scan-manager/index";
    }

    /**
     * Submit a scan request.
     *
     * @param multipartHttpServletRequest servlet request containing the scan file and parameter maps
     * @return the scans view
     * @throws ScanManagerWebappException when an error occurs while submitting the scan
     */
    @PostMapping(value = "submit-scan")
    public String startScan(MultipartHttpServletRequest multipartHttpServletRequest)
            throws ScanManagerWebappException {
        Map<String, String> parameterMap = new HashMap<>();

        for (Map.Entry<String, String[]> requestParamMap : multipartHttpServletRequest.getParameterMap().entrySet()) {
            parameterMap.put(requestParamMap.getKey(), requestParamMap.getValue()[0]);
        }
        Map<String, MultipartFile> fileMap = multipartHttpServletRequest.getFileMap();
        String scanDirectory = parameterMap.get(SCAN_NAME_PARAMETER_KEY) + "_" +
                UUID.randomUUID().toString();
        int status = scanService.submitScan(fileMap, parameterMap, scanDirectory);
        if (status == HttpStatus.SC_OK) {
            return "redirect:scans";
        } else {
            throw new ScanManagerWebappException("An error occurred while submitting the scan");
        }
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

        if (context.getAttribute(SCANNERS_CONTEXT_ATTRIBUTE) != null) {
            scannerList = (List<Scanner>) context.getAttribute(SCANNERS_CONTEXT_ATTRIBUTE);
        } else {
            scannerList = scannerService.getScanners();
            context.setAttribute(SCANNERS_CONTEXT_ATTRIBUTE, scannerList);
        }
        scannerView.addObject(STATIC_SCANNERS_ATTRIBUTE, FTPUtil.getScannersByType(scannerList,
                ScannerType.STATIC));
        scannerView.addObject(DYNAMIC_SCANNERS_ATTRIBUTE, FTPUtil.getScannersByType(scannerList,
                ScannerType.DYNAMIC));
        scannerView.addObject(DEPENDENCY_SCANNERS_ATTRIBUTE, FTPUtil.getScannersByType(scannerList,
                ScannerType.DEPENDENCY));
        return scannerView;
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
        scansView.addObject(SCAN_LIST_RESPONSE_ATTRIBUTE_NAME, scanService.getScans(page));
        return scansView;
    }

    /**
     * Getting the logs for a scan.
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
        ScanExternal scan = scanManagerLogResponse.getScan();
        logsView.addObject(LOG_RESPONSE_ATTRIBUTE_NAME, scanManagerLogResponse);
        logsView.addObject(SCAN_DATA_ATTRIBUTE_NAME, scan);
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
     * Download scan report.
     *
     * @param response HTTP servlet response
     * @param jobId    job id of the scan
     * @throws ScanManagerWebappException when an error occurs while downloading the scan report
     */
    @GetMapping(value = "report")
    public void getReport(HttpServletResponse response, @RequestParam("jobId") String jobId)
            throws ScanManagerWebappException {
        File downloadedFile = null;

        ScanExternal scan = scanService.getScan(jobId);
        if (scan != null) {
            if (StringUtils.isBlank(scan.getScanReportPath())) {
                throw new ScanManagerWebappException("Scan report path is empty");
            }
            File file = new File(scan.getScanReportPath());
            scanService.getScanReport(scan.getScanReportPath(), file.getName());
            downloadedFile = new File(file.getName());
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null) {
                mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            response.setContentType(mimeType);
            response.setHeader(CONTENT_DISPOSITION_HEADER_NAME, String.format("inline; filename=\""
                    + downloadedFile.getName() + "\""));
            response.setContentLength((int) downloadedFile.length());
        } else {
            throw new ScanManagerWebappException("Unable to find a scan for the given id: " + jobId);
        }
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(downloadedFile));) {
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new ScanManagerWebappException("Error occurred while downloading scan report for the scan: " + jobId,
                    e);
        }
    }

    /**
     * Getting all the configurations required for the scan.
     *
     * @param scannerId scanner id
     * @return a view containing required scan configurations
     * @throws ScanManagerWebappException when an error occurs while downloading the scan report
     */
    @PostMapping(value = "configuration")
    public ModelAndView getScanConfig(@RequestParam("scannerId") String scannerId) throws ScanManagerWebappException {
        List<Scanner> scannerList = null;
        ModelAndView scannerConfigModel = new ModelAndView(SCAN_MANAGER_VIEW +
                File.separator + SCAN_CONFIGURATION_VIEW);
        if (context.getAttribute(SCANNERS_CONTEXT_ATTRIBUTE) != null) {
            scannerList = (List<Scanner>) context.getAttribute(SCANNERS_CONTEXT_ATTRIBUTE);
        } else {
            scannerList = scannerService.getScanners();
            context.setAttribute(SCANNERS_CONTEXT_ATTRIBUTE, scannerList);
        }
        for (Scanner scanner : scannerList) {
            if (scanner.getId().equals(scannerId)) {
                scannerConfigModel.addObject(SCANNER_DATA_ATTRIBUTE_NAME, scanner);
                break;
            }
        }
        return scannerConfigModel;
    }
}
