package org.wso2.security.tools.reposcanner.downloader;

import org.wso2.security.tools.reposcanner.entiry.Repo;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by ayoma on 4/14/17.
 */
public class GitHubTagDownloader implements RepoDownloader {
    @Override
    public void downloadRepo(Repo repo, File destinationFolder, boolean unzip) throws IOException {
        File tempZipFile = new File(destinationFolder.getAbsoluteFile() + File.separator + repo.getRepositoryName() + "-Tag-" + repo.getTagName() + ".zip");
        DownloadUtil.downloadFile(repo.getTagZip(), tempZipFile);
        if (unzip) {
            ZipUtil.unpack(tempZipFile, destinationFolder);
        }
    }
}
