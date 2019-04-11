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
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.scanmanager.common.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.config.YAMLConfigurationReader;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.veracode.Util.FileUtil;
import org.wso2.security.tools.scanmanager.scanners.veracode.VeracodeScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.veracode.config.VeracodeScannerConfiguration;
import org.wso2.security.tools.scanmanager.scanners.veracode.model.ScanContext;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

/**
 * Represents the scan handling tasks.
 */
public class ScanTask implements Runnable {

    private static final Logger log = Logger.getLogger(ScanTask.class);
    // Veracode upload API wrapper.
    private UploadAPIWrapper uploadAPIWrapper;
    // Veracode results API wrapper.
    private ResultsAPIWrapper resultsAPIWrapper;
    // Scan request coming to the scan micro-service API.
    private ScanContext scanContext;
    // Directory used for preparation of the scan artifacts.
    private File workingDirectory = null;

    public ScanTask(UploadAPIWrapper uploadAPIWrapper, ScanContext scanContext,
                    ResultsAPIWrapper resultsAPIWrapper) {
        String logMessage;
        if (log.isDebugEnabled()) {
            logMessage = "Upload Artifact Handler thread is being initialized for the application:"
                    + scanContext.getAppId();
            log.debug(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.DEBUG);
        }

        this.uploadAPIWrapper = uploadAPIWrapper;
        this.scanContext = scanContext;
        this.resultsAPIWrapper = resultsAPIWrapper;
    }

    @Override
    public void run() {
        String logMessage;
        if (isAnyScansRunning()) {
            // If another scans is running on the actual Veracode cloud scanner, then the start scan request
            // would be failed and Scan status is updated as 'ERROR'.
            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null,
                    null);

            logMessage = "Currently another scan is running on the application id  : " + scanContext
                    .getAppId() + ". So please check this scan before proceed " + "with another scan.";
            log.error(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);

            Thread.currentThread().interrupt();
        }

        if (handleUploadingTask()) {
            if (handleResultProcessTask()) {
                logMessage = "Start scan process is successfully completed for the application: "
                        + scanContext.getAppId();
                log.info(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);
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
                    logMessage = "Artifact upload is failed. Terminating scan for app: " + scanContext.getAppId();
                }
            } else {
                logMessage = "Creating artifact zip is failed. Terminating scan for app: " + scanContext.getAppId();
            }
        } else {
            logMessage = "Cleaning previous scans is failed. Terminating scan for app: " + scanContext.getAppId();
        }
        if (!isUploadSuccess) {
            log.error(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);
            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
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
                    logMessage = "Uploading scan report is failed. Terminating scan for app:" + scanContext.getAppId();
                }
            } else {
                logMessage = "Scan starting is failed. Terminating scan for app: " + scanContext.getAppId();
            }
        } else {
            logMessage = "Pre-Scan is failed. Terminating scan for app: " + scanContext.getAppId();
        }
        if (!isResultsUploaded) {
            log.error(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);

            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
        }
        return isResultsUploaded;
    }

    /**
     * Check any scans is running on the Verocode for the current application.
     *
     * @return are there any scans currently running state
     */
    private boolean isAnyScansRunning() {
        boolean isScanRunning = false;
        String result;
        ScanStatus currentScanStatus;
        String logMessage;
        try {
            result = uploadAPIWrapper.getBuildInfo(scanContext.getAppId());
            currentScanStatus = VeracodeResultProcessor.getScanStatus(result);
            if (ScanStatus.RUNNING.equals(currentScanStatus)) {
                isScanRunning = true;
            }
        } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
            logMessage = "Error occured while retrieving the scan status for application : " + scanContext
                    .getAppId() + VeracodeResultProcessor.getFullErrorMessage(e);
            log.error(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);

            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
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
        String logMessage;

        try {
            result = uploadAPIWrapper.deleteBuild(scanContext.getAppId());

            if (log.isDebugEnabled()) {
                logMessage = "Deleted the last scan of the application :" + scanContext.getAppId();
                log.debug(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.DEBUG);
            }

            isScannerCleaned = VeracodeResultProcessor.isOperationProceedWithoutError(result);
        } catch (IOException e) {
            logMessage = "Error occured while deleting the scan status for application : " + scanContext
                    .getAppId() + VeracodeResultProcessor.getFullErrorMessage(e);
            log.error(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);

            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
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
        String logMessage;

        String scanArtifact = scanContext.getArtifactLocation();
        String productPackName = scanArtifact.substring(scanArtifact.lastIndexOf(File.separator) + 1,
                scanArtifact.length());
        String productPath = scanArtifact.substring(0, scanArtifact.lastIndexOf(File.separator));

        File productFile = new File(YAMLConfigurationReader.getInstance().getConfigProperty(ScannerConstants
                .DEFAULT_FTP_PRODUCT_PATH) + File.separator + productPackName);

        try {
            logMessage = "Product pack is downloading for the application: " + scanContext.getAppId();
            log.info(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);

            FileUtil.downloadProduct(productPath, productPackName, productFile);
            
            logMessage = "Product downloading completed for the application: " + scanContext.getAppId() + " into "
                    + productFile;
            log.info(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);

            extractedFilePath = FileUtil.extractZipFile(productFile.getAbsolutePath());
            workingDirectory = new File(extractedFilePath + VeracodeScannerConstants.WORK_DIRECTORY_SUFIX);

            if (workingDirectory.mkdirs()) {
                logMessage = "Filtering the artifacts for the scan for the application: " + scanContext.getAppId() +
                        " into " + workingDirectory;
                log.info(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);

                copyRequiredScanArtifact(extractedFilePath);
                FileUtil.zipFiles(workingDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath()
                        + ScannerConstants.ZIP_FILE_EXTENSION);
                isZipCreated = true;

                logMessage = "Created the zip artifact for the scan for the application: " + scanContext.getAppId() +
                        " as " + workingDirectory + ScannerConstants.ZIP_FILE_EXTENSION;
                log.info(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);
            } else {
                logMessage = "Error occured while creating the working directory for application : " + scanContext
                        .getAppId();
                log.error(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);

                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
            }
        } catch (IOException | JSchException | SftpException | SAXException |
                ParserConfigurationException | ArchiveException e) {
            logMessage = "Error occured while creating the scan zip artifact for application : " + scanContext
                    .getAppId() + VeracodeResultProcessor.getFullErrorMessage(e);
            log.error(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);

            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
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
        String logMessage;

        try {
            String result = uploadAPIWrapper.uploadFile(scanContext.getAppId(), workingDirectory
                    + ScannerConstants.ZIP_FILE_EXTENSION);
            isUploadSuccess = VeracodeResultProcessor.isOperationProceedWithoutError(result);
            if (isUploadSuccess) {
                result = uploadAPIWrapper.getBuildInfo(scanContext.getAppId());
                buildId = VeracodeResultProcessor.getBuildIdByResponse(result);

                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.RUNNING, null, buildId);
                logMessage = "Product scan artifacts were uploaded to Veracode scanner for the application: "
                        + scanContext.getAppId();
                log.info(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);
            } else {
                logMessage = "Product scan artifacts uploading was failed to Veracode scanner for the " +
                        "application : " + scanContext.getAppId();
                log.error(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);

                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
            }
        } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
            logMessage = "Product scan artifacts uploading was failed to Veracode scanner for the " +
                    "application : " + scanContext.getAppId() + VeracodeResultProcessor.getFullErrorMessage(e);
            log.error(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);

            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
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
        File patternXmlFile = new File(YAMLConfigurationReader.getInstance().getConfigProperty(VeracodeScannerConstants.
                JAR_FILTER_PATTERN_FILE_PATH));
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
            log.warn("File list that needs to be archived cannot be null. ");
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
        DocumentBuilderFactory dbFactory = FileUtil.getSecuredDocumentBuilderFactory();

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
        String logMessage;

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
                    logMessage = "Error occured while copying file. \nWarning Message : " + VeracodeResultProcessor
                            .getFullErrorMessage(e);
                    log.warn(logMessage);
                    CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.WARN);
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
        boolean isPreScanStarted = false;
        String logMessage;
        String buildId = null;

        try {
            result = uploadAPIWrapper.beginPreScan(scanContext.getAppId(), null, "true", "true");
            isPreScanStarted = VeracodeResultProcessor.isOperationProceedWithoutError(result);

            if (isPreScanStarted) {
                buildId = VeracodeResultProcessor.getBuildIdByResponse(result);
                logMessage = "Pre-Scan is started in Veracode scanner for the application: " + scanContext.getAppId();
                log.info(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);

                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.RUNNING, null, buildId);
            } else {
                logMessage = "Pre-Scan is failed in Veracode scanner for the application: " + scanContext.getAppId();
                log.error(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);

                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, buildId);
            }
        } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
            logMessage = "Pre-Scan is failed in Veracode scanner for the application: "
                    + scanContext.getAppId() + VeracodeResultProcessor.getFullErrorMessage(e);
            log.error(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);

            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, buildId);
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
        String logMessage;

        try {
            result = uploadAPIWrapper.beginScan(scanContext.getAppId(), "all", "true");
            isScanStarted = VeracodeResultProcessor.isOperationProceedWithoutError(result);

            if (isScanStarted) {
                logMessage = "Scan is started on the Veracode for the application: " + scanContext.getAppId();
                log.info(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);
            } else {
                logMessage = "Starting Scan failed in Veracode scanner for the application: " + scanContext
                        .getAppId();
                log.error(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);
                CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
            }
        } catch (IOException e) {
            logMessage = "Starting scan failed in Veracode scanner for the application: "
                    + scanContext.getAppId() + VeracodeResultProcessor.getFullErrorMessage(e);
            log.error(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);
            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
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
        String logMessage;

        if (log.isDebugEnabled()) {
            logMessage = "Scan started in Veracode for the application: " + scanContext.getAppId();
            log.debug(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.DEBUG);
        }

        try {
            scanStatus = getScanStatus();
            while (!scanStatus.equals(ScanStatus.COMPLETED)) {
                if (scanStatus.equals(ScanStatus.ERROR)) {
                    break;
                }
                logMessage = "Waiting for " + YAMLConfigurationReader.getInstance().getConfigProperty(
                        VeracodeScannerConstants.SCAN_RESULT_RETRY_MINS) + " mins until the scan is completed" +
                        " for the application: " + scanContext.getAppId();
                log.info(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);

                TimeUnit.MINUTES.sleep(Integer.parseInt(YAMLConfigurationReader.getInstance().getConfigProperty(
                        VeracodeScannerConstants.SCAN_RESULT_RETRY_MINS)));
                scanStatus = getScanStatus();

                logMessage = "Scan result status is : " + scanStatus + " for the application:"
                        + scanContext.getAppId();
                log.info(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);
            }

            if (scanStatus.equals(ScanStatus.COMPLETED)) {
                logMessage = "Scan results are ready for the application: " + scanContext.getAppId();
                log.info(logMessage);
                CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);

                String reportPath = VeracodeScannerConfiguration.getInstance().getOutputFolderPath() +
                        ScannerConstants.ZIP_FILE_EXTENSION;

                String scanArtifact = scanContext.getArtifactLocation();
                boolean isReportDownloaded = getReports();
                if (isReportDownloaded) {
                    FileUtil.zipFiles(VeracodeScannerConfiguration.getInstance().getOutputFolderPath(), reportPath);
                    if (uploadReportToFTP(scanArtifact, reportPath)) {
                        isReportUploaded = true;
                    }
                } else {
                    logMessage = "Downloading scan report is failed for the application: " + scanContext.getAppId();
                    log.error(logMessage);
                    CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);
                    CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
                }
            }
        } catch (InterruptedException | XPathExpressionException | ParserConfigurationException
                | SAXException | IOException | ArchiveException e) {
            logMessage = "Downloading scan report is failed for the application: " + scanContext.getAppId()
                    + VeracodeResultProcessor.getFullErrorMessage(e);
            log.error(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);
            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, null, null);
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
    private boolean uploadReportToFTP(String scanArtifact, String reportPath) {
        boolean isReportUploaded = false;
        String logMessage;
        String scanReportFTPLocation = scanArtifact.substring(0, scanArtifact.lastIndexOf(File.separator));
        try {
            FileUtil.uploadReport(scanReportFTPLocation, new File(reportPath));
            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.COMPLETED, scanReportFTPLocation, null);
            isReportUploaded = true;
            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.COMPLETED, scanReportFTPLocation, null);

            logMessage = "Scan report is uploaded to the FTP server for the application: " + scanContext.getAppId();
            log.info(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);
        } catch (SftpException | JSchException e) {
            int retryInterval = Integer.parseInt(YAMLConfigurationReader.getInstance().getConfigProperty(
                    VeracodeScannerConstants.SCAN_REPORT_UPLOAD_RETRY_SECONDS));
            log.info("Report upload will retry after " + retryInterval + " seconds since that operation was failed " +
                    "due to FTP server issue." + VeracodeResultProcessor.getFullErrorMessage(e));
            try {
                TimeUnit.SECONDS.sleep(retryInterval);
            } catch (InterruptedException e1) {
                log.error(e1);
            }
            uploadReportToFTP(scanArtifact, reportPath);
        } catch (FileNotFoundException | ScannerException e) {
            log.error(e);
            CallbackUtil.persistScanLog(scanContext.getJobId(), e.getMessage(), ScannerConstants.ERROR);
            CallbackUtil.updateScanStatus(scanContext.getJobId(), ScanStatus.ERROR, scanReportFTPLocation, null);
        }
        return isReportUploaded;
    }

    /**
     * Give the status of the scan of the current application.
     *
     * @return the Scan Status in the actual cloud Veracode scanner
     * @throws IOException
     * @throws XPathExpressionException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public ScanStatus getScanStatus() throws IOException, XPathExpressionException, SAXException,
            ParserConfigurationException {
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
        String logMessage;

        try {
            String buildId = getBuildIDByAppId(scanContext.getAppId());
            byte[] resultPdfDetailed = resultsAPIWrapper.detailedReportPdf(buildId);
            String resultXMLDetailed = resultsAPIWrapper.detailedReport(buildId);
            byte[] resultPdfSummary = resultsAPIWrapper.summaryReportPdf(buildId);
            String resultXMLSummary = resultsAPIWrapper.summaryReport(buildId);
            byte[] resultXMLThridParty = resultsAPIWrapper.thirdPartyReportPdf(buildId);

            String filePath = VeracodeScannerConfiguration.getInstance().getOutputFolderPath() + File.separator
                    + scanContext.getAppId();
            FileUtil.createReport(resultPdfDetailed, filePath + ScannerConstants.PDF_FILE_EXTENSION);
            FileUtil.createReport(resultXMLDetailed.getBytes(StandardCharsets.UTF_8.name()), filePath +
                    VeracodeScannerConstants.SUMMARY + ScannerConstants.XML_FILE_EXTENSION);
            FileUtil.createReport(resultPdfSummary, filePath + ScannerConstants.PDF_FILE_EXTENSION);
            FileUtil.createReport(resultXMLSummary.getBytes(StandardCharsets.UTF_8.name()), filePath +
                    VeracodeScannerConstants.SUMMARY + ScannerConstants.XML_FILE_EXTENSION);
            FileUtil.createReport(resultXMLThridParty, filePath + VeracodeScannerConstants.THIRD_PARTY
                    + ScannerConstants.PDF_FILE_EXTENSION);
            isReportPrinted = true;

            logMessage = "Scan reports are completed and downloaded to the location : " + VeracodeScannerConfiguration
                    .getInstance().getOutputFolderPath() + " for the application " + scanContext.getAppId();
            log.info(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.INFO);
        } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
            logMessage = "Error occured while downloading the sca reports for the application "
                    + scanContext.getAppId();
            log.error(logMessage);
            CallbackUtil.persistScanLog(scanContext.getJobId(), logMessage, ScannerConstants.ERROR);
        }
        return isReportPrinted;
    }

    /**
     * Get the last scan's unique Id of a given application.
     *
     * @param appId application id that is needed to upload
     * @return the buildId of the scan
     * @throws IOException
     * @throws XPathExpressionException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private String getBuildIDByAppId(String appId) throws IOException, XPathExpressionException, SAXException,
            ParserConfigurationException {
        String apiResult = uploadAPIWrapper.getBuildInfo(appId);
        return VeracodeResultProcessor.getBuildIdByResponse(apiResult);
    }
}
