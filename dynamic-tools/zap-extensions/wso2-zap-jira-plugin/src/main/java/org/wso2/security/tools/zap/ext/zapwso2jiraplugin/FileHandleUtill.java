/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.security.tools.zap.ext.zapwso2jiraplugin;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileHandleUtill {

    private static final Logger log = Logger.getRootLogger();

    /**
     * This method used to get the backup of the newly generated report
     *
     * @param path     is path to the backup folder
     * @param filePath is path to the file folder
     * @return
     */
    public void moveAttachmentToBackupFolder(String path, String filePath) {

        File source = new File(filePath);
        File dest = new File(path.trim());

        try {
            FileUtils.copyFileToDirectory(source, dest);
        } catch (IOException e) {
            log.error("File not found in the specified path");
        }
    }


    /**
     * Renaming the file according to the allowed format in jira
     *
     * @param product  product that get scanned
     * @param filePath file path of the report generated
     * @return filepath after renaming the file
     */
    public String renameFile(String product, String filePath) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fileNewPath = "";
        Date date = new Date();
        String newFileName = product + "-" + dateFormat.format(date).toString().substring(0, 10);
        log.info(newFileName);
        String[] filePathDirectories = filePath.split("/");

        // File (or directory) with old name
        File file = new File(filePath);

        if (filePath.contains(".xml"))
            fileNewPath = filePath
                    .replace(filePathDirectories[filePathDirectories.length - 1], newFileName.concat(".xml"));
        else if (filePath.contains(".html"))
            fileNewPath = filePath
                    .replace(filePathDirectories[filePathDirectories.length - 1], newFileName.concat(".html"));

        // File (or directory) with new name
        File file2 = new File(fileNewPath);

        // Rename file (or directory)
        boolean success = file.renameTo(file2);

        return fileNewPath;
    }

    /**
     * Compressing the file generatated during the zap scan
     *
     * @param path file path in the server
     * @return new file path for the compressed file
     */
    public String compressFile(String path) {

        byte[] buffer = new byte[1024];

        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        FileInputStream in = null;

        try {
            fos = new FileOutputStream(path.concat(".zip"));
            zos = new ZipOutputStream(fos);

            String[] directories = path.split("/");
            ZipEntry ze = new ZipEntry(directories[directories.length - 1]);
            zos.putNextEntry(ze);
            in = new FileInputStream(path);

            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {
            log.error("File need to be compressed is not found ", e);
        } catch (IOException e) {
            log.error("Exception occured during the file compression", e);
        } finally {
            try {
                in.close();
                zos.closeEntry();
                zos.close();
            } catch (IOException e) {
                log.error("Error while closing the Stream");
            }
        }

        return path.concat(".zip");
    }
}
