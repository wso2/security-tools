package org.wso2.security.tools.reposcanner.artifact;

import org.wso2.security.tools.reposcanner.entiry.Repo;
import org.wso2.security.tools.reposcanner.entiry.RepoArtifact;

import java.io.File;

/**
 * Created by ayoma on 4/15/17.
 */
public interface ArtifactInfoGenerator {
    public RepoArtifact getArtifact(String consoleTag, Repo repo, File baseFolder, File configFile) throws Exception;
}
