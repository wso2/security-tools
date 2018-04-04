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

package org.wso2.security.tools.findsecbugs.scanner.scanner;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.findsecbugs.scanner.Constants;
import org.wso2.security.tools.findsecbugs.scanner.NotificationManager;
import org.wso2.security.tools.findsecbugs.scanner.exception.FindSecBugsScannerException;
import org.wso2.security.tools.findsecbugs.scanner.exception.NotificationManagerException;
import org.wso2.security.tools.findsecbugs.scanner.handler.FileHandler;
import org.wso2.security.tools.findsecbugs.scanner.handler.GitHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.Observable;

/**
 * The class {@code FindSecBugsScannerExecutor} extends {@link Observable} and implements {@link Runnable}
 * <p>This class is to handle the product and do the scanning process asynchronously</p>
 */
public class FindSecBugsScannerExecutor extends Observable implements Runnable {

    private static String productPath = Constants.DEFAULT_PRODUCT_PATH;
    private final Logger LOGGER = LoggerFactory.getLogger(FindSecBugsScannerExecutor.class);
    private boolean isFileUpload;
    private String zipFileName;
    private String gitUrl;
    private String gitUsername;
    private String gitPassword;

    public FindSecBugsScannerExecutor(boolean isFileUpload, String zipFileName, String gitUrl, String gitUsername,
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
        FindSecBugsScannerExecutor.productPath = productPath;
    }

    /**
     * Overrides the {@code run} method to start the scan.
     * <p>If a zip file is uploaded, then it is extracted. If a GitURL is given, the product is cloned. After
     * product is available from one of the above method, modifies the pom.xml file of the product to add {@code
     * FindBugs} plugin included with {@code FindSecBugs} plugin. Then the product source is built. After report is
     * generated, notify observers</p>
     */
    @Override
    public void run() {
        try {
            startScan();
        } catch (FindSecBugsScannerException | NotificationManagerException e) {
            LOGGER.error(e.toString());
            e.printStackTrace();
        }
        setChanged();
        notifyObservers(true);
    }

    private void startScan() throws FindSecBugsScannerException, NotificationManagerException {
        boolean isProductAvailable = false;
        if (isFileUpload) {
            String folderName;
            try {
                folderName = FileHandler.extractZipFile(Constants.DEFAULT_PRODUCT_PATH + File.separator +
                        zipFileName);
                FindSecBugsScannerExecutor.setProductPath(Constants.DEFAULT_PRODUCT_PATH + File.separator + folderName);
                isProductAvailable = true;
                LOGGER.info("File successfully extracted");
                NotificationManager.notifyFileExtracted(true);
            } catch (IOException e) {
                NotificationManager.notifyFileExtracted(false);
                throw new FindSecBugsScannerException("Error occurred while extracting zip file", e);
            }
        } else {
            File productFile = new File(Constants.DEFAULT_PRODUCT_PATH);
            Git git;
            if (productFile.exists() || productFile.mkdir()) {
                try {
                    git = GitHandler.gitClone(gitUrl, gitUsername, gitPassword, Constants.DEFAULT_PRODUCT_PATH);
                    isProductAvailable = GitHandler.hasAtLeastOneReference(git.getRepository());
                    LOGGER.info("File successfully cloned");
                    NotificationManager.notifyProductCloned(true);
                } catch (GitAPIException e) {
                    NotificationManager.notifyProductCloned(false);
                    throw new FindSecBugsScannerException("Error occurred while cloning product", e);
                }
            }
        }
        if (isProductAvailable) {
            FindSecBugsScanner findSecBugsScanner = new FindSecBugsScanner();
            try {
                findSecBugsScanner.runScan();
            } catch (MavenInvocationException | TransformerException | IOException | ParserConfigurationException |
                    SAXException e) {
                NotificationManager.notifyScanStatus("failed");
                throw new FindSecBugsScannerException("Error occurred while running the scan", e);
            }
        }
    }
}

