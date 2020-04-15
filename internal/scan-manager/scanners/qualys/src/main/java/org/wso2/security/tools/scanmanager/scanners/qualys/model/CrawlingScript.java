/*
 *
 *   Copyright (c) 2020, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanmanager.scanners.qualys.model;

/**
 * Model class to represent Crawling Script
 */
public class CrawlingScript extends SeleniumScript {

    // Starting URL of crawling script. It can be either URL or Regex.
    private String startingUrl;

    // To represent authentication requirement before initating crawling job
    private Boolean isRequredAuthentication;

    // To represent whether starting URL is a regex or not.
    private Boolean isStartingUrlRegex;

    public String getStartingUrl() {
        return startingUrl;
    }

    public void setStartingUrl(String startingUrl) {
        this.startingUrl = startingUrl;
    }

    public Boolean getRequredAuthentication() {
        return isRequredAuthentication;
    }

    public void setRequredAuthentication(Boolean requredAuthentication) {
        isRequredAuthentication = requredAuthentication;
    }

    public Boolean getStartingUrlRegex() {
        return isStartingUrlRegex;
    }

    public void setStartingUrlRegex(Boolean startingUrlRegex) {
        isStartingUrlRegex = startingUrlRegex;
    }
}

