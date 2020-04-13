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
import org.wso2.security.tools.scanmanager.common.external.model.Log;
import org.wso2.security.tools.scanmanager.core.dao.LogDAO;
import org.wso2.security.tools.scanmanager.core.dao.ScanDAO;

import java.util.Collections;
import java.util.List;

/**
 * Test class for log service methods.
 */
public class LogServiceImplTest {

    @Mock
    private LogDAO mockLogDAO;

    @Mock
    private ScanDAO mockScanDAO;

    @InjectMocks
    private LogServiceImpl logService;

    @BeforeClass
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(dataProvider = "getLogData", dataProviderClass = ServiceTestDataProvider.class)
    public void testInsertError(Log log) {
        Mockito.when(mockScanDAO.getByJobId(log.getScan().getJobId())).thenReturn(log.getScan());
        Mockito.when(mockLogDAO.save(log)).thenReturn(log);

        Assert.assertTrue(logService.insertError(log.getScan(),
                new Throwable("This is an error log test case. Please ignore the below stacktrace.")));
    }

    @Test(dataProvider = "getLogData", dataProviderClass = ServiceTestDataProvider.class)
    public void testInsert(Log log) {
        Mockito.when(mockScanDAO.getByJobId(log.getScan().getJobId())).thenReturn(log.getScan());
        Mockito.when(mockLogDAO.save(log)).thenReturn(log);

        Assert.assertTrue(logService.insert(log.getScan(), log.getType(), log.getTimeStamp(), log.getMessage()));
    }

    @Test(dataProvider = "getLogData", dataProviderClass = ServiceTestDataProvider.class)
    public void testInsertWithoutTimestamp(Log log) {
        Mockito.when(mockScanDAO.getByJobId(log.getScan().getJobId())).thenReturn(log.getScan());
        Mockito.when(mockLogDAO.save(log)).thenReturn(log);

        Assert.assertTrue(logService.insert(log.getScan(), log.getType(), log.getMessage()));
    }

    @Test(dataProvider = "getLogData", dataProviderClass = ServiceTestDataProvider.class)
    public void testGetByScan(Log log) {
        Mockito.when(mockScanDAO.getByJobId(log.getScan().getJobId())).thenReturn(log.getScan());
        Mockito.when(mockLogDAO.getByScanOrderByTimeStampDesc(log.getScan(),
                PageRequest.of(1, 10))).thenReturn(new PageImpl<>(Collections.singletonList(log)));

        Page retrievedLogsPage = logService.getByScan(log.getScan(), 1, 10);
        Assert.assertNotNull(retrievedLogsPage);
        List<Log> retrievedLogs = retrievedLogsPage.getContent();
        Assert.assertNotNull(retrievedLogs);
        Assert.assertFalse(retrievedLogs.isEmpty());
        Assert.assertEquals(log.getScan(), retrievedLogs.get(0).getScan());
    }
}
