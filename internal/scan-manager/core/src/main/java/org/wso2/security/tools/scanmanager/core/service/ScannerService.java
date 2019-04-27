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

import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.external.model.ScannerApp;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;

import java.util.List;

/**
 * Scanner service class that manage the methods of the scanners.
 */
public interface ScannerService {

    /**
     * Insert a Scanner entity.
     *
     * @param scanner scanner object
     * @return inserted scanner object
     */
    public Scanner insert(Scanner scanner);

    /**
     * Update a Scanner.
     *
     * @param scanner scanner object
     * @return updated scanner object
     */
    public Scanner update(Scanner scanner);

    /**
     * Get scanner by scanner id.
     *
     * @param scannerId scanner id
     * @return scanner for a given scanner id
     */
    public Scanner getById(String scannerId);

    /**
     * Get all available scanners.
     *
     * @return a list of all the scanners
     */
    public List<Scanner> getAll();

    /**
     * Get the list of apps for a given scanner and a product name.
     *
     * @param scanner     scanner object
     * @param productName scanner app assigned product name
     * @return a list of scanner apps for the given scanner and the product name
     */
    public List<ScannerApp> getAppsByScannerAndAssignedProduct(Scanner scanner, String productName);

    /**
     * Remove a scanner by id.
     *
     * @param scannerId scanner id of the scanner to be removed
     * @throws ScanManagerException when an error occurs while removing the scanner
     */
    public void removeByScannerId(String scannerId) throws ScanManagerException;

}
