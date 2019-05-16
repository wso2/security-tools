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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.external.model.ScannerApp;
import org.wso2.security.tools.scanmanager.core.dao.ScannerAppDAO;
import org.wso2.security.tools.scanmanager.core.dao.ScannerDAO;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.security.tools.scanmanager.core.util.ScanManagerTestConstants.TEST_PRODUCT_ID;
import static org.wso2.security.tools.scanmanager.core.util.ScanManagerTestConstants.TEST_SCANNER_ID;

/**
 * Test class for scanner service methods.
 */
public class ScannerServiceImplTest {

    @Mock
    private ScannerDAO mockScannerDAO;

    @Mock
    private ScannerAppDAO mockScannerAppDAO;

    @InjectMocks
    private ScannerServiceImpl scannerService;

    @BeforeClass
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(dataProvider = "getScannerData", dataProviderClass = ServiceTestDataProvider.class)
    public void testInsert(Scanner testScanner) {
        Mockito.when(mockScannerDAO.saveAndFlush(testScanner)).thenReturn(testScanner);

        Scanner insertedScanner = scannerService.insert(testScanner);
        Assert.assertNotNull(insertedScanner);
        Assert.assertEquals(TEST_SCANNER_ID, insertedScanner.getId());
    }

    @Test(dataProvider = "getScannerData", dataProviderClass = ServiceTestDataProvider.class)
    public void testUpdate(Scanner testScanner) {
        Mockito.when(mockScannerDAO.saveAndFlush(testScanner)).thenReturn(testScanner);

        Scanner insertedScanner = scannerService.update(testScanner);
        Assert.assertNotNull(insertedScanner);
        Assert.assertEquals("testScannerImage", insertedScanner.getImage());
    }

    @Test(dataProvider = "getScannerData", dataProviderClass = ServiceTestDataProvider.class)
    public void testGetById(Scanner testScanner) {
        Mockito.when(mockScannerDAO.getScannerById(TEST_SCANNER_ID)).thenReturn(testScanner);

        Scanner insertedScanner = scannerService.getById(TEST_SCANNER_ID);
        Assert.assertNotNull(insertedScanner);
        Assert.assertEquals(TEST_SCANNER_ID, insertedScanner.getId());
    }

    @Test(dataProvider = "getScannerData", dataProviderClass = ServiceTestDataProvider.class)
    public void testGetAll(Scanner testScanner) {
        List<Scanner> scannerList = new ArrayList<>();
        scannerList.add(testScanner);
        Mockito.when(mockScannerDAO.findAll()).thenReturn(scannerList);

        List<Scanner> returnedScanners = scannerService.getAll();
        Assert.assertNotNull(returnedScanners);
        Assert.assertFalse(returnedScanners.isEmpty());
    }

    @Test(dataProvider = "getScannerData", dataProviderClass = ServiceTestDataProvider.class)
    public void testGetAppsByScannerAndAssignedProduct(Scanner testScanner) {
        Mockito.when(mockScannerAppDAO.getByScannerAndAssignedProduct(testScanner, TEST_PRODUCT_ID))
                .thenReturn(new ArrayList<>(testScanner.getApps()));

        List<ScannerApp> returnedScannerApps = scannerService.getAppsByScannerAndAssignedProduct(testScanner,
                TEST_PRODUCT_ID);

        Assert.assertNotNull(returnedScannerApps);
        Assert.assertFalse(returnedScannerApps.isEmpty());
        Assert.assertEquals(TEST_PRODUCT_ID, returnedScannerApps.get(0).getAssignedProduct());
    }

    @Test(expectedExceptions = ScanManagerException.class)
    public void testRemoveByScannerIdException() throws ScanManagerException {
        Mockito.when(mockScannerDAO.removeById(TEST_SCANNER_ID)).thenReturn(-1);
        scannerService.removeByScannerId(TEST_SCANNER_ID);
    }
}
