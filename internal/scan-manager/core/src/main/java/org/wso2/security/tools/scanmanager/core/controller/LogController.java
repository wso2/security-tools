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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wso2.security.tools.scanmanager.common.external.model.Log;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScanExternal;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerLogResponse;
import org.wso2.security.tools.scanmanager.core.config.ScanManagerConfiguration;
import org.wso2.security.tools.scanmanager.core.exception.ResourceNotFoundException;
import org.wso2.security.tools.scanmanager.core.service.LogService;
import org.wso2.security.tools.scanmanager.core.service.ScanService;

/**
 * Web controller which defines the routines for managing scanners.
 */
@Controller
@RequestMapping("scan-manager")
public class LogController {

    private LogService logService;
    private ScanService scanService;

    @Autowired
    public LogController(LogService logService, ScanService scanService) {
        this.logService = logService;
        this.scanService = scanService;
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
    public ResponseEntity<ScanManagerLogResponse> getLogs(@RequestParam("jobId") String jobId,
                                                          @RequestParam(name = "page", required = false) Integer page)
            throws ResourceNotFoundException {
        Integer logPageSize = ScanManagerConfiguration.getInstance().getLogPageSize();
        if (page == null) {
            page = 1;  // Initialize to first page if no page number is defined.
        }
        Scan scan = scanService.getByJobId(jobId);

        //internal page indexing starts at 0
        Page<Log> logs = logService.getByScan(scan, page - 1, logPageSize);
        if (scan != null) {
            return new ResponseEntity<>(new ScanManagerLogResponse(logs.getContent(), new ScanExternal(scan),
                    logs.getTotalPages(), page, logs.getSize(), logs.hasNext(), logs.hasPrevious(), logs.isFirst(),
                    logs.isLast()), HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException("Unable to find logs for the given job Id: " + jobId);
        }
    }
}
