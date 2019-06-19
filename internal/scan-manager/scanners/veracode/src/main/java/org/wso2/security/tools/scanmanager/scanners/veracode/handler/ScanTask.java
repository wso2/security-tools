/*
 *  Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.security.tools.scanmanager.scanners.veracode.handler;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.veracode.apiwrapper.wrappers.ResultsAPIWrapper;
import com.veracode.apiwrapper.wrappers.UploadAPIWrapper;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.model.CallbackLog;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.ErrorProcessingUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.FileUtil;
import org.wso2.security.tools.scanmanager.scanners.common.util.XMLUtil;
import org.wso2.security.tools.scanmanager.scanners.veracode.VeracodeScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.veracode.config.VeracodeScannerConfiguration;
import org.wso2.security.tools.scanmanager.scanners.veracode.model.ScanContext;
import org.wso2.security.tools.scanmanager.scanners.veracode.util.VeracodeAPIUtil;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import static org.wso2.security.tools.scanmanager.scanners.veracode.VeracodeScannerConstants.JAR_FILTER_FILE;

/**
 * Represents the scan handling tasks.
 */
public class ScanTask {

    private static final Logger log = LogManager.getLogger(ScanTask.class);

    // Scan request coming to the scan micro-service API.
    private ScanContext scanContext;

    // Directory used for preparation of the scan artifacts.
    private File workingDirectory = null;

    public ScanTask(ScanContext scanContext) {
        if (log.isDebugEnabled()) {
            String logMessage = "Upload Artifact Handler thread is being initialized for the application:"
                    + scanContext.getAppId();
            log.debug(new CallbackLog(scanContext.getJobId(), logMessage));
        }
        this.scanContext = scanContext;
    }

    public void run() {

        if (isScanRunning()) {
            // If another scans is running on the actual Veracode cloud scanner, then the start scan request
            // would be failed and Scan status is updated as 'ERROR'.

            String logMessage = "Currently another scan is running on the application id  : " + scanContext
                    .getAppId() + ". So please check this scan before proceed with another scan.";
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));

            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
        }

        if (handleUploadingTask()) {
            if (handleResultProcessTask()) {
                String logMessage = "Start scan process is successfully completed for the application: "
                        + scanContext.getAppId();
                log.info(new CallbackLog(scanContext.getJobId(), logMessage));
            } else {
                Thread.currentThread().interrupt();
            }
        } else {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Handle the scan artifact upload task to the scanner.
     *
     * @return is uploading task successfully completed
     */
    private boolean handleUploadingTask() {
        boolean isUploadSuccess = false;
        String logMessage = null;
        if (cleanPreviousScans()) {
            if (creatingScanArtifactZip()) {
                if (uploadScanArtifact()) {
                    isUploadSuccess = true;
                } else {
                    logMessage = "Artifact upload is failed.";
                }
            } else {
                logMessage = "Creating artifact zip is failed.";
            }
        } else {
            logMessage = "Cleaning previous scans is failed.";
        }
        if (!isUploadSuccess) {
            logMessage = logMessage.concat(" Terminating scan for app: " + scanContext.getAppId());
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));
        }
        return isUploadSuccess;
    }

    /**
     * Handle the scan process and results response task of the scanner.
     *
     * @return is result handling task successfully completed
     */
    private boolean handleResultProcessTask() {
        boolean isResultsUploaded = false;
        String logMessage = null;
        if (beginPreScan()) {
            if (beginScan()) {
                if (getScanReport()) {
                    isResultsUploaded = true;
                } else {
                    logMessage = "Uploading scan report is failed.";
                }
            } else {
                logMessage = "Scan starting is failed.";
            }
        } else {
            logMessage = "Pre-Scan is failed.";
        }
        if (!isResultsUploaded) {
            logMessage = logMessage + (" Terminating scan for app: " + scanContext.getAppId());
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));
        }
        return isResultsUploaded;
    }

    /**
     * Check any scans is running on the Verocode for the current application.
     *
     * @return are there any scans currently running status
     */
    private boolean isScanRunning() {
        boolean isScanRunning = false;
        String result;
        ScanStatus currentScanStatus;

        if (!Thread.currentThread().isInterrupted()) {
            try {
                UploadAPIWrapper uploadAPIWrapper = VeracodeAPIUtil.getUploadAPIWrapper();
                result = uploadAPIWrapper.getBuildInfo(scanContext.getAppId());
                currentScanStatus = VeracodeResultProcessor.getScanStatus(result);
                if (ScanStatus.RUNNING.equals(currentScanStatus)) {
                    isScanRunning = true;
                }
            } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
                String logMessage = "Error occured while retrieving the scan status for application : " + scanContext
                        .getAppId() + " " + ErrorProcessingUtil.getFullErrorMessage(e);
                log.error(new CallbackLog(scanContext.getJobId(), logMessage));

                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
            }
        } else {
            String logMessage = "Current thread is interrupted for application : " + scanContext.getAppId();
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));
        }
        return isScanRunning;
    }

    /**
     * Stop the last scan of the current application.
     *
     * @return whether clean the last scan operation is success
     */
    private boolean cleanPreviousScans() {
        boolean isScannerCleaned = false;
        String result;

        if (!Thread.currentThread().isInterrupted()) {
            try {
                UploadAPIWrapper uploadAPIWrapper = VeracodeAPIUtil.getUploadAPIWrapper();
                result = uploadAPIWrapper.deleteBuild(scanContext.getAppId());

                if (log.isDebugEnabled()) {
                    String logMessage = "Deleted the last scan of the application :" + scanContext.getAppId();
                    log.debug(new CallbackLog(scanContext.getJobId(), logMessage));
                }

                isScannerCleaned = VeracodeResultProcessor.isOperationProceedWithoutError(result);
            } catch (IOException e) {
                String logMessage = "Error occured while deleting the scan status for application : " + scanContext
                        .getAppId() + " " + ErrorProcessingUtil.getFullErrorMessage(e);
                log.error(new CallbackLog(scanContext.getJobId(), logMessage));

                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
            }
        } else {
            String logMessage = "Current thread is interrupted for application : " + scanContext.getAppId();
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));
        }
        return isScannerCleaned;
    }

    /**
     * Create the scan artifact zip to upload to Veracode.
     *
     * @return is scan zip artifact creation success
     */
    private boolean creatingScanArtifactZip() {
        boolean isZipCreated = false;
        String extractedFilePath;

        String scanArtifact = scanContext.getArtifactLocation();
        String productPackName = scanArtifact.substring(scanArtifact.lastIndexOf(File.separator) + 1,
                scanArtifact.length());
        String productPath = scanArtifact.substring(0, scanArtifact.lastIndexOf(File.separator));

        File productFile = new File(VeracodeScannerConfiguration.getInstance().getConfigProperty(
                ScannerConstants.DEFAULT_FTP_PRODUCT_PATH) + productPackName);
        if (!Thread.currentThread().isInterrupted()) {
            try {
                String logMessage = "Product pack is downloading for the application: " + scanContext.getAppId();
                log.info(new CallbackLog(scanContext.getJobId(), logMessage));

                FileUtil.downloadFromFtp(productPath, productPackName, productFile, VeracodeScannerConfiguration
                                .getInstance().getConfigProperty(ScannerConstants.FTP_USERNAME),
                        (VeracodeScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_PASSWORD))
                                .toCharArray(), VeracodeScannerConfiguration.getInstance().getConfigProperty(
                                ScannerConstants.FTP_HOST), Integer.parseInt(VeracodeScannerConfiguration.getInstance()
                                .getConfigProperty(ScannerConstants.FTP_PORT)));

                logMessage = "Product downloading completed for the application: " + scanContext.getAppId() + " into "
                        + productFile;
                log.info(new CallbackLog(scanContext.getJobId(), logMessage));

                extractedFilePath = FileUtil.extractArchive(productFile, productFile.getParent());
                workingDirectory = new File(extractedFilePath + VeracodeScannerConstants.WORK_DIRECTORY_SUFIX);

                if (workingDirectory.mkdirs()) {
                    logMessage = "Filtering the artifacts for the scan for the application: " + scanContext.getAppId() +
                            " into " + workingDirectory;
                    log.info(new CallbackLog(scanContext.getJobId(), logMessage));

                    copyRequiredScanArtifact(extractedFilePath);
                    FileUtil.zipFiles(workingDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath()
                            + ScannerConstants.ZIP_FILE_EXTENSION);
                    isZipCreated = true;

                    logMessage = "Created the zip artifact for the scan for the application: " + scanContext.getAppId()
                            + " as " + workingDirectory + ScannerConstants.ZIP_FILE_EXTENSION;
                    log.info(new CallbackLog(scanContext.getJobId(), logMessage));
                } else {
                    logMessage = "Error occured while creating the working directory for application : " + scanContext
                            .getAppId();
                    log.error(new CallbackLog(scanContext.getJobId(), logMessage));

                    CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
                }
            } catch (IOException | JSchException | SftpException | SAXException | ScannerException | ArchiveException |
                    ParserConfigurationException e) {
                String logMessage = "Error occured while creating the scan zip artifact for application : " +
                        scanContext.getAppId() + "\n" + ErrorProcessingUtil.getFullErrorMessage(e);
                log.error(new CallbackLog(scanContext.getJobId(), logMessage));

                if (!e.getClass().isInstance(InterruptedIOException.class)) {
                    CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
                }
            }
        } else {
            String logMessage = "Current thread is interrupted for application : " + scanContext.getAppId();
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));
        }
        return isZipCreated;
    }

    /**
     * Upload scan pack to the Veracode.
     *
     * @return is scan artifact uploaded to Veracode
     */
    private boolean uploadScanArtifact() {
        boolean isUploadSuccess = false;
        String buildId;

        if (!Thread.currentThread().isInterrupted()) {
            try {
                UploadAPIWrapper uploadAPIWrapper = VeracodeAPIUtil.getUploadAPIWrapper();
                String result = uploadAPIWrapper.uploadFile(scanContext.getAppId(), workingDirectory
                        + ScannerConstants.ZIP_FILE_EXTENSION);
                isUploadSuccess = VeracodeResultProcessor.isOperationProceedWithoutError(result);
                if (isUploadSuccess) {
                    result = uploadAPIWrapper.getBuildInfo(scanContext.getAppId());
                    buildId = VeracodeResultProcessor.getBuildIdByResponse(result);

                    CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.RUNNING, null, buildId);
                    String logMessage = "Product scan artifacts were uploaded to Veracode scanner for the application: "
                            + scanContext.getAppId();
                    log.info(new CallbackLog(scanContext.getJobId(), logMessage));
                } else {
                    String logMessage = "Product scan artifacts uploading was failed to Veracode scanner for the " +
                            "application : " + scanContext.getAppId();
                    log.error(new CallbackLog(scanContext.getJobId(), logMessage));

                    CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
                }
            } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
                String logMessage = "Product scan artifacts uploading was failed to Veracode scanner for the " +
                        "application : " + scanContext.getAppId() + " " + ErrorProcessingUtil.getFullErrorMessage(e);
                log.error(new CallbackLog(scanContext.getJobId(), logMessage));

                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
            }
        } else {
            String logMessage = "Current thread is interrupted for application : " + scanContext.getAppId();
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));
        }
        return isUploadSuccess;
    }

    /**
     * Filter the file list that matches with the pattern in a file.
     *
     * @param filePath Directory path that contains the jar list, which needs to check the matching patterns
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private void copyRequiredScanArtifact(String filePath) throws IOException, SAXException,
            ParserConfigurationException {
        File dir = new File(filePath);
        File[] files = dir.listFiles();
        File patternXmlFile = new File(JAR_FILTER_FILE);

        if (!Thread.currentThread().isInterrupted()) {
            try (InputStream input = VeracodeScannerConfiguration.class.getClassLoader()
                    .getResourceAsStream(JAR_FILTER_FILE);
                 OutputStream out = new FileOutputStream(patternXmlFile)) {
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
            } catch (IOException e) {
                throw new IOException(e);
            }
            NodeList nodeList = getScanArtifactPatternList(patternXmlFile);

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        for (int i = 0; i < nodeList.getLength(); i++) {
                            Node node = nodeList.item(i);
                            Element element = (Element) node;

                            checkFileNamePattern(element, file);
                        }
                    } else if (file.isDirectory()) {
                        copyRequiredScanArtifact(file.getAbsolutePath());
                    }
                }
            } else {
                log.warn("File list that needs to be archived cannot be null.");
            }
        } else {
            String logMessage = "Current thread is interrupted for application : " + scanContext.getAppId();
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));
        }
    }

    /**
     * Convert the given xmlFile to a XML Document.
     *
     * @param xmlFile file need to be converted
     * @return Converted Node list from the XML file
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private NodeList getScanArtifactPatternList(File xmlFile) throws ParserConfigurationException, SAXException,
            IOException {
        DocumentBuilder dBuilder;
        Document doc;
        DocumentBuilderFactory dbFactory = XMLUtil.getSecuredDocumentBuilderFactory();

        dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        return doc.getElementsByTagName("format");
    }

    /**
     * Check whether the file matches with the given prefix and suffix and copy to a specific location.
     *
     * @param element XML element contains the prefix and suffix values
     * @param file    needs to check for the pattern
     * @throws IOException
     */
    private void checkFileNamePattern(Element element, File file) throws IOException {
        String prefix = "";
        String suffix = "";

        if (element.getElementsByTagName(VeracodeScannerConstants.PREFIX).item(0) != null) {
            prefix = String.valueOf(element.getElementsByTagName(VeracodeScannerConstants.PREFIX).item(0)
                    .getChildNodes().item(0).getTextContent());
        }
        if (element.getElementsByTagName(VeracodeScannerConstants.SUFFIX).item(0) != null) {
            suffix = String.valueOf(element.getElementsByTagName(VeracodeScannerConstants.SUFFIX).item(0)
                    .getChildNodes().item(0).getTextContent());
        }
        if (file.getName().endsWith(suffix) && file.getName().startsWith(prefix)) {
            try {
                File destFile = new File(workingDirectory + File.separator + file.getName());
                Files.copy(file.getAbsoluteFile().toPath(), destFile.toPath());
            } catch (IOException e) {
                if ((e).toString().startsWith("java.nio.file.FileAlreadyExistsException")) {
                    String logMessage = "Error occured while copying file. \nWarning Message : " +
                            ErrorProcessingUtil.getFullErrorMessage(e);
                    log.warn(new CallbackLog(scanContext.getJobId(), logMessage));
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Begin pre scan in Veracode before submit to the Scan.
     *
     * @return whether stating the pre scan success
     */
    private boolean beginPreScan() {
        String result;
        String buildId = null;
        boolean isPreScanStarted = false;

        if (!Thread.currentThread().isInterrupted()) {
            try {
                UploadAPIWrapper uploadAPIWrapper = VeracodeAPIUtil.getUploadAPIWrapper();
                result = uploadAPIWrapper.beginPreScan(scanContext.getAppId(), null, "true", "true");
                isPreScanStarted = VeracodeResultProcessor.isOperationProceedWithoutError(result);

                if (isPreScanStarted) {
                    buildId = VeracodeResultProcessor.getBuildIdByResponse(result);
                    String logMessage = "Pre-Scan is started in Veracode scanner for the application: " +
                            scanContext.getAppId();
                    log.info(new CallbackLog(scanContext.getJobId(), logMessage));

                    CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.RUNNING, null, buildId);
                } else {
                    String logMessage = "Pre-Scan is failed in Veracode scanner for the application: " +
                            scanContext.getAppId();
                    log.error(new CallbackLog(scanContext.getJobId(), logMessage));

                    CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, buildId);
                }
            } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
                String logMessage = "Pre-Scan is failed in Veracode scanner for the application: "
                        + scanContext.getAppId() + " " + ErrorProcessingUtil.getFullErrorMessage(e);
                log.error(new CallbackLog(scanContext.getJobId(), logMessage));

                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, buildId);
            }
        } else {
            String logMessage = "Current thread is interrupted for application : " + scanContext.getAppId();
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));
        }
        return isPreScanStarted;
    }

    /**
     * Begin scan in Veracode.
     *
     * @return whether stating the pre scan success
     */
    private boolean beginScan() {
        String result;
        boolean isScanStarted = false;

        if (!Thread.currentThread().isInterrupted()) {
            try {
                UploadAPIWrapper uploadAPIWrapper = VeracodeAPIUtil.getUploadAPIWrapper();
                result = uploadAPIWrapper.beginScan(scanContext.getAppId(), "all", "true");
                isScanStarted = VeracodeResultProcessor.isOperationProceedWithoutError(result);

                if (isScanStarted) {
                    String logMessage = "Scan is started on Veracode for the application: " + scanContext.getAppId();
                    log.info(new CallbackLog(scanContext.getJobId(), logMessage));
                } else {
                    String logMessage = "Starting Scan failed in Veracode scanner for the application: " + scanContext
                            .getAppId();
                    log.error(new CallbackLog(scanContext.getJobId(), logMessage));

                    CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
                }
            } catch (IOException e) {
                String logMessage = "Starting scan failed in Veracode scanner for the application: "
                        + scanContext.getAppId() + " " + ErrorProcessingUtil.getFullErrorMessage(e);
                log.error(new CallbackLog(scanContext.getJobId(), logMessage));

                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
            }
        } else {
            String logMessage = "Current thread is interrupted for application : " + scanContext.getAppId();
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));
        }
        return isScanStarted;
    }

    /**
     * Wait and poll until the scan is completed.
     *
     * @return the file path of the output report.
     */
    private boolean getScanReport() {
        ScanStatus scanStatus;
        boolean isReportUploaded = false;

        if (log.isDebugEnabled()) {
            String logMessage = "Scan started in Veracode for the application: " + scanContext.getAppId();
            log.debug(new CallbackLog(scanContext.getJobId(), logMessage));
        }

        try {
            scanStatus = getScanStatus();
            while (!scanStatus.equals(ScanStatus.COMPLETED) && !Thread.currentThread().isInterrupted()) {
                if (scanStatus.equals(ScanStatus.ERROR)) {
                    break;
                }
                String logMessage = "Waiting for " + VeracodeScannerConfiguration.getInstance().getConfigProperty(
                        VeracodeScannerConstants.SCAN_RESULT_RETRY_MINS) + " mins until the scan is completed" +
                        " for the application: " + scanContext.getAppId();
                log.info(new CallbackLog(scanContext.getJobId(), logMessage));

                TimeUnit.MINUTES.sleep(Integer.parseInt(VeracodeScannerConfiguration.getInstance().getConfigProperty(
                        VeracodeScannerConstants.SCAN_RESULT_RETRY_MINS)));
                scanStatus = getScanStatus();

                logMessage = "Scan result status is : " + scanStatus + " for the application:"
                        + scanContext.getAppId();
                log.info(new CallbackLog(scanContext.getJobId(), logMessage));
            }

            if (scanStatus.equals(ScanStatus.COMPLETED)) {
                String logMessage = "Scan results are ready for the application: " + scanContext.getAppId();
                log.info(new CallbackLog(scanContext.getJobId(), logMessage));

                String reportPath = VeracodeScannerConfiguration.getInstance().getConfigProperty(
                        VeracodeScannerConstants.VERACODE_OUTPUT_FOLDER_PATH);

                String scanArtifact = scanContext.getArtifactLocation();
                boolean isReportDownloaded = getReports();
                if (isReportDownloaded) {
                    FileUtil.zipFiles(reportPath, reportPath + ScannerConstants.ZIP_FILE_EXTENSION);
                    if (uploadReportToFtp(scanArtifact, reportPath + ScannerConstants.ZIP_FILE_EXTENSION)) {
                        isReportUploaded = true;
                    }
                } else {
                    logMessage = "Downloading scan report is failed for the application: " + scanContext.getAppId();
                    log.error(new CallbackLog(scanContext.getJobId(), logMessage));

                    CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
                }
            }
        } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException |
                ArchiveException | ScannerException e) {
            String logMessage;
            if (e.getClass().isInstance(ScannerException.class)) {
                logMessage = "Extracting scan report zip is failed for the application: " + scanContext.getAppId()
                        + " " + ErrorProcessingUtil.getFullErrorMessage(e);
            } else {
                logMessage = "Downloading scan report is failed for the application: " + scanContext.getAppId()
                        + " " + ErrorProcessingUtil.getFullErrorMessage(e);
            }
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));

            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
        } catch (InterruptedException e) {
            String logMessage = "Current thread is interrupted for application : " + scanContext.getAppId();
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));
        }
        return isReportUploaded;
    }

    /**
     * Upload the scan report to FTP location.
     *
     * @param scanArtifact scanned artifact
     * @param reportPath   path that needs to upload
     * @return is uploading success
     */
    private boolean uploadReportToFtp(String scanArtifact, String reportPath) {
        boolean isReportUploaded = false;
        String scanReportFtpLocation = scanArtifact.substring(0, scanArtifact.lastIndexOf(File.separator));

        File reports = new File(reportPath);

        if (!Thread.currentThread().isInterrupted()) {
            try {
                FileUtil.uploadReport(scanReportFtpLocation, reports,
                        VeracodeScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_USERNAME),
                        (VeracodeScannerConfiguration.getInstance().getConfigProperty(ScannerConstants.FTP_PASSWORD))
                                .toCharArray(), VeracodeScannerConfiguration.getInstance().getConfigProperty(
                                ScannerConstants.FTP_HOST), Integer.parseInt(VeracodeScannerConfiguration.getInstance()
                                .getConfigProperty(ScannerConstants.FTP_PORT)));

                isReportUploaded = true;
                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.COMPLETED, scanReportFtpLocation +
                        File.separator + reports.getName(), null);

                String logMessage = "Scan report is uploaded to the FTP server for the application: " +
                        scanContext.getAppId();
                log.info(new CallbackLog(scanContext.getJobId(), logMessage));
            } catch (SftpException | JSchException e) {
                int retryInterval = Integer.parseInt(VeracodeScannerConfiguration.getInstance().getConfigProperty(
                        VeracodeScannerConstants.SCAN_REPORT_UPLOAD_RETRY_SECONDS));
                log.info("Report upload will retry after " + retryInterval + " seconds since that operation was failed"
                        + "due to FTP server issue. \n" + ErrorProcessingUtil.getFullErrorMessage(e));
                try {
                    TimeUnit.SECONDS.sleep(retryInterval);
                } catch (InterruptedException e1) {
                    log.error(e1);
                }
                uploadReportToFtp(scanArtifact, reportPath);
            } catch (ScannerException | IOException e) {
                log.error(new CallbackLog(scanContext.getJobId(), e.getMessage()));
                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, scanReportFtpLocation, null);
            }
        } else {
            String logMessage = "Current thread is interrupted for application : " + scanContext.getAppId();
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));
        }
        return isReportUploaded;
    }

    /**
     * Give the status of the scan of the current application.
     *
     * @return the Scan Status in the actual cloud Veracode scanner
     * @throws IOException                  when unable to retrieve scan status due to IO errors
     * @throws XPathExpressionException     when the given expression is malformed or wrong
     * @throws SAXException                 when unable to parse the XML to get status
     * @throws ParserConfigurationException when unable to create a document builder
     */
    public ScanStatus getScanStatus() throws IOException, XPathExpressionException, SAXException,
            ParserConfigurationException {
        UploadAPIWrapper uploadAPIWrapper = VeracodeAPIUtil.getUploadAPIWrapper();
        String result = uploadAPIWrapper.getBuildInfo(scanContext.getAppId());

        return VeracodeResultProcessor.getScanStatus(result);
    }

    /**
     * Download and return the status for the current product application.
     *
     * @return whether the report is downloaded
     */
    public boolean getReports() {
        boolean isReportPrinted = false;

        if (!Thread.currentThread().isInterrupted()) {
            try {
                ResultsAPIWrapper resultsAPIWrapper = VeracodeAPIUtil.getResultAPIWrapper();
                String buildId = getBuildIDByAppId(scanContext.getAppId());
                byte[] resultPdfDetailed = resultsAPIWrapper.detailedReportPdf(buildId);
                String resultXMLDetailed = resultsAPIWrapper.detailedReport(buildId);
                byte[] resultPdfSummary = resultsAPIWrapper.summaryReportPdf(buildId);
                String resultXMLSummary = resultsAPIWrapper.summaryReport(buildId);
                byte[] resultXMLThridParty = resultsAPIWrapper.thirdPartyReportPdf(buildId);

                String filePath = VeracodeScannerConfiguration.getInstance().getConfigProperty(
                        VeracodeScannerConstants.VERACODE_OUTPUT_FOLDER_PATH) + File.separator + scanContext.getAppId();
                FileUtil.saveReport(resultPdfDetailed, filePath + ScannerConstants.PDF_FILE_EXTENSION);
                FileUtil.saveReport(resultXMLDetailed.getBytes(StandardCharsets.UTF_8.name()), filePath +
                        ScannerConstants.XML_FILE_EXTENSION);
                FileUtil.saveReport(resultPdfSummary, filePath + VeracodeScannerConstants.SUMMARY +
                        ScannerConstants.PDF_FILE_EXTENSION);
                FileUtil.saveReport(resultXMLSummary.getBytes(StandardCharsets.UTF_8.name()), filePath +
                        VeracodeScannerConstants.SUMMARY + ScannerConstants.XML_FILE_EXTENSION);
                FileUtil.saveReport(resultXMLThridParty, filePath + VeracodeScannerConstants.THIRD_PARTY
                        + ScannerConstants.PDF_FILE_EXTENSION);
                isReportPrinted = true;

                String logMessage = "Scan reports are completed and downloaded to the location : " +
                        VeracodeScannerConfiguration.getInstance().getConfigProperty(VeracodeScannerConstants.
                                VERACODE_OUTPUT_FOLDER_PATH) + " for the application " + scanContext.getAppId();
                log.info(new CallbackLog(scanContext.getJobId(), logMessage));
            } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException |
                    ScannerException e) {
                String logMessage = "Error occured while downloading the sca reports for the application "
                        + scanContext.getAppId();
                log.error(new CallbackLog(scanContext.getJobId(), logMessage));
            }
        } else {
            String logMessage = "Current thread is interrupted for application : " + scanContext.getAppId();
            log.error(new CallbackLog(scanContext.getJobId(), logMessage));
        }
        return isReportPrinted;
    }

    /**
     * Get the last scan's unique Id of a given application.
     *
     * @param appId application id that is needed to upload
     * @return the buildId of the scan
     * @throws IOException                  when unable to retrieve scan status due to IO errors
     * @throws XPathExpressionException     when the given expression is malformed or wrong
     * @throws SAXException                 when unable to parse the XML to get status
     * @throws ParserConfigurationException when unable to create a document builder
     */
    private String getBuildIDByAppId(String appId) throws IOException, XPathExpressionException, SAXException,
            ParserConfigurationException {
        UploadAPIWrapper uploadAPIWrapper = VeracodeAPIUtil.getUploadAPIWrapper();
        String apiResult = uploadAPIWrapper.getBuildInfo(appId);
        return VeracodeResultProcessor.getBuildIdByResponse(apiResult);
    }
}
