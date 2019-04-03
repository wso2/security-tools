package org.wso2.security.tools.scanmanager.scanners.common.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.config.ConfigurationReader;
import org.wso2.security.tools.scanmanager.common.ScanLogRequest;
import org.wso2.security.tools.scanmanager.common.ScanStatus;
import org.wso2.security.tools.scanmanager.common.ScanStatusUpdateRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Util class to represent the scan manager callback operations.
 */
public class CallbackUtil {

    private static final Logger log = Logger.getLogger(CallbackUtil.class);

    private CallbackUtil(){
    }

    /**
     * Update the scan satus in the scan manager.
     *
     * @param jobId      job id of the scan manager for the current scan
     * @param scanStatus scan status enum
     */
    public static void updateScanStatus(String jobId,
                                        ScanStatus scanStatus, String reportPath, String scannerScanId) {
        int responseCode = 0;
        ScanStatusUpdateRequest scanStatusUpdateRequest = new ScanStatusUpdateRequest();
        scanStatusUpdateRequest.setJobId(jobId);
        String scanManagerCallbackURL = ConfigurationReader.getConfigProperty(ScannerConstants.
                SCAN_MANAGER_CALLBACK_URL) + ConfigurationReader.getConfigProperty(ScannerConstants
                .SCAN_MANAGER_CALLBACK_STATUS);
        if (scanStatus.equals(ScanStatus.COMPLETED)) {
            scanStatusUpdateRequest.setScanReportPath(reportPath);
        }

        try {
            responseCode = doHttpPost(scanManagerCallbackURL, scanStatusUpdateRequest.toString());
        } catch (IOException e) {
            log.error(e);
        }

        scanStatusUpdateRequest.setScanStatus(scanStatus.toString());

        if (HttpStatus.OK.value() == responseCode) {
            log.info("Callback status update is successfully completed. ");
        } else if (HttpStatus.NOT_FOUND.value() == responseCode) {
            try {
                log.info("Callback endpoint is not currently unavailable and will retry after " +
                        Long.valueOf(ConfigurationReader.getConfigProperty(
                                ScannerConstants.CALLBACK_RETRY_INTERVAL_SECONDS)) + "Seconds");

                TimeUnit.MINUTES.sleep(Long.parseLong(ConfigurationReader.getConfigProperty(
                        ScannerConstants.CALLBACK_RETRY_INTERVAL_SECONDS)));
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
    public static void persistScanLog(String jobId, String message, String type) {
        int responseCode = 0;
        Long retryTimeInterval = Long.valueOf(ConfigurationReader.getConfigProperty(
                ScannerConstants.CALLBACK_RETRY_INTERVAL_SECONDS));
        ScanLogRequest scanLogRequest = new ScanLogRequest();
        scanLogRequest.setJobId(jobId);

        String scanManagerCallbackURL = ConfigurationReader.getConfigProperty(ScannerConstants
                .SCAN_MANAGER_CALLBACK_URL) + ConfigurationReader.getConfigProperty(ScannerConstants
                .SCAN_MANAGER_CALLBACK_LOG);

        scanLogRequest.setMessage(message);
        scanLogRequest.setType(type);

        try {
            responseCode = doHttpPost(scanManagerCallbackURL, scanLogRequest.toString());
        } catch (IOException e) {
            log.error(e);
        }

        if (HttpStatus.OK.value() == responseCode) {
            log.info("Callback log persistence is successfully completed. ");
        } else if (HttpStatus.NOT_FOUND.value() == responseCode) {
            retryTimeInterval += Long.parseLong(ConfigurationReader.getConfigProperty(
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
     * @param urlString       url to do the http request
     * @param scanEntityParam String for the http request body
     * @return the response code of the http request response
     * @throws IOException
     */
    private static int doHttpPost(String urlString, String scanEntityParam) throws IOException {
        int responseCode;
        String line;
        StringEntity postRequestJson;
        StringBuilder result;

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(urlString);

        postRequestJson = new StringEntity(scanEntityParam);
        post.setEntity(postRequestJson);

        HttpResponse response = client.execute(post);
        responseCode = response.getStatusLine().getStatusCode();

        try (BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8.name()))) {
            result = new StringBuilder();

            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        }
        log.info("Response message : " + result);

        return responseCode;
    }
}
