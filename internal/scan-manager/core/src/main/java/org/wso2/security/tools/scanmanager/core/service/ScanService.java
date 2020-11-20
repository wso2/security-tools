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

import java.util.List;

/**
 * The service class that manage the methods of the scans.
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
     * Insert a Scan entity.
     *
     * @param scan scan object
     * @return inserted scan object
     */
    public Scan insert(Scan scan);

    /**
     * Get all scans by page.
     *
     * @param pageNumber page number
     * @param pageSize   size of the page
     * @return a page containing the requested scans
     */
    public Page<Scan> getAll(Integer pageNumber, Integer pageSize);

    /**
     * Get scan by job id.
     *
     * @param jobId job id for the scan
     * @return scan object for the given job id
     */
    public Scan getByJobId(String jobId);

    /**
     * Get scan status by job id.
     *
     * @param jobId job id for the scan
     * @return scan status for the given job id
     */
    public ScanStatus getStatusByJobId(String jobId);

    /**
     * Get scan context by job id.
     * @param jobId job id for the scan
     * @return scan context for the given job id
     */
    public String getScanContextByJobId(String jobId);

    /**
     * Update the scan status.
     *
     * @param jobId  job id for the scan
     * @param status status of the scan
     * @throws ScanManagerException when an error occurs while updating the scan status
     */
    public void updateStatus(String jobId, ScanStatus status) throws ScanManagerException;

    /**
     * Update the scan context
     *
     * @param jobId job id for the scan
     * @param scanContextString scan context string
     * @throws ScanManagerException when an error occurs while updating the scan status
     */
    public void updateScanContext(String jobId, String scanContextString) throws ScanManagerException;

    /**
     * Update scan priority.
     *
     * @param jobId    job id of the scan
     * @param priority priority of the scan
     * @throws ScanManagerException when an error occurs while updating the scan priority
     */
    public void updatePriority(String jobId, ScanPriority priority) throws ScanManagerException;

    /**
     * Update scanner app id.
     *
     * @param jobId        job id of the scan
     * @param scannerAppId scanner app id
     * @throws ScanManagerException when an error occurs while updating the scanner app id for the scan
     */
    public void updateScannerAppId(String jobId, String scannerAppId) throws ScanManagerException;

    /**
     * Update containerId of the scan
     * @param jobId job id of the scan
     * @param containerID container id of the scna
     * @throws ScanManagerException when an error occurs while updating the scan container Id.
     */
    public void updateContainerID(String jobId, String containerID) throws ScanManagerException;

    /**
     * Get scans with a given status.
     *
     * @param status status of the scan
     * @return list of scan objects with the given status
     */
    public List<Scan> getByStatus(ScanStatus status);

    /**
     * Get scan by status and order by priority and submitted timestamp
     *
     * @param status status of the scan
     * @return a list of scans with given priority and ordered by priority and submitted timestamp
     */
    public List<Scan> getPendingScans(ScanStatus status);

    /**
     * Get scans with a given list of statuses, scanner and product.
     *
     * @param statuses statuses of the scan
     * @param scanner  scanner object
     * @param product  scanning product
     * @return list of scan objects with a given status, scanner and product
     */
    public List<Scan> getByStatusesAndScannerAndProduct(List<ScanStatus> statuses, Scanner scanner, String product);
}
