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

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.wso2.security.tools.scanmanager.common.internal.model.ScanLogRequest;
import org.wso2.security.tools.scanmanager.common.internal.model.ScanStatusUpdateRequest;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.config.YAMLConfigurationReader;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Util class to represent the scan manager callback operations.
 */
public class CallbackUtil {

    private static final Logger log = Logger.getLogger(CallbackUtil.class);

    private CallbackUtil() {
    }

    /**
     * Update the scan satus in the scan manager.
     *
     * @param jobId      job id of the scan manager for the current scan
     * @param scanStatus scan status enum
     */
    public static void updateScanStatus(String jobId, ScanStatus scanStatus, String reportPath, String scannerScanId) {
        int responseCode = -1;
        ScanStatusUpdateRequest scanStatusUpdateRequest = new ScanStatusUpdateRequest();
        Gson gson = new Gson();
        StringEntity postingString;
        String scanManagerCallbackURL = YAMLConfigurationReader.getInstance().getConfigProperty(ScannerConstants
                .SCAN_MANAGER_CALLBACK_URL) + YAMLConfigurationReader.getInstance().getConfigProperty(ScannerConstants
                .SCAN_MANAGER_CALLBACK_LOG);
        Long retryTimeInterval = Long.valueOf(0);

        scanStatusUpdateRequest.setJobId(jobId);
        scanStatusUpdateRequest.setScanStatus(scanStatus);
        if (scanStatus.equals(ScanStatus.COMPLETED)) {
            scanStatusUpdateRequest.setScanReportPath(reportPath);
        }

        try {
            postingString = new StringEntity(gson.toJson(scanStatusUpdateRequest));
            responseCode = doHttpPost(scanManagerCallbackURL, postingString);
        } catch (IOException e) {
            log.error(e);
        }

        if (HttpStatus.OK.value() == responseCode) {
            log.info("Callback status update is successfully completed. ");
        } else if (HttpStatus.NOT_FOUND.value() == responseCode || HttpStatus.INTERNAL_SERVER_ERROR.value()
                == responseCode || responseCode == -1) {
            retryTimeInterval += Long.parseLong(YAMLConfigurationReader.getInstance().getConfigProperty(
                    ScannerConstants.CALLBACK_RETRY_INCREASE_SECONDS));
            try {
                log.info("Callback endpoint is not currently unavailable and will retry after " +
                        Long.valueOf(YAMLConfigurationReader.getInstance().getConfigProperty(ScannerConstants
                                .CALLBACK_RETRY_INTERVAL_SECONDS)) + "Seconds");

                TimeUnit.MINUTES.sleep(retryTimeInterval);
            } catch (InterruptedException e) {
                log.error(e);
            }
            //re-trying updating the scan status in scan manager
            updateScanStatus(jobId, scanStatus, reportPath, scannerScanId);
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
        int responseCode = -1;
        ScanLogRequest scanLogRequest = new ScanLogRequest();
        Gson gson = new Gson();
        StringEntity postingString;
        String scanManagerCallbackURL = YAMLConfigurationReader.getInstance().getConfigProperty(ScannerConstants
                .SCAN_MANAGER_CALLBACK_URL) + YAMLConfigurationReader.getInstance().getConfigProperty(ScannerConstants
                .SCAN_MANAGER_CALLBACK_LOG);
        Long retryTimeInterval = Long.valueOf(0);

        scanLogRequest.setJobId(jobId);
        scanLogRequest.setMessage(message);
        scanLogRequest.setType(type);

        try {
            postingString = new StringEntity(gson.toJson(scanLogRequest));
            responseCode = doHttpPost(scanManagerCallbackURL, postingString);
        } catch (IOException e) {
            log.error(e);
        }

        if (HttpStatus.OK.value() == responseCode) {
            log.info("Callback log persistence is successfully completed. ");
        } else if (HttpStatus.NOT_FOUND.value() == responseCode || HttpStatus.INTERNAL_SERVER_ERROR.value()
                == responseCode || responseCode == -1) {
            retryTimeInterval += Long.parseLong(YAMLConfigurationReader.getInstance().getConfigProperty(
                    ScannerConstants.CALLBACK_RETRY_INCREASE_SECONDS));
            try {
                log.info("Callback log endpoint is not currently available and will retry after " +
                        retryTimeInterval + " Seconds");

                TimeUnit.MINUTES.sleep(retryTimeInterval);
            } catch (InterruptedException e) {
                log.error(e);
            }
            //re-trying updating the scan status in scan manager
            persistScanLog(jobId, message, type);
        } else {
            log.warn("Callback log persistence failed with the response code : " + responseCode);
        }
    }

    /**
     * Does a http post request.
     *
     * @param urlString  url to do the http request
     * @param bodyEntity parameter set for the http request
     * @return the response code of the http request response
     * @throws IOException
     */
    private static int doHttpPost(String urlString, StringEntity bodyEntity) throws IOException {
        int responseCode;
        String line;

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(urlString);

        post.setEntity(bodyEntity);
        post.setHeader(ScannerConstants.CONTENT_TYPE, ScannerConstants.APPLICATION_JSON);

        HttpResponse response = client.execute(post);
        responseCode = response.getStatusLine().getStatusCode();

        return responseCode;
    }
}
