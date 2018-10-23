/*
 *  Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.security.tools.scanner.config;

public interface Configuration {
    /**
     * Set the scanner class dynamically in the container environment
     *
     * @param scannerClass
     */
    void setScannerClass(String scannerClass);

    /**
     * Get the scanner class name that should be engaged
     *
     * @return
     */
    String getScannerClass();

    /**
     * Set the directory that the product packs are stored
     *
     * @param productPath The path to directory
     */
    void setProductPathForZipFileUpload(String productPath);

    /**
     * Get the directory that the product packs are stored
     *
     * @return directory path
     */
    String getProductPathForZipFileUpload();

    /**
     * Set the directory that the github products should be cloned
     *
     * @param productPath The path to directory which products should be cloned
     */
    void setProductPathForGitClone(String productPath);

    /**
     * Get the directory that the github product are cloned
     *
     * @return local git repository path
     */
    String getProductPathForGitClone();

}