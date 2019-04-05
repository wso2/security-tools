/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.automation.manager.entity.dynamicscanner;

import org.wso2.security.tools.automation.manager.entity.productmanager.ProductManagerEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * The abstract class {@code DynamicScannerEntity} is a database entity to store dynamic scanners.
 * <p>{@code Inheritance} type is defined as {@code JOINED}, so that a database table for {@code
 * DynamicScannerEntity} is created and any entity inherited from this class is stored in this table. Also tables are
 * generated for each sub class, and only sub class specific fields are saved in those tables</p>
 */
@SuppressWarnings("unused")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DynamicScannerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected int id;
    @NotNull
    protected String userId;
    protected String type;
    protected String createdTime;
    protected String status;
    protected String scanStatus;
    protected int scanProgress = -1;
    protected String scanProgressTime;
    protected boolean reportReady;
    protected String reportReadyTime;
    protected boolean reportSent;
    protected String reportSentTime;
    protected String message;

    //Defines one to one relationship with ProductManagerEntity
    @OneToOne(mappedBy = "dynamicScannerEntity")
    protected ProductManagerEntity productManagerEntity;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getScanStatus() {
        return scanStatus;
    }

    public void setScanStatus(String scanStatus) {
        this.scanStatus = scanStatus;
    }

    public int getScanProgress() {
        return scanProgress;
    }

    public void setScanProgress(int scanProgress) {
        this.scanProgress = scanProgress;
    }

    public String getScanProgressTime() {
        return scanProgressTime;
    }

    public void setScanProgressTime(String scanProgressTime) {
        this.scanProgressTime = scanProgressTime;
    }

    public boolean isReportReady() {
        return reportReady;
    }

    public void setReportReady(boolean reportReady) {
        this.reportReady = reportReady;
    }

    public String getReportReadyTime() {
        return reportReadyTime;
    }

    public void setReportReadyTime(String reportReadyTime) {
        this.reportReadyTime = reportReadyTime;
    }

    public boolean isReportSent() {
        return reportSent;
    }

    public void setReportSent(boolean reportSent) {
        this.reportSent = reportSent;
    }

    public String getReportSentTime() {
        return reportSentTime;
    }

    public void setReportSentTime(String reportSentTime) {
        this.reportSentTime = reportSentTime;
    }
}
