/*
 *  Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.security.tools.scanmanager.common;

import java.util.List;
import java.util.Map;

/**
 * Class to represent the object that comes with the Scan operation http servlet request.
 */
public class ScanRequest {

    private String appId;
    private String jobId;
    private String productName;
    Map<String, List<String>> fileMap;
    Map<String, List<String>> propertyMap;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Map<String, List<String>> getFileMap() {
        return fileMap;
    }

    public void setFileMap(Map<String, List<String>> fileMap) {
        this.fileMap = fileMap;
    }

    public Map<String, List<String>> getPropertyMap() {
        return propertyMap;
    }

    public void setPropertyMap(Map<String, List<String>> propertyMap) {
        this.propertyMap = propertyMap;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
