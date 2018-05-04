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
import org.wso2.security.tools.scanner.dependency.js.model.Product;
import org.wso2.security.tools.scanner.dependency.js.utils.HttpDownloadUtility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

    @Override
    public List<String> downloadProductPack(Product productDto, String path) {
        return download(path);
    }

    private List<String> download(String path) {
        BufferedReader reader = null;
        List<String> zipFilePathList = null;
        String name = null;
        try {
            zipFilePathList = new ArrayList<>();
            String urlString = JSScannerConstants.ATUWA_BASE_URL + JSScannerConstants.OB_INDEX_FILE;
            String downloadUrl;
            // create the url
            URL url = new URL(urlString);

            // open the url stream, wrap it an a few "readers"
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

            // write the output to stdout
            String line;
            File tarDir = null;
            while ((line = reader.readLine()) != null) {
                String element[] = line.split(":");
                Long dateDiff = getDateDiffFromLastWeeklyRelease(element[1]);
                if (dateDiff < 8) {
                    if (isWeeklyRelease(element[0])) {
                        tarDir = new File(path + File.separator + "weeklyRelease");
                    } else if (isGARelease(element[0])) {
                        tarDir = new File(path + File.separator + "GARelease");
                    }
                    if (tarDir != null) {
                        createDirectory(tarDir);
                    }
                    name = element[0];
                    downloadUrl = JSScannerConstants.ATUWA_BASE_URL + element[0];
                    if (path != null) {
                        assert tarDir != null;
                        zipFilePathList.add(HttpDownloadUtility.downloadFile(downloadUrl, tarDir.getAbsolutePath()));
                    }
                } else {
                    break;
                }
            }
        } catch (ParseException | DownloaderException | IOException e) {
            log.error("Unable to download : " + name + e.getMessage());
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
