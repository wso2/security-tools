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
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.wso2.security.tools.scanmanager.common.model.ScannerType;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
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
    private ScannerType type;

    @Column(name = "SCANNER_IMAGE")
    private String image;

    @OneToMany(mappedBy = "scanner")
    @Cascade(CascadeType.ALL)
    @JsonManagedReference
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy("order ASC")
    private List<ScannerField> fields;

    @OneToMany(mappedBy = "scanner")
    @Cascade(CascadeType.ALL)
    @JsonManagedReference
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ScannerApp> apps;

    public Scanner() {
    }

    public Scanner(String id, String name, ScannerType type, String image, List<ScannerField> fields,
                   List<ScannerApp> apps) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.image = image;
        this.fields = fields;
        this.apps = apps;
    }

    public Scanner(String id) {
        this.id = id;
    }

    public List<ScannerField> getFields() {
        return fields;
    }

    public void setFields(List<ScannerField> fields) {
        this.fields = fields;
    }

    public List<ScannerApp> getApps() {
        return apps;
    }

    public void setApps(List<ScannerApp> apps) {
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

    public ScannerType getType() {
        return type;
    }

    public void setType(ScannerType type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
