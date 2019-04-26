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
package org.wso2.security.tools.scanmanager.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.core.exception.InvalidRequestException;
import org.wso2.security.tools.scanmanager.core.util.Constants;

import java.sql.Timestamp;

/**
 * Service class that manage the method implementations of the callback endpoint.
 */
@Service
public class CallbackServiceImpl implements CallbackService {

    private ScanEngineService scanEngineService;
    private ScanService scanService;

    @Autowired
    public CallbackServiceImpl(ScanEngineService scanEngineService, ScanService scanService) {
        this.scanEngineService = scanEngineService;
        this.scanService = scanService;
    }

    @Override
    public void updateScan(Scan scan, ScanStatus scanStatus, String scannerScanId, String scanReportPath)
            throws InvalidRequestException {
        synchronized (Constants.LOCK) {

            //get the scan details again from database as the scan details might have been changed before entering the
            // synchronized block
            Scan newScanObject = scanService.getByJobId(scan.getJobId());
            switch (scanStatus) {
                case RUNNING:
                    if (newScanObject.getStatus() == ScanStatus.SUBMITTED) {
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        newScanObject.setStartTimestamp(timestamp);
                        newScanObject.setScannerScanId(scannerScanId);
                        newScanObject.setStatus(scanStatus);
                    }
                    break;
                case COMPLETED:
                    newScanObject.setReportPath(scanReportPath);
                    newScanObject.setStatus(scanStatus);
                    scanEngineService.removeContainer(newScanObject);
                    new Thread(() -> scanEngineService.beginPendingScans(), "BeginPendingScansFromCallback").start();
                    break;
                case ERROR:
                case CANCELED:
                    newScanObject.setStatus(scanStatus);
                    scanEngineService.removeContainer(newScanObject);
                    new Thread(() -> scanEngineService.beginPendingScans(), "BeginPendingScansFromCallback").start();
                    break;
                default:
                    throw new InvalidRequestException("Unsupported scan status: " + scanStatus);
            }
            scanService.update(newScanObject);
        }
    }
}
