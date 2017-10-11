package org.wso2.security.tools.reposcanner.repository;

import org.wso2.security.tools.reposcanner.entiry.Repo;

import java.util.List;

/**
 * Created by ayoma on 4/15/17.
 */
public interface RepoInfoGenerator {
    public List<Repo> getRepoList(String consleTag, List<String> users);
}
