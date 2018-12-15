/*
 *  Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanner.handler;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.Session;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.scanner.config.ConfigurationReader;
import org.wso2.security.tools.scanner.exception.ScannerException;
import org.wso2.security.tools.scanner.utils.ScannerConstants;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for file handling.
 */
public class FileHandler {

    private FileHandler() {
    }

    /**
     * Extract a zip file and returns name of the extracted folder.
     *
     * @param zipFilePath ZIP file path
     * @return Extracted folder name
     * @throws ScannerException If an error occurs while I/O operations
     */
    public static String extractZipFile(String zipFilePath) throws ScannerException {
        int buffer = 2048;

        File file = new File(zipFilePath);
        ZipFile zip;
        try {
            zip = new ZipFile(file);
        } catch (IOException e) {
            throw new ScannerException("Error occured while creating new file to extract the zip file. ", e);
        }

        String newPath = file.getParent();
        String fileName = file.getName();

        Enumeration zipFileEntries = zip.entries();

        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(newPath, currentEntry);
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
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
                    throw new ScannerException("Error occured while extracting zip file in the container", e);
                }
            }
        }
        return fileName.substring(0, fileName.length() - 4);

    }


    /**
     * Reads the required property from the property file using the given key and returns the corresponding value.
     *
     * @param directoryToZip The key mapping to the required value of the property file.
     */
    public static void zipFiles(File directoryToZip) throws ScannerException {
        FileOutputStream fos;

        try {
            fos = new FileOutputStream(directoryToZip + ScannerConstants.ZIP_FILE_EXTENSION);
        } catch (FileNotFoundException e) {
            throw new ScannerException("Cannot find the Zip file location. ", e);
        }

        File[] files = directoryToZip.listFiles();
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        byte[] bytes = new byte[1024];
        int length;

        for (File fileToZip : files) {
            try (FileInputStream fis = new FileInputStream(fileToZip)) {
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }

            } catch (FileNotFoundException e) {
                throw new ScannerException("Cannot find the file to zip. ", e);
            } catch (IOException e) {
                throw new ScannerException("IOException occured while getting next file to zip. ", e);
            }
        }
        try {
            fos.close();
            zipOut.close();
        } catch (IOException e) {
            throw new ScannerException("Error occured while closing the output streams. ", e);
        }
    }

    /**
     * Upload a {@code MultipartFile} to a specified location.
     *
     * @param file            File to be uploaded
     * @param destinationPath Destination path to upload the file
     * @throws ScannerException If an error occurs while I/O operations
     */
    public static boolean uploadFile(MultipartFile file, String destinationPath)
            throws ScannerException {
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(
                new File(destinationPath)))) {
            byte[] bytes = file.getBytes();
            stream.write(bytes);

            return true;
        } catch (IOException e) {
            throw new ScannerException("IOException occured while uploading the file to container. ", e);
        }
    }

    /**
     * Download the product pack from the FTP location.
     *
     * @param productPathInFTP path to download pack in the FTP location.
     * @param productName      file name to download.
     * @throws ScannerException
     */
    public static void downloadProductFromFTP(String productPathInFTP, String productName) throws ScannerException {
        byte[] buffer = new byte[1024];

        //get the FTP configuration to build the connection with FTP server.
        String ftpHost = ConfigurationReader.getConfigProperty(ScannerConstants.FTP_HOST);
        String ftpUsername = ConfigurationReader.getConfigProperty(ScannerConstants.FTP_USERNAME);
        char[] ftpPassword = (ConfigurationReader.getConfigProperty(ScannerConstants.FTP_PASSWORD)).toCharArray();
        int ftpPort = Integer.getInteger(ConfigurationReader.getConfigProperty(ScannerConstants.FTP_PORT));

        JSch jsch = new JSch();
        Session session = null;
        File newFile = new File(ConfigurationReader.getConfigProperty(ScannerConstants.DEFAULT_FTP_PRODUCT_PATH),
                productName + ScannerConstants.ZIP_FILE_EXTENSION);
        Channel channel;
        ChannelSftp sftp;
        try {
            session = jsch.getSession(ftpUsername, ftpHost, ftpPort);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(Arrays.toString(ftpPassword));
            session.connect();

            channel = session.openChannel(ScannerConstants.SFTP);
            sftp = (ChannelSftp) channel;
            sftp.connect();
            sftp.cd(productPathInFTP);
        } catch (JSchException e) {
            throw new ScannerException("JSchException occured while creating ftp connection. ", e);
        } catch (SftpException e) {
            throw new ScannerException("SftpException occured while executing sftp commands. ", e);
        }

        try (BufferedInputStream bis = new BufferedInputStream(sftp.get(productName));
             OutputStream os = new FileOutputStream(newFile);
             BufferedOutputStream bos = new BufferedOutputStream(os)) {
            int readCount;

            while ((readCount = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, readCount);
            }
        } catch (IOException e) {
            throw new ScannerException("IOException occured while downloading product " +
                    "from ftp location. ", e);
        } catch (SftpException e) {
            throw new ScannerException("IOException occured while sftp command to get " +
                    "the product location. ", e);
        }

        for (int i = 0; i < ftpPassword.length; i++) {
            ftpPassword[i] = '\0';
        }
    }
}
