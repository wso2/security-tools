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

package org.wso2.security.tools.scanmanager.webapp.service;

import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerLogResponse;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;

import java.sql.Timestamp;

/**
 * Log service interface that defines the log service methods.
 */
public interface LogService {

    /**
     * Get the list of scan logs for a given scan.
     *
     * @param jobId      job id of the scan
     * @param pageNumber page number
     * @return scan manager response containing a list of logs for the given scan
     * @throws ScanManagerWebappException when an error occurs when getting the list of logs
     */
    public ScanManagerLogResponse getLogs(String jobId, Integer pageNumber) throws ScanManagerWebappException;

    /**
     * Insert an error log entity during an exception.
     *
     * @param scan scan details
     * @param e    throwable error message
     */
    public void insertError(Scan scan, Throwable e);

    /**
     * Insert a log entity.
     *
     * @param scan    scan details
     * @param type    log type
     * @param message log message
     */
    public void insert(Scan scan, LogType type, String message);

    /**
     * Insert a log entity with timestamp.
     *
     * @param scan      scan details
     * @param type      log type
     * @param timestamp timestamp for the log
     * @param message   log message
     */
    public void insert(Scan scan, LogType type, Timestamp timestamp, String message);

    /**
     * Remove log entries for a scan under preparation. Scans that have scan preparation tasks (before submitting to
     * scan manager API) such as uploading files to the FTP are categorized as scans under preparation. Once those
     * tasks are completed and the scan is uploaded to the scan manager API, the log entries for that particular
     * scan needs to be removed.
     *
     * @param scanJobId job id of the scan under preparation
     * @return true if the scan is found among the preparing scan logs
     */
    public boolean removeLogsForPreparingScan(String scanJobId);
}
