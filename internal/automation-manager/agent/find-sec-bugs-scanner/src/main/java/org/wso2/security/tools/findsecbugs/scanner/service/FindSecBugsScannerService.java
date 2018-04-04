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

package org.wso2.security.tools.findsecbugs.scanner.service;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.findsecbugs.scanner.Constants;
import org.wso2.security.tools.findsecbugs.scanner.NotificationManager;
import org.wso2.security.tools.findsecbugs.scanner.exception.FindSecBugsScannerException;
import org.wso2.security.tools.findsecbugs.scanner.exception.NotificationManagerException;
import org.wso2.security.tools.findsecbugs.scanner.handler.FileHandler;
import org.wso2.security.tools.findsecbugs.scanner.handler.GitHandler;
import org.wso2.security.tools.findsecbugs.scanner.scanner.FindSecBugsScannerExecutor;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observer;

/**
 * The class {@code FindSecBugsScannerService} contains methods regards to scanner.
 */
@Service
public class FindSecBugsScannerService {
    private final Logger LOGGER = LoggerFactory.getLogger(FindSecBugsScannerService.class);

    private boolean configureNotificationManager(String containerId, String automationManagerHost, int
            automationManagerPort) {
        NotificationManager.configure(containerId, automationManagerHost, automationManagerPort);
        return NotificationManager.isConfigured();
    }

    /**
     * Start a new thread to run scans asynchronously
     * <p>
     * <p>Configure {@link NotificationManager}, validates a request, uploads a zip file if available and start a new
     * thread to start scan. An {@link Observer} is created and observes till the scan is over and report is ready.
     * then the report folder is zipped</p>
     *
     * @param automationManagerHost Automation Manager host
     * @param automationManagerPort Automation Manager port
     * @param containerId           Container Id
     * @param isFileUpload          Boolean to indicate whether the file is uploaded via a zip file. False means file
     *                              should be cloned from GitHub
     * @param zipFile               Zip file of the product source code
     * @param gitUrl                GitHub URL if the product has to clone
     * @param gitUsername           GitHub username if a private repository
     * @param gitPassword           GitHub password if a private repository
     * @throws FindSecBugsScannerException
     */
    public void startScan(String automationManagerHost, int automationManagerPort, String containerId, boolean
            isFileUpload, MultipartFile zipFile, String gitUrl, String gitUsername, String gitPassword) throws
            FindSecBugsScannerException {
        String zipFileName = null;
        File productFolder = new File(Constants.DEFAULT_PRODUCT_PATH);
        try {
            validate(automationManagerHost, automationManagerPort, containerId, isFileUpload, zipFile, gitUrl);
            if (isFileUpload) {
                try {
                    zipFileName = zipFile.getOriginalFilename();
                    uploadZipFile(zipFile, productFolder);
                    NotificationManager.notifyFileUploaded(true);
                } catch (IOException e) {
                    NotificationManager.notifyFileUploaded(false);
                    throw new FindSecBugsScannerException("Error occurred while uploading zip file", e);
                }
            } else {
                try {
                    GitHandler.gitClone(gitUrl, gitUsername, gitPassword, Constants.DEFAULT_PRODUCT_PATH);
                    NotificationManager.notifyProductCloned(true);
                } catch (GitAPIException e) {
                    NotificationManager.notifyProductCloned(false);
                    throw new FindSecBugsScannerException("Error occurred while cloning", e);
                }
            }
            Observer mainScannerObserver = observe();
            FindSecBugsScannerExecutor findSecBugsScannerExecutor = new FindSecBugsScannerExecutor(isFileUpload,
                    zipFileName, gitUrl, gitUsername, gitPassword);
            findSecBugsScannerExecutor.addObserver(mainScannerObserver);
            new Thread(findSecBugsScannerExecutor).start();
        } catch (NotificationManagerException e) {
            LOGGER.error(e.toString());
        }
    }

    private void validate(String automationManagerHost, int automationManagerPort, String containerId, boolean
            isFileUpload, MultipartFile zipFile, String gitUrl) throws FindSecBugsScannerException {
        if (!configureNotificationManager(containerId, automationManagerHost, automationManagerPort)) {
            throw new FindSecBugsScannerException("Notification Manager not configured");
        }
        if (isFileUpload) {
            if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                throw new FindSecBugsScannerException("No zip file available");
            }
        } else {
            if (gitUrl == null) {
                throw new FindSecBugsScannerException("Git URL is not defined");
            }
        }
    }

    private void uploadZipFile(MultipartFile zipFile, File productFolder) throws IOException,
            NotificationManagerException {
        String zipFileName = zipFile.getOriginalFilename();
        if (productFolder.exists() || productFolder.mkdir()) {
            String fileUploadPath = Constants.DEFAULT_PRODUCT_PATH + File.separator + zipFileName;
            FileHandler.uploadFile(zipFile, fileUploadPath);
            LOGGER.info("File successfully uploaded");
            NotificationManager.notifyFileUploaded(true);
        }
    }

    private Observer observe() {
        return (o, arg) -> {
            try {
                if (new File(Constants.REPORTS_FOLDER_PATH + File.separator + Constants
                        .FIND_SEC_BUGS_REPORTS_FOLDER + Constants.ZIP_FILE_EXTENSION).exists()) {
                    NotificationManager.notifyReportReady(true);
                } else {
                    NotificationManager.notifyReportReady(false);
                }
            } catch (NotificationManagerException e) {
                LOGGER.error(e.toString());
            }
        };
    }

    /**
     * Get the generated report
     *
     * @param response HttpServletResponse
     * @throws FindSecBugsScannerException
     */
    public void getReport(HttpServletResponse response) throws FindSecBugsScannerException {
        String reportsPath = Constants.REPORTS_FOLDER_PATH + Constants.ZIP_FILE_EXTENSION;
        try {
            InputStream inputStream = new FileInputStream(reportsPath);
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
            LOGGER.info("Successfully written to output stream");

        } catch (IOException e) {
            throw new FindSecBugsScannerException("Error occurred while accessing reports file", e);
        }
    }
}
