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
 *
 */

package org.wso2.security.tools.scanner.dependency.js.model;

/**
 * Attributes of GitUploader Properties.
 */
public class GitUploaderProperties {
    private char[] gitUsername;
    private char[] gitPassword;
    private String repoURL;

    public GitUploaderProperties(char[] gitUsername, char[] gitPassword, String repoURL) {
        this.gitUsername = gitUsername;
        this.gitPassword = gitPassword;
        this.repoURL = repoURL;
    }

    public char[] getGitUsername() {
        return gitUsername;
    }

    public void setGitUsername(char[] gitUsername) {
        this.gitUsername = gitUsername;
    }

    public char[] getGitPassword() {
        return gitPassword;
    }

    public void setGitPassword(char[] gitPassword) {
        this.gitPassword = gitPassword;
    }

    public String getRepoURL() {
        return repoURL;
    }

    public void setRepoURL(String repoURL) {
        this.repoURL = repoURL;
    }
}
