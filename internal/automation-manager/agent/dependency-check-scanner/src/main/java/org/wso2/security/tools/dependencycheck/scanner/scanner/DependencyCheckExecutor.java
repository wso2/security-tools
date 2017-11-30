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

package org.wso2.security.tools.dependencycheck.scanner.scanner;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.dependencycheck.scanner.NotificationManager;
import org.wso2.security.tools.dependencycheck.scanner.config.ScannerProperties;
import org.wso2.security.tools.dependencycheck.scanner.exception.DependencyCheckScannerException;
import org.wso2.security.tools.dependencycheck.scanner.exception.NotificationManagerException;
import org.wso2.security.tools.dependencycheck.scanner.handler.FileHandler;
import org.wso2.security.tools.dependencycheck.scanner.handler.GitHandler;

import java.io.File;
import java.io.IOException;
import java.util.Observable;

/**
 * The class {@code DependencyCheckExecutor} extends {@link Observable} and implements {@link Runnable}
 * <p>This class is to handle the product and do the scanning process asynchronously</p>
 */
public class DependencyCheckExecutor extends Observable implements Runnable {

    private static String productPath = ScannerProperties.getDefaultProductFolderPath();
    private final Logger LOGGER = LoggerFactory.getLogger(DependencyCheckExecutor.class);
    private boolean isFileUpload;
    private String zipFileName;
    private String gitUrl;
    private String gitUsername;
    private String gitPassword;

    public DependencyCheckExecutor(boolean isFileUpload, String zipFileName, String gitUrl, String gitUsername,
                                   String gitPassword) {
        this.isFileUpload = isFileUpload;
        this.zipFileName = zipFileName;
        this.gitUrl = gitUrl;
        this.gitUsername = gitUsername;
        this.gitPassword = gitPassword;
    }

    /**
     * Returns the product path (where the product is in)
     *
     * @return Product path
     */
    public static String getProductPath() {
        return productPath;
    }

    /**
     * This method is to set the product path, if the default product path is changed
     * <p>If a file is extracted, a new folder is created. When invoking maven commands, the product path should be
     * defined</p>
     *
     * @param productPath Product path
     */
    private static void setProductPath(String productPath) {
        DependencyCheckExecutor.productPath = productPath;
    }

    /**
     * Overrides the {@code run} method to start the scan.
     * <p>If a zip file is uploaded, then it is extracted. If a GitURL is given, the product is cloned. After
     * product is available from one of the above method, the product source is built. After report is
     * generated, notify observers</p>
     */
    @Override
    public void run() {
        try {
            startScan();
        } catch (DependencyCheckScannerException | NotificationManagerException e) {
            LOGGER.error(e.toString());
            e.printStackTrace();
        }
        setChanged();
        notifyObservers(true);
    }

    private void startScan() throws DependencyCheckScannerException, NotificationManagerException {
        boolean isProductAvailable = false;
        if (isFileUpload) {
            String folderName;
            try {
                folderName = FileHandler.extractZipFile(ScannerProperties.getDefaultProductFolderPath() + File
                        .separator +
                        zipFileName);
                DependencyCheckExecutor.setProductPath(ScannerProperties.getDefaultProductFolderPath() + File
                        .separator + folderName);
                isProductAvailable = true;
                LOGGER.info("File successfully extracted");
                NotificationManager.notifyFileExtracted(true);
            } catch (IOException e) {
                NotificationManager.notifyFileExtracted(false);
                throw new DependencyCheckScannerException("Error occurred while extracting zip file", e);
            }
        } else {
            File productFile = new File(ScannerProperties.getDefaultProductFolderPath());
            Git git;
            if (productFile.exists() || productFile.mkdir()) {
                try {
                    git = GitHandler.gitClone(gitUrl, gitUsername, gitPassword, ScannerProperties
                            .getDefaultProductFolderPath());
                    isProductAvailable = GitHandler.hasAtLeastOneReference(git.getRepository());
                    LOGGER.info("File successfully cloned");
                    NotificationManager.notifyProductCloned(true);
                } catch (GitAPIException e) {
                    NotificationManager.notifyProductCloned(false);
                    throw new DependencyCheckScannerException("Error occurred while cloning product", e);
                }
            }
        }
        if (isProductAvailable) {
            DependencyCheckScanner dependencyCheckScanner = new DependencyCheckScanner();
            try {
                dependencyCheckScanner.runScan();
                LOGGER.info("Dependency Check scan completed");
                NotificationManager.notifyScanStatus(ScannerProperties.getScanStatusCompleted());
            } catch (MavenInvocationException | IOException e) {
                NotificationManager.notifyScanStatus(ScannerProperties.getScanStatusFailed());
                throw new DependencyCheckScannerException("Error occurred while running the scan", e);
            }
        }
    }
}

