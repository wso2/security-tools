/*
 *
 *   Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.security.tools.scanner.dependency.js.reportpublisher;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.wso2.security.tools.scanner.dependency.js.constants.JSScannerConstants;
import org.wso2.security.tools.scanner.dependency.js.exception.FileHandlerException;
import org.wso2.security.tools.scanner.dependency.js.utils.ReportWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * This class is responsible to upload reports to git repository. The generated reports for each products are pushed
 * into the security artifact repo.
 */
public class GitUploader extends ReportUploader {
    private static final Logger log = Logger.getLogger(GitUploader.class);

    private char[] gitUsername;
    private char[] gitPassword;
    private Git gitRepo;


    public GitUploader(char[] username, char[] password) throws GitAPIException {
        super();
        this.gitUsername = username.clone();
        this.gitPassword = password.clone();
        File artifactFile = new File(JSScannerConstants.SECURITY_ARTIFACT_HOME);
        if (!artifactFile.exists()) {
            createDirectory(artifactFile);
            gitRepo = Git.cloneRepository()
                    .setURI(JSScannerConstants.SECURITY_ARTIFACT_REPO)
                    .setDirectory(new File(artifactFile.getAbsolutePath()))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(new String(gitUsername),
                            new String(gitPassword)))
                    .call();
            log.info("[JS_SEC_DAILY_SCAN]  " + "Security artifact repo cloned successfully.");
        } else {
            if (gitPull(artifactFile)) {
                log.info("[JS_SEC_DAILY_SCAN]  " + "Security artifact repo pull action performed successfully.");
            } else {
                log.error("[JS_SEC_DAILY_SCAN]  " + "Security artifact repo pull action failed.");
            }
        }
    }


    /**
     * Publish report to any endpoint. currently it supports to github.
     *
     * @param productResponseMapper Mapper for product and scan result.
     * @throws GitAPIException      Exception occurred during github API call.
     * @throws IOException          IO Exception.
     * @throws FileHandlerException Exception occurred during report generation.
     */
    @Override
    public void publishReport(HashMap<String, String> productResponseMapper) throws GitAPIException, IOException,
            FileHandlerException {
        storeFiles(productResponseMapper);
        gitCommit();
        gitPush();
    }

    /**
     * Add generated reports to git repo
     *
     * @param productResponseMapper Mapper for product and scan result.
     * @throws GitAPIException      Exception occurred during github API call.
     * @throws IOException          IO Exception.
     * @throws FileHandlerException Exception occurred during report generation.
     */
    private void storeFiles(HashMap<String, String> productResponseMapper) throws GitAPIException, IOException,
            FileHandlerException {
        Repository repository = gitRepo.getRepository();

        File targetDir = new File(repository.getDirectory().getParentFile().getAbsolutePath()
                + JSScannerConstants.SCN_REPORT_DIRECTORY_PATH);
        HashMap<String, String> fileMapper = ReportWriter.callWriter(productResponseMapper, targetDir);
        // Stage all files in the repo including new files
        gitRepo.add().addFilepattern(".").call();
        this.setReportFileMapper(fileMapper);
        log.info("[JS_SEC_DAILY_SCAN]  " + "Added files to Security artifact repo");

    }

    /**
     * perform git commit.
     *
     * @throws GitAPIException Exception occurred during gitcommit.
     */
    private void gitCommit() throws GitAPIException {
        // and then commit the changes.
        gitRepo.commit()
                .setMessage("test commit: Added vulnerability report " + java.time.LocalDate.now().toString())
                .call();
        log.info("[JS_SEC_DAILY_SCAN]  " + "Commit to Security artifact repo");
    }

    /**
     * perform git push.
     *
     * @throws GitAPIException Exception occurred during gitcommit.
     */
    private void gitPush() throws GitAPIException {
        gitRepo.push()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(new String(gitUsername),
                        new String(gitPassword)))
                .call();
        log.info("[JS_SEC_DAILY_SCAN]  " + "Push to Security artifact repo");
    }

    /**
     * Create directory.
     *
     * @param dir target directory.
     */
    private void createDirectory(File dir) {

        if (!dir.exists()) {
            boolean isDirCreated = dir.mkdir();
            if (!isDirCreated) {
                log.error((dir.getAbsolutePath() + " is not created"));
            }
        }
    }

    /**
     * Execute git pull command on the given repository.
     * It populates an ArrayList with all the updated files.
     *
     * @param localPath The path where the project is.
     * @return Returns true if you should update plugins, false otherwise.
     */
    private boolean gitPull(File localPath) {
        try {
            Repository localRepo = new FileRepository(localPath.getAbsolutePath() + "/.git");
            gitRepo = new Git(localRepo);


            if (populateDiff()) {
                PullCommand pullCmd = gitRepo.pull();
                pullCmd.call();
                return true;
            } else {
                return false;
            }

        } catch (GitAPIException | IOException ex) {
            log.error("Failed to pull : " + localPath.getName());
        }
        return true;
    }

    /**
     * Populate all the files to update, if the system should update.
     */
    private boolean populateDiff() {
        try {
            gitRepo.fetch().call();
            Repository repo = gitRepo.getRepository();
            ObjectId fetchHead = repo.resolve("FETCH_HEAD^{tree}");
            ObjectId head = repo.resolve("HEAD^{tree}");

            ObjectReader reader = repo.newObjectReader();
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, head);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, fetchHead);
            List<DiffEntry> diffs = gitRepo.diff().setShowNameAndStatusOnly(true)
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call();

            return !diffs.isEmpty();
        } catch (GitAPIException | IOException ex) {
            log.error("Unable to populate all the files to update.");
        }
        return true; //assume true
    }
}
