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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.core.exception.InvalidRequestException;
import org.wso2.security.tools.scanmanager.core.model.Container;

/**
 * Test class for callback service methods.
 */
public class CallbackServiceImplTest {

    @Mock
    private ScanEngineService scanEngineService;

    @Mock
    private ScanService scanService;

    @InjectMocks
    private CallbackServiceImpl callbackService;

    private static final String TEST_SCAN_REPORT_PATH = "/testScanReportPath";
    private static final String TEST_SCANNER_SCAN_ID = "testScannerScanId";

    @BeforeClass
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(dataProvider = "getScanData", dataProviderClass = ServiceTestDataProvider.class)
    public void testUpdateScanWithRunningStatus(Scan scan) throws InvalidRequestException {
        Mockito.when(scanService.getByJobId(scan.getJobId())).thenReturn(scan);
        Mockito.when(scanService.update(Mockito.any(Scan.class))).thenReturn(scan);

        callbackService.updateScan(scan, ScanStatus.RUNNING, TEST_SCANNER_SCAN_ID, null);

        ArgumentCaptor<Scan> scanUpdateCapture = ArgumentCaptor.forClass(Scan.class);
        Mockito.verify(scanService, Mockito.atLeastOnce()).update(scanUpdateCapture.capture());
        Scan updatedScan = scanUpdateCapture.getValue();
        Assert.assertNotNull(updatedScan);
        Assert.assertEquals(ScanStatus.RUNNING, updatedScan.getStatus());
        Assert.assertNotNull(updatedScan.getStartTimestamp());
        Assert.assertEquals(TEST_SCANNER_SCAN_ID, updatedScan.getScannerScanId());
    }

    @Test(dataProvider = "getScanEngineData", dataProviderClass = ServiceTestDataProvider.class)
    public void testUpdateScanWithCompletedStatus(Scan scan, Container container) throws InvalidRequestException {
        Mockito.when(scanService.getByJobId(scan.getJobId())).thenReturn(scan);
        Mockito.doNothing().when(scanEngineService).beginPendingScans();
        Mockito.when(scanEngineService.removeContainer(scan)).thenReturn(container);
        Mockito.when(scanService.update(Mockito.any(Scan.class))).thenReturn(scan);

        callbackService.updateScan(scan, ScanStatus.COMPLETED, null, TEST_SCAN_REPORT_PATH);

        ArgumentCaptor<Scan> scanUpdateCapture = ArgumentCaptor.forClass(Scan.class);
        Mockito.verify(scanService, Mockito.atLeastOnce()).update(scanUpdateCapture.capture());
        Scan updatedScan = scanUpdateCapture.getValue();
        Assert.assertNotNull(updatedScan);
        Assert.assertEquals(ScanStatus.COMPLETED, updatedScan.getStatus());
        Assert.assertEquals(TEST_SCAN_REPORT_PATH, updatedScan.getReportPath());
    }

    @Test(dataProvider = "getScanEngineData", dataProviderClass = ServiceTestDataProvider.class)
    public void testUpdateScanWithErrorStatus(Scan scan, Container container) throws InvalidRequestException {
        Mockito.when(scanService.getByJobId(scan.getJobId())).thenReturn(scan);
        Mockito.doNothing().when(scanEngineService).beginPendingScans();
        Mockito.when(scanEngineService.removeContainer(scan)).thenReturn(container);
        Mockito.when(scanService.update(Mockito.any(Scan.class))).thenReturn(scan);

        callbackService.updateScan(scan, ScanStatus.ERROR, null, null);

        ArgumentCaptor<Scan> scanUpdateCapture = ArgumentCaptor.forClass(Scan.class);
        Mockito.verify(scanService, Mockito.atLeastOnce()).update(scanUpdateCapture.capture());
        Scan updatedScan = scanUpdateCapture.getValue();
        Assert.assertNotNull(updatedScan);
        Assert.assertEquals(ScanStatus.ERROR, updatedScan.getStatus());
    }

    @Test(dataProvider = "getScanEngineData", dataProviderClass = ServiceTestDataProvider.class)
    public void testUpdateScanWithCanceledStatus(Scan scan, Container container) throws InvalidRequestException {
        Mockito.when(scanService.getByJobId(scan.getJobId())).thenReturn(scan);
        Mockito.doNothing().when(scanEngineService).beginPendingScans();
        Mockito.when(scanEngineService.removeContainer(scan)).thenReturn(container);
        Mockito.when(scanService.update(Mockito.any(Scan.class))).thenReturn(scan);

        callbackService.updateScan(scan, ScanStatus.CANCELED, null, null);

        ArgumentCaptor<Scan> scanUpdateCapture = ArgumentCaptor.forClass(Scan.class);
        Mockito.verify(scanService, Mockito.atLeastOnce()).update(scanUpdateCapture.capture());
        Scan updatedScan = scanUpdateCapture.getValue();
        Assert.assertNotNull(updatedScan);
        Assert.assertEquals(ScanStatus.CANCELED, updatedScan.getStatus());
    }
}
