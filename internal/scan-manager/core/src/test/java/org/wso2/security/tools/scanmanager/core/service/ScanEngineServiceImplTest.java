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

import org.junit.Assert;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockObjectFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScannerApp;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.core.config.ScanManagerConfiguration;
import org.wso2.security.tools.scanmanager.core.config.ScanMangerConfigurationBuilder;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.core.handler.ContainerHandler;
import org.wso2.security.tools.scanmanager.core.model.Container;
import org.wso2.security.tools.scanmanager.core.model.HTTPRequest;
import org.wso2.security.tools.scanmanager.core.util.HTTPUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_ENV_NAME_SCAN_MANAGER_HOST;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_ENV_NAME_SCAN_MANAGER_PORT;
import static org.wso2.security.tools.scanmanager.core.util.ScanManagerTestConstants.SCANNER_APP_ID;
import static org.wso2.security.tools.scanmanager.core.util.ScanManagerTestConstants.SCANNER_APP_NAME;

/**
 * Test class for scan engine service methods.
 */
@PrepareForTest(HTTPUtil.class)
@PowerMockIgnore({"javax.xml.*", "org.xml.sax.*", "org.w3c.dom.*", "org.springframework.context.*",
        "org.apache.log4j.*"})
public class ScanEngineServiceImplTest {

    @Mock
    private ScanService scanService;

    @Mock
    private ScannerService scannerService;

    @Mock
    private ContainerHandler containerHandler;

    @Mock
    private LogService logService;

    @InjectMocks
    private ScanEngineServiceImpl scanEngineService;

    private static final String OCCUPIED_SCANNER_APP_ID = "testScannerApp1";
    private static final String JOB_ID_PARAMETER_NAME = "jobId";
    private static final String SCANNER_APP_ID_PARAMETER_NAME = "appId";
    private static final String PROPERTY_MAP_PARAMETER_NAME = "propertyMap";
    private static final String FILE_MAP_PARAMETER_NAME = "fileMap";

    @BeforeClass
    public void setUp() throws ScanManagerException {
        MockitoAnnotations.initMocks(this);
        ScanManagerConfiguration.getInstance().initScanConfiguration(ScanMangerConfigurationBuilder.getConfiguration());
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new PowerMockObjectFactory();
    }

    @Test(dataProvider = "getScanEngineData", dataProviderClass = ServiceTestDataProvider.class)
    public void testBeginPendingScans(Object scanDataObject, Object containerDataObject) {
        try {
            Scan scan = ServiceTestDataProvider.parseScanObject(scanDataObject);
            Container container = ServiceTestDataProvider.parseContainerObject(containerDataObject);
            if (scan != null && container != null) {
                scan.setStatus(ScanStatus.SUBMIT_PENDING);
                Mockito.when(scanService.getPendingScans(ScanStatus.SUBMIT_PENDING))
                        .thenReturn(Collections.singletonList(scan));
                Mockito.when(scanService.getByJobId(scan.getJobId())).thenReturn(scan);

                Scan existingTestScan = new Scan();
                existingTestScan.setScannerAppId(OCCUPIED_SCANNER_APP_ID);
                Mockito.when(scanService.getByStatusesAndScannerAndProduct(new ArrayList<>(
                                Arrays.asList(ScanStatus.SUBMITTED, ScanStatus.RUNNING, ScanStatus.CANCEL_PENDING)),
                        scan.getScanner(), scan.getProduct())).thenReturn(Collections.singletonList(existingTestScan));

                List<ScannerApp> testScannerAppList = Arrays.asList(new ScannerApp(scan.getScanner(),
                                OCCUPIED_SCANNER_APP_ID, OCCUPIED_SCANNER_APP_ID, scan.getProduct()),
                        new ScannerApp(scan.getScanner(), SCANNER_APP_ID, SCANNER_APP_NAME, scan.getProduct()));
                Mockito.when(scannerService.getAppsByScannerAndAssignedProduct(scan.getScanner(),
                        scan.getProduct())).thenReturn(testScannerAppList);
                Mockito.when(scanService.update(Mockito.any(Scan.class))).thenReturn(scan);
                Mockito.when(containerHandler.create(scan.getScanner().getImage(), ScanManagerConfiguration
                                .getInstance().getScannerServiceHost(), ScanManagerConfiguration.getInstance()
                                .getScannerServicePort(), container.getLabels(), new ArrayList<>(),
                        new String[]{CONTAINER_ENV_NAME_SCAN_MANAGER_HOST + "=" + ScanManagerConfiguration
                                .getInstance().getScanManagerHost(),
                                CONTAINER_ENV_NAME_SCAN_MANAGER_PORT + "=" + ScanManagerConfiguration
                                        .getInstance().getScanManagerPort()})).thenReturn(container);
                Mockito.doNothing().when(containerHandler).start(container.getId());

                PowerMockito.mockStatic(HTTPUtil.class);
                PowerMockito.when(HTTPUtil.sendPOST(Matchers.any(HTTPRequest.class)))
                        .thenReturn(new ResponseEntity(HttpStatus.OK));

                scanEngineService.beginPendingScans();

                ArgumentCaptor<HTTPRequest> startScanRequestCaptor = ArgumentCaptor.forClass(HTTPRequest.class);
                PowerMockito.verifyStatic();
                HTTPUtil.sendPOST(startScanRequestCaptor.capture());
                HTTPRequest startScanRequest = startScanRequestCaptor.getValue();
                Assert.assertNotNull(startScanRequest);
                Assert.assertEquals(scan.getJobId(), startScanRequest.getRequestParams().get(JOB_ID_PARAMETER_NAME));
                Assert.assertEquals(scan.getScannerAppId(),
                        startScanRequest.getRequestParams().get(SCANNER_APP_ID_PARAMETER_NAME));
                Map<String, List<String>> fileMap =
                        (Map<String, List<String>>) startScanRequest.getRequestParams().get(FILE_MAP_PARAMETER_NAME);
                Map<String, List<String>> propertyMap =
                        (Map<String, List<String>>) startScanRequest.getRequestParams()
                                .get(PROPERTY_MAP_PARAMETER_NAME);

                scan.getFileList().forEach(scanFile -> Assert.assertTrue(fileMap
                        .get(scanFile.getName()).contains(scanFile.getLocation())));
                scan.getPropertyList().forEach(scanProperty -> Assert.assertTrue(propertyMap
                        .get(scanProperty.getName()).contains(scanProperty.getValue())));
            } else {
                throw new ScanManagerException("Unable to get the scan test data");
            }
        } catch (ScanManagerException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(dataProvider = "getScanEngineData", dataProviderClass = ServiceTestDataProvider.class)
    public void testCancelScan(Object scanDataObject, Object containerDataObject) {
        try {
            Scan scan = ServiceTestDataProvider.parseScanObject(scanDataObject);
            Container container = ServiceTestDataProvider.parseContainerObject(containerDataObject);
            if (scan != null && container != null) {
                Mockito.when(scanService.getByJobId(scan.getJobId())).thenReturn(scan);
                PowerMockito.mockStatic(HTTPUtil.class);
                PowerMockito.when(HTTPUtil.sendDelete(Matchers.any(HTTPRequest.class)))
                        .thenReturn(new ResponseEntity(HttpStatus.OK));
                PowerMockito.when(containerHandler.list()).thenReturn(Collections.singletonList(container));
                Mockito.doNothing().when(scanService).updateStatus(scan.getJobId(), ScanStatus.CANCEL_PENDING);

                scanEngineService.cancelScan(scan);

                PowerMockito.verifyStatic();
                ArgumentCaptor<HTTPRequest> requestParamsCaptor = ArgumentCaptor.forClass(HTTPRequest.class);
                HTTPUtil.sendDelete(requestParamsCaptor.capture());
                HTTPRequest requestParam = requestParamsCaptor.getValue();
                Assert.assertNotNull(requestParam);
                Assert.assertEquals(scan.getJobId(), requestParam.getRequestParams().get(JOB_ID_PARAMETER_NAME));
                Assert.assertEquals(scan.getScannerAppId(),
                        requestParam.getRequestParams().get(SCANNER_APP_ID_PARAMETER_NAME));
            } else {
                throw new ScanManagerException("Unable to get the scan test data");
            }
        } catch (ScanManagerException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(dataProvider = "getScanEngineData", dataProviderClass = ServiceTestDataProvider.class)
    public void testRemoveContainer(Object scanDataObject, Object containerDataObject) {
        try {
            Scan scan = ServiceTestDataProvider.parseScanObject(scanDataObject);
            Container container = ServiceTestDataProvider.parseContainerObject(containerDataObject);
            if (scan != null && container != null) {
                Mockito.when(containerHandler.list()).thenReturn(Collections.singletonList(container));
                Mockito.doNothing().when(containerHandler).clean(container.getId());

                scanEngineService.removeContainer(scan);
            } else {
                throw new ScanManagerException("Unable to get the scan test data");
            }
        } catch (ScanManagerException e) {
            Assert.fail(e.getMessage());
        }
    }
}
