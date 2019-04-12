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
 * Model Class to represent a Scan Property.
 */
@Entity
@Table(name = "SCAN_PROPERTY", uniqueConstraints = @UniqueConstraint(columnNames = {"JOB_ID", "PROPERTY_NAME"}))
public class ScanProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false, nullable = false)
    private Integer scanPropertyId;

    @ManyToOne
    @JoinColumn(name = "JOB_ID")
    @JsonBackReference
    private Scan scan;

    @Column(name = "PROPERTY_NAME")
    private String propertyName;

    @Column(name = "PROPERTY_VALUE")
    private String propertyValue;

    public ScanProperty() {
    }

    public ScanProperty(Scan scan, String propertyName, String propertyValue) {
        this.scan = scan;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public Integer getScanPropertyId() {
        return scanPropertyId;
    }

    public void setScanPropertyId(Integer scanPropertyId) {
        this.scanPropertyId = scanPropertyId;
    }

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
}
