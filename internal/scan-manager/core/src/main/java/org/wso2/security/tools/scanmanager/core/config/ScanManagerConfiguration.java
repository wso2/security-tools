/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.core.config;

import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.wso2.security.tools.scanmanager.core.util.Constants.DEFAULT_LOG_PAGE_SIZE;
import static org.wso2.security.tools.scanmanager.core.util.Constants.DEFAULT_SCAN_PAGE_SIZE;

/**
 * Scan Manager configuration model class.
 */
public class ScanManagerConfiguration {

    private String scanManagerHost;
    private Integer scanManagerPort;
    private Integer scannerServicePort;
    private String scannerServiceHost;
    private Integer scanPageSize;
    private Integer logPageSize;

    // Email notification related configurations
    private Boolean isNotificationEnabled;
    private String notificationSubject;
    private String smtpServerHost;
    private Integer smtpServerPort;
    private String smtpUserName;
    private String emailFromaddress;
    private List<String> emailCCaddress;

    private static final String SCAN_MANAGER_HOST_KEY = "scanManagerHost";
    private static final String SCAN_MANAGER_PORT_KEY = "scanManagerPort";
    private static final String SCANNER_SERVICE_HOST_KEY = "scannerServiceHost";
    private static final String SCANNER_SERVICE_PORT_KEY = "scannerServicePort";
    private static final String SCAN_PAGE_SIZE = "scanPageSize";
    private static final String LOG_PAGE_SIZE = "logPageSize";
    private static final String SMTP_SERVERHOST_KEY = "smtpServerHost";
    private static final String SMTP_SERVERPORT_KEY = "smtpServerPort";
    private static final String SMTP_USERNAME = "smtpUsername";
    private static final String SMTP_EMAIL_FROM_ADDRESS = "emailFromaddress";
    private static final String SMTP_EMAIL_CC_ADDRESS = "emailCCaddress";
    private static final String IS_NOTIFICATION_ENABLED = "isNotificationEnabled";
    private static final String NOTIFICATION_SUBJECT = "notificationSubject";

    private static final ScanManagerConfiguration scanManagerConfiguration = new ScanManagerConfiguration();

    private ScanManagerConfiguration() {
    }

    public static ScanManagerConfiguration getInstance() {
        return scanManagerConfiguration;
    }

    /**
     * Initializing the configuration.
     *
     * @param configObjectMap configuration map read from the scan-manager-config.yaml
     * @throws ScanManagerException when the required configurations are not found
     */
    public void initScanConfiguration(Map<String, Object> configObjectMap) throws ScanManagerException {
        String scanManagerHost = (String) configObjectMap.get(SCAN_MANAGER_HOST_KEY);
        Integer scanManagerPort = (Integer) configObjectMap.get(SCAN_MANAGER_PORT_KEY);
        String scannerServiceHost = (String) configObjectMap.get(SCANNER_SERVICE_HOST_KEY);
        Integer scannerServicePort = (Integer) configObjectMap.get(SCANNER_SERVICE_PORT_KEY);
        String smtpServerHost = (String) configObjectMap.get(SMTP_SERVERHOST_KEY);
        Integer smtpServerPort = (Integer) configObjectMap.get(SMTP_SERVERPORT_KEY);
        String smtpUserName = (String) configObjectMap.get(SMTP_USERNAME);
        String smtpEmailFromAddress = (String) configObjectMap.get(SMTP_EMAIL_FROM_ADDRESS);
        String smtpEmailCCAddress = (String) configObjectMap.get(SMTP_EMAIL_CC_ADDRESS);
        Boolean isNotificationEnabled = (Boolean) configObjectMap.get(IS_NOTIFICATION_ENABLED);
        String notificiationSubject = (String) configObjectMap.get(NOTIFICATION_SUBJECT);

        if (scanManagerHost != null) {
            this.scanManagerHost = scanManagerHost;
        } else {
            throw new ScanManagerException("Unable to find scan manager host configuration");
        }
        if (scanManagerPort != null) {
            this.scanManagerPort = scanManagerPort;
        } else {
            throw new ScanManagerException("Unable to find scan manager port configuration");
        }
        if (scannerServiceHost != null) {
            this.scannerServiceHost = scannerServiceHost;
        } else {
            throw new ScanManagerException("Unable to find scanner service host configuration");
        }
        if (scannerServicePort != null) {
            this.scannerServicePort = scannerServicePort;
        } else {
            throw new ScanManagerException("Unable to find scaner service port configuration");
        }
        if (smtpServerHost != null || smtpServerPort != null || smtpUserName != null) {
            this.smtpServerHost = smtpServerHost;
            this.smtpServerPort = smtpServerPort;
            this.smtpUserName = smtpUserName;
        } else {
            throw new ScanManagerException("Unable to find email notification related configuration");
        }
        if (smtpEmailFromAddress != null) {
            this.emailFromaddress = smtpEmailFromAddress;
        } else {
            throw new ScanManagerException("Unable to find valid email address of sender");
        }

        // CC email addresses are not mandatory.
        if (smtpEmailCCAddress != null) {
            this.emailCCaddress = Arrays.asList(smtpEmailCCAddress.trim().split(","));
        }

        // Not mandatory as there are default values.
        this.scanPageSize = (Integer) configObjectMap.get(SCAN_PAGE_SIZE);
        this.logPageSize = (Integer) configObjectMap.get(LOG_PAGE_SIZE);
        this.isNotificationEnabled = isNotificationEnabled;
        this.notificationSubject = notificiationSubject;
    }

    public String getScanManagerHost() {
        return scanManagerHost;
    }

    public Integer getScanManagerPort() {
        return scanManagerPort;
    }

    public Integer getScannerServicePort() {
        return scannerServicePort;
    }

    public String getScannerServiceHost() {
        return scannerServiceHost;
    }

    public Integer getScanPageSize() {
        if (scanPageSize == null) {
            return DEFAULT_SCAN_PAGE_SIZE;
        }
        return scanPageSize;
    }

    public Integer getLogPageSize() {
        if (logPageSize == null) {
            return DEFAULT_LOG_PAGE_SIZE;
        }
        return logPageSize;
    }

    public Boolean getNotificationEnabled() {
        return isNotificationEnabled;
    }

    public String getNotificationSubject() {
        return notificationSubject;
    }

    public String getSmtpServerHost() {
        return smtpServerHost;
    }

    public Integer getSmtpServerPort() {
        return smtpServerPort;
    }

    public String getSmtpUserName() {
        return smtpUserName;
    }

    public String getEmailFromaddress() {
        return emailFromaddress;
    }

    public List<String> getEmailCCaddress() {
        return emailCCaddress;
    }
}
