/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.security.tools.advisorytool.config;

import org.apache.commons.codec.binary.Base64;
import org.wso2.security.tools.advisorytool.model.Extension;
import org.wso2.security.tools.advisorytool.model.Platform;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class to store advisory tool configurations
 */
public class Configuration {

    private char[] patchListAPIUsername;
    private char[] patchListAPIPassword;
    private String patchListAPI;
    private String patchDetailsAPI;
    private String advisoryDetailsAPI;

    private String patchDetailsAPIAuthToken;
    private String advisoryDetailsAPIAuthToken;

    private String patchZIPCustomerLocation;
    private String patchZIPPublicLocation;

    private int patchSupportPeriod;

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private List<Platform> platforms = new ArrayList<>();
    private List<Extension> outputGenerators = new ArrayList<>();
    private List<Extension> advisoryBuilders = new ArrayList<>();

    private Configuration() {
    }

    private static volatile Configuration instance;

    public static Configuration getInstance() {
        if (instance == null) {
            synchronized (Configuration.class) {
                if (instance == null) {
                    instance = new Configuration();
                }
            }
        }
        return instance;
    }

    public char[] getPatchListAPIUsername() {
        return patchListAPIUsername;
    }

    public void setPatchListAPIUsername(char[] patchListAPIUsername) {
        this.patchListAPIUsername = patchListAPIUsername;
    }

    public char[] getPatchListAPIPassword() {
        return patchListAPIPassword;
    }

    public void setPatchListAPIPassword(char[] patchListAPIPassword) {
        this.patchListAPIPassword = patchListAPIPassword;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<Platform> platforms) {
        this.platforms = platforms;
    }

    public String getPatchListAPI() {
        return patchListAPI;
    }

    public void setPatchListAPI(String patchListAPI) {
        this.patchListAPI = patchListAPI;
    }

    public String getPatchDetailsAPI() {
        return patchDetailsAPI;
    }

    public void setPatchDetailsAPI(String patchDetailsAPI) {
        this.patchDetailsAPI = patchDetailsAPI;
    }

    public String getPatchZIPCustomerLocation() {
        return patchZIPCustomerLocation;
    }

    public void setPatchZIPCustomerLocation(String patchZIPCustomerLocation) {
        this.patchZIPCustomerLocation = patchZIPCustomerLocation;
    }

    public String getPatchZIPPublicLocation() {
        return patchZIPPublicLocation;
    }

    public void setPatchZIPPublicLocation(String patchZIPPublicLocation) {
        this.patchZIPPublicLocation = patchZIPPublicLocation;
    }

    public String getAdvisoryDetailsAPI() {
        return advisoryDetailsAPI;
    }

    public void setAdvisoryDetailsAPI(String advisoryDetailsAPI) {
        this.advisoryDetailsAPI = advisoryDetailsAPI;
    }

    public String getPatchListAPIAuthHeader() {
        String credentials = new String(patchListAPIUsername) + ":" + new String(patchListAPIPassword);
        return "Basic " + new String(Base64.encodeBase64(credentials.getBytes(UTF_8)), UTF_8);
    }

    public String getPatchDetailsAPIAuthHeader() {
        return "Bearer" + patchDetailsAPIAuthToken;
    }

    public String getPatchDetailsAPIAuthToken() {
        return patchDetailsAPIAuthToken;
    }

    public void setPatchDetailsAPIAuthToken(String patchDetailsAPIAuthToken) {
        this.patchDetailsAPIAuthToken = patchDetailsAPIAuthToken;
    }

    public String getAdvisoryDetailsAPIAuthHeader() {
        return "Bearer" + advisoryDetailsAPIAuthToken;
    }

    public String getAdvisoryDetailsAPIAuthToken() {
        return advisoryDetailsAPIAuthToken;
    }

    public void setAdvisoryDetailsAPIAuthToken(String advisoryDetailsAPIAuthToken) {
        this.advisoryDetailsAPIAuthToken = advisoryDetailsAPIAuthToken;
    }

    public List<Extension> getOutputGenerators() {
        return outputGenerators;
    }

    public List<Extension> getAdvisoryBuilders() {
        return advisoryBuilders;
    }

    public void setAdvisoryBuilders(List<Extension> advisoryBuilders) {
        this.advisoryBuilders = advisoryBuilders;
    }

    public void setOutputGenerators(List<Extension> outputGenerators) {
        this.outputGenerators = outputGenerators;
    }

    public int getPatchSupportPeriod() {
        return patchSupportPeriod;
    }

    public void setPatchSupportPeriod(int patchSupportPeriod) {
        this.patchSupportPeriod = patchSupportPeriod;
    }

}
