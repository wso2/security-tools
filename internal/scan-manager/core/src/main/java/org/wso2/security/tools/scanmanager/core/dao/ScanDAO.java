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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.model.ScanPriority;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;

import java.util.List;

/**
 * The class {@code ScanDAO} is the DAO class that manage the persistence methods of the scans.
 */
@Repository
public interface ScanDAO extends PagingAndSortingRepository<Scan, String> {

    /**
     * Get all scans.
     *
     * @param pageable page request object
     * @return page containing the list of requested scans
     */
    public Page<Scan> findAllByOrderBySubmittedTimestampDesc(Pageable pageable);

    /**
     * Get scan by job id.
     *
     * @param jobId job id assigned for the scan
     * @return scan object for the given job id
     */
    public Scan getByJobId(String jobId);

    /**
     * Update scan status.
     *
     * @param status scan status
     * @param jobId  scan job id
     * @return number of rows that were updated
     */
    @Modifying
    @Query("update Scan u set u.status = ?1 where u.jobId = ?2")
    public int updateScanStatus(ScanStatus status, String jobId);

    /**
     * Update scan priority.
     *
     * @param priority priority level to be updated
     * @param jobId    job id of the scan that needs to be updated
     * @return number of rows that were updated
     */
    @Modifying
    @Query("update Scan u set u.priority = ?1 where u.jobId = ?2")
    public int updateScanPriority(ScanPriority priority, String jobId);

    /**
     * Get scan by status.
     *
     * @param status scan status
     * @return list of scans with the given status
     */
    public List<Scan> getByStatus(ScanStatus status);

    /**
     * Get the scans by a given status, scanner and a product.
     *
     * @param status  scan status
     * @param scanner assigned scanner
     * @param product assigned product
     * @return list of scans for the given status, scanner and the product
     */
    public List<Scan> getByStatusAndScannerAndProduct(ScanStatus status, Scanner scanner, String product);
}
