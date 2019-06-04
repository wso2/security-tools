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
package org.wso2.security.tools.scanmanager.scanners.common.util;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.wso2.security.tools.scanmanager.common.model.HTTPRequest;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.common.util.HTTPUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Util class to represent the scan manager callback operations.
 */
public class CallbackUtil {

    private static final Logger log = Logger.getLogger(CallbackUtil.class);
    private static String scanManagerLogCallbackURL;
    private static String scanManagerStatusCallbackURL;
    private static Long retryTimeInterval = Long.valueOf(0);

    public static void setCallbackUrls(String scanManagerLogCallbackURL, String scanManagerStatusCallbackURL,
                                       Long retryTimeInterval) {
        CallbackUtil.scanManagerLogCallbackURL = scanManagerLogCallbackURL;
        CallbackUtil.scanManagerStatusCallbackURL = scanManagerStatusCallbackURL;
        CallbackUtil.retryTimeInterval = retryTimeInterval;
    }

    /**
     * Update the scan status in the scan manager.
     *
     * @param jobId         job id of the scan manager for the current scan
     * @param scanStatus    scan status enum
     * @param reportPath    scan report location
     * @param scannerScanId actual scan Id
     */
    public static void updateScanStatus(String jobId, ScanStatus scanStatus, String reportPath, String scannerScanId) {
        updateScanStatus(jobId, scanStatus, reportPath, scannerScanId, Long.valueOf(0));
    }

    /**
     * Update the scan status in the scan manager.
     *
     * @param jobId                         job id of the scan manager for the current scan
     * @param scanStatus                    scan status enum
     * @param reportPath                    scan report location
     * @param scannerScanId                 actual scan Id
     * @param statusUpdateRetryTimeInterval retrying time to callback for updating the status
     */
    public static void updateScanStatus(String jobId, ScanStatus scanStatus, String reportPath, String scannerScanId,
                                        Long statusUpdateRetryTimeInterval) {
        int responseCode = -1;
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("jobId", jobId);
        requestParams.put("scanStatus", scanStatus.name());

        if (scanStatus.equals(ScanStatus.COMPLETED)) {
            requestParams.put("scanReportPath", reportPath);
        }
        if (scannerScanId != null && !scannerScanId.isEmpty()) {
            requestParams.put("scannerScanId", scannerScanId);
        }

        try {
            HTTPRequest scanUpdateRequest = new HTTPRequest(scanManagerStatusCallbackURL, null, requestParams);
            responseCode = HTTPUtil.sendPOST(scanUpdateRequest).getStatusCode().value();
        } catch (RestClientException e) {
            log.error(e);
        }

        if (HttpStatus.OK.value() == responseCode) {
            log.info("Callback status update is successfully completed. ");
        } else if (HttpStatus.NOT_FOUND.value() == responseCode || HttpStatus.INTERNAL_SERVER_ERROR.value()
                == responseCode || responseCode == -1) {
            statusUpdateRetryTimeInterval += retryTimeInterval;
            try {
                log.info("Callback log endpoint is not currently available and will retry after " +
                        statusUpdateRetryTimeInterval + " Seconds");
                TimeUnit.MINUTES.sleep(statusUpdateRetryTimeInterval);
            } catch (InterruptedException e) {
                log.error(e);
            }
            // Retrying updating the scan status in scan manager.
            updateScanStatus(jobId, scanStatus, reportPath, scannerScanId, statusUpdateRetryTimeInterval);
        } else {
            log.warn("Callback status update failed with the response code : " + responseCode);
        }
    }

    /**
     * Persist the log in the Scan Manager.
     *
     * @param jobId   id of the scan manager for the current scan
     * @param message log message
     * @param type    log type
     */
    public static void persistScanLog(String jobId, String message, LogType type) {
        persistScanLog(jobId, message, type, Long.valueOf(0));
    }

    /**
     * Persist the log in the Scan Manager.
     *
     * @param jobId                      id of the scan manager for the current scan
     * @param message                    log message
     * @param type                       log type
     * @param logUpdateRetryTimeInterval retrying time to callback for updating the status
     */
    public static void persistScanLog(String jobId, String message, LogType type, Long logUpdateRetryTimeInterval) {
        int responseCode = -1;
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("jobId", jobId);
            requestParams.put("message", message);
            requestParams.put("type", type);
            requestParams.put("timestamp", System.currentTimeMillis());
            HTTPRequest persistLogRequest = new HTTPRequest(scanManagerLogCallbackURL, null, requestParams);
            ResponseEntity logResponse = HTTPUtil.sendPOST(persistLogRequest);
            responseCode = logResponse.getStatusCode().value();
        } catch (RestClientException e) {
            log.error(e);
        }

        if (HttpStatus.OK.value() == responseCode) {
            log.info("Callback log persistence is successfully completed. ");
        } else if (HttpStatus.NOT_FOUND.value() == responseCode || HttpStatus.INTERNAL_SERVER_ERROR.value()
                == responseCode || responseCode == -1) {
            logUpdateRetryTimeInterval += retryTimeInterval;
            try {
                log.info("Callback log endpoint is not currently available and will retry after " + retryTimeInterval
                        + " Seconds");

                TimeUnit.MINUTES.sleep(retryTimeInterval);
            } catch (InterruptedException e) {
                log.error(e);
            }
            // Retrying updating the scan status in scan manager.
            persistScanLog(jobId, message, type, logUpdateRetryTimeInterval);
        } else {
            log.warn("Callback log persistence failed with the response code : " + responseCode);
        }
    }
}
