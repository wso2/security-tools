/*
 *
 *   Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.security.tools.scanmanager.scanner.qualys.handler;

import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.qualys.service.QualysScanner;
import org.wso2.security.tools.scanmanger.scanners.qualys.QualysScannerConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * This method handles the report generation part
 */
public class ReportHandler {

    List<String> reportFilePathList = new ArrayList<>();
    String[] reportTypes = { QualysScannerConstants.PDF_TYPE, QualysScannerConstants.XML_TYPE,
            QualysScannerConstants.HTML_BASE64_TYPE, QualysScannerConstants.CSV_V2_TYPE };
    QualysScanHandler qualysScanHandler;

    public List<String> getReportFilePathList() {
        return reportFilePathList;
    }

    public void setReportFilePathList(List<String> reportFilePathList) {
        this.reportFilePathList = reportFilePathList;
    }

    public ReportHandler(QualysScanHandler qualysScanHandler) {
        this.qualysScanHandler = qualysScanHandler;
    }

    /**
     *This method is responsible for tasks relevant to report.
     * @param webId Web app ID
     * @param jobId Job ID
     * @throws ScannerException Error occurred while executing report handler.
     */
    public void execute(String webId, String jobId) throws ScannerException {
        for (String types : reportTypes) {
            String reportId = qualysScanHandler.createReport(QualysScanner.host, webId, jobId, types);
            String filepath = qualysScanHandler.downloadReport(QualysScanner.host, jobId, reportId);
            reportFilePathList.add(filepath);
            // TODO: 4/30/19  upload to fttp and update with report file and zip files

        }
    }
}
