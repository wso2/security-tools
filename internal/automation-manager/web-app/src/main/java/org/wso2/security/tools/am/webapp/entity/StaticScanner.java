/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.security.tools.am.webapp.entity;
/**
 * The class {@link StaticScanner} is the model to store user inputs of static scanner
 */
public class StaticScanner {

    private String userId;
    private String testName;
    private String productName;
    private String wumLevel;
    private boolean isFindSecBugs;
    private boolean isDependencyCheck;

    /**
     * @return User id of the user who initiated the scan
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user id of the user who initiates the scan
     *
     * @param userId User id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return Test name given by the user
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Set a test name to the scan
     *
     * @param testName Test name
     */
    public void setTestName(String testName) {
        this.testName = testName;
    }

    /**
     * @return The product name is to be scanned
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Set the product name
     *
     * @param productName Product name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * @return WUM level of the project
     */
    public String getWumLevel() {
        return wumLevel;
    }

    /**
     * Set the WUM level of the project
     *
     * @param wumLevel WUM level
     */
    public void setWumLevel(String wumLevel) {
        this.wumLevel = wumLevel;
    }

    /**
     * Set scan type to find sec bugs
     *
     * @param findSecBugs Boolean to indicate whether find sec bugs should be initiated
     */
    public void setFindSecBugs(boolean findSecBugs) {
        this.isFindSecBugs = findSecBugs;
    }
    /**
     * Set scan type to dependency check
     *
     * @param dependencyCheck Boolean to indicate whether dependency check should be initiated
     */
    public void setDependencyCheck(boolean dependencyCheck) {
        this.isDependencyCheck = dependencyCheck;
    }


    /**
     * @return Is find sec bugs scan initiated
     */
    public boolean isFindSecBugs() {
        return isFindSecBugs;
    }

    /**
     * @return Is dependency check scan initiated
     */
    public boolean isDependencyCheck() {
        return isDependencyCheck;
    }
}
