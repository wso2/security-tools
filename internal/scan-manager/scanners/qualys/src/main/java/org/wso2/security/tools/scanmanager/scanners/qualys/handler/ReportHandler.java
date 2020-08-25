/*
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
 */

package org.wso2.security.tools.scanmanager.scanners.qualys.handler;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.security.scanmanager.common.exception.RetryExceededException;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.model.CallbackLog;
import org.wso2.security.tools.scanmanager.scanners.common.util.ErrorProcessingUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.FileUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.qualys.config.QualysScannerConfiguration;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.QualysScanContext;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This method handles the report generation task.
 */
public class ReportHandler {

    private static final Logger log = LogManager.getLogger(ReportHandler.class);
    private String[] reportTypes = { QualysScannerConstants.PDF_TYPE, QualysScannerConstants.XML_TYPE,
            QualysScannerConstants.HTML_BASE64_TYPE, QualysScannerConstants.CSV_V2_TYPE };
    private QualysScanHandler qualysScanHandler;

    public ReportHandler(QualysScanHandler qualysScanHandler) {
        this.qualysScanHandler = qualysScanHandler;
    }

    /**
     * This method is responsible for tasks relevant to report.
     *
     * @param qualysScanContext qualysScanContext
     * @return true if report is successfully uploaded
     * @throws ScannerException error occurred while executing report handler
     */
    public boolean execute(QualysScanContext qualysScanContext) throws ScannerException {
        boolean isReportUploaded = false;
        String reportFolderPath = QualysScannerConfiguration.getInstance()
                .getConfigProperty(QualysScannerConstants.QUALYS_REPORT_FOLDER_PATH);
        String scanScriptLocation = qualysScanContext.getScriptFilesLocation();

        // Generate report for defined report types.
        for (String type : reportTypes) {
            String reportId = qualysScanHandler.createReport(qualysScanContext.getWebAppId(), qualysScanContext.getJobID(), type,
                    qualysScanContext.getReportTemplateId());
            awaitReportCreation(qualysScanContext.getJobID(), reportId, type);
            String filepath = qualysScanHandler.downloadReport(qualysScanContext.getJobID(), reportId, reportFolderPath);
            String logMessage = "Scan report for the application: " + qualysScanContext.getWebAppName() + " is downloaded."
                    + " Scan Report Type : " + type + " Location : " + filepath;
            log.info(new CallbackLog(qualysScanContext.getJobID(), logMessage));
        }

        // Zip all downloaded reports.
        try {
            FileUtil.zipFiles(reportFolderPath, reportFolderPath + ScannerConstants.ZIP_FILE_EXTENSION);
            log.info(new CallbackLog(qualysScanContext.getJobID(), "Zip file for downloaded report is created."));
        } catch (ArchiveException | IOException e) {
            throw new ScannerException("Error occurred while creating the zip files of generated report.");
        }

        // Upload created zip file to provided ftp location.
        if (uploadReportToFtp(qualysScanContext, scanScriptLocation,
                reportFolderPath + ScannerConstants.ZIP_FILE_EXTENSION)) {
            isReportUploaded = true;
        }
        return isReportUploaded;
    }

    /**
     * Upload the scan report to FTP location.
     *
     * @param qualysScanContext           scan context
     * @param scanReportFtpLocation ftp location of scan reports
     * @param reportPath            path that needs to upload
     * @return true if reports are uploaded to ftp location successfully
     */
    private boolean uploadReportToFtp(QualysScanContext qualysScanContext, String scanReportFtpLocation, String reportPath)
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
                    "Scan report is uploaded to the FTP server for the application: " + qualysScanContext.getWebAppName();
            log.info(new CallbackLog(qualysScanContext.getJobID(), logMessage));
            isReportUploaded = true;
        } catch (SftpException | JSchException e) {
            int retryInterval = Integer.parseInt(QualysScannerConfiguration.getInstance()
                    .getConfigProperty(QualysScannerConstants.REPORT_UPLOAD_RETRY_INTERVAL));
            String logMessage =
                    "Report upload will retry after " + retryInterval + " seconds since that operation was failed "
                            + "due to FTP server issue. \n" + ErrorProcessingUtil.getFullErrorMessage(e);
            log.error(new CallbackLog(qualysScanContext.getJobID(), logMessage));
            try {
                TimeUnit.SECONDS.sleep(retryInterval);
            } catch (InterruptedException e1) {
                throw new ScannerException(" Failed to upload report to FTP location.", e1);
            }
            uploadReportToFtp(qualysScanContext, scanReportFtpLocation, reportPath);
        } catch (IOException e) {
            throw new ScannerException(" Failed to upload report to FTP location.", e);
        }
        return isReportUploaded;
    }

    /**
     * This method waits for report creation task completion.
     *
     * @param jobId      job ID
     * @param reportId   report ID
     * @param reportType report type
     * @throws ScannerException Error occurred while getting the status of report creation
     */
    private void awaitReportCreation(String jobId, String reportId, String reportType) throws ScannerException {
        String status = null;
        boolean isReportCreationCompleted = false;
        try {
            status = qualysScanHandler.getReportStatus(reportId);
        } catch (IOException | InterruptedException | RetryExceededException e) {
            // If report type is XML throw the exception.
            if (QualysScannerConstants.XML_TYPE.equalsIgnoreCase(reportType)) {
                throw new ScannerException("Error occurred while XML type report. " + reportId, e);
            }
        }
        if (status != null) {
            switch (status) {
            case QualysScannerConstants.COMPLETE:
                isReportCreationCompleted = true;
                break;
            case QualysScannerConstants.ERROR:
                if (QualysScannerConstants.XML_TYPE.equalsIgnoreCase(reportType)) {
                    throw new ScannerException("Failed to create a XML type report. " + reportId);
                }
                break;
            case QualysScannerConstants.RUNNING:
                try {
                    TimeUnit.SECONDS.sleep(QualysScannerConstants.REPORT_STATUS_CHECK_DELAY);
                } catch (InterruptedException e) {
                    throw new ScannerException("Error occurred while retrieving the report status. ", e);
                }
                awaitReportCreation(jobId, reportId, reportType);
                break;
            default:
                break;
            }
        }
        if (!isReportCreationCompleted) {
            String logMessage = "Failed to create " + reportType + " Report ID : " + reportId;
            log.error(new CallbackLog(jobId, logMessage));
        }
    }
}
