/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.security.tools.findsecbugs.scanner.handler;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility methods for handling Git
 *
 * @see org.eclipse.jgit
 */
@SuppressWarnings({"unused"})
public class GitHandler {

    /**
     * Clone from GitHub and returns a {@link Git} object. If the URL is related to a private
     * repository, GitHub account credentials should be given
     *
     * @param gitURL   GitHub URL to clone the product. By default master branch is cloned. If a specific branch or
     *                 tag to be cloned, the specified URL for the branch or tag should be given
     * @param username GitHub user name if the product is in a private repository
     * @param password GitHub password if the product is in private repository
     * @param filePath Set directory to clone the product
     * @return {@link Git} object
     * @throws GitAPIException Exceptions thrown by {@link org.eclipse.jgit}
     */
    public static Git gitClone(String gitURL, String username, String password, String filePath) throws
            GitAPIException {
        String branch = "master";
        if (gitURL.contains("/tree/")) {
            branch = gitURL.substring(gitURL.lastIndexOf("/") + 1);
            gitURL = gitURL.substring(0, gitURL.indexOf("/tree/"));
        }
        CloneCommand cloneCommand = Git.cloneRepository()
                .setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out)))
                .setURI(gitURL)
                .setDirectory(new File(filePath))
                .setBranch(branch);
        if (username != null && password != null) {
            cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
        }
        return cloneCommand.call();
    }

    /**
     * Check whether a git clone operation is success
     *
     * @param repo Repository to check
     * @return Boolean to indicate whether a git operation is success
     */
    public static boolean hasAtLeastOneReference(Repository repo) {
        for (Ref ref : repo.getAllRefs().values()) {
            if (ref.getObjectId() == null) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * Perform Git checkout operation and returns a {@link Ref} object
     *
     * @param tagOrBranch Tag or branch to checkout
     * @param git         Git object to checkout (When a Git clone operation is done, a Git object is returned. It
     *                    should be passed to checkout)
     * @return a reference object to the branch/ tag
     * @throws GitAPIException Exceptions thrown by {@link org.eclipse.jgit}
     */
    private static Ref gitCheckout(String tagOrBranch, Git git) throws GitAPIException {
        return git.checkout().setName(tagOrBranch).call();
    }

    /**
     * @param productPath The repository to open. May be either the GIT_DIR, or the working tree directory that
     *                    contains {@code .git}
     * @return {@link Git} object for the existing git repository
     * @throws IOException
     */
    public static Git gitOpen(String productPath) throws IOException {
        return Git.open(new File(productPath));
    }
}
