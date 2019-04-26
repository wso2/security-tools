/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.common.external.model.ScannerApp;
import org.wso2.security.tools.scanmanager.core.dao.ScannerAppDAO;
import org.wso2.security.tools.scanmanager.core.dao.ScannerDAO;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;

import java.util.List;

/**
 * Scanner Service class that manage the method implementations of the scanners.
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
public class ScannerServiceImpl implements ScannerService {

    private ScannerDAO scannerDAO;
    private ScannerAppDAO scannerAppDAO;

    @Autowired
    public ScannerServiceImpl(ScannerDAO scannerDAO, ScannerAppDAO scannerAppDAO) {
        this.scannerDAO = scannerDAO;
        this.scannerAppDAO = scannerAppDAO;
    }

    @Override
    public Scanner insert(Scanner scanner) {
        return scannerDAO.saveAndFlush(scanner);
    }

    @Override
    public Scanner update(Scanner scanner) {
        return scannerDAO.saveAndFlush(scanner);
    }

    @Override
    public Scanner getById(String scannerId) {
        return scannerDAO.getScannerById(scannerId);
    }

    @Override
    public List<Scanner> getAll() {
        return scannerDAO.findAll();
    }

    @Override
    public List<ScannerApp> getAppsByScannerAndAssignedProduct(Scanner scanner, String productName) {
        return scannerAppDAO.getByScannerAndAssignedProduct(scanner, productName);
    }

    @Override
    public void removeByScannerId(String scannerId) throws ScanManagerException {
        Integer updatedRows = scannerDAO.removeById(scannerId);
        if (updatedRows != 1) {
            throw new ScanManagerException("Error occurred while removing scanner: " + scannerId);
        }
    }
}
