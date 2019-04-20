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

import org.springframework.data.domain.Page;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.model.ScanPriority;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.core.model.ContainerInfo;

import java.util.List;
import java.util.Map;

/**
 * The class {@code ScanService} is the service class that manage the methods of the scans.
 */
public interface ScanService {

    /**
     * Updating a Scan entity.
     *
     * @param scan scan object
     * @return updated scan object
     */
    public Scan update(Scan scan);

    /**
     * Cancel a scan.
     *
     * @param scan details of the scan that needs to be canceled
     * @return canceled scan details
     * @throws ScanManagerException when an error occurs while cancelling the scan
     */
    public Scan cancelScan(Scan scan) throws ScanManagerException;

    /**
     * Get all scans by page.
     *
     * @param pageNumber page number
     * @param pageSize   size of the page
     * @return a page containing the requested scans
     */
    public Page<Scan> findAll(Integer pageNumber, Integer pageSize);

    /**
     * Get scan by job id.
     *
     * @param jobId job id for the scan
     * @return scan object for the given job id
     */
    public Scan getScanByJobId(String jobId);

    /**
     * Update the scan status.
     *
     * @param jobId  job id for the scan
     * @param status status of the scan
     * @return number of rows updated
     */
    public Integer updateScanStatus(String jobId, ScanStatus status);

    /**
     * Update scan priority.
     *
     * @param jobId    job id of the scan
     * @param priority priority of the scan
     * @return number of rows updated
     */
    public Integer updateScanPriority(String jobId, ScanPriority priority);

    /**
     * Get scans with a given status.
     *
     * @param status status of the scan
     * @return list of scan objects with the given status
     */
    public List<Scan> getScansByStatus(ScanStatus status);

    /**
     * Get scans with a given status, scanner and product.
     *
     * @param status  status of the scan
     * @param scanner scanner object
     * @param product scanning product
     * @return list of scan objects with a given status, scanner and product
     */
    public List<Scan> getScansByStatusAndScannerAndProduct(ScanStatus status, Scanner scanner, String product);

    /**
     * Get the map of occupied scanner apps.
     *
     * @return a map containing scanner id as the key and list of scanner apps as the value
     */
    public Map<String, List<String>> getOccupiedApps();

    /**
     * Remove the container created to a given scan.
     *
     * @param scan scan details of the container that needs to be removed
     * @return container information of the removed container
     */
    public ContainerInfo removeContainer(Scan scan);

    /**
     * Begin all pending scans.
     */
    public void beginPendingScans();
}
