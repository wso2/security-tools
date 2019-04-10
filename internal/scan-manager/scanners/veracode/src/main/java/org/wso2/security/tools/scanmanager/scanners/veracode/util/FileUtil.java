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
package org.wso2.security.tools.scanmanager.scanners.veracode.Util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.config.YAMLConfigurationReader;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility class for file handling.
 */
public class FileUtil {

    private static final Logger log = Logger.getLogger(FileUtil.class);

    private FileUtil() {
    }

    /**
     * Extract a zip file and returns name of the extracted folder.
     *
     * @param zipFilePath ZIP file path
     * @return Extracted folder name
     * @throws IOException
     */
    public static String extractZipFile(String zipFilePath) throws IOException {
        int buffer = 2048;
        File file = new File(zipFilePath);

        try (ZipFile zip = new ZipFile(file)) {
            String newPath = file.getParent();
            String fileName = file.getName();
            Enumeration zipFileEntries = zip.entries();

            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                File destFile = new File(newPath, currentEntry);
                File destinationParent = destFile.getParentFile();

                if (destinationParent.mkdirs()) {
                    byte[] data = new byte[buffer];
                    if (!entry.isDirectory()) {
                        int currentByte;
                        try (BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                             FileOutputStream fos = new FileOutputStream(destFile);
                             BufferedOutputStream dest = new BufferedOutputStream(fos, buffer)) {
                            while ((currentByte = is.read(data, 0, buffer)) != -1) {
                                dest.write(data, 0, currentByte);
                            }
                        }
                    }
                }
            }
            return file.getParent() + File.separator + fileName.substring(0, fileName.length() - 4);
        }
    }

    /**
     * Zip the given folder.
     *
     * @param source      source directory to be zipped
     * @param destination file of the created zip
     * @throws ArchiveException
     * @throws IOException
     */
    public static void zipFiles(String source, String destination) throws ArchiveException, IOException {
        try (ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(
                ArchiveStreamFactory.ZIP, new FileOutputStream(destination))) {
            File newFile = new File(source);
            File[] fileList = newFile.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    ZipArchiveEntry entry = new ZipArchiveEntry(file.getPath());
                    archive.putArchiveEntry(entry);
                    try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
                        IOUtils.copy(input, archive);
                    } finally {
                        archive.closeArchiveEntry();
                    }
                }
            } else {
                log.warn("File list that needs to be archived cannot be null. ");
            }
            archive.finish();
        }
    }

    /**
     * Download the product pack from the FTP location.
     *
     * @param productPathInFTP path to download pack in the FTP location.
     * @param productName      file name to download.
     * @throws ScannerException
     */
    public static void downloadProduct(String productPathInFTP, String productName, File file) throws
            IOException, SftpException, JSchException {
        ChannelSftp sftp = goToFTPProductLocation(productPathInFTP);

        downloadFromFTP(sftp.get(productName), file);
        sftp.disconnect();
    }

    /**
     * Go to the FTP location where the product pack is.
     *
     * @param productPathInFTP path to the product pack
     * @return SFTP channel to access the FTP location
     * @throws JSchException
     * @throws SftpException
     */
    private static ChannelSftp goToFTPProductLocation(String productPathInFTP) throws JSchException, SftpException {
        JSch jsch = new JSch();
        Session session;
        Channel channel;
        ChannelSftp sftp;

        //get the FTP configuration to build the connection with FTP server.
        String ftpHost = YAMLConfigurationReader.getInstance().getConfigProperty(ScannerConstants.FTP_HOST);
        String ftpUsername = YAMLConfigurationReader.getInstance().getConfigProperty(ScannerConstants.FTP_USERNAME);
        char[] ftpPassword = (YAMLConfigurationReader.getInstance().getConfigProperty(ScannerConstants.FTP_PASSWORD))
                .toCharArray();
        int ftpPort = Integer.parseInt(YAMLConfigurationReader.getInstance().getConfigProperty(ScannerConstants
                .FTP_PORT));

        session = jsch.getSession(ftpUsername, ftpHost, ftpPort);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(String.valueOf(ftpPassword));
        session.connect();

        channel = session.openChannel(ScannerConstants.SFTP);
        sftp = (ChannelSftp) channel;
        sftp.connect();
        sftp.cd(productPathInFTP);

        cleanFTPPassword(ftpPassword);

        return sftp;
    }

    /**
     * Download the product from FTP to local location.
     *
     * @param input InputStream to the product pack file
     * @param file  product file
     * @throws IOException
     */
    private static void downloadFromFTP(InputStream input, File file) throws IOException {
        byte[] buffer = new byte[1024];
        int readCount;

        try (BufferedInputStream bis = new BufferedInputStream(input); OutputStream os = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(os)) {
            while ((readCount = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, readCount);
            }
        }
    }

    /**
     * Clean the FTP password from the variable.
     *
     * @param ftpPassword
     */
    private static void cleanFTPPassword(char[] ftpPassword) {
        for (int i = 0; i < ftpPassword.length; i++) {
            ftpPassword[i] = '\0';
        }
    }

    /**
     * Upload the scan report to the FTP location.
     *
     * @param ftpReportUploadPath path to upload the scan report in the FTP location.
     * @param fileToUpload        scan report file to upload.
     * @throws SftpException
     * @throws JSchException
     * @throws FileNotFoundException
     * @throws ScannerException
     */
    public static void uploadReport(String ftpReportUploadPath, File fileToUpload) throws SftpException, JSchException,
            FileNotFoundException, ScannerException {
        ChannelSftp sftp = goToFTPProductLocation(ftpReportUploadPath);
        uploadFileToFTP(sftp, fileToUpload);
    }

    /**
     * Upload the scan report to from container to the FTP location.
     *
     * @param sftp         ChannelSFTP to connect t the FTP.
     * @param fileToUpload scan report file that upload
     * @throws FileNotFoundException
     * @throws SftpException
     */
    private static void uploadFileToFTP(ChannelSftp sftp, File fileToUpload) throws FileNotFoundException,
            SftpException, ScannerException {
        if (fileToUpload != null) {
            File file = new File(String.valueOf(fileToUpload));

            sftp.put(new FileInputStream(file), file.getName());
        } else {
            throw new ScannerException("File that going to upload cannot be null. ");
        }
        sftp.disconnect();
    }

    /**
     * Create DocumentBuilderFactory with the XXE and XEE prevention measurements.
     *
     * @return DocumentBuilderFactory instance
     */
    public static DocumentBuilderFactory getSecuredDocumentBuilderFactory() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        int entityExpansionLimit = 0;
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or "
                    + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE +
                    " or secure-processing.");
        }
        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(entityExpansionLimit);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);
        return dbf;
    }

    /**
     * Print the pdf report according to the Veracode response result report.
     *
     * @param bytesResult Veracode response result
     * @return whether report is successfully printed
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static boolean createReport(byte[] bytesResult, String filePath) throws FileNotFoundException,
            UnsupportedEncodingException {
        byte[] pdfResult;
        pdfResult = bytesResult;
        boolean printStatus = false;

        if (pdfResult != null) {
            if (!(filePath.isEmpty())) {
                try (PrintStream writer = new PrintStream(new FileOutputStream(filePath), true,
                        StandardCharsets.UTF_8.name())) {
                    writer.write(pdfResult, 0, pdfResult.length);
                    printStatus = true;
                }
            } else {
                log.error("Resulted in the following error : Output filepath is missing.");
            }
        } else {
            log.error("Unable to retrieve data from byte stream. ");
        }
        return printStatus;
    }
}
