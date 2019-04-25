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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.model.ScanPriority;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.core.dao.ScanDAO;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;

import java.util.List;

/**
 * The service class that manage the method implementations of the scans.
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
public class ScanServiceImpl implements ScanService {

    private ScanDAO scanDAO;

    @Autowired
    public ScanServiceImpl(ScanDAO scanDAO) {
        this.scanDAO = scanDAO;
    }

    @Override
    public Scan update(Scan scan) {
        return scanDAO.save(scan);
    }

    @Override
    public Scan insert(Scan scan) {
        return scanDAO.save(scan);
    }

    @Override
    public Page<Scan> findAll(Integer pageNumber, Integer pageSize) {
        Pageable pageable = new PageRequest(pageNumber, pageSize);
        return scanDAO.findAllByOrderBySubmittedTimestampDesc(pageable);
    }

    @Override
    public Scan getByJobId(String jobId) {
        return scanDAO.getByJobId(jobId);
    }

    @Override
    public void updateStatus(String jobId, ScanStatus status) throws ScanManagerException {
        Integer updatedRows = scanDAO.updateScanStatus(status, jobId);
        if (updatedRows != 1) {
            throw new ScanManagerException("Error occurred while updating scan status of the scan: " + jobId);
        }
    }

    @Override
    public void updatePriority(String jobId, ScanPriority priority) throws ScanManagerException {
        Integer updatedRows = scanDAO.updateScanPriority(priority, jobId);
        if (updatedRows != 1) {
            throw new ScanManagerException("Error occurred while updating scan priority of the scan: " + jobId);
        }
    }

    @Override
    public void updateScannerAppId(String jobId, String scannerAppId) throws ScanManagerException {
        Integer updatedRows = scanDAO.updateScannerAppId(scannerAppId, jobId);
        if (updatedRows != 1) {
            throw new ScanManagerException("Error occurred while updating scanner app id of the scan: " + jobId);
        }
    }

    @Override
    public List<Scan> getByStatus(ScanStatus status) {
        return scanDAO.getByStatus(status);
    }

    @Override
    public List<Scan> getPendingScans(ScanStatus status) {
        return scanDAO.getByStatusOrderByPriorityAscSubmittedTimestampAsc(status);
    }

    @Override
    public List<Scan> getByStatusesAndScannerAndProduct(List<ScanStatus> statuses, Scanner scanner, String product) {
        return scanDAO.getByStatusInAndScannerAndProduct(statuses, scanner, product);
    }
}
