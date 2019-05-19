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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
 * Model class to represent an HTML field for a scanner.
 */
@Entity
@Table(name = "SCANNER_FIELD", uniqueConstraints = @UniqueConstraint(columnNames = {"FIELD_ID", "SCANNER_ID"}))
public class ScannerField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "FIELD_ID", nullable = false)
    private String fieldId;

    @ManyToOne
    @JoinColumn(name = "SCANNER_ID")
    @JsonBackReference
    @JsonIgnoreProperties(ignoreUnknown = true)
    private Scanner scanner;

    @Column(name = "FIELD_NAME")
    private String displayName;

    @Column(name = "FIELD_TYPE")
    private String type;

    @Column(name = "FIELD_ORDER")
    private Integer order;

    @Column(name = "IS_REQUIRED")
    private boolean isRequired;

    @Column(name = "HAS_DEFAULT")
    private boolean hasDefault;

    public ScannerField() {
    }

    public ScannerField(String fieldId, Scanner scanner, String displayName, String type, Integer order,
                        boolean isRequired, boolean hasDefault) {
        this.fieldId = fieldId;
        this.scanner = scanner;
        this.displayName = displayName;
        this.type = type;
        this.order = order;
        this.isRequired = isRequired;
        this.hasDefault = hasDefault;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean getHasDefault() {
        return hasDefault;
    }

    public void setHasDefault(boolean hasDefault) {
        this.hasDefault = hasDefault;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public boolean isHasDefault() {
        return hasDefault;
    }
}
