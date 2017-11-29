/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.security.tools.automation.manager.entity.productmanager;

import org.wso2.security.tools.automation.manager.entity.dynamicscanner.DynamicScannerEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * The class {@code ProductManagerEntity} is a database entity to store product manager objects related to dynamic
 * scans. {@code ProductManagerEntity} can be a Docker container or else a product that is already in up and running
 * state.
 */
@SuppressWarnings("unused")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ProductManagerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @NotNull
    private String userId;
    private int relatedDynamicScannerId;
    private String testName;
    private String productName;
    private String wumLevel;
    private String createdTime;
    private String status;
    private boolean scanFinished;
    private String scanFinishedTime;
    private String message;

    //Defines the one to one relationship with DynamicScannerEntity
    @OneToOne(cascade = CascadeType.ALL)
    //Defines the foreign key constraint for the column "relatedDynamicScannerId"
    @JoinColumn(name = "relatedDynamicScannerId", insertable = false, updatable = false)
    private DynamicScannerEntity dynamicScannerEntity;

    public int getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRelatedDynamicScannerId() {
        return relatedDynamicScannerId;
    }

    public void setRelatedDynamicScannerId(int relatedDynamicScannerId) {
        this.relatedDynamicScannerId = relatedDynamicScannerId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getWumLevel() {
        return wumLevel;
    }

    public void setWumLevel(String wumLevel) {
        this.wumLevel = wumLevel;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isScanFinished() {
        return scanFinished;
    }

    public void setScanFinished(boolean scanFinished) {
        this.scanFinished = scanFinished;
    }

    public String getScanFinishedTime() {
        return scanFinishedTime;
    }

    public void setScanFinishedTime(String scanFinishedTime) {
        this.scanFinishedTime = scanFinishedTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
