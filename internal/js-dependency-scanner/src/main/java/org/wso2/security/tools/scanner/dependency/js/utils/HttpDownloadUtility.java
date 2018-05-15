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

import org.apache.log4j.Logger;
import org.wso2.security.tools.scanner.dependency.js.exception.DownloaderException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A utility that downloads a file from a URL.
 */
public class HttpDownloadUtility {
    private static final Logger log = Logger.getLogger(HttpDownloadUtility.class);

    private static final int BUFFER_SIZE = 4096;

    /**
     * Downloads a file from a URL.
     *
     * @param fileURL HTTP URL of the file to be downloaded.
     * @param saveDir path of the directory to save the file.
     * @throws DownloaderException exception occurred while downloading resources.
     */
    public static String downloadFile(String fileURL, String saveDir) throws DownloaderException {
        URL url;
        String saveFilePath = null;
        String fileName = "";
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String disposition = httpConn.getHeaderField("Content-Disposition");
                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 9,
                                disposition.length());
                    }
                } else {
                    // extracts file name from URL
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                            fileURL.length());
                }

                // opens input stream from the HTTP connection
                inputStream = httpConn.getInputStream();
                saveFilePath = saveDir + File.separator + fileName;

                // opens an output stream to save into file

                outputStream = new FileOutputStream(saveFilePath);
                int bytesRead;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                log.info("[JS_SEC_DAILY_SCAN]  " + fileName + " has downloaded from " + url + " and saved to the " +
                        "directory " + saveFilePath);
            } else {
                log.warn("No file to download. Server replied HTTP code: " + responseCode);
            }
            httpConn.disconnect();
        } catch (MalformedURLException e) {
            throw new DownloaderException("Error occurred in downloading file : " + fileName + " malformed URL has " +
                    "occurred", e);
        } catch (FileNotFoundException e) {
            throw new DownloaderException("Error occurred in downloading file :" + fileName + " Error message: ", e);
        } catch (IOException e) {
            throw new DownloaderException("Error occurred in downloading file :" + fileName + " Error message:", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    throw new DownloaderException("Error occurred in downloading file :" + fileName + " Error message:"
                            , e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new DownloaderException("Error occurred in downloading file :" + fileName + " Error message:"
                            , e);
                }

            }
        }
        return saveFilePath;
    }
}
