/*
 *
 *   Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.security.tools.scanner.dependency.js.utils;


import org.wso2.security.tools.scanner.dependency.js.constants.JSScannerConstants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Util class to unzip product pack. It takes the product pack zip file path as a input and extract that zip file
 * recursively. It also extract nested zip, war and jar files. It returns unzipped file path to caller.
 */
public class UnZipper {

    /**
     * Extract zip directory.
     *
     * @param zipFile zip file.
     * @return path where the zip file is extracted.
     * @throws IOException IO Exception.
     */
    public static String extractFolder(String zipFile) throws IOException {
        int buffer = 2048;
        File file = new File(zipFile);

        ZipFile zip = null;
        String newPath = zipFile.substring(0, zipFile.length() - 4);
        try {
            zip = new ZipFile(file);
            String currentEntry;

            boolean iscreated = new File(newPath).mkdir();

            Enumeration zipFileEntries = zip.entries();

            // Process each entry
            while (zipFileEntries.hasMoreElements()) {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                currentEntry = entry.getName();
                File destFile = new File(newPath, currentEntry);
                //destFile = new File(newPath, destFile.getName());
                File destinationParent = destFile.getParentFile();

                // create the parent directory structure if needed
                boolean isDestcreated = destinationParent.mkdirs();

                if (!entry.isDirectory()) {
                    BufferedInputStream bufferedInputStream = null;
                    BufferedOutputStream bufferedOutputStream = null;
                    InputStream inputStream = null;
                    try {
                        inputStream = zip.getInputStream(entry);
                        bufferedInputStream = new BufferedInputStream(inputStream);
                        int currentByte;
                        // establish buffer for writing file
                        byte data[] = new byte[buffer];

                        // write the current file to disk
                        FileOutputStream fos = new FileOutputStream(destFile);
                        bufferedOutputStream = new BufferedOutputStream(fos,
                                buffer);

                        // read and write until last byte is encountered
                        while ((currentByte = bufferedInputStream.read(data, 0, buffer)) != -1) {
                            bufferedOutputStream.write(data, 0, currentByte);
                        }
                    } finally {
                        if (bufferedOutputStream != null) {
                            bufferedOutputStream.flush();
                            bufferedOutputStream.close();
                        }
                        if (bufferedInputStream != null) {
                            bufferedInputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }

                }

                if (currentEntry.endsWith(JSScannerConstants.ZIP_PREFIX)) {
                    // found a zip file, try to open
                    extractFolder(destFile.getAbsolutePath());
                }
                if (currentEntry.endsWith(JSScannerConstants.JAR_PREFIX)) {
                    // found a zip file, try to open
                    extractFolder(destFile.getAbsolutePath());
                }
                if (currentEntry.endsWith(JSScannerConstants.WAR_PREFIX)) {
                    // found a zip file, try to open
                    extractFolder(destFile.getAbsolutePath());

                }
            }
        } finally {
            assert zip != null;
            zip.close();
        }
        return newPath;
    }
}
