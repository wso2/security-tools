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
 *
 *
 */

package org.wso2.security.tools.veracode.scanner.utils;

import com.jcraft.jsch.*;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.veracode.scanner.ScannerConstants;
import org.wso2.security.tools.veracode.scanner.config.ConfigurationReader;
import org.wso2.security.tools.veracode.scanner.exception.ScannerException;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility class for file handling.
 */
public class FileUtil {

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
        ZipFile zip = new ZipFile(file);

        String newPath = file.getParent();
        String fileName = file.getName();

        Enumeration zipFileEntries = zip.entries();

        while (zipFileEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(newPath, currentEntry);
            File destinationParent = destFile.getParentFile();

            destinationParent.mkdirs();

            byte[] data = new byte[buffer];

            if (!entry.isDirectory()) {
                int currentByte;

                try (BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                     FileOutputStream fos = new FileOutputStream(destFile);
                     BufferedOutputStream dest = new BufferedOutputStream(fos, buffer)) {

                    while ((currentByte = is.read(data, 0, buffer)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                } catch (IOException e) {
                    throw e;
                }
            }
        }
        return file.getParent() + File.separator + fileName.substring(0, fileName.length() - 4);
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
        OutputStream archiveStream = new FileOutputStream(destination);
        ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(
                ArchiveStreamFactory.ZIP, archiveStream);
        File newFile = new File(source);
        File[] fileList = newFile.listFiles();

        for (File file : fileList) {
            ZipArchiveEntry entry = new ZipArchiveEntry(file.getPath());
            archive.putArchiveEntry(entry);

            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
            IOUtils.copy(input, archive);

            input.close();
            archive.closeArchiveEntry();
        }

        archive.finish();
        archiveStream.close();
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
        String ftpHost = ConfigurationReader.getConfigProperty(ScannerConstants.FTP_HOST);
        String ftpUsername = ConfigurationReader.getConfigProperty(ScannerConstants.FTP_USERNAME);
        char[] ftpPassword = (ConfigurationReader.getConfigProperty(ScannerConstants.FTP_PASSWORD)).toCharArray();
        int ftpPort = Integer.parseInt(ConfigurationReader.getConfigProperty(ScannerConstants.FTP_PORT));

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
     * @param file product file
     * @throws IOException
     */
    private static void downloadFromFTP(InputStream input, File file) throws IOException {
        byte[] buffer = new byte[1024];
        int readCount;

        try (BufferedInputStream bis = new BufferedInputStream(input);
             OutputStream os = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(os)) {

            while ((readCount = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, readCount);
            }
        } catch (IOException e) {
            throw e;
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
     */
    public static void uploadReport(String ftpReportUploadPath, File fileToUpload) throws SftpException,
            JSchException, FileNotFoundException {
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
            SftpException {

        File file = new File(String.valueOf(fileToUpload));
        sftp.put(new FileInputStream(file), file.getName());
    }

}
