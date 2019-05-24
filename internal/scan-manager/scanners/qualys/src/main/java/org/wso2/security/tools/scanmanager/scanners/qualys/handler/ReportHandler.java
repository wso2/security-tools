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

package org.wso2.security.tools.scanmanager.scanners.qualys.handler;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.ErrorProcessingUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.FileUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.config.QualysScannerConfiguration;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanContext;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This method handles the report generation part
 */
public class ReportHandler {

    private static final Log log = LogFactory.getLog(ReportHandler.class);
    String[] reportTypes = { QualysScannerConstants.PDF_TYPE, QualysScannerConstants.XML_TYPE,
            QualysScannerConstants.HTML_BASE64_TYPE, QualysScannerConstants.CSV_V2_TYPE };
    QualysScanHandler qualysScanHandler;

    public ReportHandler(QualysScanHandler qualysScanHandler) {
        this.qualysScanHandler = qualysScanHandler;
    }

    /**
     * This method is responsible for tasks relevant to report.
     *
     * @param scanContext ScanContext
     * @return true if report is suceesfully uploaded
     * @throws ScannerException Error occurred while executing report handler.
     */
    public boolean execute(ScanContext scanContext) throws ScannerException {
        boolean isReportUploaded = false;
        String reportFolderPath = QualysScannerConfiguration.getInstance()
                .getConfigProperty(QualysScannerConstants.QUALYS_REPORT_FOLDER_PATH) + File.separator + scanContext
                .getWebAppId();

        String scanScriptLocation = scanContext.getScriptFilesLocation();

        for (String types : reportTypes) {
            String reportId = qualysScanHandler
                    .createReport(QualysScannerConfiguration.getInstance().getHost(), scanContext.getWebAppId(),
                            scanContext.getJobID(), types);
            String filepath = qualysScanHandler
                    .downloadReport(QualysScannerConfiguration.getInstance().getHost(), scanContext.getJobID(),
                            reportId, reportFolderPath);
            String logMessage = "Scan report for the application: " + scanContext.getWebAppId() + " is downloaded."
                    + " Scan Report Type : " + types + " Location : " + filepath;
            CallbackUtil.persistScanLog(scanContext.getJobID(), logMessage, LogType.INFO);
        }
        try {
            FileUtil.zipFiles(reportFolderPath, reportFolderPath + ScannerConstants.ZIP_FILE_EXTENSION);
        } catch (ArchiveException | IOException e) {
            throw new ScannerException("Error occurred while creating the zip files of generated report.");
        }
        if (uploadReportToFtp(scanContext, scanScriptLocation, reportFolderPath)) {
            isReportUploaded = true;
        }
        return isReportUploaded;
    }

    /**
     * Upload the scan report to FTP location.
     *
     * @param scanContext           Scan context
     * @param scanReportFtpLocation Ftp location of scan reports
     * @param reportPath            path that needs to upload
     * @return is uploading success
     */
    private boolean uploadReportToFtp(ScanContext scanContext, String scanReportFtpLocation, String reportPath)
            throws ScannerException {
        boolean isReportUploaded = false;
        try {
            FileUtil.uploadReport(scanReportFtpLocation, new File(reportPath),
                    QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_USERNAME),
                    (QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_PASSWORD))
                            .toCharArray(),
                    QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_HOST),
                    Integer.parseInt(
                            QualysScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_PORT)));

            String logMessage =
                    "Scan report is uploaded to the FTP server for the application: " + scanContext.getWebAppId();
            CallbackUtil.persistScanLog(scanContext.getJobID(), logMessage, LogType.INFO);
            isReportUploaded = true;
        } catch (SftpException | JSchException e) {
            int retryInterval = Integer.parseInt(QualysScannerConfiguration.getInstance()
                    .getConfigProperty(QualysScannerConstants.REPORT_UPLOAD_RETRY_INTERVAL));
            String logMessage =
                    "Report upload will retry after " + retryInterval + " seconds since that operation was failed "
                            + "due to FTP server issue. \n" + ErrorProcessingUtil.getFullErrorMessage(e);
            CallbackUtil.persistScanLog(scanContext.getJobID(), logMessage, LogType.INFO);
            try {
                TimeUnit.SECONDS.sleep(retryInterval);
            } catch (InterruptedException e1) {
                throw new ScannerException(" Failed to upload report to FTP location.", e1);
            }
            uploadReportToFtp(scanContext, scanReportFtpLocation, reportPath);
        } catch (IOException e) {
            throw new ScannerException(" Failed to upload report to FTP location.", e);
        }
        return isReportUploaded;
    }
}
