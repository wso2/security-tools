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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.common.model.ScanType;

import java.sql.Timestamp;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Model class to represent a scan.
 */
@Entity
@Table(name = "SCAN")
public class Scan {

    @Id
    @Column(name = "JOB_ID", nullable = true)
    private String jobId;

    @Column(name = "SCAN_NAME")
    private String name;

    @Column(name = "SCAN_DESCRIPTION")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "SCANNER_ID")
    private Scanner scanner;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private ScanStatus status;

    @Column(name = "PRIORITY")
    private int priority;

    @Column(name = "PRODUCT")
    private String product;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private ScanType type;

    @Column(name = "USER")
    private String user;

    @Column(name = "SUBMITTED_TIMESTAMP")
    private Timestamp submittedTimestamp;

    @Column(name = "STARTED_TIMESTAMP")
    private Timestamp startTimestamp;

    @Column(name = "SCANNER_SCAN_ID")
    private String scannerScanId;

    @Column(name = "SCANNER_APP_ID")
    private String scannerAppId;

    @Column(name = "REPORT_PATH")
    private String reportPath;

    @OneToMany(mappedBy = "scan", fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
    @JsonManagedReference
    private Set<ScanFile> fileList;

    @OneToMany(mappedBy = "scan", fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
    @JsonManagedReference
    private Set<ScanProperty> propertyList;

    public Scan() {
    }

    public Scan(String jobId, String name, String description, Scanner scanner, ScanStatus status, int priority,
                String product, ScanType type, String user, String scannerScanId, String scannerAppId,
                String reportPath, Set<ScanFile> fileList, Set<ScanProperty> propertyList,
                Timestamp submittedTimestamp, Timestamp startTimestamp) {
        this.jobId = jobId;
        this.name = name;
        this.description = description;
        this.scanner = scanner;
        this.status = status;
        this.priority = priority;
        this.product = product;
        this.type = type;
        this.user = user;
        this.scannerScanId = scannerScanId;
        this.scannerAppId = scannerAppId;
        this.reportPath = reportPath;
        this.fileList = fileList;
        this.propertyList = propertyList;

        if (submittedTimestamp != null) {
            this.submittedTimestamp = new Timestamp(submittedTimestamp.getTime());
        }
        if (startTimestamp != null) {
            this.startTimestamp = new Timestamp(startTimestamp.getTime());
        }
    }

    public Scan(String jobId) {
        this.jobId = jobId;
    }

    public String getScannerScanId() {
        return scannerScanId;
    }

    public void setScannerScanId(String scannerScanId) {
        this.scannerScanId = scannerScanId;
    }

    public String getScannerAppId() {
        return scannerAppId;
    }

    public void setScannerAppId(String scannerAppId) {
        this.scannerAppId = scannerAppId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scannerId) {
        this.scanner = scannerId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Timestamp getStartTimestamp() {
        if (startTimestamp != null) {
            return new Timestamp(startTimestamp.getTime());
        } else {
            return null;
        }
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

    public void setStartTimestamp(Timestamp startTimestamp) {
        if (startTimestamp != null) {
            this.startTimestamp = new Timestamp(startTimestamp.getTime());
        }
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    public Set<ScanFile> getFileList() {
        return fileList;
    }

    public void setFileList(Set<ScanFile> fileList) {
        this.fileList = fileList;
    }

    public Set<ScanProperty> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(Set<ScanProperty> propertyList) {
        this.propertyList = propertyList;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public ScanType getType() {
        return type;
    }

    public void setType(ScanType type) {
        this.type = type;
    }
}
