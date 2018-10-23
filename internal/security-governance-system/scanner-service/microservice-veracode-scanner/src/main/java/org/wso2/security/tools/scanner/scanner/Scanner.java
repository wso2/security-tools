/*
 *  Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanner.scanner;

import org.wso2.security.tools.scanner.exception.ScannerException;

public interface Scanner {

    /**
     * Run scan using the product zip file
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return Path to the scan report or status of the scan
     * @throws ScannerException
     */
    String runScanUsingProductZip(ScannerRequestObject scannerRequestObject) throws ScannerException;

    /**
     * Run scan using the github location of a given product
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return Path to the scan report or status of the scan
     * @throws ScannerException
     */
    String runScanUsingProductGitURL(ScannerRequestObject scannerRequestObject) throws ScannerException;

    /**
     * Initialise the Scanner
     *
     * @throws ScannerException
     */
    void init() throws ScannerException;

    /**
     * Returns the status of the last scan
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return Enum of the ScannerStatus
     */
    ScannerStatus getLastScanStatus(ScannerRequestObject scannerRequestObject) throws ScannerException;

    /**
     * Controller method to stop the last scan for a given application
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return whether delete scan operation success
     */
    boolean deleteLastScan(ScannerRequestObject scannerRequestObject) throws ScannerException;

    /**
     * Controller method to stop the last scan for a given application
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return whether the report is downloaded
     */
    boolean detailedReportPdf(ScannerRequestObject scannerRequestObject) throws ScannerException;

}