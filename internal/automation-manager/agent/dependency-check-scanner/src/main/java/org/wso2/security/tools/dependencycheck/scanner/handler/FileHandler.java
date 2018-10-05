/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.security.tools.dependencycheck.scanner.handler;

import org.codehaus.plexus.util.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.dependencycheck.scanner.Constants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Utility methods for file handling
 */
@SuppressWarnings({"unused"})
public class FileHandler {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);

    /**
     * Traverse and find files with a specific name, rename them and move to a new folder.
     * <p>Since products have different modules with pom.xml files, after building the product scanning reports with
     * the same name are are generated in target folders.
     * Therefore, these files are renamed with the file path, and all the files are moved to one folder</p>
     *
     * @param sourcePath      Path of the folder to be traversed
     * @param destinationPath Path of the folder to add reports
     * @param fileName        Name of the file to be searched
     */
    public static void findFilesRenameAndMoveToFolder(String sourcePath, String destinationPath, String fileName) {
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
                    LOGGER.error("Error occurred while moving the files", e);
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
    public static String extractZipFile(String zipFilePath) throws IOException {
        int BUFFER = 2048;
        File file = new File(zipFilePath);
        ZipFile zip = new ZipFile(file);
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
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[BUFFER];
                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }
            if (currentEntry.endsWith(Constants.ZIP_FILE_EXTENSION)) {
                // found a zip file, try to open
                extractZipFile(destFile.getAbsolutePath());
            }
        }
        zip.close();
        return fileName.substring(0, fileName.length() - 4);
    }

    /**
     * Upload a {@code MultipartFile} to a specified location
     *
     * @param file            File to be uploaded
     * @param destinationPath Destination path to upload the file
     * @throws IOException If an error occurs while I/O operations
     */
    public static void uploadFile(MultipartFile file, String destinationPath) throws IOException {
        byte[] bytes = file.getBytes();
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(destinationPath)));
        stream.write(bytes);
        stream.close();
    }
}
