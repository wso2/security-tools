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
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.security.tools.scanner.dependency.js.constants.JSScannerConstants;
import org.wso2.security.tools.scanner.dependency.js.exception.ApiInvokerException;
import org.wso2.security.tools.scanner.dependency.js.exception.DownloaderException;
import org.wso2.security.tools.scanner.dependency.js.model.Product;
import org.wso2.security.tools.scanner.dependency.js.utils.CommonApiInvoker;
import org.wso2.security.tools.scanner.dependency.js.utils.HttpDownloadUtility;
import org.wso2.security.tools.scanner.dependency.js.utils.UnZipper;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Resource downloader class to download product pack from github.
 * This class extends ResourceDownloader abstract class and implements downloadProductPack method.
 */
public class GitDownloader extends ResourceDownloader {

    private static final Logger log = Logger.getLogger(GitDownloader.class);

    /**
     * Download product pack from github repository.
     *
     * @param product Repository Name.
     * @param path    parent path where downloaded pack to be saved.
     * @return path where downloaded pack to be saved.
     * @throws ApiInvokerException exception occurred while Calling API
     * @throws DownloaderException exception occurred while downloading file.
     */
    @Override
    public List<String> downloadProductPack(Product product, String path) throws ApiInvokerException,
            DownloaderException {
        List<String> zipFilePathList = new ArrayList<>();

        //git api endpoint to get releases of particular product.
        String gitReleaseUrl = JSScannerConstants.GIT_BASE_URL + product.getProductRepoName() +
                JSScannerConstants.RELEASES;
        String downloadURL;
        //call release end point and get results
        String apiResponse = CommonApiInvoker.connectGitAPI(gitReleaseUrl);
        if (apiResponse != null) {
            JSONArray resultJsonArray = new JSONArray(apiResponse);
            for (int i = 0, size = resultJsonArray.length(); i < size; i++) {
                JSONObject currentReleaseInfo = resultJsonArray.getJSONObject(i);
                String releaseDate = currentReleaseInfo.get("published_at").toString().substring(0, 10);
                String version = currentReleaseInfo.getString("tag_name");
                Long dateDiff;
                try {
                    dateDiff = getDateDiffFromLastWeeklyRelease(releaseDate);
                } catch (ParseException e) {
                    throw new DownloaderException("Error occurred while parsing release date.", e);
                }
                if (dateDiff <= 2) {
                    File tarDir = null;
                    JSONArray assetsArray = (JSONArray) currentReleaseInfo.get(JSScannerConstants.ASSETS);
                    String name;
                    for (int j = 0, assetSize = assetsArray.length(); j < assetSize; j++) {
                        JSONObject assetInArray = assetsArray.getJSONObject(j);
                        //perform exact matching
                        name = assetInArray.get(JSScannerConstants.NAME).toString();
                        if ((name.contains(JSScannerConstants.AM) || name.contains(JSScannerConstants.APIM) ||
                                name.contains(JSScannerConstants.INTEGRATION) ||
                                name.contains(JSScannerConstants.STREAMPROCESSOR) ||
                                name.contains(JSScannerConstants.IDENTITYSERVER)) &&
                                name.endsWith(JSScannerConstants.ZIP_PREFIX)) {
                            if (isWeeklyRelease(name)) {
                                //If current product asset is weekly release then the target directory is weekly release
                                // folder of particular product.
                                tarDir = new File(path + File.separator + JSScannerConstants.WEEKLY_RELEASE);
                            } else if (isGARelease(name)) {
                                //If current product asset is GA release then the target directory is GA release
                                //folder of particular product.
                                tarDir = new File(path + File.separator + JSScannerConstants.GA_RELEASE);
                            }
                            if (tarDir != null) {
                                createResourceDirectory(tarDir);
                            }
                            log.info(name + " is published on " + releaseDate);
                            //get download endpoint url
                            downloadURL = (String) assetInArray.get(JSScannerConstants.DOWNLOAD_URL);
                            if (downloadURL != null && path != null) {
                                // the path of downloaded product pack

                                String filePath = null;
                                if (tarDir != null) {
                                    filePath = HttpDownloadUtility.downloadFile(downloadURL,
                                            tarDir.getAbsolutePath());
                                }
                                // the path of unzipped product directory
                                String unzippedDirPath;
                                try {
                                    unzippedDirPath = UnZipper.extractFolder(filePath);
                                } catch (IOException e) {
                                    throw new DownloaderException("Error occurred while unzip file " + filePath, e);
                                }
                                System.out.println(filePath);
                                zipFilePathList.add(unzippedDirPath);
                                //Check whether the current product has react components and
                                // if so download package.json files.
                                if (product.getRepoVersionMapper().size() > 1) {
                                    //since the wso2am versions below the 3.0.0 hasn't any react components.
                                    // Package.json files won't be there. This implementation is temporary.
                                    if (!name.contains(JSScannerConstants.AM)) {
                                        //download react files
                                        ReactFileDownloader.downloadReactFiles(version, product.getRepoVersionMapper(),
                                                new File(unzippedDirPath), product.getProductRepoName());
                                    }
                                }
                            }
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
        }

        return zipFilePathList;
    }

}
