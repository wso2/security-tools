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

package org.wso2.security.tools.advisorytool.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a Product Version.
 */
public class Version {

    @SerializedName("version-number")
    String versionNumber;

    @SerializedName("kernel-version-number")
    String kernelVersionNumber;

    @SerializedName("platform-version-number")
    String platformVersionNumber;

    @SerializedName("patch-list")
    List<String> patchNamesList = new ArrayList<>();

    @SerializedName("is-wum-supported")
    @JsonProperty
    private boolean isWumSupported;

    @SerializedName("is-patch-supported")
    @JsonProperty
    private boolean isPatchSupported;

    @SerializedName("is-public-supported")
    @JsonProperty
    private boolean isPublicSupported;

    @SerializedName("is-deprecated")
    @JsonProperty (required = true)
    private boolean isDeprecated;

    @JsonProperty
    private Date releaseDate;

    public Date getReleasedDate() {
        return new Date(releaseDate.getTime());
    }

    public void setReleasedDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isPublicSupported() {
        return isPublicSupported;
    }

    public void setPublicSupported(boolean publicSupported) {
        isPublicSupported = publicSupported;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public void setDeprecated(boolean deprecated) {
        isDeprecated = deprecated;
    }

    public boolean isWumSupported() {
        return isWumSupported;
    }

    public void setWumSupported(boolean wumSupported) {
        isWumSupported = wumSupported;
    }

    public boolean isPatchSupported() {
        return isPatchSupported;
    }

    public void setPatchSupported(boolean patchSupported) {
        isPatchSupported = patchSupported;
    }

    public List<String> getPatchNamesList() {
        return patchNamesList;
    }

    public void setPatchNamesList(List <String> patchNamesList) {
        this.patchNamesList =patchNamesList;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public String getKernelVersionNumber() {
        return kernelVersionNumber;
    }

    public void setKernelVersionNumber(String kernelVersionNumber) {
        this.kernelVersionNumber = kernelVersionNumber;
    }

    public String getPlatformVersionNumber() {
        return platformVersionNumber;
    }

    public void setPlatformVersionNumber(String platformVersionNumber) {
        this.platformVersionNumber = platformVersionNumber;
    }

}

