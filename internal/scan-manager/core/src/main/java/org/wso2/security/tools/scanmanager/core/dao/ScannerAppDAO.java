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
package org.wso2.security.tools.scanmanager.core.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.external.model.ScannerApp;

import java.util.List;

/**
 * The DAO class that manage the persistence methods of the scanner apps files.
 */
@Repository
public interface ScannerAppDAO extends JpaRepository<ScannerApp, Integer> {

    /**
     * Get scanner apps by scanner.
     *
     * @param scanner scanner object
     * @return list of scanner apps for the given scanner
     */
    public List<ScannerApp> getByScanner(Scanner scanner);

    /**
     * Get scanner app by app id and scanner.
     *
     * @param scanner scanner object
     * @param appId   application id and the scanner app
     * @return scanner app for the given app is and scanner
     */
    public ScannerApp getByScannerAndAppId(Scanner scanner, String appId);

    /**
     * Get scanner app by scanner and assigned product name.
     *
     * @param productName assigned product name
     * @return list of scanner apps for a scanner and assigned product name
     */
    public List<ScannerApp> getByScannerAndAssignedProduct(Scanner scanner, String productName);
}
