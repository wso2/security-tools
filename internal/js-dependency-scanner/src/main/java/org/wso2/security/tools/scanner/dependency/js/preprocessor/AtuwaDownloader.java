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

package org.wso2.security.tools.scanner.dependency.js.preprocessor;

import org.apache.log4j.Logger;
import org.wso2.security.tools.scanner.dependency.js.constants.JSScannerConstants;
import org.wso2.security.tools.scanner.dependency.js.exception.DownloaderException;
import org.wso2.security.tools.scanner.dependency.js.exception.FileHandlerException;
import org.wso2.security.tools.scanner.dependency.js.model.Product;
import org.wso2.security.tools.scanner.dependency.js.utils.HttpDownloadUtility;
import org.wso2.security.tools.scanner.dependency.js.utils.UnZipper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Download product pack from Atuwa.
 * This class extends ResourceDownloader abstract class and implements downloadProductPack method.
 */
public class AtuwaDownloader extends ResourceDownloader {

    private static final Logger log = Logger.getLogger(PreProcessor.class);
    private static String atuwaBaseURL;

    public static void setAtuwaBaseURL(String atuwaBaseURL) {
        AtuwaDownloader.atuwaBaseURL = atuwaBaseURL;
    }

    /**
     * <p>
     * This is an override method, so javadoc should be visible. This method responsible to download
     * product packs from atuwa.
     * </p>
     * {@inheritDoc}
     */
    @Override
    public List<String> downloadProductPack(Product productDto, String path) throws DownloaderException {
        return download(path);
    }

    /**
     * Download product pack from atuwa URL.
     *
     * @param path Root directory file path to store the downloaded file.
     * @return list of downloaded weekly release zip file paths.
     */
    private List<String> download(String path) throws DownloaderException {
        BufferedReader reader = null;
        List<String> zipFilePathList;
        String name = null;
        try {
            zipFilePathList = new ArrayList<>();
            String urlString = atuwaBaseURL + File.separator + JSScannerConstants.OB_INDEX_FILE;
            String downloadURL;
            // build the url
            URL urlObject;
            try {
                urlObject = new URL(urlString);
                // open the url stream, wrap it an a few "readers"
                reader = new BufferedReader(new InputStreamReader(urlObject.openStream()));
            } catch (MalformedURLException e) {
                throw new DownloaderException("Malformed URL has occurred. : " + urlString + " ", e);
            } catch (IOException e) {
                throw new DownloaderException("Unable to reach URL  .", e);
            }
            String line;
            File tarDir = null;
            try {
                while ((line = reader.readLine()) != null) {
                    String element[] = line.split(":");
                    Long dateDiff = getDateDiffFromLastWeeklyRelease(element[1]);
                    if (dateDiff < 28) {
                        if (isWeeklyRelease(element[0])) {
                            tarDir = new File(path + File.separator + "weeklyRelease");
                        } else if (isGARelease(element[0])) {
                            tarDir = new File(path + File.separator + "GARelease");
                        }
                        if (tarDir != null) {
                            createResourceDirectory(tarDir);
                        }
                        name = element[0];
                        downloadURL = atuwaBaseURL + File.separator + element[0];
                        if (path != null) {
                            if (tarDir != null) {
                                String filePath = HttpDownloadUtility.downloadFile(downloadURL,
                                        tarDir.getAbsolutePath());
                                String unzippedDirPath = UnZipper.extractFolder(filePath);
                                zipFilePathList.add(filePath);
                            }
                        }
                        break;
                    }
                }
            } catch (ParseException e) {
                throw new DownloaderException("Error occurred while parsing Date.", e);
            } catch (IOException e) {
                throw new DownloaderException("Error occurred while creating directory :", e);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Unable to close stream : " + name + e.getMessage());
                }
            }
        }
        return zipFilePathList;
    }
}
