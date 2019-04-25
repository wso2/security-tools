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

import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.core.exception.InvalidRequestException;

/**
 * Service class that manage the methods for callback endpoint.
 */
public interface CallbackService {

    /**
     * Update scan by details from scanners.
     *
     * @param scan           scan object
     * @param scanStatus     scan status from the scanners
     * @param scannerScanId  scan id assigned to the scan from the actual scanner
     * @param scanReportPath scan report path
     * @throws InvalidRequestException when the provided scan status is not valid
     */
    public void updateScan(Scan scan, ScanStatus scanStatus, String scannerScanId, String scanReportPath)
            throws InvalidRequestException;
}
