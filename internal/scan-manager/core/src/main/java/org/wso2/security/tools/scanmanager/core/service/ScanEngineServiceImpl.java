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
package org.wso2.security.tools.scanmanager.core.service;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScanFile;
import org.wso2.security.tools.scanmanager.common.external.model.ScanProperty;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.external.model.ScannerApp;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.core.config.ScanManagerConfiguration;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.core.handler.ContainerHandler;
import org.wso2.security.tools.scanmanager.core.model.Container;
import org.wso2.security.tools.scanmanager.core.util.Constants;
import org.wso2.security.tools.scanmanager.core.util.HTTPUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_APP_LABEL_NAME;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_ENV_NAME_SCAN_MANAGER_HOST;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_ENV_NAME_SCAN_MANAGER_PORT;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_SCANNER_LABEL_NAME;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_SCAN_JOB_ID_LABEL_NAME;
import static org.wso2.security.tools.scanmanager.core.util.Constants.SCHEME;

/**
 * Scan engine service class that holds the scan engine service method implementations.
 */
@Service
public class ScanEngineServiceImpl implements ScanEngineService {

    private ScanService scanService;
    private ScannerService scannerService;
    private LogService logService;
    private ContainerHandler dockerHandler;

    private static final String PROPERTY_MAP_PARAMETER_NAME = "propertyMap";
    private static final String FILE_MAP_PARAMETER_NAME = "fileMap";
    private static final String JOB_ID_PARAMETER_NAME = "jobId";
    private static final String SCANNER_APP_ID_PARAMETER_NAME = "appId";
    private static final String SCANNER_SCAN_ENDPOINT = "/scanner/scan";

    @Autowired
    public ScanEngineServiceImpl(ScanService scanService, ScannerService scannerService, LogService logService,
                                 ContainerHandler dockerHandler) {
        this.scanService = scanService;
        this.scannerService = scannerService;
        this.logService = logService;
        this.dockerHandler = dockerHandler;
    }

    @Override
    public void beginPendingScans() {
        scanService.getPendingScans(ScanStatus.SUBMIT_PENDING).forEach(scan -> beginScan(scan));
    }

    private void beginScan(Scan scan) {
        synchronized (Constants.LOCK) {

            // Check if the scan status have been changed before entering the synchronized block.
            Scan newScanObject = scanService.getByJobId(scan.getJobId());
            if (newScanObject.getStatus() == ScanStatus.SUBMIT_PENDING) {
                try {

                    // There can be multiple scanner apps for a given product in a particular scanner. We need to
                    // identify the currently occupied apps and check for any available free app to start the scan.
                    List<String> occupiedApps = getOccupiedApps(newScanObject.getScanner(),
                            newScanObject.getProduct());
                    List<ScannerApp> scannerApps =
                            scannerService.getAppsByScannerAndAssignedProduct(newScanObject.getScanner(),
                                    newScanObject.getProduct());

                    logService.insert(newScanObject, LogType.INFO,
                            "Checking for a free scanner application for the scan: " + scan.getJobId());
                    boolean freeAppFound = false;
                    for (ScannerApp scannerApp : scannerApps) {
                        if (!occupiedApps.contains(scannerApp.getAppId())) {
                            freeAppFound = true;

                            logService.insert(newScanObject, LogType.INFO,
                                    "Free scanner app found. Initiating the scan with the scanner app id: " +
                                            scannerApp.getAppId());

                            //initiating the scan request to create a scanner container and send the start scan request
                            // to the container micro service
                            initiateScanRequest(newScanObject, scannerApp);

                            newScanObject.setStatus(ScanStatus.SUBMITTED);
                            newScanObject.setScannerAppId(scannerApp.getAppId());
                            scanService.update(newScanObject);
                            logService.insert(newScanObject, LogType.INFO, "Scan submitted");
                            break;
                        }
                    }
                    if (!freeAppFound) {
                        logService.insert(newScanObject, LogType.WARN,
                                "Unable to find a free application for scan: " + newScanObject.getJobId());
                    }
                } catch (ScanManagerException e) {
                    logService.insertError(newScanObject, e);
                }
            }
        }
    }

    private List<String> getOccupiedApps(Scanner scanner, String product) {
       return scanService.getByStatusesAndScannerAndProduct(new ArrayList<>(Arrays.asList(ScanStatus.SUBMITTED,
                ScanStatus.RUNNING, ScanStatus.CANCEL_PENDING)), scanner, product).stream()
               .map(Scan::getScannerAppId)
               .collect(Collectors.toList());
    }

    @Override
    public void cancelScan(Scan scan) throws ScanManagerException {
        synchronized (Constants.LOCK) {

            // Get the scan details again from database as the scan details might have been changed before entering the
            // synchronized block.
            Scan newScanObject = scanService.getByJobId(scan.getJobId());

            // A scan can be cancelled only if the scan is any of the following status.
            if (newScanObject.getStatus() == ScanStatus.SUBMIT_PENDING ||
                    newScanObject.getStatus() == ScanStatus.SUBMITTED ||
                    newScanObject.getStatus() == ScanStatus.RUNNING) {
                List<Container> containerInfos = dockerHandler.list();
                boolean isContainerFound = false;
                for (Container containerInfo : containerInfos) {
                    Map<String, String> labels = containerInfo.getLabels();
                    if (labels != null && labels.containsKey(CONTAINER_SCAN_JOB_ID_LABEL_NAME) &&
                            labels.get(CONTAINER_SCAN_JOB_ID_LABEL_NAME).equals(scan.getJobId())) {
                        isContainerFound = true;

                        // A container is running for this particular scan. Hence we need to send a cancel scan
                        // request to the container.
                        URI uri = buildScannerScanURI(containerInfo);
                        Map<String, Object> requestParams = new HashMap<>();
                        requestParams.put(JOB_ID_PARAMETER_NAME, scan.getJobId());
                        requestParams.put(SCANNER_APP_ID_PARAMETER_NAME, labels.get(CONTAINER_APP_LABEL_NAME));
                        MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();
                        ResponseEntity response = HTTPUtil.sendDelete(uri.toString(), requestHeaders,
                                requestParams);
                        if (response.getStatusCode().isError()) {
                            throw new ScanManagerException("Unable to submit the cancel scan request");
                        }

                        // Cannot update the status to canceled from here as the scanner microservice needs to conduct
                        // the actual scan cancellation and update the status as cancelled. Hence, updating the status
                        // to cancel pending.
                        scanService.updateStatus(scan.getJobId(), ScanStatus.CANCEL_PENDING);
                        break;
                    }
                }
                if (!isContainerFound) {

                    // No container has been found for this scan. Hence, we can change the status to canceled.
                    scanService.updateStatus(scan.getJobId(), ScanStatus.CANCELED);
                }
            }
        }
    }

    private URI buildScannerScanURI(Container containerInfo) throws ScanManagerException {
        try {
            return (new URIBuilder())
                    .setHost(ScanManagerConfiguration.getInstance().getScannerServiceHost())
                    .setPort(containerInfo.getPortMappings().get(ScanManagerConfiguration.getInstance()
                            .getScannerServicePort()))
                    .setScheme(SCHEME).setPath(SCANNER_SCAN_ENDPOINT)
                    .build();
        } catch (URISyntaxException e) {
            throw new ScanManagerException("Error occurred while building the scan URI", e);
        }
    }

    public Container removeContainer(Scan scan) {
        Container removedContainerInfo = null;

        try {
            logService.insert(scan, LogType.INFO, "Removing the scanner container for the scan: " + scan.getJobId());
            for (Container containerInfo : dockerHandler.list()) {
                Map<String, String> labels = containerInfo.getLabels();
                if (labels.get(CONTAINER_SCAN_JOB_ID_LABEL_NAME).equals(scan.getJobId())) {
                    dockerHandler.clean(containerInfo.getId());
                    removedContainerInfo = containerInfo;
                    logService.insert(scan, LogType.INFO,
                            "Scanner container successfully removed. Container id: " + containerInfo.getId());
                    break;
                }
            }
        } catch (ScanManagerException e) {
            logService.insertError(scan, e);
        }
        return removedContainerInfo;
    }

    private void initiateScanRequest(Scan scan, ScannerApp scannerApp) throws ScanManagerException {
        Container containerInfo = null;
        try {
            logService.insert(scan, LogType.INFO, "Creating a container for the scan");
            containerInfo = createContainer(scan, scannerApp, ScanManagerConfiguration
                    .getInstance().getScannerServiceHost(), ScanManagerConfiguration.getInstance()
                    .getScannerServicePort());
            dockerHandler.start(containerInfo.getId());
            logService.insert(scan, LogType.INFO,
                    "Scanner container started. Container id: " + containerInfo.getId());

            //send the start scan request to the scanner container
            sendStartScanRequest(containerInfo, scannerApp, scan);
        } catch (ScanManagerException e) {
            logService.insertError(scan, e);
            if (containerInfo != null) {
                dockerHandler.clean(containerInfo.getId());
            }
        }
    }

    private void sendStartScanRequest(Container containerInfo, ScannerApp scannerApp, Scan scan)
            throws ScanManagerException {
        try {
            URI uri = buildScannerScanURI(containerInfo);
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(JOB_ID_PARAMETER_NAME, scan.getJobId());
            requestParams.put(SCANNER_APP_ID_PARAMETER_NAME, scannerApp.getAppId());

            Map<String, List<String>> fileMap = scan.getFileList().stream()
                    .collect(Collectors.toMap(ScanFile::getName,
                            scanFile -> Collections.singletonList(scanFile.getLocation())));
            Map<String, List<String>> propertyMap = scan.getPropertyList().stream()
                    .collect(Collectors.toMap(ScanProperty::getName,
                            scanProperty -> Collections.singletonList(scanProperty.getValue())));
            requestParams.put(FILE_MAP_PARAMETER_NAME, fileMap);
            requestParams.put(PROPERTY_MAP_PARAMETER_NAME, propertyMap);
            MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();
            ResponseEntity response = HTTPUtil.sendPOST(uri.toString(), requestHeaders,
                    requestParams);

            if (response.getStatusCode().isError()) {
                throw new ScanManagerException("Error occurred while sending start scan request to the scanner " +
                        "service");
            }
        } catch (RestClientException e) {
            throw new ScanManagerException("Error occurred while connecting to the scanner service endpoint");
        }
    }

    private Container createContainer(Scan scan, ScannerApp scannerApp, String containerHost,
                                      Integer containerPort) throws ScanManagerException {
        Map<String, String> labels = new HashMap<>();
        labels.put(CONTAINER_SCAN_JOB_ID_LABEL_NAME, scan.getJobId());
        labels.put(CONTAINER_APP_LABEL_NAME, scannerApp.getAppId());
        labels.put(CONTAINER_SCANNER_LABEL_NAME, scannerApp.getScanner().getName());

        String[] envVariables =
                new String[]{CONTAINER_ENV_NAME_SCAN_MANAGER_HOST + "=" + ScanManagerConfiguration.getInstance()
                .getScanManagerHost(), CONTAINER_ENV_NAME_SCAN_MANAGER_PORT + "=" + ScanManagerConfiguration
                .getInstance().getScanManagerPort()};
        return dockerHandler.create(scannerApp.getScanner().getImage(),
                containerHost, containerPort, labels, new ArrayList<>(), envVariables);
    }
}
