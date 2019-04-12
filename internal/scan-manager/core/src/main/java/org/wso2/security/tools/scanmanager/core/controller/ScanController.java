/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.tools.scanmanager.common.external.model.Log;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScanFile;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerScanRequest;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerScanResponse;
import org.wso2.security.tools.scanmanager.common.external.model.ScanPriorityUpdateRequest;
import org.wso2.security.tools.scanmanager.common.external.model.ScanProperty;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanPriority;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.common.model.ScanType;
import org.wso2.security.tools.scanmanager.core.config.ScanManagerConfiguration;
import org.wso2.security.tools.scanmanager.core.exception.InvalidRequestException;
import org.wso2.security.tools.scanmanager.core.exception.ResourceNotFoundException;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.core.service.LogService;
import org.wso2.security.tools.scanmanager.core.service.ScanService;
import org.wso2.security.tools.scanmanager.core.service.ScannerService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.wso2.security.tools.scanmanager.core.util.Constants.DEFAULT_LOG_PAGE_SIZE;
import static org.wso2.security.tools.scanmanager.core.util.Constants.DEFAULT_SCANNER_PAGE_SIZE;
import static org.wso2.security.tools.scanmanager.core.util.Constants.DEFAULT_SCAN_PAGE_SIZE;
import static org.wso2.security.tools.scanmanager.core.util.Constants.SCAN_ARTIFACT;
import static org.wso2.security.tools.scanmanager.core.util.Constants.SCAN_URL;

/**
 * The class {@code ScanController} is the web controller which defines the routines for managing
 * scans.
 */
@Controller
@RequestMapping("scan-manager")
public class ScanController {

    private ScannerService scannerService;
    private ScanService scanService;
    private LogService logService;

    @Autowired
    public ScanController(ScannerService scannerService, ScanService scanService, LogService logService) {
        this.scannerService = scannerService;
        this.scanService = scanService;
        this.logService = logService;
    }

    /**
     * Initiate a scan request.
     *
     * @param scanRequest scan request object
     * @return details of the persisted scan
     * @throws InvalidRequestException when the submitted request is invalid
     */
    @PostMapping(value = "scans", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScanManagerScanResponse> startScan(@RequestBody ScanManagerScanRequest scanRequest)
            throws InvalidRequestException {
        Scanner scanner = validateScanRequest(scanRequest);
        Scan scan = new Scan();
        scan.setScanner(scanner);

        String jobId = UUID.randomUUID().toString();
        scan.setJobId(jobId);
        scan.setScanType(scanRequest.getScanType());
        scan.setSubmittedTimestamp(new Timestamp(System.currentTimeMillis()));
        scan.setScanName(scanRequest.getScanName());
        scan.setScanDescription(scanRequest.getScanDescription());
        scan.setProduct(scanRequest.getProductName());
        scan.setStatus(ScanStatus.SCAN_PENDING);
        scan.setPriority(ScanPriority.MEDIUM);

        if (scanRequest.getPropertyMap() != null) {
            scan.setScanPropertyList(buildScanPropertyList(scanRequest.getPropertyMap(), scan));
        }
        if (scanRequest.getFileMap() != null) {
            scan.setScanFileList(buildScanFileList(scanRequest.getFileMap(), scan));
        }
        scan = scanService.update(scan);
        new Thread(() -> scanService.beginPendingScans()).start();
        return new ResponseEntity<>(new ScanManagerScanResponse(scan), HttpStatus.OK);
    }

    private Scanner validateScanRequest(ScanManagerScanRequest scanRequest) throws InvalidRequestException {
        if (ScanType.DYNAMIC.equals(scanRequest.getScanType())
                && ((scanRequest.getPropertyMap() == null) || !scanRequest.getPropertyMap().containsKey(SCAN_URL))) {
            throw new InvalidRequestException("Scan URL field is mandatory for Dynamic scans");
        } else if (ScanType.STATIC.equals(scanRequest.getScanType())
                && ((scanRequest.getFileMap() == null) || !scanRequest.getFileMap().containsKey(SCAN_ARTIFACT))) {
            throw new InvalidRequestException("Scan artifact field is mandatory for Static scans");
        } else if (ScanType.DEPENDENCY.equals(scanRequest.getScanType())
                && ((scanRequest.getFileMap() == null) || !scanRequest.getFileMap().containsKey(SCAN_ARTIFACT))) {
            throw new InvalidRequestException("Scan artifact field is mandatory for Dependency scans");
        }

        Scanner scanner = scannerService.getScannerById(scanRequest.getScannerId());
        if (scanner == null) {
            throw new InvalidRequestException("Invalid scanner id");
        }
        return scanner;
    }

    /**
     * Get the list of available scans by page.
     *
     * @param page required page number
     * @return a list of available scans for a given page
     */
    @GetMapping(path = "scans")
    @ResponseBody
    public ResponseEntity<List<ScanManagerScanResponse>> getScans(@RequestParam("page") Integer page) {
        List<ScanManagerScanResponse> scanManagerScanResponseList = new ArrayList<>();

        Integer scanPageSize = ScanManagerConfiguration.getInstance().getScanPageSize();
        if (scanPageSize == null) {
            scanPageSize = DEFAULT_SCAN_PAGE_SIZE;
        }
        for (Scan scan : scanService.findAll(page, scanPageSize)) {
            scanManagerScanResponseList.add(new ScanManagerScanResponse(scan));
        }
        return new ResponseEntity<>(scanManagerScanResponseList, HttpStatus.OK);
    }

    /**
     * Get scan details for a given id.
     *
     * @param jobId job id of the scan
     * @return scan details for the given job id
     * @throws ResourceNotFoundException when the requested scan is not found
     */
    @GetMapping(value = "scans/{id}")
    public ResponseEntity<ScanManagerScanResponse> getScan(@PathVariable("id") String jobId)
            throws ResourceNotFoundException {
        Scan scan = scanService.getScanByJobId(jobId);
        if (scan != null) {
            return new ResponseEntity<>(new ScanManagerScanResponse(scan), HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException("Unable to find a scan for the given job Id:` " + jobId);
        }
    }

    /**
     * Submit cancel scan request.
     *
     * @param jobId job id of the scan that needs to be canceled
     * @return success response if the scan cancel request is successfully submitted
     * @throws ResourceNotFoundException when scan details cannot be found for the given job id
     * @throws ScanManagerException      when an error occurs while cancelling the scan
     */
    @DeleteMapping(value = "scans/{id}")
    public ResponseEntity cancelScan(@PathVariable("id") String jobId) throws ResourceNotFoundException,
            ScanManagerException {
        Scan scan = scanService.getScanByJobId(jobId);
        if (scan != null) {
            logService.persist(scan, LogType.INFO, new Timestamp(System.currentTimeMillis()),
                    "Scan cancel requested");
            ScanStatus scanStatus = scan.getStatus();
            switch (scanStatus) {
                case SCAN_PENDING:
                    scanService.updateScanStatus(jobId, ScanStatus.CANCELED);
                    break;
                case SUBMITTED:
                case RUNNING:
                    scanService.updateScanStatus(jobId, ScanStatus.CANCEL_PENDING);
                    scanService.cancelScan(scan);
                    break;
                default:
                    String errorMessage = "The scan is not in a running state";
                    logService.persist(scan, LogType.ERROR, new Timestamp(System.currentTimeMillis()), errorMessage);
                    throw new ScanManagerException(errorMessage);
            }
        } else {
            throw new ResourceNotFoundException("Unable to find a scan for the given job Id:` " + jobId);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Filter scans by scan status.
     *
     * @param status status of the scan
     * @return list of scans for a given status
     */
    @GetMapping(value = "scans/status/{status}")
    public ResponseEntity<List<ScanManagerScanResponse>> getScansByState(@PathVariable("status") ScanStatus status) {
        List<ScanManagerScanResponse> scanManagerScanResponseList = new ArrayList<>();
        for (Scan scan : scanService.getScansByStatus(status)) {
            scanManagerScanResponseList.add(new ScanManagerScanResponse(scan));
        }
        return new ResponseEntity<>(scanManagerScanResponseList, HttpStatus.OK);
    }

    /**
     * Update priority of a scan.
     *
     * @param jobId                     job id of the scan to be updated
     * @param scanPriorityUpdateRequest object containing the priority value that needs to be updated
     * @return success if the priority is successfully updated
     */
    @PostMapping(value = "scans/{jobId}")
    public ResponseEntity updateScanPriority(@PathVariable("jobId") String jobId,
                                             @RequestBody ScanPriorityUpdateRequest scanPriorityUpdateRequest)
            throws ResourceNotFoundException {
        Scan scan = scanService.getScanByJobId(jobId);
        if (scan != null) {
            Integer updatedRows = scanService.updateScanPriority(jobId, scanPriorityUpdateRequest.getPriority());
            if (updatedRows == 1) {
                return new ResponseEntity(HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            throw new ResourceNotFoundException("Unable to find a scan for the given job Id:` " + jobId);
        }
    }

    private Set<ScanFile> buildScanFileList(Map<String, String> fileMap, Scan scan) {
        Set<ScanFile> scanFileList = new HashSet<>();
        Iterator fileIterator = fileMap.entrySet().iterator();
        while (fileIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) fileIterator.next();
            ScanFile scanFile = new ScanFile();
            scanFile.setScan(scan);
            scanFile.setScanFileName(pair.getKey().toString());
            scanFile.setScanFileLocation(pair.getValue().toString());
            scanFileList.add(scanFile);
            fileIterator.remove();
        }
        return scanFileList;
    }

    private Set<ScanProperty> buildScanPropertyList(Map<String, String> propertyMap, Scan scan) {
        Set<ScanProperty> scanPropertyList = new HashSet<>();
        Iterator paramIterator = propertyMap.entrySet().iterator();
        while (paramIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) paramIterator.next();
            ScanProperty scanProperty = new ScanProperty();
            scanProperty.setPropertyName(pair.getKey().toString());
            scanProperty.setPropertyValue(pair.getValue().toString());
            scanProperty.setScan(scan);

            scanPropertyList.add(scanProperty);
            paramIterator.remove();
        }
        return scanPropertyList;
    }

    /**
     * Get the list  of available scanners by page.
     *
     * @param page required page number
     * @return a list of available scanners in the requested page
     */
    @GetMapping(path = "scanners")
    @ResponseBody
    public ResponseEntity<List<Scanner>> getScanners(@RequestParam("page") Integer page) {
        Integer scannerPageSize = ScanManagerConfiguration.getInstance().getScannerPageSize();
        if (scannerPageSize == null) {
            scannerPageSize = DEFAULT_SCANNER_PAGE_SIZE;
        }
        return new ResponseEntity<>(scannerService.getScanners(page, scannerPageSize), HttpStatus.OK);
    }

    /**
     * Add or update a scanner.
     *
     * @param scanner scanner details to be added or updated
     * @return inserted scanner details
     */
    @PostMapping(path = "scanners", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Scanner> addOrUpdateScanner(@RequestBody Scanner scanner) {
        if (scannerService.persistScanner(scanner) != null) {
            return new ResponseEntity<>(scannerService.getScannerById(scanner.getId()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Remove a scanner.
     *
     * @param scannerId scanner id of the scanner to be removed
     * @return success response if the scanner was removed successfully
     * @throws InvalidRequestException when the provided scanner id is invalid
     */
    @DeleteMapping(path = "scanners/{scannerId}")
    @ResponseBody
    public ResponseEntity removeScanner(@PathVariable("scannerId") String scannerId) throws InvalidRequestException {
        Scanner scanner = scannerService.getScannerById(scannerId);
        if (scanner != null) {
            Integer count = scannerService.removeByScannerId(scannerId);
            if (count == 1) {
                return new ResponseEntity<>(scanner, HttpStatus.OK);
            } else {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            throw new InvalidRequestException("Invalid scanner id: " + scannerId);
        }
    }

    /**
     * Get logs by scan.
     *
     * @param jobId job id of the scan
     * @param page  required page number
     * @return a list of logs for the given scan
     * @throws ResourceNotFoundException if the scan cannot be found for the given job id
     */
    @GetMapping(value = "logs")
    public ResponseEntity<List<Log>> getLogs(@RequestParam("jobId") String jobId,
                                             @RequestParam("page") Integer page)
            throws ResourceNotFoundException {
        Integer logPageSize = ScanManagerConfiguration.getInstance().getLogPageSize();
        if (logPageSize == null) {
            logPageSize = DEFAULT_LOG_PAGE_SIZE;
        }

        Scan scan = scanService.getScanByJobId(jobId);
        if (scan != null) {
            return new ResponseEntity<>(logService.getLogsByScan(scan, page, logPageSize), HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException("Unable to find logs for the given job Id:` " + jobId);
        }
    }
}
