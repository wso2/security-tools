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

package org.wso2.security.tools.reposcanner.scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;
import org.wso2.security.tools.reposcanner.AppConfig;
import org.wso2.security.tools.reposcanner.artifact.ArtifactInfoGenerator;
import org.wso2.security.tools.reposcanner.artifact.MavenArtifactInfoGenerator;
import org.wso2.security.tools.reposcanner.downloader.GitHubTagDownloader;
import org.wso2.security.tools.reposcanner.downloader.RepoDownloader;
import org.wso2.security.tools.reposcanner.entiry.Repo;
import org.wso2.security.tools.reposcanner.entiry.RepoArtifact;
import org.wso2.security.tools.reposcanner.entiry.RepoError;
import org.wso2.security.tools.reposcanner.repository.GitHubRepoInfoGenerator;
import org.wso2.security.tools.reposcanner.repository.RepoInfoGenerator;
import org.wso2.security.tools.reposcanner.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GitHubRepoScanner implements RepoScanner {
    private static Logger log = Logger.getLogger(GitHubRepoScanner.class.getName());

    private char[] oAuth2Token;

    public GitHubRepoScanner(char[] oAuth2Token) {
        this.oAuth2Token = oAuth2Token;
    }

    public void scan(Storage storage) throws Exception {
        String consoleTag = "[GIT] ";
        log.info(consoleTag + "GIT repository scanning started.");

        //BuildConfigLocator buildConfigLocator = new MavenBuildConfigLocator();
        ArtifactInfoGenerator mavenArtifactInfoGenerator = new MavenArtifactInfoGenerator();
        RepoDownloader gitRepoDownloader = new GitHubTagDownloader();

        //Create temp folder for storing downloaded repository content
        File gitTempFolder = new File("temp-git");
        if (gitTempFolder.exists()) {
            FileUtils.deleteDirectory(gitTempFolder);
        }
        if (gitTempFolder.mkdir()) {
            log.info(consoleTag + "Temporary folder created at: " + gitTempFolder.getAbsolutePath());
        } else {
            log.error(consoleTag + "Unable to create temporary folder at: " + gitTempFolder.getAbsolutePath());
            return;
        }

        //Get list of repositories from GitHub
        RepoInfoGenerator repoInfoGenerator = new GitHubRepoInfoGenerator(oAuth2Token);
        List<Repo> repoList = null;
        if (AppConfig.getGithubAccounts() == null || AppConfig.getGithubAccounts().isEmpty()) {
            log.error(consoleTag + "No GitHub user accounts provided for the scan. Terminating...");
            return;
        } else {
            repoList = repoInfoGenerator.getRepoList(consoleTag, AppConfig.getGithubAccounts());
        }

        if (!AppConfig.isSkipScan()) {
            repoList.parallelStream().forEach(repo -> {
                String newConsoleTag = consoleTag + "[User:" + repo.getUser() + ",Repo:" + repo.getRepositoryUrl() + ",Tag:" + repo.getTagName() + "] ";
                try {
                    if (AppConfig.isRescanRepos() || !storage.isRepoPresent(repo)) {

                        log.info(newConsoleTag + "[Adding] Adding repo to scanning pool");

                        //Create folder to store files from Github
                        String identifier = repo.getRepositoryName() + "-Tag-" + repo.getTagName();
                        File artifactTempFolder = new File(gitTempFolder.getAbsoluteFile() + File.separator + identifier);
                        artifactTempFolder.mkdir();
                        log.info(consoleTag + "Temporary folder created at: " + artifactTempFolder.getAbsolutePath());

                        try {
                            //Download from GitHub and extract ZIP
                            log.info(consoleTag + "Downloading started");
                            gitRepoDownloader.downloadRepo(repo, artifactTempFolder);
                            log.info(consoleTag + "Downloading completed");

                            //Locate POM files within the extracted ZIP
                            log.info(consoleTag + "POM searching started");
                            Collection<File> sourceFiles = FileUtils.listFiles(artifactTempFolder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
                            List<File> mavenBuildConfigFiles = sourceFiles.stream().filter(file -> file.getName().equals("pom.xml")).collect(Collectors.toList());
                            log.info(consoleTag + "POM searching completed");

                            //Execute maven executor plugin on each POM to get Maven ID (groupId, artifactId, packaging, version)
                            ExecutorService artifactWorkerExecutorService = Executors.newWorkStealingPool();

                            List<Callable<RepoArtifact>> callableArrayList = new ArrayList<>();

                            mavenBuildConfigFiles.parallelStream().forEach(mavenBuildConfigFile -> {
                                try {
                                    String path = mavenBuildConfigFile.getAbsolutePath().substring(artifactTempFolder.getAbsolutePath().length(), mavenBuildConfigFile.getAbsolutePath().length());
                                    path = path.substring(path.indexOf(File.separator, 1), path.length());
                                    String finalPath = path.replace("pom.xml", "");

                                    String pathIncludedConsoleTag = consoleTag + "[" + path + "] ";

                                    //If this is a repo re-scan, only the artifacts that are not already indexed should be scanned.
                                    //If thus is not a repo re-scan, repo itself will be skipped if it is already indexed
                                    boolean scanArtifact = true;
                                    if (AppConfig.isRescanRepos() && (storage.isArtifactPresent(repo, path) || storage.isErrorPresent(repo, path))) {
                                        scanArtifact = false;
                                    }
                                    if (scanArtifact) {
                                        log.info(pathIncludedConsoleTag + "[Adding] Adding POM for artifact information gathering pool");
                                        Callable<RepoArtifact> callable = () -> {
                                            try {
                                                RepoArtifact repoArtifactInfo = mavenArtifactInfoGenerator.getArtifact(consoleTag, repo, artifactTempFolder, mavenBuildConfigFile);
                                                log.info(consoleTag + "Maven ID extracted. Sending for storage.");
                                                return repoArtifactInfo;
                                            } catch (Exception e) {
                                                RepoError repoError = new RepoError(finalPath, "MavenID not found", repo, new Date());
                                                storage.persistError(repoError);

                                                if (AppConfig.isVerbose()) {
                                                    log.warn(consoleTag + "[Skipping] Could not extract Maven ID from Maven executor", e);
                                                } else {
                                                    log.warn(consoleTag + "[Skipping] Could not extract Maven ID from Maven executor");
                                                }
                                                return null;
                                            }
                                        };
                                        callableArrayList.add(callable);
                                    } else {
                                        log.warn(pathIncludedConsoleTag + "[Skipping] Artifact is already present in storage.");
                                    }
                                } catch (Exception e) {
                                    log.error(consoleTag + "Exception in extracting artifact information for repository: " + repo + " config file: " + mavenBuildConfigFile.getAbsolutePath(), e);
                                }
                            });

                            artifactWorkerExecutorService.invokeAll(callableArrayList).parallelStream().forEach(artifactFuture -> {
                                RepoArtifact repoArtifact = null;
                                try {
                                    repoArtifact = artifactFuture.get();
                                    if (repoArtifact != null) {
                                        storage.persist(repoArtifact);
                                    }
                                } catch (Exception e) {
                                    log.error(consoleTag + "Exception in persisting Artifact: " + repoArtifact, e);
                                }
                            });

                            //Do cleanup and storage release
                            log.info(consoleTag + "All threads complete. Clean up tasks started.");
                            log.info(consoleTag + "Deleting: " + artifactTempFolder.getAbsolutePath());
                            FileUtils.deleteDirectory(artifactTempFolder);
                        } catch (Exception e) {
                            try {
                                FileUtils.deleteDirectory(artifactTempFolder);
                            } catch (IOException e1) {
                                log.warn("Exception in removing temp folder: " + artifactTempFolder.getAbsolutePath());
                            }
                            log.error(consoleTag + "Git repository scanning failed: " + identifier, e);
                        }

                    } else {
                        log.warn(newConsoleTag + "[Skipping] Repo is already present in storage.");
                    }
                } catch (Exception e) {
                    log.error(consoleTag + "Exception in scanning repository: " + repo, e);
                }
            });
        } else {
            log.warn(consoleTag + "[Skipping] SkipScan parameter is set.");
        }

        //Do cleanup and storage release
        log.info(consoleTag + "All threads complete. Clean up tasks started.");
        FileUtils.deleteDirectory(gitTempFolder);
    }
}
