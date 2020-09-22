/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.security.tools.scanmanager.webapp.service;

import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.springframework.http.ResponseEntity;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScanExternal;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerScansResponse;
import org.wso2.security.tools.scanmanager.common.external.model.User;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;

import java.util.List;

/**
 * Scan service interface that defines the scan service methods.
 */
public interface ScanService {

    /**
     * Start a scan.
     *
     * @param user  user
     * @param fileItemIterator item iterator of the http request
     * @return Scan object that was received by the webapp
     */
    public Scan submitScan(User user, FileItemIterator fileItemIterator);

    /**
     * Get the list of scans for a given page.
     *
     * @param pageNumber page number
     * @return scan manager response containing a list of scans for the given page number
     * @throws ScanManagerWebappException when an error occurs when getting the list of scans
     */
    public ScanManagerScansResponse getScans(Integer pageNumber) throws ScanManagerWebappException;

    /**
     * Get the list of scans under preparation to be submitted to scan manager API.
     *
     * @return scan manager response containing a list of scans under preparation to be submitted to scan manager API
     */
    public List<ScanExternal> getPreparingScans();

    /**
     * Get details of a scan.
     *
     * @param jobId job id for the scan
     * @return scan response object with the requested scan details
     * @throws ScanManagerWebappException when an error occurs when getting the details of the scan
     */
    public ScanExternal getScan(String jobId) throws ScanManagerWebappException;

    /**
     * Get the scan report from FTP server.
     *
     * @param reportPath     report file path
     * @param outputFilePath file download location
     * @throws ScanManagerWebappException when an error occurs while downloading the scan report
     */
    public void getScanReport(String reportPath, String outputFilePath) throws ScanManagerWebappException;

    /**
     * Send a cancel scan request.
     *
     * @param id scan jobId
     * @return response success if the scan cancel request was successfully submitted
     * @throws ScanManagerWebappException when an error occurs while cancelling the scan
     */
    public ResponseEntity stopScan(String id) throws ScanManagerWebappException;

    /**
     * Clear a scan
     *
     * @param id scan jobId
     * @return return true if the scan was successfully cleared.
     */
    public boolean clearScan(String id);
}
