/*
 *  Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.security.tools.scanner.handler;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.wso2.security.tools.scanner.config.ConfigurationReader;
import org.wso2.security.tools.scanner.exception.ScannerException;
import org.wso2.security.tools.scanner.utils.ScannerConstants;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility class for handling Git.
 *
 * @see org.eclipse.jgit
 */
public class GitHandler {

    private GitHandler() {
    }

    /**
     * Clone from GitHub and returns a {@link Git} object. If the URL is related to a private
     * repository, GitHub account credentials should be given.
     *
     * @param gitURL   GitHub URL to clone the product. By default master branch is cloned.
     *                 If a specific branch or tag to be cloned, the specified URL for
     *                 the branch or tag should be given
     * @param filePath Set directory to clone the product
     * @param branch   Set branch to be cloned
     * @return {@link Git} object
     * @throws ScannerException GitAPIException thrown by {@link org.eclipse.jgit}
     */
    public static Git gitClone(String gitURL, String filePath, String branch)
            throws ScannerException {
        String username = ConfigurationReader.getConfigProperty(ScannerConstants.GIT_USERNAME);
        char[] password = ConfigurationReader.getConfigProperty(ScannerConstants.GIT_PASSWORD).
                toCharArray();

        if (gitURL.contains("/tree/")) {
            branch = gitURL.substring(gitURL.lastIndexOf('/') + 1);
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

        for (int i = 0; i < password.length; i++) {
            password[i] = '\0';
        }

        try {
            return cloneCommand.call();
        } catch (GitAPIException e) {
            throw new ScannerException("Error occured while checkout the Git branch", e);
        }
    }

    /**
     * Checkout Git repository to a specific branch in a given Git repository.
     *
     * @param projectDir Git repository location
     * @param branch     Git branch that needs to checkout
     * @return Boolean to indicate whether a git checkout is success
     * @throws ScannerException IOException
     */
    public static boolean gitCheckout(String projectDir, String branch) throws ScannerException {
        try (Git git = Git.open(new File(projectDir))) {
            git.checkout().setName(branch);

            return true;
        } catch (IOException e) {
            throw new ScannerException("Error occured while checkout the Git branch", e);
        }
    }

    /**
     * Check whether a git clone operation is success.
     *
     * @param repo Repository to check
     * @return Boolean to indicate whether a git clone operation is success
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
     * Open the local git repository.
     *
     * @param productPath The repository to open. May be either the GIT_DIR, or the working tree directory that
     *                    contains {@code .git}
     * @return {@link Git} object for the existing git repository
     * @throws IOException
     */
    public static Git gitOpen(String productPath) throws IOException {
        return Git.open(new File(productPath));
    }
}
