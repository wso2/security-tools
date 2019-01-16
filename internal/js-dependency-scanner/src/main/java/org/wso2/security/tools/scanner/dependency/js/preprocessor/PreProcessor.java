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
import org.wso2.security.tools.scanner.dependency.js.exception.ConfigParserException;
import org.wso2.security.tools.scanner.dependency.js.exception.DownloaderException;
import org.wso2.security.tools.scanner.dependency.js.exception.FileHandlerException;
import org.wso2.security.tools.scanner.dependency.js.model.Product;
import org.wso2.security.tools.scanner.dependency.js.utils.CommonApiInvoker;
import org.wso2.security.tools.scanner.dependency.js.utils.CommonUtils;
import org.wso2.security.tools.scanner.dependency.js.utils.ConfigParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is responsible to perform pre-processing tasks prior to execute retire.js scan.
 * Pre processing includes following processes :
 * Parse configurations
 * Download product packs
 * Download package.json files
 * Unzip product directory
 */
public class PreProcessor {

    private static final Logger log = Logger.getLogger(PreProcessor.class);
    //Root directory where all products are downloaded.
    private File productRootDirectory;
    //Mapper to hold product name as key and each product's path where product pack are downloaded as value.
    private HashMap<String, String> productFileMapper;

    /**
     * Entry point to start pre-processing task.
     * Following are sub tasks of this method.
     * Parse configuration
     * Download product packs
     * Download package.json files
     * Unzip product directory
     *
     * @return productFileMapper Mapper to hold product name as key and each product's path where product
     * pack are downloaded as value.
     */
    public HashMap<String, String> startPreProcessing() {

        try {
            List<String> productList;
            //Parse config details.
            //Get supported product list from config file
            productList = ConfigParser.parseProductList();
            List<Product> supportedProductDtoList = new ArrayList<>();
            //Get product details from respective product configuration files.
            for (String product : productList) {
                supportedProductDtoList.add(ConfigParser.parseProductConfiguration(product));
            }
            //Create Root Directory for products if not exists
            productRootDirectory = new File(JSScannerConstants.PRODUCT_HOME);
            CommonUtils.createDirectory(productRootDirectory);
            log.info("[JS_SEC_DAILY_SCAN] Start downloading product packs");
            //download resources
            downloadResources(supportedProductDtoList);
            CommonUtils.clearAccssToken(CommonApiInvoker.getGitToken());
        } catch (DownloaderException | ConfigParserException | FileHandlerException e) {
            log.error("Error occurred while downloading product pack.", e);
        }
        return productFileMapper;
    }

    /**
     * Download resources. It can be either product pack or package.json files.
     * Product pack can be downloaded from any resource.
     * Package.json files can be downloaded from github repo as raw materials.
     *
     * @param productDtoList Product list
     * @throws DownloaderException   Exception occurred while downloading files.
     * @throws ConfigParserException Exception occurred while parsing configuration properties.
     * @throws FileHandlerException  Exception occured while creating root directory.
     */
    private void downloadResources(List<Product> productDtoList) throws DownloaderException,
            FileHandlerException, ConfigParserException {
        ConfigParser.parseGitAccessToken();
        productFileMapper = new HashMap<>();
        String filename;
        for (Product productDto : productDtoList) {
            // Create directory for current product
            File currentProductDir = new File(productRootDirectory.getAbsolutePath() + File.separator +
                    productDto.getProductRepoName());
            CommonUtils.createDirectory(currentProductDir);
            List<String> downloadedProductRootDirPathList = new ArrayList<>();
            // Download from git repository
            if (productDto.getInputSourceType().equals(JSScannerConstants.GIT)) {
                ResourceDownloader gitDownloader = new GitDownloader();
                downloadedProductRootDirPathList.addAll(executeDownloader(productDto, currentProductDir,
                        gitDownloader));
            }
            // Download from Atuwa
            if (productDto.getInputSourceType().equals(JSScannerConstants.ATUWA)) {
                ResourceDownloader atuwaDownloader = new AtuwaDownloader();
                ConfigParser.parseAtuwaUrl();
                downloadedProductRootDirPathList.addAll(executeDownloader(productDto, currentProductDir,
                        atuwaDownloader));
            }
            if (downloadedProductRootDirPathList.size() > 0) {
                for (String filePath : downloadedProductRootDirPathList) {
                    filename = new File(filePath).getName();
                    productFileMapper.put(filename.substring(0, (filename.length() -
                                    JSScannerConstants.FILE_SUFFIX_LENGTH)),
                            currentProductDir.getAbsolutePath());
                    log.info("[JS_SEC_DAILY_SCAN]  " + " Unzipped file : " + downloadedProductRootDirPathList);
                }
            }
        }
    }

    /**
     * This method is common entry point to start downloading product packs.
     *
     * @param productDto         object which contains details of product.
     * @param currentProductDir  Current product root directory.
     * @param resourceDownloader Resource downloader. This object indicates the type of source where we download
     *                           product packs. Eg : Github, Atuwa.
     * @throws DownloaderException Exception occurred when downloading files.
     */
    private List<String> executeDownloader(Product productDto, File currentProductDir, ResourceDownloader
            resourceDownloader) throws DownloaderException {
        List<String> downloadedProductPackPathList = resourceDownloader.downloadProductPack(productDto,
                currentProductDir.getAbsolutePath());
        return downloadedProductPackPathList;
    }

}
