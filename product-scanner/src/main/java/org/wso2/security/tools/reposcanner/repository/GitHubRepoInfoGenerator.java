package org.wso2.security.tools.reposcanner.repository;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryTag;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.wso2.security.tools.reposcanner.AppConfig;
import org.wso2.security.tools.reposcanner.downloader.GitHubMasterDownloader;
import org.wso2.security.tools.reposcanner.downloader.GitHubTagDownloader;
import org.wso2.security.tools.reposcanner.downloader.RepoDownloader;
import org.wso2.security.tools.reposcanner.entiry.Repo;
import org.wso2.security.tools.reposcanner.entiry.RepoType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by ayoma on 4/15/17.
 */
public class GitHubRepoInfoGenerator implements RepoInfoGenerator {
    private static Logger log = Logger.getLogger(GitHubRepoInfoGenerator.class.getName());

    private GitHubClient client;

    private RepoDownloader gitMasterDownloader;
    private File masterDownloadFolder;

    private RepoDownloader gitTagDownloader;
    private File tagsDownloadFolder;

    public GitHubRepoInfoGenerator(char[] oAuth2Token) {
        client = new GitHubClient();
        client.setOAuth2Token(new String(oAuth2Token));

        if (AppConfig.isDownloadMaster()) {
            gitMasterDownloader = new GitHubMasterDownloader();
            masterDownloadFolder = new File("source-master");
            if (masterDownloadFolder.exists()) {
                try {
                    FileUtils.deleteDirectory(masterDownloadFolder);
                } catch (IOException e) {
                    log.error("Error in removing master download folder: " + masterDownloadFolder.getAbsolutePath(), e);
                }
            }
            masterDownloadFolder.mkdir();
        }

        if (AppConfig.isDownloadTags()) {
            gitTagDownloader = new GitHubTagDownloader();
            tagsDownloadFolder = new File("source-tags");
            if (tagsDownloadFolder.exists()) {
                try {
                    FileUtils.deleteDirectory(tagsDownloadFolder);
                } catch (IOException e) {
                    log.error("Error in removing tags download folder: " + tagsDownloadFolder.getAbsolutePath(), e);
                }
            }
            tagsDownloadFolder.mkdir();
        }
    }

    @Override
    public List<Repo> getRepoList(String consoleTag, List<String> users) {
        RepositoryService repositoryService = new RepositoryService(client);
        List<Repo> repoList = Collections.synchronizedList(new ArrayList());

        //Get the list of git repositories for each GitHub user account
        users.parallelStream().forEach(user -> {
            log.info(consoleTag + "Fetching repositories for GitHub user account: " + user);
            try {
                List<Repository> userRepositoryList = repositoryService.getRepositories(user);
                log.info(consoleTag + userRepositoryList.size() + " repositories found for user account: " + user);

                if (AppConfig.isDownloadMaster()) {
                    userRepositoryList.parallelStream().forEach(repository -> {
                        try {
                            log.info(consoleTag + "[DownloadMaster] Started downloading master branch of: " + repository.getName());
                            Repo tempRepo = new Repo(RepoType.GIT, repository.getOwner().getLogin(), repository.getName(), repository.getCloneUrl(), null, null, null);
                            gitMasterDownloader.downloadRepo(tempRepo, masterDownloadFolder, false);
                            log.info(consoleTag + "[DownloadMaster] Completed downloading master branch of: " + repository.getName());
                        } catch (Exception e) {
                            log.error("Error in downloading master branch ZIP for GitHub user account: " + user + " repository: " + repository.getName(), e);
                        }
                    });
                }

                if (!AppConfig.isSkipScan() || AppConfig.isDownloadTags()) {
                    //Get the list of tags for each repository
                    userRepositoryList.parallelStream().forEach(repository -> {
                        log.info(consoleTag + "Fetching tags for GitHub user account: " + user + " repository: " + repository.getName());
                        try {
                            List<RepositoryTag> repositoryTagLists = repositoryService.getTags(repository);
                            log.info(consoleTag + repositoryTagLists.size() + " tags found for user account: " + user + " repository:" + repository.getName());

                            //Create persistable Repo object with repository and tag information
                            repositoryTagLists.parallelStream().forEach(repositoryTag -> {
                                Repo repo = new Repo(RepoType.GIT, repository.getOwner().getLogin(), repository.getName(), repository.getCloneUrl(), repositoryTag.getName(), repositoryTag.getZipballUrl(), new Date());
                                repoList.add(repo);

                                if (AppConfig.isDownloadTags()) {
                                    try {
                                        log.info(consoleTag + "[DownloadTags] Started downloading tag: " + repo.getTagName() + " of: " + repository.getName());
                                        gitTagDownloader.downloadRepo(repo, tagsDownloadFolder, false);
                                        log.info(consoleTag + "[DownloadTags] Completed downloading tag: " + repo.getTagName() + " of: " + repository.getName());
                                    } catch (Exception e) {
                                        log.error("Error in downloading master branch ZIP for GitHub user account: " + user + " repository: " + repository.getName(), e);
                                    }
                                }
                            });

                        } catch (Exception e) {
                            log.error("Error in fetching tags for GitHub user account: " + user + " repository: " + repository.getName(), e);
                        }
                    });
                } else {
                    log.warn(consoleTag + "[Skipping] SkipScan parameter is set and tag download is not enabled. Skipping tag information retrieval.");
                }
            } catch (Exception e) {
                log.error("Error in fetching repositories for GitHub user account: " + user, e);
            }
        });

        return repoList;
    }
}
