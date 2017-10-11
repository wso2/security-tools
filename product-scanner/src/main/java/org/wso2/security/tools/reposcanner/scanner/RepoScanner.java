package org.wso2.security.tools.reposcanner.scanner;

import org.wso2.security.tools.reposcanner.storage.Storage;

/**
 * Created by ayoma on 4/15/17.
 */
public interface RepoScanner {
    public void scan(Storage storage) throws Exception;
}
