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
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;

import java.util.List;

/**
 * The DAO class that manage the persistence methods of the scans.
 */
@Repository
public interface ScanDAO extends PagingAndSortingRepository<Scan, String> {

    /**
     * Get all scans.
     *
     * @param product product name
     * @param pageable page request object
     * @return page containing the list of requested scans
     */
    @Query("select o from Scan o where o.product = :product")
    public Page<Scan> getScanByProductByOrderBySubmittedTimestampDesc(@Param("product") String product,
            Pageable pageable);

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
    public int updateStatus(ScanStatus status, String jobId);

    /**
     * Update scanner app id that has been selected for the scan.
     *
     * @param scannerAppId scanner app id
     * @param jobId        scan job id
     * @return number of rows that were updated
     */
    @Modifying
    @Query("update Scan u set u.scannerAppId = ?1 where u.jobId = ?2")
    public int updateScannerAppId(String scannerAppId, String jobId);

    /**
     * Update scan priority.
     *
     * @param priority priority level to be updated
     * @param jobId    job id of the scan that needs to be updated
     * @return number of rows that were updated
     */
    @Modifying
    @Query("update Scan u set u.priority = ?1 where u.jobId = ?2")
    public int updatePriority(Integer priority, String jobId);

    /**
     * Get scan by status.
     *
     * @param status scan status
     * @return list of scans with the given status
     */
    public List<Scan> getByStatus(ScanStatus status);

    /**
     * Get scan by status and order by priority and submitted timestamp
     *
     * @param status status of the scan
     * @return a list of scans with given priority and ordered by priority and submitted timestamp
     */
    public List<Scan> getByStatusOrderByPriorityAscSubmittedTimestampAsc(ScanStatus status);

    /**
     * Get the scans by a given set of statuses, scanner and a product.
     *
     * @param statuses list of scan statuses
     * @param scanner  assigned scanner
     * @param product  assigned product
     * @return list of scans for the given status, scanner and the product
     */
    @Query("select o from Scan o where o.status in :statuses and o.scanner = :scanner and o.product = :product")
    public List<Scan> getByStatusInAndScannerAndProduct(@Param("statuses") List<ScanStatus> statuses, @Param(
            "scanner") Scanner scanner, @Param("product") String product);

//    /**
//     * Get logs by scan.
//     *
//     * @param product scan details
//     * @param pageable page information
//     * @return list of logs for a given scan
//     */
//    public Page<Scan> getByScanOrderByTimeStampDesc(String product, Pageable pageable);

    @Query("select o from Scan o where o.product = :product")
    public List<Scan> getByProduct(@Param("product") String product);
}
