/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.security.tools.reposcanner;

import java.util.ArrayList;
import java.util.List;

public class AppConfig {
    private static boolean verbose;
    private static boolean debug;
    private static boolean createDB;
    private static boolean rescanRepos;
    private static boolean downloadMaster;
    private static boolean downloadTags;
    private static boolean skipScan;

    private static String mavenHome;
    private static List<String> githubAccounts;
    private static List<String> mavenOutputSkipPatterns;

    public static boolean isVerbose() {
        return verbose;
    }

    public static void setVerbose(boolean verbose) {
        AppConfig.verbose = verbose;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        AppConfig.debug = debug;
    }

    public static boolean isCreateDB() {
        return createDB;
    }

    public static void setCreateDB(boolean createDB) {
        AppConfig.createDB = createDB;
    }

    public static boolean isRescanRepos() {
        return rescanRepos;
    }

    public static void setRescanRepos(boolean rescanRepos) {
        AppConfig.rescanRepos = rescanRepos;
    }

    public static String getMavenHome() {
        return mavenHome;
    }

    public static void setMavenHome(String mavenHome) {
        AppConfig.mavenHome = mavenHome;
    }

    public static List<String> getGithubAccounts() {
        return githubAccounts;
    }

    public static void setGithubAccounts(List<String> githubAccounts) {
        AppConfig.githubAccounts = githubAccounts;
    }

    public static void addGithubAccount(String githubAccount) {
        if (AppConfig.githubAccounts == null) {
            AppConfig.githubAccounts = new ArrayList<>();
        }
        AppConfig.githubAccounts.add(githubAccount);
    }

    public static List<String> getMavenOutputSkipPatterns() {
        return mavenOutputSkipPatterns;
    }

    public static void setMavenOutputSkipPatterns(List<String> mavenOutputSkipPatterns) {
        AppConfig.mavenOutputSkipPatterns = mavenOutputSkipPatterns;
    }

    public static void addMavenOutputSkipPattern(String mavenOutputSkipPattern) {
        if (AppConfig.mavenOutputSkipPatterns == null) {
            AppConfig.mavenOutputSkipPatterns = new ArrayList<>();
        }
        AppConfig.mavenOutputSkipPatterns.add(mavenOutputSkipPattern);
    }

    static {
        AppConfig.addMavenOutputSkipPattern("[");
        AppConfig.addMavenOutputSkipPattern("Download");
    }

    public static boolean isDownloadMaster() {
        return downloadMaster;
    }

    public static void setDownloadMaster(boolean downloadMaster) {
        AppConfig.downloadMaster = downloadMaster;
    }

    public static boolean isDownloadTags() {
        return downloadTags;
    }

    public static void setDownloadTags(boolean downloadTags) {
        AppConfig.downloadTags = downloadTags;
    }

    public static boolean isSkipScan() {
        return skipScan;
    }

    public static void setSkipScan(boolean skipScan) {
        AppConfig.skipScan = skipScan;
    }
}
