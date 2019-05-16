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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.wso2.security.tools.scanmanager.common.external.model.Log;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.core.dao.LogDAO;
import org.wso2.security.tools.scanmanager.core.dao.ScanDAO;

import java.sql.Timestamp;

/**
 * The service class that manage the method implementations of the Scan logs.
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
public class LogServiceImpl implements LogService {

    private static final Logger logger = Logger.getLogger(LogServiceImpl.class);

    private LogDAO logDAO;
    private ScanDAO scanDAO;

    @Autowired
    public LogServiceImpl(LogDAO logDAO, ScanDAO scanDAO) {
        this.logDAO = logDAO;
        this.scanDAO = scanDAO;
    }

    @Override
    public boolean insertError(Scan scan, Throwable e) {
        logger.error("An error occurred", e);

        boolean isScanFound = false;
        if (scanDAO.getByJobId(scan.getJobId()) != null) {
            Log log = new Log(scan, LogType.ERROR, new Timestamp(System.currentTimeMillis()), getFullErrorMessage(e));
            logDAO.save(log);
            isScanFound = true;
        }
        return isScanFound;
    }

    @Override
    public boolean insert(Scan scan, LogType type, String message) {
        return insert(scan, type, new Timestamp(System.currentTimeMillis()), message);
    }

    @Override
    public boolean insert(Scan scan, LogType type, Timestamp timestamp, String message) {
        switch (type) {
            case ERROR:
                logger.error(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
        }

        boolean isScanFound = false;
        if (scanDAO.getByJobId(scan.getJobId()) != null) {
            Log log = new Log(scan, type, timestamp, message);
            logDAO.save(log);
            isScanFound = true;
        }
        return isScanFound;
    }

    @Override
    public Page<Log> getByScan(Scan scan, Integer pageNumber, Integer pageSize) {
        Pageable pageable = new PageRequest(pageNumber, pageSize);
        return logDAO.getByScanOrderByTimeStampDesc(scan, pageable);
    }

    private String getFullErrorMessage(Throwable e) {
        if (e.getCause() == null) {
            return e.getMessage();
        }
        return e.getMessage() + "\n\nCaused by: " + getFullErrorMessage(e.getCause());
    }
}
