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
package org.wso2.security.tools.scanmanager.common.external.model;

import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.common.model.ScanType;

import java.sql.Timestamp;

/**
 * Model class for representing the API response after initiating a scan.
 */
public class ScanExternal {

    private String jobId;
    private String name;
    private String description;
    private String scannerId;
    private String scannerName;
    private ScanStatus status;
    private String product;
    private ScanType type;
    private Timestamp submittedTimestamp;
    private Timestamp startedTimestamp;
    private String user;
    private String scanReportPath;

    public ScanExternal(String jobId, String name, String description, String scannerId,
                        String scannerName, ScanStatus status, String product, ScanType type,
                        Timestamp submittedTimestamp, Timestamp startedTimestamp, String user,
                        String scanReportPath) {
        this.jobId = jobId;
        this.name = name;
        this.description = description;
        this.scannerId = scannerId;
        this.scannerName = scannerName;
        this.status = status;
        this.product = product;
        this.type = type;
        this.user = user;
        this.scanReportPath = scanReportPath;

        if (submittedTimestamp != null) {
            this.submittedTimestamp = new Timestamp(submittedTimestamp.getTime());
        }
        if (startedTimestamp != null) {
            this.startedTimestamp = new Timestamp(startedTimestamp.getTime());
        }
    }

    public ScanExternal(Scan scan) {
        if (scan != null) {
            this.jobId = scan.getJobId();
            this.name = scan.getName();
            this.description = scan.getDescription();
            this.status = scan.getStatus();
            this.product = scan.getProduct();
            this.type = scan.getType();
            this.submittedTimestamp = scan.getSubmittedTimestamp();
            this.startedTimestamp = scan.getStartTimestamp();
            this.user = scan.getUser();
            this.scanReportPath = scan.getReportPath();

            if (scan.getScanner() != null) {
                this.scannerId = scan.getScanner().getId();
                this.scannerName = scan.getScanner().getName();
            }
        }
    }

    public ScanExternal() {
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScannerId() {
        return scannerId;
    }

    public void setScannerId(String scannerId) {
        this.scannerId = scannerId;
    }

    public String getScannerName() {
        return scannerName;
    }

    public void setScannerName(String scannerName) {
        this.scannerName = scannerName;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public ScanType getType() {
        return type;
    }

    public void setType(ScanType type) {
        this.type = type;
    }

    public Timestamp getStartedTimestamp() {
        if (startedTimestamp != null) {
            return new Timestamp(startedTimestamp.getTime());
        } else {
            return null;
        }
    }

    public void setStartedTimestamp(Timestamp startedTimestamp) {
        if (startedTimestamp != null) {
            this.startedTimestamp = new Timestamp(startedTimestamp.getTime());
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getScanReportPath() {
        return scanReportPath;
    }

    public void setScanReportPath(String scanReportPath) {
        this.scanReportPath = scanReportPath;
    }

    public Timestamp getSubmittedTimestamp() {
        if (submittedTimestamp != null) {
            return new Timestamp(submittedTimestamp.getTime());
        } else {
            return null;
        }
    }

    public void setSubmittedTimestamp(Timestamp submittedTimestamp) {
        if (submittedTimestamp != null) {
            this.submittedTimestamp = new Timestamp(submittedTimestamp.getTime());
        }
    }
}
