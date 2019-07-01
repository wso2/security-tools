/*
 *  Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.security.tools.scanmanager.scanners.common.service;

import org.wso2.security.tools.scanmanager.common.internal.model.ScannerScanRequest;

/**
 * Interface for the scanner.
 */
public interface Scanner {

    /**
     * Run scan.
     *
     * @param scanRequest Object that represent the required information for tha scanner operation
     */
    public void startScan(ScannerScanRequest scanRequest);

    /**
     * Validate the start scan request.
     *
     * @param scannerScanRequest start scan request
     * @return whether start scan request is a valid one
     */
    public boolean validateStartScan(ScannerScanRequest scannerScanRequest);

    /**
     * Stop the last scan for a given application.
     *
     * @param scanRequest Object that represent the required information for tha scanner operation
     */
    public void cancelScan(ScannerScanRequest scanRequest);

    /**
     * Validate the cancel scan request.
     *
     * @param scannerScanRequest cancel scan request
     * @return whether cancel scan request is a valid one
     */
    public boolean validateCancelScan(ScannerScanRequest scannerScanRequest);
}
