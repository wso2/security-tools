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

package org.wso2.security.tools.scanmanager.model;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Model class to represent a scan.
 */

@Entity
@Table(name = "SCAN")
public class Scan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false, nullable = false)
    private Integer scanId;

    @Column(name = "SCANNER")
    private String scanner;

    @Column(name = "PRODUCT")
    private String product;

    @Column(name = "SOURCE_PATH")
    private String sourcePath;

    @Column(name = "TYPE")
    private String scanType;

    @Column(name = "USER")
    private String user;

    @Column(name = "RESPONSE_PATH")
    private String responsePath;

    @Column(name = "STARTED_TIMESTAMP")
    private java.sql.Timestamp startTimestamp;

    @Column(name = "REPORT_PATH")
    private String reportPath;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "SUBMITTED_TIMESTAMP")
    private java.sql.Timestamp submittedTimestamp;

    @Column(name = "ACTUAL_SCANNER_ID")
    private String actualScannerId;

    public Scan() {
    }

    public Scan(String scanner, String product, String sourcePath, String scanType, String user,
                String responsePath, String reportPath, String status, java.sql.Timestamp submitTime) {
        this.scanner = scanner;
        this.product = product;
        this.sourcePath = sourcePath;
        this.scanType = scanType;
        this.user = user;
        this.responsePath = responsePath;
        this.reportPath = reportPath;
        this.status = status;
        this.submittedTimestamp = submitTime;
    }

    public Integer getScanId() {
        return scanId;
    }

    public void setScanId(Integer scanId) {
        this.scanId = scanId;
    }

    public String getScanner() {
        return scanner;
    }

    public void setScanner(String scanner) {
        this.scanner = scanner;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getResponsePath() {
        return responsePath;
    }

    public void setResponsePath(String responsePath) {
        this.responsePath = responsePath;
    }

    public Timestamp getStartTimestamp() {
        return startTimestamp;
    }

    public Timestamp getSubmittedTimestamp() {
        return submittedTimestamp;
    }

    public void setSubmittedTimestamp(Timestamp submittedTimestamp) {
        this.submittedTimestamp = submittedTimestamp;
    }

    public void setStartTimestamp(Timestamp startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActualScannerId() {
        return actualScannerId;
    }

    public void setActualScannerId(String actualScannerId) {
        this.actualScannerId = actualScannerId;
    }
}
