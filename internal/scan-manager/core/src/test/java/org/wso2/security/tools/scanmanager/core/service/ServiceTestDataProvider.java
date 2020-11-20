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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.log4j.Logger;
import org.testng.annotations.DataProvider;
import org.wso2.security.tools.scanmanager.common.external.model.Log;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScanFile;
import org.wso2.security.tools.scanmanager.common.external.model.ScanProperty;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.external.model.ScannerApp;
import org.wso2.security.tools.scanmanager.common.external.model.ScannerField;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanPriority;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.common.model.ScanType;
import org.wso2.security.tools.scanmanager.common.model.ScannerType;
import org.wso2.security.tools.scanmanager.core.config.ScanManagerConfiguration;
import org.wso2.security.tools.scanmanager.core.model.Container;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_APP_LABEL_NAME;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_ENV_NAME_SCAN_MANAGER_HOST;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_ENV_NAME_SCAN_MANAGER_PORT;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_SCANNER_LABEL_NAME;
import static org.wso2.security.tools.scanmanager.core.util.Constants.CONTAINER_SCAN_JOB_ID_LABEL_NAME;
import static org.wso2.security.tools.scanmanager.core.util.ScanManagerTestConstants.SCANNER_APP_ID;
import static org.wso2.security.tools.scanmanager.core.util.ScanManagerTestConstants.SCANNER_APP_NAME;
import static org.wso2.security.tools.scanmanager.core.util.ScanManagerTestConstants.TEST_PRODUCT_ID;
import static org.wso2.security.tools.scanmanager.core.util.ScanManagerTestConstants.TEST_SCANNER_ID;
import static org.wso2.security.tools.scanmanager.core.util.ScanManagerTestConstants.TEST_SCAN_ID;

/**
 * Base class for service tests.
 */
public class ServiceTestDataProvider {

    private static final Logger logger = Logger.getLogger(ServiceTestDataProvider.class);

    @DataProvider(name = "getScannerData")
    public static Object[][] getScannerData() {
        Scan scan = buildScan();
        return new Object[][]{{scan.getScanner()}};
    }

    private static Scan buildScan() {
        Scanner scanner = new Scanner();
        ScannerApp scannerApp = new ScannerApp(null, SCANNER_APP_ID, SCANNER_APP_NAME, TEST_PRODUCT_ID);
        ScannerField scannerField = new ScannerField("testField", null, "testField",
                null, "file", 2, true, "test1,test2");

        scanner.setId(TEST_SCANNER_ID);
        scanner.setName("testScanner");
        scanner.setImage("testScannerImage");
        scanner.setType(ScannerType.STATIC);
        scanner.setApps(Collections.singletonList(scannerApp));
        scanner.setFields(Collections.singletonList(scannerField));

        ScanFile scanFile = new ScanFile(null, "testScanFile", "testScanFileLocation");
        Set scanFileSet = new HashSet<>();
        scanFileSet.add(scanFile);
        ScanProperty scanProperty = new ScanProperty(null, "testScanProperty", "testScanPropertyValue");
        Set scanPropertySet = new HashSet<>();
        scanPropertySet.add(scanProperty);

        Scan scan = new Scan(TEST_SCAN_ID, "testScan", "testScan", scanner, ScanStatus.SUBMITTED,
                ScanPriority.MEDIUM.getValue(), TEST_PRODUCT_ID, ScanType.STATIC, 1, null, SCANNER_APP_ID, null, null,
                null, scanFileSet, scanPropertySet, new Timestamp(System.currentTimeMillis()), null);
        return scan;
    }

    @DataProvider(name = "getScanEngineData")
    public static Object[][] getScanEngineData() {
        Scan scan = buildScan();
        Map<String, String> labels = new HashMap<>();
        labels.put(CONTAINER_SCAN_JOB_ID_LABEL_NAME, scan.getJobId());
        labels.put(CONTAINER_APP_LABEL_NAME, SCANNER_APP_ID);
        labels.put(CONTAINER_SCANNER_LABEL_NAME, scan.getScanner().getName());

        String[] envVariables =
                new String[]{CONTAINER_ENV_NAME_SCAN_MANAGER_HOST + "=" + ScanManagerConfiguration.getInstance()
                        .getScanManagerHost(), CONTAINER_ENV_NAME_SCAN_MANAGER_PORT + "=" + ScanManagerConfiguration
                        .getInstance().getScanManagerPort()};
        Container container = new Container("123456", true, Collections.singletonMap(8080, 9081),
                Collections.EMPTY_LIST, Arrays.asList(envVariables), labels);
        return new Object[][]{{scan, container}};
    }

    @DataProvider(name = "getScanData")
    public static Object[][] getScanData() {
        Scan scan = buildScan();
        return new Object[][]{{scan}};
    }

    @DataProvider(name = "getLogData")
    public static Object[][] getLogData() {
        Scan scan = buildScan();
        Log log = new Log(scan, LogType.INFO, new Timestamp(System.currentTimeMillis()), "testMessage");
        return new Object[][]{{log}};
    }

    /**
     * Parse a given object to a Scan object.
     *
     * @param obj object that needs to be parsed
     * @return parsed scan object
     */
    public static Scan parseScanObject(Object obj) {
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(obj);
            return new ObjectMapper().readValue(json, Scan.class);
        } catch (IOException e) {
            logger.error("Error occurred while parsing the scan object", e);
            return null;
        }
    }

    /**
     * Parse a given object to a Container object.
     *
     * @param obj object that needs to be parsed
     * @return parsed container object
     */
    public static Container parseContainerObject(Object obj) {
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(obj);
            return new ObjectMapper().readValue(json, Container.class);
        } catch (IOException e) {
            logger.error("Error occurred while parsing the container object", e);
            return null;
        }
    }
}
