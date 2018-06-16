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

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.security.tools.scanner.dependency.js.constants.JSScannerConstants;
import org.wso2.security.tools.scanner.dependency.js.exception.ApiInvokerException;
import org.wso2.security.tools.scanner.dependency.js.exception.DownloaderException;
import org.wso2.security.tools.scanner.dependency.js.exception.FileHandlerException;
import org.wso2.security.tools.scanner.dependency.js.utils.CommonApiInvoker;
import org.wso2.security.tools.scanner.dependency.js.utils.CommonUtils;
import org.wso2.security.tools.scanner.dependency.js.utils.HttpDownloadUtility;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ResourceDownloader to download package.json files. It reads the pom file from product repository
 * then get the component version where those package.json files need to be downloaded. After that download
 * package.json files from particular version release tag of relevant component. These are performed using
 * github api.
 */
public class ReactFileDownloader {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final Logger log = Logger.getLogger(ReactFileDownloader.class);

    /**
     * Download Package.json files.
     *
     * @param version           version of the product.
     * @param repoVersionMapper Version of components. Package.json files are from these components.
     * @param rootDirectory     Directory where the files will be downloaded.
     * @param productRepo       Repository of product.
     * @throws DownloaderException Exception occurred while downloading the product.
     * @throws ApiInvokerException Exception occurred while invoking the Github API.
     */
    public static void downloadReactFiles(String version, HashMap<String, String> repoVersionMapper, File rootDirectory,
                                          String productRepo)
            throws DownloaderException, ApiInvokerException {
        for (Map.Entry<String, String> entry : repoVersionMapper.entrySet()) {
            String componentVersion = getComponentVersionFromPom(version, productRepo, entry.getValue());
            try {
                getPackagejsonFiles(componentVersion, entry.getKey(), rootDirectory);
            } catch (FileHandlerException e) {
                throw new DownloaderException("Failed to download package.json file : ", e);
            }

        }
    }

    /**
     * Install npm modules which are referenced in package.json files.
     *
     * @param path Directory where the package.json file is placed.
     * @throws DownloaderException exception occurred while downloading NPM Modules.
     */
    private static void installNPMModules(String path) throws DownloaderException {
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(JSScannerConstants.NPM,
                    JSScannerConstants.INSTALL);
            processBuilder.directory(new File(path));
            process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new DownloaderException("Error occurred while downloading NPM modules due to : ", e);
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }


    /**
     * Get component version from product main pom file.
     *
     * @param version    product version
     * @param repoName   reponame of the product
     * @param versionTag version tag of component
     * @return component version
     * @throws ApiInvokerException Exception occurred while API Call.
     */
    private static String getComponentVersionFromPom(String version, String repoName, String versionTag) throws
            ApiInvokerException {
        String componentVersion = null;
        //api endpoint to get pom file
        String url = JSScannerConstants.GIT_SEARCH_URL + JSScannerConstants.POM + "+repo:wso2/" + repoName + "/tree/" +
                version;
        String response = CommonApiInvoker.connectGitAPI(url);
        if (response != null) {
            JSONObject resultJsonObject = new JSONObject(response);
            //array of pom files in product repo
            JSONArray itemsArray = resultJsonObject.getJSONArray("items");
            if (itemsArray.length() > 0) {
                for (int i = 0, size = itemsArray.length(); i < size; i++) {
                    JSONObject currentItem = itemsArray.getJSONObject(i);
                    if (currentItem.get("path").equals(JSScannerConstants.POM)) {
                        //endpoint to get content of pom file
                        String contentAPIURL = currentItem.getString("url");
                        String contentAPIResponse = CommonApiInvoker.connectGitAPI(contentAPIURL);
                        if (contentAPIResponse != null) {
                            JSONObject contentResponseObject = new JSONObject(contentAPIResponse);
                            //get encoded content of pom file
                            String encodedContent = contentResponseObject.getString("content");
                            componentVersion = getVersion(encodedContent, versionTag);
                        }
                        break;
                    }
                }
            } else {
                log.error("Could not find pom.xml for " + repoName);
            }
        }
        return componentVersion;
    }

    /**
     * Get component version from content of pom file.
     *
     * @param encodedContent base64 content of pom file.
     * @param versionTag     version tag of component.
     * @return version.
     */
    private static String getVersion(String encodedContent, String versionTag) {
        String componentVersion = null;
        if (Base64.isBase64(encodedContent)) {
            //get decoded content of pom file
            byte[] encodedContentBytes = Base64.decodeBase64(encodedContent);
            String decodedContent = new String(encodedContentBytes, UTF_8);
            String[] lines = decodedContent.split(System.getProperty("line.separator"));
            //get component version
            for (String line : lines) {
                if (line.contains(versionTag)) {
                    Pattern pattern = Pattern.compile(JSScannerConstants.POM_VERSION_REGEX);
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        componentVersion = matcher.group(0);
                    }
                }
            }
        }
        return componentVersion;
    }

    /**
     * Filer package.json files and download them.
     *
     * @param version       version
     * @param componentRepo component repository name
     * @param rootDir       directory where the package.json files to be downloaded.
     * @throws ApiInvokerException Exception occurred while API Call.
     * @throws DownloaderException Exception occurred while downloading package.json files.
     */
    private static void getPackagejsonFiles(String version, String componentRepo, File rootDir) throws
            ApiInvokerException, DownloaderException, FileHandlerException {
        //endpoint to get package.json files
        String url = JSScannerConstants.GIT_SEARCH_URL + JSScannerConstants.PACKAGE_JSON + "+repo:wso2/" +
                componentRepo + "/tree/" + "v" + version;
        String response = CommonApiInvoker.connectGitAPI(url);
        if (response != null) {
            JSONObject resultJsonObject = new JSONObject(response);
            //get all package.json files from component repo
            JSONArray itemsArray = resultJsonObject.getJSONArray("items");
            if (itemsArray.length() > 0) {
                for (int i = 0, size = itemsArray.length(); i < size; i++) {
                    JSONObject currentItem = itemsArray.getJSONObject(i);
                    if (currentItem.get("name").equals(JSScannerConstants.PACKAGE_JSON)) {
                        //endpoint to get download url
                        String currentFileURL = currentItem.getString("url");
                        String downloadURLResponse = CommonApiInvoker.connectGitAPI(currentFileURL);
                        if (downloadURLResponse != null) {
                            downloadPackageFile(downloadURLResponse, rootDir);
                        }
                    }
                }
            } else {
                log.error("Could not find package.json files for " + componentRepo);
            }
        }
    }

    /**
     * Download package.json file.
     *
     * @param downloadURLResponse response string which contains download url for package.json file.
     * @param rootDir             Target directory.
     * @throws DownloaderException  Exception occurred while downloading package.json files.
     * @throws FileHandlerException Exception occurred while creating folder.
     */
    private static void downloadPackageFile(String downloadURLResponse, File rootDir) throws DownloaderException,
            FileHandlerException {
        JSONObject currentJSONObject = new JSONObject(downloadURLResponse);
        //download url endpoint
        String downloadURL = currentJSONObject.getString("download_url");
        File currentFile = new File(downloadURL);
        String parentFileName = currentFile.getParentFile().getName();
        //Since there could be different package.json files with in a component. To maintain unique folder
        //for each pacakage.json files, the timestamp is attached with folder name.
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        File parentDir = new File(rootDir.getAbsolutePath() + File.separator + parentFileName +
                timestamp.getTime());
        CommonUtils.createDirectory(parentDir);
        //Download package.json files.
        HttpDownloadUtility.downloadFile(downloadURL, parentDir.getAbsolutePath());
        //Install NPM modules
        installNPMModules(parentDir.getAbsolutePath());
    }

}
