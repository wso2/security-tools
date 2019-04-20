/* * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScanFile;
import org.wso2.security.tools.scanmanager.common.external.model.ScanProperty;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.external.model.ScannerApp;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanPriority;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.core.config.ScanManagerConfiguration;
import org.wso2.security.tools.scanmanager.core.dao.ScanDAO;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.core.handler.DockerHandler;
import org.wso2.security.tools.scanmanager.core.model.ContainerInfo;
import org.wso2.security.tools.scanmanager.core.util.HTTPUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_APP_LABEL_NAME;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_ENV_NAME_SCAN_MANAGER_HOST;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_ENV_NAME_SCAN_MANAGER_PORT;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_SCANNER_LABEL_NAME;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_SCAN_JOB_ID_LABEL_NAME;
import static org.wso2.security.tools.scanmanager.core.util.Constants.SCHEME;

/**
 * The class {@code ScanServiceImpl} is the service class that manage the method implementations of the
 * scans.
 */
@Service
public class ScanServiceImpl implements ScanService {

    private ScanDAO scanDAO;
    private LogService logService;
    private ScannerService scannerService;
    private DockerHandler dockerHandler;

    private static final String PROPERTY_MAP_PARAMETER_NAME = "propertyMap";
    private static final String FILE_MAP_PARAMETER_NAME = "fileMap";
    private static final String JOB_ID_PARAMETER_NAME = "jobId";
    private static final String SCANNER_APP_ID_PARAMETER_NAME = "appId";
    private static final Integer WAIT_TIME_TILL_CONTAINER_REMOVE = 5000;

    private static final String SCANNER_SCAN_ENDPOINT = "/scanner/scan";

    @Autowired
    public ScanServiceImpl(ScanDAO scanDAO, LogService logService, ScannerService scannerService,
                           DockerHandler dockerHandler) {
        this.scanDAO = scanDAO;
        this.logService = logService;
        this.scannerService = scannerService;
        this.dockerHandler = dockerHandler;
    }

    @Override
    @Transactional
    public Scan update(Scan scan) {
        return scanDAO.save(scan);
    }

    @Override
    public Scan cancelScan(Scan scan) throws ScanManagerException {
        List<ContainerInfo> containerInfos = dockerHandler.getContainersList();

        for (ContainerInfo containerInfo : containerInfos) {
            Map<String, String> labels = containerInfo.getLabels();
            if (labels != null && labels.containsKey(CONTAINER_SCAN_JOB_ID_LABEL_NAME) &&
                    labels.get(CONTAINER_SCAN_JOB_ID_LABEL_NAME).equals(scan.getJobId())) {
                URI uri = buildScannerScanURI(containerInfo);
                Map<String, Object> requestParams = new HashMap<>();
                requestParams.put(JOB_ID_PARAMETER_NAME, scan.getJobId());
                requestParams.put(SCANNER_APP_ID_PARAMETER_NAME, labels.get(CONTAINER_APP_LABEL_NAME));
                MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();
                ResponseEntity response = HTTPUtil.sendDelete(uri.toString(), requestHeaders, requestParams);
                if (response.getStatusCode().isError()) {
                    throw new ScanManagerException("Unable to submit the cancel scan request");
                }
            }
        }
        return scan;
    }

    @Override
    @Transactional
    public Page<Scan> findAll(Integer pageNumber, Integer pageSize) {
        Pageable pageable = new PageRequest(pageNumber, pageSize);
        return scanDAO.findAllByOrderBySubmittedTimestampDesc(pageable);
    }

    @Override
    @Transactional
    public Scan getScanByJobId(String jobId) {
        return scanDAO.getByJobId(jobId);
    }

    @Override
    @Transactional
    public Integer updateScanStatus(String jobId, ScanStatus status) {
        return scanDAO.updateScanStatus(status, jobId);
    }

    @Override
    @Transactional
    public Integer updateScanPriority(String jobId, ScanPriority priority) {
        return scanDAO.updateScanPriority(priority, jobId);
    }

    @Override
    @Transactional
    public List<Scan> getScansByStatus(ScanStatus status) {
        return scanDAO.getByStatus(status);
    }

    @Override
    @Transactional
    public List<Scan> getScansByStatusAndScannerAndProduct(ScanStatus status, Scanner scanner, String product) {
        return scanDAO.getByStatusAndScannerAndProduct(status, scanner, product);
    }

    @Override
    @Transactional
    public synchronized void beginPendingScans() {
        List<Scan> pendingAllScansList = scanDAO.getByStatus(ScanStatus.SCAN_PENDING);

        List<Scan> pendingHighPriorityScans = new ArrayList<>();
        List<Scan> pendingMediumPriorityScans = new ArrayList<>();
        List<Scan> pendingLowPriorityScans = new ArrayList<>();
        categorizePendingScans(pendingAllScansList, pendingHighPriorityScans, pendingMediumPriorityScans,
                pendingLowPriorityScans);

        for (Scan scan : pendingHighPriorityScans) {
            beginScan(scan);
        }
        for (Scan scan : pendingMediumPriorityScans) {
            beginScan(scan);
        }
        for (Scan scan : pendingLowPriorityScans) {
            beginScan(scan);
        }
    }

    private void categorizePendingScans(List<Scan> pendingScanList, List<Scan> pendingHighPriorityScans,
                                        List<Scan> pendingMediumPriorityScans, List<Scan> pendingLowPriorityScans) {
        for (Scan scan : pendingScanList) {
            if (ScanPriority.HIGH.equals(scan.getPriority())) {
                pendingHighPriorityScans.add(scan);
            } else if (ScanPriority.MEDIUM.equals(scan.getPriority())) {
                pendingMediumPriorityScans.add(scan);
            } else if (ScanPriority.LOW.equals(scan.getPriority())) {
                pendingLowPriorityScans.add(scan);
            }
        }
        pendingHighPriorityScans.sort(Comparator.comparing(Scan::getSubmittedTimestamp));
        pendingMediumPriorityScans.sort(Comparator.comparing(Scan::getSubmittedTimestamp));
        pendingLowPriorityScans.sort(Comparator.comparing(Scan::getSubmittedTimestamp));
    }

    private void beginScan(Scan scan) {
        try {
            Map<String, List<String>> occupiedApps = getOccupiedApps();
            List<ScannerApp> scannerApps = scannerService.getAppsByScannerAndAssignedProduct(scan.getScanner(),
                    scan.getProduct());
            if (occupiedApps.containsKey(scan.getScanner().getId())) {
                List<String> occupiedScannerAppList = occupiedApps.get(scan.getScanner().getId());

                boolean freeAppFound = false;
                for (ScannerApp scannerApp : scannerApps) {
                    if (!occupiedScannerAppList.contains(scannerApp.getAppId())) {
                        freeAppFound = true;
                        initiateScanRequest(scan, scannerApp);
                        updateScanStatus(scan.getJobId(), ScanStatus.SUBMITTED);
                        logService.persist(scan, LogType.INFO, new Timestamp(System.currentTimeMillis()), "Scan " +
                                "submitted");
                        break;
                    }
                }
                if (!freeAppFound) {
                    logService.persist(scan, LogType.WARN, new Timestamp(System.currentTimeMillis()),
                            "Unable to find a free application for scan: " + scan.getJobId());
                }
            } else {
                if (!scannerApps.isEmpty()) {
                    ScannerApp scannerApp = scannerApps.get(0); //get the first available app
                    initiateScanRequest(scan, scannerApp);
                    updateScanStatus(scan.getJobId(), ScanStatus.SUBMITTED);
                    logService.persist(scan, LogType.INFO, new Timestamp(System.currentTimeMillis()), "Scan " +
                            "submitted");
                }
            }
        } catch (ScanManagerException e) {
            logService.persist(scan, LogType.ERROR, new Timestamp(System.currentTimeMillis()), e);
            removeContainer(scan);
        }
    }

    private URI buildScannerScanURI(ContainerInfo containerInfo) throws ScanManagerException {
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

    @Override
    public Map<String, List<String>> getOccupiedApps() {
        Map<String, List<String>> occupiedApps = new ConcurrentHashMap<>();
        List<Scan> initiatedScans = new ArrayList<>();

        initiatedScans.addAll(getScansByStatus(ScanStatus.SUBMITTED));
        initiatedScans.addAll(getScansByStatus(ScanStatus.RUNNING));
        initiatedScans.addAll(getScansByStatus(ScanStatus.CANCEL_PENDING));
        for (Scan scan : initiatedScans) {
            List<ScannerApp> scannerApps = scannerService.getAppsByScannerAndAssignedProduct(scan.getScanner(),
                    scan.getProduct());
            for (ScannerApp scannerApp : scannerApps) {
                if (occupiedApps.containsKey(scan.getScanner().getId())) {
                    occupiedApps.get(scan.getScanner().getId()).add(scannerApp.getAppId());
                } else {
                    List<String> scannerAppIds = new ArrayList<>();
                    scannerAppIds.add(scannerApp.getAppId());
                    occupiedApps.put(scan.getScanner().getId(), scannerAppIds);
                }
            }
        }
        return occupiedApps;
    }

    @Override
    public ContainerInfo removeContainer(Scan scan) {
        ContainerInfo removedContainerInfo = null;

        try {
            logService.persist(scan, LogType.INFO, new Timestamp(System.currentTimeMillis()),
                    "Removing the scanner container for the scan: " + scan.getJobId());

            //wait till the container receives the response from the scan manager.
            Thread.sleep(WAIT_TIME_TILL_CONTAINER_REMOVE);
            for (ContainerInfo containerInfo : dockerHandler.getContainersList()) {
                Map<String, String> labels = containerInfo.getLabels();
                if (labels.get(CONTAINER_SCAN_JOB_ID_LABEL_NAME).equals(scan.getJobId())) {
                    dockerHandler.killContainer(containerInfo.getContainerId());
                    dockerHandler.removeContainer(containerInfo.getContainerId());
                    removedContainerInfo = containerInfo;
                    logService.persist(scan, LogType.INFO, new Timestamp(System.currentTimeMillis()),
                            "Scanner container successfully removed. Container id: " +
                                    containerInfo.getContainerId());
                    break;
                }
            }
        } catch (InterruptedException | ScanManagerException e) {
            logService.persist(scan, LogType.ERROR, new Timestamp(System.currentTimeMillis()), e);
        }
        return removedContainerInfo;
    }

    private void initiateScanRequest(Scan scan, ScannerApp scannerApp) throws ScanManagerException {
        ContainerInfo containerInfo = createContainer(scan, scannerApp, ScanManagerConfiguration
                .getInstance().getScannerServiceHost(), ScanManagerConfiguration.getInstance()
                .getScannerServicePort());
        dockerHandler.startContainer(containerInfo.getContainerId());
        logService.persist(scan, LogType.INFO, new Timestamp(System.currentTimeMillis()),
                "Scanner container started. Container id: " + containerInfo.getContainerId());

        boolean isSuccess = sendStartScanRequest(containerInfo, scannerApp, scan);
        if (!isSuccess) {
            dockerHandler.killContainer(containerInfo.getContainerId());
            dockerHandler.removeContainer(containerInfo.getContainerId());
            logService.persist(scan, LogType.ERROR, new Timestamp(System.currentTimeMillis()), "Scan " +
                    "initiation failed. Removing the scanner container. Container id: " +
                    containerInfo.getContainerId());
        }
    }

    private boolean sendStartScanRequest(ContainerInfo containerInfo, ScannerApp scannerApp, Scan scan)
            throws ScanManagerException {
        try {
            URI uri = buildScannerScanURI(containerInfo);
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(JOB_ID_PARAMETER_NAME, scan.getJobId());
            requestParams.put(SCANNER_APP_ID_PARAMETER_NAME, scannerApp.getAppId());
            Map<String, List<String>> fileMap = new HashMap<>();
            for (ScanFile scanFile : scan.getScanFileList()) {
                List<String> fileLocations = new ArrayList<>();
                fileLocations.add(scanFile.getScanFileLocation());
                fileMap.put(scanFile.getScanFileName(), fileLocations);
            }
            Map<String, List<String>> propertyMap = new HashMap<>();
            for (ScanProperty scanProperty : scan.getScanPropertyList()) {
                List<String> params = new ArrayList<>();
                params.add(scanProperty.getPropertyValue());
                propertyMap.put(scanProperty.getPropertyName(), params);
            }
            requestParams.put(FILE_MAP_PARAMETER_NAME, fileMap);
            requestParams.put(PROPERTY_MAP_PARAMETER_NAME, propertyMap);
            MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();
            ResponseEntity response = HTTPUtil.sendPOST(uri.toString(), requestHeaders,
                    requestParams);
            return !response.getStatusCode().isError();
        } catch (RestClientException e) {
            throw new ScanManagerException("Error occurred while connecting to the scanner service endpoint");
        }
    }

    private ContainerInfo createContainer(Scan scan, ScannerApp scannerApp, String containerHost,
                                          Integer containerPort) throws ScanManagerException {
        Map<String, String> labels = new HashMap<>();
        labels.put(CONTAINER_SCAN_JOB_ID_LABEL_NAME, scan.getJobId());
        labels.put(CONTAINER_APP_LABEL_NAME, scannerApp.getAppId());
        labels.put(CONTAINER_SCANNER_LABEL_NAME, scannerApp.getScanner().getName());
        return dockerHandler.createContainer(scannerApp.getScanner().getScannerImage(),
                containerHost, containerPort, labels, new ArrayList<>(),
                new String[]{CONTAINER_ENV_NAME_SCAN_MANAGER_HOST + "=" + ScanManagerConfiguration.getInstance()
                        .getScanManagerHost(), CONTAINER_ENV_NAME_SCAN_MANAGER_PORT + "=" + ScanManagerConfiguration
                        .getInstance().getScanManagerPort()});
    }
}
