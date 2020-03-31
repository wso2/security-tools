/*
 *   Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.security.tools.scanmanager.scanners.qualys.config;

import org.wso2.security.tools.scanmanager.common.config.YAMLConfigurationReader;

/**
 * Qualys Scanner Specific Configuration.
 */
public class QualysScannerConfiguration extends YAMLConfigurationReader {

    private static final QualysScannerConfiguration INSTANCE = new QualysScannerConfiguration();

    private QualysScannerConfiguration() {
    }

    public static QualysScannerConfiguration getInstance() {
        return INSTANCE;
    }

    private long schedulerDelay;
    private String host;
    private String defaultScanType;
    private String defaultProfileId;
    private String defaultScannerAppliance;
    private String defaultProgressiveScanning;
    private String defaultReportTemplateID;

    public String getDefaultCrawlingScope() {
        return defaultCrawlingScope;
    }

    public void setDefaultCrawlingScope(String defaultCrawlingScope) {
        this.defaultCrawlingScope = defaultCrawlingScope;
    }

    private String defaultCrawlingScope;

    public String getDefaultReportTemplateID() {
        return defaultReportTemplateID;
    }

    public void setDefaultReportTemplateID(String defaultReportTemplateID) {
        this.defaultReportTemplateID = defaultReportTemplateID;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getSchedulerDelay() {
        return schedulerDelay;
    }

    public void setSchedulerDelay(long schedulerDelay) {
        this.schedulerDelay = schedulerDelay;
    }

    public String getDefaultScanType() {
        return defaultScanType;
    }

    public void setDefaultScanType(String defaultScanType) {
        this.defaultScanType = defaultScanType;
    }

    public String getDefaultProfileId() {
        return defaultProfileId;
    }

    public void setDefaultProfileId(String defaultProfileId) {
        this.defaultProfileId = defaultProfileId;
    }

    public String getDefaultScannerAppliance() {
        return defaultScannerAppliance;
    }

    public void setDefaultScannerAppliance(String defaultScannerAppliance) {
        this.defaultScannerAppliance = defaultScannerAppliance;
    }

    public String getDefaultProgressiveScanning() {
        return defaultProgressiveScanning;
    }

    public void setDefaultProgressiveScanning(String defaultProgressiveScanning) {
        this.defaultProgressiveScanning = defaultProgressiveScanning;
    }

}
