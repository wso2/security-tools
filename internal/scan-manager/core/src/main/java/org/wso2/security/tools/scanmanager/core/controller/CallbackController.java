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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.internal.model.ScanLogRequest;
import org.wso2.security.tools.scanmanager.common.internal.model.ScanStatusUpdateRequest;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.core.exception.InvalidRequestException;
import org.wso2.security.tools.scanmanager.core.exception.ResourceNotFoundException;
import org.wso2.security.tools.scanmanager.core.service.LogService;
import org.wso2.security.tools.scanmanager.core.service.ScanService;

import java.sql.Timestamp;

/**
 * The class {@code CallbackController} is the web controller that provides an endpoint to interact with scanner
 * services.
 */
@Controller
@RequestMapping("callback")
public class CallbackController {

    private LogService logService;
    private ScanService scanService;

    @Autowired
    public CallbackController(ScanService scanService, LogService logService) {
        this.scanService = scanService;
        this.logService = logService;
    }

    /**
     * Persisting logs from the scan micro services.
     *
     * @param scanLogRequest scan log request object
     * @return success response if the scan log is successfully persisted
     * @throws InvalidRequestException when an invalid job id is found
     */
    @PostMapping(value = "persist-scan-log")
    @ResponseBody
    public ResponseEntity persistScanLog(@RequestBody ScanLogRequest scanLogRequest) throws InvalidRequestException {
        if (scanService.getScanByJobId(scanLogRequest.getJobId()) == null) {
            throw new InvalidRequestException("Invalid job id");
        }
        logService.persist(new Scan(scanLogRequest.getJobId()), scanLogRequest.getType(), scanLogRequest.getTimestamp(),
                scanLogRequest.getMessage());
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Update the scan status.
     *
     * @param scanStatusUpdateRequest scan status update request
     * @return success response if the scan is successfully updated
     * @throws ResourceNotFoundException when unable to retrieve the scan for the given job id
     * @throws InvalidRequestException   when the request parameters are invalid
     */
    @PostMapping(value = "update-scan-status")
    @ResponseBody
    public ResponseEntity updateScanStatus(@RequestBody ScanStatusUpdateRequest scanStatusUpdateRequest)
            throws ResourceNotFoundException, InvalidRequestException {
        Scan scan;
        ScanStatus scanStatus;

        if (StringUtils.isNotEmpty(scanStatusUpdateRequest.getJobId())) {
            scanStatus = scanStatusUpdateRequest.getScanStatus();
            scan = scanService.getScanByJobId(scanStatusUpdateRequest.getJobId());
        } else {
            throw new InvalidRequestException("parameter jobId cannot be found");
        }
        if (scan != null) {
            scan.setStatus(scanStatus);
            switch (scanStatus) {
                case RUNNING:
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    scan.setStartTimestamp(timestamp);
                    scan.setScannerScanId(scanStatusUpdateRequest.getScannerScanId());
                    scanService.update(scan);
                    break;
                case COMPLETED:
                    scan.setReportPath(scanStatusUpdateRequest.getScanReportPath());
                    scanService.update(scan);
                    new Thread(() -> scanService.removeContainer(scan)).start();
                    new Thread(() -> scanService.beginPendingScans()).start();
                    break;
                case ERROR:
                    scanService.updateScanStatus(scan.getJobId(), ScanStatus.ERROR);
                    new Thread(() -> scanService.removeContainer(scan)).start();
                    new Thread(() -> scanService.beginPendingScans()).start();
                    break;
                case CANCELED:
                    scanService.updateScanStatus(scan.getJobId(), ScanStatus.CANCELED);
                    new Thread(() -> scanService.removeContainer(scan)).start();
                    new Thread(() -> scanService.beginPendingScans()).start();
                    break;
                default:
                    throw new InvalidRequestException("Unsupported scan status: " + scanStatus);
            }
        } else {
            throw new ResourceNotFoundException("Unable to retrieve the Scan for the job id: " +
                    scanStatusUpdateRequest.getJobId());
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
