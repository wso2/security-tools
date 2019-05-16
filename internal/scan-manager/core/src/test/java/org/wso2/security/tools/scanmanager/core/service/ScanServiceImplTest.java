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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.model.ScanPriority;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.core.dao.ScanDAO;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;

import java.util.Collections;
import java.util.List;

import static org.wso2.security.tools.scanmanager.core.util.ScanManagerTestConstants.SCANNER_APP_ID;

/**
 * Test class for scan service methods.
 */
public class ScanServiceImplTest {

    @Mock
    private ScanDAO mockScanDAO;

    @InjectMocks
    private ScanServiceImpl scanService;

    @BeforeClass
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(dataProvider = "getScanData", dataProviderClass = ServiceTestDataProvider.class)
    public void testUpdate(Scan scan) {
        Mockito.when(mockScanDAO.save(scan)).thenReturn(scan);

        Assert.assertNotNull(scanService.update(scan));
    }

    @Test(dataProvider = "getScanData", dataProviderClass = ServiceTestDataProvider.class)
    public void testInsert(Scan scan) {
        Mockito.when(mockScanDAO.save(scan)).thenReturn(scan);

        Assert.assertNotNull(scanService.insert(scan));
    }

    @Test(dataProvider = "getScanData", dataProviderClass = ServiceTestDataProvider.class)
    public void testGetAll(Scan scan) {
        Mockito.when(mockScanDAO.getAllByOrderBySubmittedTimestampDesc(new PageRequest(1, 10)))
                .thenReturn(new PageImpl<>(Collections.singletonList(scan)));

        Page retrievedScansPage = scanService.getAll(1, 10);
        Assert.assertNotNull(retrievedScansPage);

        List<Scan> retrievedScans = retrievedScansPage.getContent();
        Assert.assertNotNull(retrievedScans);
        Assert.assertFalse(retrievedScans.isEmpty());
        Assert.assertEquals(scan, retrievedScans.get(0));
    }

    @Test(dataProvider = "getScanData", dataProviderClass = ServiceTestDataProvider.class)
    public void testGetByJobId(Scan scan) {
        Mockito.when(mockScanDAO.getByJobId(scan.getJobId())).thenReturn(scan);

        Scan retrievedScan = scanService.getByJobId(scan.getJobId());
        Assert.assertNotNull(retrievedScan);
        Assert.assertEquals(scan.getJobId(), retrievedScan.getJobId());
    }

    @Test(dataProvider = "getScanData", dataProviderClass = ServiceTestDataProvider.class, expectedExceptions =
            ScanManagerException.class)
    public void testUpdateStatusException(Scan scan) throws ScanManagerException {
        Mockito.when(mockScanDAO.updateStatus(ScanStatus.SUBMITTED, scan.getJobId())).thenReturn(-1);

        scanService.updateStatus(scan.getJobId(), ScanStatus.SUBMITTED);
    }

    @Test(dataProvider = "getScanData", dataProviderClass = ServiceTestDataProvider.class, expectedExceptions =
            ScanManagerException.class)
    public void testUpdatePriorityException(Scan scan) throws ScanManagerException {
        Mockito.when(mockScanDAO.updatePriority(ScanPriority.MEDIUM.getValue(), scan.getJobId())).thenReturn(-1);

        scanService.updatePriority(scan.getJobId(), ScanPriority.MEDIUM);
    }

    @Test(dataProvider = "getScanData", dataProviderClass = ServiceTestDataProvider.class, expectedExceptions =
            ScanManagerException.class)
    public void testUpdateScannerAppIdException(Scan scan) throws ScanManagerException {
        Mockito.when(mockScanDAO.updateScannerAppId(SCANNER_APP_ID, scan.getJobId())).thenReturn(-1);

        scanService.updateScannerAppId(scan.getJobId(), SCANNER_APP_ID);
    }

    @Test(dataProvider = "getScanData", dataProviderClass = ServiceTestDataProvider.class)
    public void testGetByStatus(Scan scan) {
        Mockito.when(mockScanDAO.getByStatus(ScanStatus.SUBMITTED)).thenReturn(Collections.singletonList(scan));

        List<Scan> retrievedScans = scanService.getByStatus(ScanStatus.SUBMITTED);
        Assert.assertNotNull(retrievedScans);
        Assert.assertFalse(retrievedScans.isEmpty());
        Assert.assertEquals(scan.getStatus(), retrievedScans.get(0).getStatus());
    }

    @Test(dataProvider = "getScanData", dataProviderClass = ServiceTestDataProvider.class)
    public void testGetPendingScans(Scan scan) {
        Mockito.when(mockScanDAO.getByStatusOrderByPriorityAscSubmittedTimestampAsc(ScanStatus.SUBMIT_PENDING))
                .thenReturn(Collections.singletonList(scan));

        List<Scan> retrievedScans = scanService.getPendingScans(ScanStatus.SUBMIT_PENDING);
        Assert.assertNotNull(retrievedScans);
        Assert.assertFalse(retrievedScans.isEmpty());
        Assert.assertEquals(scan.getStatus(), retrievedScans.get(0).getStatus());
    }

    @Test(dataProvider = "getScanData", dataProviderClass = ServiceTestDataProvider.class)
    public void testGetByStatusesAndScannerAndProduct(Scan scan) {
        Mockito.when(mockScanDAO.getByStatusInAndScannerAndProduct(Collections
                        .singletonList(ScanStatus.SUBMIT_PENDING), scan.getScanner(),
                scan.getProduct())).thenReturn(Collections.singletonList(scan));

        List<Scan> retrievedScans = scanService.getByStatusesAndScannerAndProduct(Collections
                .singletonList(ScanStatus.SUBMIT_PENDING), scan.getScanner(), scan.getProduct());
        Assert.assertNotNull(retrievedScans);
        Assert.assertFalse(retrievedScans.isEmpty());
        Assert.assertEquals(scan.getStatus(), retrievedScans.get(0).getStatus());
    }
}
