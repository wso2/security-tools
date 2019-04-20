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
import org.wso2.security.tools.scanmanager.common.model.ScannerType;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Model class to represent a scanner.
 */
@Entity
@Table(name = "SCANNER")
public class Scanner {

    @Id
    @Column(name = "SCANNER_ID", nullable = false)
    private String id;

    @Column(name = "SCANNER_NAME")
    private String name;

    @Column(name = "SCANNER_TYPE")
    @Enumerated(EnumType.STRING)
    private ScannerType scannerType;

    @Column(name = "SCANNER_IMAGE")
    private String scannerImage;

    @OneToMany(mappedBy = "scanner", fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
    @JsonManagedReference
    private Set<ScannerField> fields;

    @OneToMany(mappedBy = "scanner", fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
    @JsonManagedReference
    private Set<ScannerApp> apps;

    public Scanner() {
    }

    public Scanner(String id) {
        this.id = id;
    }

    public Set<ScannerField> getFields() {
        return fields;
    }

    public void setFields(Set<ScannerField> fields) {
        this.fields = fields;
    }

    public Set<ScannerApp> getApps() {
        return apps;
    }

    public void setApps(Set<ScannerApp> apps) {
        this.apps = apps;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScannerType getScannerType() {
        return scannerType;
    }

    public void setScannerType(ScannerType scannerType) {
        this.scannerType = scannerType;
    }

    public String getScannerImage() {
        return scannerImage;
    }

    public void setScannerImage(String scannerImage) {
        this.scannerImage = scannerImage;
    }
}
