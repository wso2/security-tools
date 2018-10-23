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

import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTPClient;
import org.codehaus.plexus.util.FileUtils;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.scanner.Constants;
import org.wso2.security.tools.scanner.config.ConfigurationReader;
import org.wso2.security.tools.scanner.exception.ScannerException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for file handling
 */
public class FileHandler {
    /**
     * Traverse and find files with a specific name, rename them and move to a new folder.
     * <p>Since products have different modules with pom.xml files, after building the product scanning reports with
     * the same name are are generated in target folders.
     * Therefore, these files are renamed with the file path, and all the files are moved to one folder</p>
     *
     * @param sourcePath      Path of the folder to be traversed
     * @param destinationPath Path of the folder to add reports
     * @param fileName        Name of the file to be searched
     * @throws IOException If an error occurs while I/O operations
     */
    public static void findFilesRenameAndMoveToFolder(String sourcePath, String destinationPath, String fileName)
            throws IOException {
        File dir = new File(destinationPath);

        if (dir.mkdir()) {
            Files.find(Paths.get(sourcePath), Integer.MAX_VALUE, (filePath, fileAttr) -> filePath.getFileName()
                    .toString().equals(fileName)).forEach((f) -> {
                File file = f.toFile();
                String newFileName = file.getAbsolutePath().replace(sourcePath, Constants.NULL_STRING).replace(File
                        .separator, Constants.UNDERSCORE);
                File newFile = new File(destinationPath + File.separator + newFileName);
                file.renameTo(newFile);

                try {
                    FileUtils.copyFileToDirectory(newFile, dir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Compress a folder into a ZIP file
     *
     * @param fileToZip              File to zip
     * @param fileToZipName          Name of the file to zip
     * @param destinationZipFilePath Path of the destination zip file
     * @throws IOException If an error occurs while I/O operations
     */
    public static void zipFolder(File fileToZip, String fileToZipName, String destinationZipFilePath) throws
            IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    zipFolder(childFile, fileToZipName + File.separator + childFile.getName(), destinationZipFilePath);
                }
                return;
            }
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZipName);
        FileOutputStream fos = new FileOutputStream(destinationZipFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
        zipOut.close();
        fos.close();
    }

    /**
     * Extract a zip file and returns name of the extracted folder
     *
     * @param zipFilePath ZIP file path
     * @return Extracted folder name
     * @throws IOException If an error occurs while I/O operations
     */
    public static String extractZipFile(String zipFilePath) throws ScannerException {
        int BUFFER = 2048;

        File file = new File(zipFilePath);
        ZipFile zip = null;
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

            if (!entry.isDirectory()) {
                try {
                    BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                    int currentByte;

                    // establish buffer for writing file
                    byte data[] = new byte[BUFFER];

                    // write the current file to disk
                    FileOutputStream fos = null;
                    fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }

                    dest.flush();
                    dest.close();
                    is.close();
                } catch (FileNotFoundException e) {
                    throw new ScannerException("Error occured while extracting the files. Destination file cannot be found.", e);
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
     * @return returns the value corresponding to the given key value.
     */
    public static void zipFiles(File directoryToZip) throws ScannerException {
        FileOutputStream fos;

        try {
            fos = new FileOutputStream(directoryToZip + Constants.ZIP_FILE_EXTENSION);
        } catch (FileNotFoundException e) {
            throw new ScannerException("Cannot find the Zip file location. ", e);
        }

        File[] files = directoryToZip.listFiles();
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        byte[] bytes = new byte[1024];
        int length;

        for (File fileToZip : files) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }

            } catch (FileNotFoundException e) {
                throw new ScannerException("Cannot find the file to zip. ", e);
            } catch (IOException e) {
                throw new ScannerException("Error occured while getting next file to zip. ", e);
            }

            try {
                fis.close();
            } catch (IOException e) {
                throw new ScannerException("Error occured while closing the input stream. ", e);
            }
        }
        try {
            zipOut.close();
            fos.close();
        } catch (IOException e) {
            throw new ScannerException("Error occured while closing the output streams. ", e);
        }

    }

    /**
     * Upload a {@code MultipartFile} to a specified location
     *
     * @param file            File to be uploaded
     * @param destinationPath Destination path to upload the file
     * @throws ScannerException If an error occurs while I/O operations
     */
    public static boolean uploadFile(MultipartFile file, String destinationPath) throws ScannerException {
        try {
            byte[] bytes = file.getBytes();
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(destinationPath)));
            stream.write(bytes);
            stream.close();
            return true;
        } catch (IOException e) {
            throw new ScannerException("IOException occured while uploading the file to container. ", e);
        }
    }

    /**
     * Download the product pack from the FTP location
     *
     * @param productPathInFTP path to download pack in the FTP location.
     * @param productName      file name to download.
     * @throws ScannerException
     */
    public static void DownloadProductFromFTP(String productPathInFTP, String productName) throws ScannerException {
        FTPClient client = new FTPClient();
//        productPathInFTP="cloudprod/wum-pack";
//        productName="wso2am-2.5.0.zip";

        // verify whether it is ok to pass username and password
        try {
            //get the FTP configuration to build the connection with FTP server.
            String host = ConfigurationReader.getConfigProperty(Constants.FTP_HOST);
            String user = ConfigurationReader.getConfigProperty(Constants.FTP_USERNAME);
            String pass = ConfigurationReader.getConfigProperty(Constants.FTP_PASSWORD);
            int port = Integer.getInteger(ConfigurationReader.getConfigProperty(Constants.FTP_PORT));

            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(pass);
            session.connect();

            Channel channel = session.openChannel(Constants.SFTP);
            ChannelSftp sftp = (ChannelSftp) channel;
            sftp.connect();
            sftp.cd(productPathInFTP);

            byte[] buffer = new byte[1024];
            BufferedInputStream bis = new BufferedInputStream(sftp.get(productName));
            File newFile = new File(ConfigurationReader.getConfigProperty(Constants.DEFAULT_FTP_PRODUCT_PATH),
                    productName + Constants.ZIP_FILE_EXTENSION);
            OutputStream os = new FileOutputStream(newFile);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            int readCount;

            while ((readCount = bis.read(buffer)) > 0) {
                System.out.println("Writing: ");
                bos.write(buffer, 0, readCount);
            }

            bis.close();
            bos.close();
        } catch (JSchException e) {
            throw new ScannerException("Error occured while creating ftp connetion. ", e);
        } catch (IOException e) {
            throw new ScannerException("IOException occured while downlaoding product from ftp location. ", e);
        } catch (SftpException e) {
            throw new ScannerException("IOException occured while executing sftp commands. ", e);
        }
    }

}
