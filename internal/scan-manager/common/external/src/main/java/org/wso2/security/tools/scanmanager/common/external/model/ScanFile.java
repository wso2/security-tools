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

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Model class to represent a file required for a scanner.
 */
@Entity
@Table(name = "SCAN_FILE", uniqueConstraints = @UniqueConstraint(columnNames = {"JOB_ID", "SCAN_FILE_NAME"}))
public class ScanFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false, nullable = false)
    private Integer scanFileId;

    @ManyToOne
    @JoinColumn(name = "JOB_ID")
    @JsonBackReference
    private Scan scan;

    @Column(name = "SCAN_FILE_NAME")
    private String scanFileName;

    @Column(name = "SCAN_FILE_LOCATION")
    private String scanFileLocation;

    public ScanFile() {
    }

    public ScanFile(Scan scan, String scanFileName, String scanFileLocation) {
        this.scan = scan;
        this.scanFileName = scanFileName;
        this.scanFileLocation = scanFileLocation;
    }

    public Integer getScanFileId() {
        return scanFileId;
    }

    public void setScanFileId(Integer scanFileId) {
        this.scanFileId = scanFileId;
    }

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public String getScanFileName() {
        return scanFileName;
    }

    public void setScanFileName(String scanFileName) {
        this.scanFileName = scanFileName;
    }

    public String getScanFileLocation() {
        return scanFileLocation;
    }

    public void setScanFileLocation(String scanFileLocation) {
        this.scanFileLocation = scanFileLocation;
    }
}
