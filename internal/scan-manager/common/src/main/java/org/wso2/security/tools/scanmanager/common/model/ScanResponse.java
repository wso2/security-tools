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
package org.wso2.security.tools.scanmanager.common.model;

import java.sql.Timestamp;

/**
 * Model class for representing the API response after initiating a scan.
 */
public class ScanResponse {

    private String jobId;
    private String scanName;
    private String scanDescription;
    private String scannerId;
    private ScanStatus status;
    private String product;
    private ScannerType scanType;
    private Timestamp submittedTimestamp;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getScanName() {
        return scanName;
    }

    public void setScanName(String scanName) {
        this.scanName = scanName;
    }

    public String getScanDescription() {
        return scanDescription;
    }

    public void setScanDescription(String scanDescription) {
        this.scanDescription = scanDescription;
    }

    public String getScannerId() {
        return scannerId;
    }

    public void setScannerId(String scannerId) {
        this.scannerId = scannerId;
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

    public ScannerType getScanType() {
        return scanType;
    }

    public void setScanType(ScannerType scanType) {
        this.scanType = scanType;
    }

    public Timestamp getSubmittedTimestamp() {
        return submittedTimestamp;
    }

    public void setSubmittedTimestamp(Timestamp submittedTimestamp) {
        this.submittedTimestamp = submittedTimestamp;
    }
}
