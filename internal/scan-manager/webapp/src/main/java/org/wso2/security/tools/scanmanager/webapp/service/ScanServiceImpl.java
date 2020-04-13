/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.webapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScanExternal;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerScansResponse;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.external.model.User;
import org.wso2.security.tools.scanmanager.common.model.HTTPRequest;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanPriority;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.common.model.ScanType;
import org.wso2.security.tools.scanmanager.common.util.HTTPUtil;
import org.wso2.security.tools.scanmanager.webapp.config.ScanManagerWebappConfiguration;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;
import org.wso2.security.tools.scanmanager.webapp.util.FTPUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.security.tools.scanmanager.webapp.util.Constants.FILES_BY_URL_POSTFIX;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.FILES_BY_URL_SEPARATOR;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.PAGE_PARAM_NAME;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.PRE_JOB_ID_PREFIX;

/**
 * Scan service implementation class.
 */
@Service
public class ScanServiceImpl implements ScanService {

    private LogService logService;

    private static final String SCAN_REQUEST_FILE_MAP_ATTRIBUTE_NAME = "fileMap";
    private static final String SCAN_REQUEST_PROPERTY_MAP_ATTRIBUTE_NAME = "propertyMap";
    private static final String SCAN_REQUEST_SCAN_NAME_ATTRIBUTE_NAME = "scanName";
    private static final String SCAN_REQUEST_SCAN_DESCRIPTION_ATTRIBUTE_NAME = "scanDescription";
    private static final String SCAN_REQUEST_SCAN_TYPE_ATTRIBUTE_NAME = "scanType";
    private static final String SCAN_REQUEST_PRODUCT_NAME_ATTRIBUTE_NAME = "productName";
    private static final String SCAN_REQUEST_SCANNER_ID_ATTRIBUTE_NAME = "scannerId";
    private static final String SCAN_REQUEST_USER = "user";
    private static final String SCAN_REQUEST_SCANNER_NAME_ATTRIBUTE_NAME = "scannerName";
    private static final String SCAN_ARTIFACT_DIRECTORY = "scans";

    private static final Integer ARTIFACT_DOWNLOAD_CONNECTION_TIMEOUT = 10000;
    private static final Integer ARTIFACT_DOWNLOAD_READ_TIMEOUT = 10000;

    // Temporary hold the scans till the pre-scan tasks are completed.
    private Map<String, Scan> preparingScans = new ConcurrentHashMap<>();

    @Autowired
    public ScanServiceImpl(LogService logService) {
        this.logService = logService;
    }

    @Override
    public Scan submitScan(Map<String, MultipartFile> fileMap, Map<String, String> parameterMap, User user) {
        Map<String, String> filesToBeDownloadedFromURL = new HashMap<>();
        Map<String, String> storedFileMap = new HashMap<>();
        File scanDirectoryLocation = null;

        // Add scan to the scans under preparation list till the pre scan tasks are completed.
        Scan preparingScan = addScanToPreparing(parameterMap);
        logService.insert(preparingScan, LogType.INFO, "Scan is preparing to be submitted to scan manager API. Job " +
                "Id: " + preparingScan.getJobId());
        try {
            // Create a directory with the scan job id.
            scanDirectoryLocation = new File(SCAN_ARTIFACT_DIRECTORY + File.separator + preparingScan.getJobId());
            if (!scanDirectoryLocation.exists() && !scanDirectoryLocation.mkdirs()) {
                throw new ScanManagerWebappException("Error occurred while creating the scan directory");
            }

            // Store the uploaded files in a temp scan directory.
            for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    String unifiedFileName = entry.getKey() + "." +
                            FilenameUtils.getExtension(entry.getValue().getOriginalFilename());
                    Path unifiedFilePath = Paths.get(scanDirectoryLocation.toPath().toString(), unifiedFileName);
                    writeToFile(entry.getValue(), unifiedFilePath);
                    storedFileMap.put(entry.getKey(), unifiedFilePath.toString());
                }
            }

            Iterator<Map.Entry<String, String>> iter = parameterMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> param = iter.next();
                if (param.getValue().isEmpty()) {
                    iter.remove();
                } else {
                    if (param.getKey().contains(FILES_BY_URL_SEPARATOR + FILES_BY_URL_POSTFIX)) {

                        // If the param map contains files as a URL (param names containing "@byURL"), add them to
                        // a separate map to download later.
                        String fileExtension = FilenameUtils.getExtension(param.getValue());
                        if (StringUtils.isBlank(fileExtension)) {
                            throw new ScanManagerWebappException("Unable to identify the file extension from the " +
                                    "file URL: " + param.getValue());
                        }
                        String fileName =
                                StringUtils.substringBeforeLast(param.getKey(),
                                        FILES_BY_URL_SEPARATOR) + "." + fileExtension;

                        if (!filesToBeDownloadedFromURL.containsKey(fileName)) {
                            filesToBeDownloadedFromURL.put(fileName, param.getValue());
                        }
                    }
                }
            }

            // Begin the pre scans tasks and initiate the scan submission.
            new Thread(() -> beginScanSubmit(storedFileMap, parameterMap, filesToBeDownloadedFromURL,
                    preparingScan, user), "BeginScanSubmitToScanManagerAPI").start();
        } catch (ScanManagerWebappException e) {

            // Update the status if the scan under preparation to ERROR.
            if (preparingScans.containsKey(preparingScan.getJobId())) {
                preparingScans.get(preparingScan.getJobId()).setStatus(ScanStatus.ERROR);
            }
            logService.insertError(preparingScan, e);

            // Delete local scan artifacts directory.
            try {
                FileUtils.deleteDirectory(scanDirectoryLocation);
            } catch (IOException ex) {
                logService.insertError(preparingScan, ex);
            }
        }
        return preparingScan;
    }

    @Override
    public ScanManagerScansResponse getScans(Integer pageNumber) throws ScanManagerWebappException {
        ScanManagerScansResponse scansResponse = null;
        List<NameValuePair> nameValuePairs = new ArrayList<>();

        try {
            if (pageNumber != null) {
                nameValuePairs.add(new BasicNameValuePair(PAGE_PARAM_NAME, pageNumber.toString()));
            }

            HTTPRequest getScansRequest = new HTTPRequest(ScanManagerWebappConfiguration.getInstance()
                    .getScanURL("", nameValuePairs).toString(), null, null);
            ResponseEntity<String> responseEntity = HTTPUtil.sendGET(getScansRequest);
            if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                scansResponse = mapper.readValue(responseEntity.getBody(), ScanManagerScansResponse.class);
            } else {
                throw new ScanManagerWebappException("Unable to get the scans from scan manager");
            }
        } catch (RestClientException | IOException e) {
            throw new ScanManagerWebappException("Unable to get the scans", e);
        }
        return scansResponse;
    }

    @Override
    public List<ScanExternal> getPreparingScans() {
        // Convert the scan object to scan external object.
        List<ScanExternal> convertedPreparingScans = new ArrayList<>();
        preparingScans.forEach(((scanId, scan) -> convertedPreparingScans.add(new ScanExternal(scan))));
        convertedPreparingScans.sort(Comparator.comparing(ScanExternal::getSubmittedTimestamp).reversed());
        return convertedPreparingScans;
    }

    @Override
    public ScanExternal getScan(String jobId) throws ScanManagerWebappException {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        ScanExternal scan = null;
        try {
            if (jobId.startsWith(PRE_JOB_ID_PREFIX)) {
                if (preparingScans.containsKey(jobId)) {
                    return new ScanExternal(preparingScans.get(jobId));
                }
            } else {
                HTTPRequest getScanRequest = new HTTPRequest(ScanManagerWebappConfiguration.getInstance()
                        .getScanURL("/" + jobId, nameValuePairs).toString(), null, null);
                ResponseEntity<String> responseEntity = HTTPUtil.sendGET(getScanRequest);
                if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
                    ObjectMapper mapper = new ObjectMapper();
                    scan = mapper.readValue(responseEntity.getBody(), ScanExternal.class);
                } else {
                    throw new ScanManagerWebappException("Unable to get the scans from scan manager");
                }
            }
        } catch (RestClientException | IOException e) {
            throw new ScanManagerWebappException("Unable to get the scan details for the job id: " + jobId, e);
        }
        return scan;
    }

    @Override
    public void getScanReport(String reportPath, String outputFilePath) throws ScanManagerWebappException {
        FTPUtil.downloadFromFTP(reportPath, outputFilePath);
    }

    @Override
    public ResponseEntity stopScan(String id) throws ScanManagerWebappException {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        try {
            HTTPRequest stopScanRequest = new HTTPRequest(ScanManagerWebappConfiguration.getInstance()
                    .getScanURL(id, nameValuePairs).toString(), null, null);
            return HTTPUtil.sendDELETE(stopScanRequest);
        } catch (RestClientException e) {
            throw new ScanManagerWebappException("Error occurred while stopping the scan with the job id: " + id);
        }
    }

    @Override
    public boolean clearScan(String id) {
        // Clear scan from preparing scans list if the scan status is ERROR.
        if (preparingScans.containsKey(id) && preparingScans.get(id).getStatus() == ScanStatus.ERROR) {
            preparingScans.remove(id);
            return logService.removeLogsForPreparingScan(id);
        } else {
            return false;
        }
    }

    private void writeToFile(MultipartFile file, Path filepath) throws ScanManagerWebappException {
        try (OutputStream destinationOutputStream = Files.newOutputStream(filepath)) {
            destinationOutputStream.write(file.getBytes());
        } catch (IOException e) {
            throw new ScanManagerWebappException("IO Exception occurred while writing files to the disk", e);
        }
    }

    private Scan addScanToPreparing(Map<String, String> parameterMap) {
        String preJobId = PRE_JOB_ID_PREFIX + UUID.randomUUID().toString();
        Scan preparingScan = new Scan(preJobId);
        preparingScan.setName(parameterMap.get(SCAN_REQUEST_SCAN_NAME_ATTRIBUTE_NAME));
        preparingScan.setDescription(parameterMap.get(SCAN_REQUEST_SCAN_DESCRIPTION_ATTRIBUTE_NAME));

        Scanner scanner = new Scanner(parameterMap.get(SCAN_REQUEST_SCANNER_ID_ATTRIBUTE_NAME));
        scanner.setName(parameterMap.get(SCAN_REQUEST_SCANNER_NAME_ATTRIBUTE_NAME));
        preparingScan.setScanner(scanner);
        preparingScan.setStatus(ScanStatus.PREPARING);
        preparingScan.setPriority(ScanPriority.MEDIUM.getValue());
        preparingScan.setSubmittedTimestamp(new Timestamp(System.currentTimeMillis()));
        preparingScan.setProduct(parameterMap.get(SCAN_REQUEST_PRODUCT_NAME_ATTRIBUTE_NAME));
        preparingScan.setType(ScanType.valueOf(parameterMap.get(SCAN_REQUEST_SCAN_TYPE_ATTRIBUTE_NAME)));

        preparingScans.put(preJobId, preparingScan);
        return preparingScan;
    }

    private void beginScanSubmit(Map<String, String> storedFileMap, Map<String, String> parameterMap, Map<String,
            String> filesToBeDownloadedFromURL, Scan scan, User user) {
        Map<String, String> uploadedFileMap = new HashMap<>();
        File scanDirectoryLocation = null;

        try {
            scanDirectoryLocation = new File(SCAN_ARTIFACT_DIRECTORY + File.separator + scan.getJobId());
            if (!scanDirectoryLocation.exists() && !scanDirectoryLocation.mkdirs()) {
                throw new ScanManagerWebappException("Error occurred while creating the scan directory");
            }

            // If there are files that have been given as a URL, they needs to be downloaded from the given URL
            // location and store temporally till there are being uploaded to the FTP server.
            if (!filesToBeDownloadedFromURL.isEmpty()) {
                for (Map.Entry<String, String> file : filesToBeDownloadedFromURL.entrySet()) {
                    logService.insert(scan, LogType.INFO,
                            "Initiating file download from the URL: " + file.getValue());
                    FileUtils.copyURLToFile(
                            new URL(file.getValue()),
                            new File(scanDirectoryLocation.getPath() + File.separator + file.getKey()),
                            ARTIFACT_DOWNLOAD_CONNECTION_TIMEOUT, ARTIFACT_DOWNLOAD_READ_TIMEOUT);
                    storedFileMap.put(FilenameUtils.getBaseName(file.getKey()), scanDirectoryLocation.getPath() +
                            File.separator + file.getKey());
                    logService.insert(scan, LogType.INFO,
                            "File download completed from the URL: " + file.getValue());
                }
            }

            // Upload the submitted files into the FTP server by creating a directory with the scan id and include the
            // file name and the location for each file in a separate map.
            if (!storedFileMap.isEmpty()) {
                logService.insert(scan, LogType.INFO, "Uploading scan files to FTP.");
                uploadedFileMap = FTPUtil.uploadFilesToFTP(scan.getJobId(), storedFileMap);
                logService.insert(scan, LogType.INFO, "Uploading scan files to FTP is completed.");
            }

            // Send scan submit request to scan manager API.
            ResponseEntity responseEntity = sendSubmitScanRequest(uploadedFileMap, parameterMap, user);
            if (responseEntity != null && (responseEntity.getStatusCode().is2xxSuccessful())) {
                if (preparingScans.containsKey(scan.getJobId())) {
                    preparingScans.remove(scan.getJobId());
                    logService.removeLogsForPreparingScan(scan.getJobId());
                }
            } else {
                if (preparingScans.containsKey(scan.getJobId())) {
                    preparingScans.get(scan.getJobId()).setStatus(ScanStatus.ERROR);
                }
                logService.insert(scan, LogType.ERROR, "Error occurred while submitting the scan request to scan " +
                        "manager API");
            }
        } catch (IOException | ScanManagerWebappException e) {
            if (preparingScans.containsKey(scan.getJobId())) {
                preparingScans.get(scan.getJobId()).setStatus(ScanStatus.ERROR);
            }
            logService.insertError(scan, e);
        } finally {

            // Delete local scan artifacts directory.
            if (scanDirectoryLocation != null) {
                try {
                    FileUtils.deleteDirectory(scanDirectoryLocation);
                } catch (IOException e) {
                    logService.insertError(scan, e);
                }
            }
        }
    }

    private ResponseEntity sendSubmitScanRequest(Map<String, String> uploadedFileMap, Map<String, String> parameterMap,
                                                 User user) throws ScanManagerWebappException {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        Map<String, Object> requestParams = new HashMap<>();

        try {
            requestParams.put(SCAN_REQUEST_FILE_MAP_ATTRIBUTE_NAME, uploadedFileMap);
            requestParams.put(SCAN_REQUEST_PROPERTY_MAP_ATTRIBUTE_NAME, parameterMap);
            requestParams.put(SCAN_REQUEST_SCAN_NAME_ATTRIBUTE_NAME,
                    parameterMap.get(SCAN_REQUEST_SCAN_NAME_ATTRIBUTE_NAME));
            requestParams.put(SCAN_REQUEST_SCAN_DESCRIPTION_ATTRIBUTE_NAME,
                    parameterMap.get(SCAN_REQUEST_SCAN_DESCRIPTION_ATTRIBUTE_NAME));
            requestParams.put(SCAN_REQUEST_SCAN_TYPE_ATTRIBUTE_NAME,
                    parameterMap.get(SCAN_REQUEST_SCAN_TYPE_ATTRIBUTE_NAME));
            requestParams.put(SCAN_REQUEST_PRODUCT_NAME_ATTRIBUTE_NAME,
                    parameterMap.get(SCAN_REQUEST_PRODUCT_NAME_ATTRIBUTE_NAME));
            requestParams.put(SCAN_REQUEST_SCANNER_ID_ATTRIBUTE_NAME,
                    parameterMap.get(SCAN_REQUEST_SCANNER_ID_ATTRIBUTE_NAME));
            requestParams.put(SCAN_REQUEST_USER, user);

            HTTPRequest submitScanRequest = new HTTPRequest(ScanManagerWebappConfiguration.getInstance().getScanURL("",
                    nameValuePairs).toString(), null, requestParams);
            return HTTPUtil.sendPOST(submitScanRequest);
        } catch (RestClientException e) {
            throw new ScanManagerWebappException("Error occurred while submitting the scan request to scan manager " +
                    "API.", e);
        }
    }
}
