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

import org.wso2.security.tools.scanmanager.common.model.ScanType;

import java.util.Map;

/**
 * Model class to represent a scan request for scan manager.
 */
public class ScanManagerScanRequest {

    private String scanName;
    private String scanDescription;
    private ScanType scanType;
    private String productName;
    private String scannerId;
    private User user;
    private Map<String, String> fileMap;
    private Map<String, String> propertyMap;

    public ScanManagerScanRequest() {
    }

    public ScanManagerScanRequest(String scanName, String scanDescription, ScanType scanType, String productName,
                                  String scannerId, Map<String, String> fileMap, Map<String, String> propertyMap,
                                  User user) {
        this.scanName = scanName;
        this.scanDescription = scanDescription;
        this.scanType = scanType;
        this.productName = productName;
        this.scannerId = scannerId;
        this.fileMap = fileMap;
        this.propertyMap = propertyMap;
        this.user = user;
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

    public ScanType getScanType() {
        return scanType;
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getScannerId() {
        return scannerId;
    }

    public void setScannerId(String scannerId) {
        this.scannerId = scannerId;
    }

    public User getUser() {
        return user;
    }

    public Map<String, String> getFileMap() {
        return fileMap;
    }

    public void setFileMap(Map<String, String> fileMap) {
        this.fileMap = fileMap;
    }

    public Map<String, String> getPropertyMap() {
        return propertyMap;
    }

    public void setPropertyMap(Map<String, String> propertyMap) {
        this.propertyMap = propertyMap;
    }
}
