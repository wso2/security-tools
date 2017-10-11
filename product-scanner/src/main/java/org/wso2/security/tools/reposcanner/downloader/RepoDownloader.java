package org.wso2.security.tools.reposcanner.downloader;

import org.wso2.security.tools.reposcanner.entiry.Repo;

import java.io.File;

/**
 * Created by ayoma on 4/14/17.
 */
public interface RepoDownloader {
    public void downloadRepo(Repo repo, File destinationFolder, boolean unzip) throws Exception;

    default void downloadRepo(Repo repo, File destinationFolder) throws Exception {
        downloadRepo(repo, destinationFolder, true);
    }
}
