package org.wso2.security.tools.reposcanner.storage;

import org.wso2.security.tools.reposcanner.entiry.Repo;
import org.wso2.security.tools.reposcanner.entiry.RepoArtifact;
import org.wso2.security.tools.reposcanner.entiry.RepoError;

/**
 * Created by ayoma on 4/13/17.
 */
public interface Storage {
    public boolean isRepoPresent(Repo repo) throws Exception;

    public boolean isArtifactPresent(Repo repo, String path) throws Exception;

    public boolean persist(RepoArtifact repoArtifactInfo) throws Exception;

    public boolean persistError(RepoError repoError) throws Exception;

    public void close();

    public boolean isErrorPresent(Repo repo, String path) throws Exception;
}
