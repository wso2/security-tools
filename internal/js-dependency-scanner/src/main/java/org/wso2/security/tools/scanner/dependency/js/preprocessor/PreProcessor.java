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
import org.wso2.security.tools.scanner.dependency.js.exception.ApiInvokerException;
import org.wso2.security.tools.scanner.dependency.js.exception.ConfigParserException;
import org.wso2.security.tools.scanner.dependency.js.exception.DownloaderException;
import org.wso2.security.tools.scanner.dependency.js.exception.FileHandlerException;
import org.wso2.security.tools.scanner.dependency.js.model.Product;
import org.wso2.security.tools.scanner.dependency.js.utils.CommonApiInvoker;
import org.wso2.security.tools.scanner.dependency.js.utils.CommonUtils;
import org.wso2.security.tools.scanner.dependency.js.utils.ConfigParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class is responsible to perform pre-processing tasks prior to execute retire.js scan.
 * Pre processing includes following processes :
 * Parse configuration
 * Download product packs
 * Download package.json files
 * Unzip product directory
 */
public class PreProcessor {

    private static final Logger log = Logger.getLogger(PreProcessor.class);
    //Root directory where all products are downloaded.
    private File productRootDirectory;
    //Mapper to hold values each product's path where it is downloaded.
    private HashMap<String, String> productFileMapper;

    /**
     * Entry point to start pre-processing task.
     * Following tasks are sub task of this method.
     * Parse configuration
     * Download product packs
     * Download package.json files
     * Unzip product directory
     *
     * @return productFileMapper which holds the values each product's path where it is downloaded.
     */
    public HashMap<String, String> startPreprocessing() {

        try {
            List<String> productList;
            //Parse config details.
            //Get supported product list from config file
            productList = ConfigParser.parseProductList();
            List<Product> supportedProductList = new ArrayList<>();
            //Get product details from respective product configuration files.
            for (String product : productList) {
                supportedProductList.add(ConfigParser.parseProductConfiguration(product));
            }

            //Create Root Directory for products if not exists
            productRootDirectory = new File(JSScannerConstants.PRODUCT_HOME);
            CommonUtils.createDirectory(productRootDirectory);
            log.info("[JS_SEC_DAILY_SCAN] Start downloading product packs");
            //download resources
            downloadResources(supportedProductList);
            Arrays.fill(CommonApiInvoker.getGitToken(),
                    JSScannerConstants.RANDOM_STRING.charAt(ConfigParser.getRandomNumber()));
        } catch (ApiInvokerException | DownloaderException | ConfigParserException | FileHandlerException e) {
            log.error("Error occurred while downloading product pack.", e);
        }
        return productFileMapper;
    }

    /**
     * Download resources. It can be either product pack or package.json files.
     * Product pack can be downloaded from any resource. Currently it supports GitHub.
     * Package.json files can be downloaded from github repo as raw materials.
     *
     * @param productDtoList Product list
     * @throws DownloaderException exception occurred while downloading files.
     */
    private void downloadResources(List<Product> productDtoList) throws DownloaderException,
            ApiInvokerException, ConfigParserException, FileHandlerException {
        ConfigParser.parseGitAccessToken();
        productFileMapper = new HashMap<>();
        String filename;
        for (Product productDto : productDtoList) {
            //create directory for current product
            File currentProductDir = new File(productRootDirectory.getAbsolutePath() + File.separator +
                    productDto.getProductRepoName());
            CommonUtils.createDirectory(currentProductDir);

            List<String> zipFileList = new ArrayList<>();
            //download from git repository
            if (productDto.getInputSourceType().equals(JSScannerConstants.GIT)) {
                ResourceDownloader gitDownloader = new GitDownloader();
                startDownload(productDto, currentProductDir, zipFileList, gitDownloader);
            }
            if (productDto.getInputSourceType().equals(JSScannerConstants.ATUWA)) {
                ResourceDownloader atuwaDownloader = new AtuwaDownloader();
                ConfigParser.parseAtuwaUrl();
                startDownload(productDto, currentProductDir, zipFileList, atuwaDownloader);
            }
            //unzip downloaded product pack

            if (zipFileList.size() > 0) {
                for (String filePath : zipFileList) {
                    filename = new File(filePath).getName();
                    productFileMapper.put(filename.substring(0, (filename.length() -
                                    JSScannerConstants.FILE_SUFFIX_LENGTH)),
                            currentProductDir.getAbsolutePath());
                    log.info("[JS_SEC_DAILY_SCAN]  " + " Unzipped file : " + zipFileList);
                }
            }
        }
    }

    /**
     * This method is common entry point to start downloading product packs.
     *
     * @param productDto         object which contains details of product.
     * @param currentProductDir  Current product root directory.
     * @param zipFileList        List to store downloaded product packs zip file list.
     * @param resourceDownloader Resource downloader. This object indicates the type of source where we download
     *                           product packs. Eg : Github, Atuwa.
     * @throws ApiInvokerException Exception occurred when calling API.
     * @throws DownloaderException Exception occurred when downloading files.
     */
    private void startDownload(Product productDto, File currentProductDir, List<String> zipFileList, ResourceDownloader
            resourceDownloader) throws ApiInvokerException, DownloaderException {
        List<String> fileList = resourceDownloader.downloadProductPack(productDto,
                currentProductDir.getAbsolutePath());
        zipFileList.addAll(fileList);
    }

}
