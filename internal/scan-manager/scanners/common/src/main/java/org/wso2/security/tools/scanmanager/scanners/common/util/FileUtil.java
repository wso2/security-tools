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
package org.wso2.security.tools.scanmanager.scanners.common.util;

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
import org.wso2.security.tools.scanmanager.scanners.common.ScannerConstants;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    /**
     * Extract a zip file and returns name of the extracted folder.
     *
     * @param sourceFile  ZIP file path to be extracted
     * @param destination folder path of the destination
     * @return Extracted archive file
     * @throws IOException when the required source or destination files are not found
     */
    public static String extractArchive(File sourceFile, String destination) throws IOException {
        BufferedInputStream inputStream = null;
        InputStream zipInputStream = null;
        FileOutputStream outputStream = null;
        String archive = null;

        try (ZipFile zip = new ZipFile(sourceFile)) {
            Enumeration zipFileEntries = zip.entries();
            int index = 0;

            // Process each entry
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                if (index == 0) {
                    archive = destination + File.separator + currentEntry.substring(0, currentEntry.indexOf("/"));
                    --index;
                }

                // Handing for the Zip Slip Vulnerability
                File destinationFile = new File(destination, currentEntry);
                String canonicalizedDestinationFilePath = destinationFile.getCanonicalPath();
                if (canonicalizedDestinationFilePath.startsWith(new File(destination).getCanonicalPath())) {
                    // if a valid zip file uploaded
                } else {
                    String errorMessage = "Attempt to upload invalid zip archive with file at " + currentEntry +
                            ". File path is outside target directory";
                    log.error(errorMessage);
                }

                if (entry.isDirectory()) {
                    // if the entry is a directory
                } else {
                    zipInputStream = zip.getInputStream(entry);
                    inputStream = new BufferedInputStream(zipInputStream);
                    if (destinationFile.getParentFile().mkdirs()) {
                        outputStream = new FileOutputStream(destinationFile);
                        IOUtils.copy(inputStream, outputStream);
                    }
                }
            }
            return archive;
        } finally {
            IOUtils.closeQuietly(zipInputStream);
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * Zip the given folder.
     *
     * @param source      source directory to be zipped
     * @param destination file of the created zip
     * @throws ArchiveException when unable to create the archive stream for the output file stream
     * @throws IOException      when the required file is not found or fails to create the file streams
     * @throws ScannerException when the file list that need to be archived is null
     */
    public static void zipFiles(String source, String destination) throws ArchiveException, IOException,
            ScannerException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(destination);
             ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(
                     ArchiveStreamFactory.ZIP, fileOutputStream)) {
            File newFile = new File(source);
            File[] fileList = newFile.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    ZipArchiveEntry entry = new ZipArchiveEntry(file.getName());
                    archive.putArchiveEntry(entry);
                    try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
                        IOUtils.copy(input, archive);
                    } finally {
                        archive.closeArchiveEntry();
                    }
                }
            } else {
                throw new ScannerException("File list that needs to be archived cannot be null.");
            }
            archive.finish();
        }
    }

    /**
     * Download a file from a FTP location to a local location.
     *
     * @param filePathInFtp path to download file in the FTP location
     * @param fileName      file to download
     * @param outputFile    output file
     * @param ftpUsername   username of the ftp location where file is located
     * @param ftpPassword   password of the ftp location where file is located
     * @param ftpHost       host of the ftp location where file is located
     * @param ftpPort       port of the ftp location where file is located
     * @throws IOException   when unable to download the file from the FTP server due fails of streams
     * @throws JSchException when unable to create the session for connecting the FTP server
     * @throws SftpException when unable to connect to the FTP server
     */
    public static void downloadFromFtp(String filePathInFtp, String fileName, File outputFile, String ftpUsername
            , char[] ftpPassword, String ftpHost, int ftpPort) throws JSchException, SftpException, IOException {
        ChannelSftp sftp = openFtpLocation(filePathInFtp, ftpUsername, ftpPassword, ftpHost, ftpPort);

        downloadFromFtp(sftp.get(fileName), outputFile);
        sftp.disconnect();
    }

    /**
     * Upload the scan report to the FTP location.
     *
     * @param ftpReportUploadPath path to upload the scan report in the FTP location
     * @param fileToUpload        scan report file to upload
     * @param ftpUsername         username of the ftp location where file is located
     * @param ftpPassword         password of the ftp location where file is located
     * @param ftpHost             host of the ftp location where file is located
     * @param ftpPort             port of the ftp location where file is located
     * @throws JSchException    when unable to create the session for connecting the FTP server
     * @throws SftpException    when unable to connect to the FTP server or unable to copy the file to the ftp location
     * @throws IOException      when unable to crate stream using the upload file
     * @throws ScannerException when the upload file is null
     */
    public static void uploadReport(String ftpReportUploadPath, File fileToUpload, String ftpUsername, char[]
            ftpPassword, String ftpHost, int ftpPort) throws SftpException, JSchException, IOException,
            ScannerException {
        ChannelSftp sftp = openFtpLocation(ftpReportUploadPath, ftpUsername, ftpPassword, ftpHost, ftpPort);
        uploadFileToFtp(sftp, fileToUpload);
    }

    /**
     * Create the PDF report from the given byte stream.
     *
     * @param bytesResult byte stream of the report
     * @param filePath    output file to be saved
     * @return whether report is successfully saved
     * @throws FileNotFoundException        when unable to find a file in the given file path
     * @throws UnsupportedEncodingException when unable to create the print stream due to encoding type
     * @throws ScannerException             when unable to find the file path or unable to retrieve data from stream
     */
    public static boolean saveReport(byte[] bytesResult, String filePath) throws FileNotFoundException,
            UnsupportedEncodingException, ScannerException {

        if (bytesResult != null) {
            if (filePath.isEmpty()) {
                throw new ScannerException("Output file path is missing.");
            } else {
                try (PrintStream writer = new PrintStream(new FileOutputStream(filePath), true, StandardCharsets
                        .UTF_8.name())) {
                    writer.write(bytesResult, 0, bytesResult.length);
                    return true;
                }
            }
        } else {
            throw new ScannerException("Unable to retrieve data from byte stream.");
        }
    }

    /**
     * Open the FTP location of the file and return the created channel.
     *
     * @param filePathInFtp path to the file
     * @param ftpUsername   username of the ftp location where file is located
     * @param ftpPassword   password of the ftp location where file is located
     * @param ftpHost       host of the ftp location where file is located
     * @param ftpPort       port of the ftp location where file is located
     * @return SFTP channel to access the FTP location
     * @throws JSchException when unable to create the session for connecting the FTP server
     * @throws SftpException when unable to connect to the FTP server
     */
    private static ChannelSftp openFtpLocation(String filePathInFtp, String ftpUsername, char[] ftpPassword,
                                               String ftpHost, int ftpPort) throws JSchException, SftpException {
        JSch jsch = new JSch();
        Session session;
        Channel channel;
        ChannelSftp sftp;

        session = jsch.getSession(ftpUsername, ftpHost, ftpPort);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(String.valueOf(ftpPassword));
        session.connect();

        channel = session.openChannel(ScannerConstants.SFTP);
        sftp = (ChannelSftp) channel;
        sftp.connect();
        sftp.cd(filePathInFtp);

        cleanPassword(ftpPassword);

        return sftp;
    }

    /**
     * Download a file from a FTP location to a local location.
     *
     * @param input      InputStream to the ftp file
     * @param outputFile output will be written to this file
     * @throws IOException when unable to download the file from the FTP server
     */
    private static void downloadFromFtp(InputStream input, File outputFile) throws IOException {
        byte[] buffer = new byte[1024];
        int readCount;

        try (BufferedInputStream sftpFileInputStream = new BufferedInputStream(input);
             BufferedOutputStream downloadOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            while ((readCount = sftpFileInputStream.read(buffer)) > 0) {
                downloadOutputStream.write(buffer, 0, readCount);
            }
        }
    }

    /**
     * Clean the FTP password from the variable.
     *
     * @param password password that needs to be cleared
     */
    public static void cleanPassword(char[] password) {
        for (int i = 0; i < password.length; i++) {
            password[i] = '\0';
        }
    }

    /**
     * Upload the scan report to from container to the FTP location.
     *
     * @param sftp         Channel to connect to the FTP.
     * @param fileToUpload scan report file that upload
     * @throws IOException      when unable to crate stream using the upload file
     * @throws SftpException    when unable to copy the file to the ftp location
     * @throws ScannerException when the upload file is null
     */
    private static void uploadFileToFtp(ChannelSftp sftp, File fileToUpload) throws IOException, SftpException,
            ScannerException {
        if (fileToUpload != null) {
            File file = new File(String.valueOf(fileToUpload));
            try (InputStream inputStream = new FileInputStream(file)) {
                sftp.put(inputStream, file.getName());
            }
        } else {
            throw new ScannerException("Upload file cannot be null.");
        }
        sftp.disconnect();
    }
}
