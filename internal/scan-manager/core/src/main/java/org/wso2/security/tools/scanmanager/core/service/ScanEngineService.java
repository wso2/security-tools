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
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.core.model.ScanManagerContainer;

/**
 * Scan engine service class that holds the scan engine service methods.
 */
public interface ScanEngineService {

    /**
     * Begin all pending scans.
     */
    public void beginPendingScans();

    /**
     * Cancel a given scan.
     *
     * @param scan scan to be canceled
     * @throws ScanManagerException when an error occurs while cancelling the scan
     */
    public void cancelScan(Scan scan) throws ScanManagerException;

    /**
     * Remove a given scan container.
     *
     * @param scan scan object
     * @return scan manager container model representing the removed scanner
     */
    public ScanManagerContainer removeContainer(Scan scan);
}
