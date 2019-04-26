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
import org.springframework.data.domain.Page;
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
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScanExternal;
import org.wso2.security.tools.scanmanager.common.external.model.ScanFile;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerScanRequest;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerScansResponse;
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
import org.wso2.security.tools.scanmanager.core.service.ScanEngineService;
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
import java.util.stream.Collectors;

import static org.wso2.security.tools.scanmanager.core.util.Constants.SCAN_ARTIFACT;
import static org.wso2.security.tools.scanmanager.core.util.Constants.SCAN_URL;

/**
 * The Web controller which defines the routines for managing scans.
 */
@Controller
@RequestMapping("scan-manager")
public class ScanController {

    private ScannerService scannerService;
    private ScanService scanService;
    private LogService logService;
    private ScanEngineService scanEngineService;

    @Autowired
    public ScanController(ScannerService scannerService, ScanService scanService, LogService logService,
                          ScanEngineService scanEngineService) {
        this.scannerService = scannerService;
        this.scanService = scanService;
        this.logService = logService;
        this.scanEngineService = scanEngineService;
    }

    /**
     * Initiate a scan request.
     *
     * @param scanRequest scan request object
     * @return details of the persisted scan
     * @throws InvalidRequestException when the submitted request is invalid
     */
    @PostMapping(value = "scans", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScanExternal> startScan(@RequestBody ScanManagerScanRequest scanRequest)
            throws InvalidRequestException {
        Scanner scanner = validateScanRequest(scanRequest);
        Scan scan = new Scan();
        scan.setScanner(scanner);

        String jobId = UUID.randomUUID().toString();
        scan.setJobId(jobId);
        scan.setType(scanRequest.getScanType());
        scan.setSubmittedTimestamp(new Timestamp(System.currentTimeMillis()));
        scan.setName(scanRequest.getScanName());
        scan.setDescription(scanRequest.getScanDescription());
        scan.setProduct(scanRequest.getProductName());
        scan.setStatus(ScanStatus.SUBMIT_PENDING);
        scan.setPriority(ScanPriority.MEDIUM.getValue());

        if (scanRequest.getPropertyMap() != null) {
            scan.setPropertyList(buildScanPropertyList(scanRequest.getPropertyMap(), scan));
        }
        if (scanRequest.getFileMap() != null) {
            scan.setFileList(buildScanFileList(scanRequest.getFileMap(), scan));
        }
        scan = scanService.insert(scan);

        // Starting the pending scans.
        new Thread(() -> scanEngineService.beginPendingScans(), "BeginPendingScansFromStartScanRequest").start();
        return new ResponseEntity<>(new ScanExternal(scan), HttpStatus.ACCEPTED);
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

        Scanner scanner = scannerService.getById(scanRequest.getScannerId());
        if (scanner == null) {
            throw new InvalidRequestException("Invalid scanner id");
        }
        return scanner;
    }

    /**
     * Get the list of available scans by page.
     *
     * @param page required page number
     * @return the requested scans page
     */
    @GetMapping(path = "scans")
    @ResponseBody
    public ResponseEntity<ScanManagerScansResponse> getScans(@RequestParam(name = "page", required = false)
                                                                     Integer page) {
        Integer scanPageSize = ScanManagerConfiguration.getInstance().getScanPageSize();
        if (page == null) {
            page = 1;  // Initialize to first page if no page number is defined.
        }

        // Internal page indexing starts at 0
        Page<Scan> scansPage = scanService.findAll(page - 1, scanPageSize);
        List<ScanExternal> scanExternalList =
                scansPage.getContent().parallelStream()
                        .map(ScanExternal::new)
                        .collect(Collectors.toList());
        return new ResponseEntity<>(new ScanManagerScansResponse(scanExternalList, scansPage.getTotalPages(),
                page, scansPage.getSize(), scansPage.hasNext(), scansPage.hasPrevious(), scansPage.isFirst(),
                scansPage.isLast()), HttpStatus.OK);
    }

    /**
     * Get scan details for a given id.
     *
     * @param jobId job id of the scan
     * @return scan details for the given job id
     * @throws ResourceNotFoundException when the requested scan is not found
     */
    @GetMapping(value = "scans/{id}")
    public ResponseEntity<ScanExternal> getScan(@PathVariable("id") String jobId)
            throws ResourceNotFoundException {
        Scan scan = scanService.getByJobId(jobId);
        if (scan != null) {
            return new ResponseEntity<>(new ScanExternal(scan), HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException("Unable to find a scan for the given job Id: " + jobId);
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
        Scan scan = scanService.getByJobId(jobId);
        if (scan != null) {
            logService.insert(scan, LogType.INFO, "Scan cancel requested for the scan: " + jobId);
            ScanStatus scanStatus = scan.getStatus();
            if (scanStatus == ScanStatus.SUBMIT_PENDING || scanStatus == ScanStatus.SUBMITTED ||
                    scanStatus == ScanStatus.RUNNING) {
                scanEngineService.cancelScan(scan);
            } else {
                String errorMessage = "The scan is not in a running state";
                logService.insert(scan, LogType.ERROR, errorMessage);
                throw new ScanManagerException(errorMessage);
            }
        } else {
            throw new ResourceNotFoundException("Unable to find a scan for the given job Id: " + jobId);
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
    public ResponseEntity<List<ScanExternal>> getScansByState(@PathVariable("status") ScanStatus status) {
        return new ResponseEntity<>(scanService.getByStatus(status).parallelStream()
                .map(ScanExternal::new)
                .collect(Collectors.toList()), HttpStatus.OK);
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
            throws ResourceNotFoundException, ScanManagerException {
        Scan scan = scanService.getByJobId(jobId);
        if (scan != null) {
            scanService.updatePriority(jobId, scanPriorityUpdateRequest.getPriority());
            logService.insert(scan, LogType.INFO, "Updated scan priority: " +
                    scanPriorityUpdateRequest.getPriority());
            return new ResponseEntity(HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException("Unable to find a scan for the given job Id: " + jobId);
        }
    }

    private Set<ScanFile> buildScanFileList(Map<String, String> fileMap, Scan scan) {
        Set<ScanFile> scanFileList = new HashSet<>();
        Iterator fileIterator = fileMap.entrySet().iterator();
        while (fileIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) fileIterator.next();
            ScanFile scanFile = new ScanFile();
            scanFile.setScan(scan);
            scanFile.setName(pair.getKey().toString());
            scanFile.setLocation(pair.getValue().toString());
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
            scanProperty.setName(pair.getKey().toString());
            scanProperty.setValue(pair.getValue().toString());
            scanProperty.setScan(scan);

            scanPropertyList.add(scanProperty);
            paramIterator.remove();
        }
        return scanPropertyList;
    }
}
