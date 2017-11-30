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

package org.wso2.security.tools.dependencycheck.scanner.service;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.dependencycheck.scanner.Constants;
import org.wso2.security.tools.dependencycheck.scanner.NotificationManager;
import org.wso2.security.tools.dependencycheck.scanner.config.ScannerProperties;
import org.wso2.security.tools.dependencycheck.scanner.exception.DependencyCheckScannerException;
import org.wso2.security.tools.dependencycheck.scanner.exception.NotificationManagerException;
import org.wso2.security.tools.dependencycheck.scanner.handler.FileHandler;
import org.wso2.security.tools.dependencycheck.scanner.handler.GitHandler;
import org.wso2.security.tools.dependencycheck.scanner.scanner.DependencyCheckExecutor;

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
public class DependencyCheckScannerService {

    private final Logger LOGGER = LoggerFactory.getLogger(DependencyCheckScannerService.class);

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
     * @throws DependencyCheckScannerException
     */
    public void startScan(String automationManagerHost, int automationManagerPort, String containerId, boolean
            isFileUpload, MultipartFile zipFile, String gitUrl, String gitUsername, String gitPassword) throws
            DependencyCheckScannerException {
        String zipFileName = null;
        File productFolder = new File(ScannerProperties.getDefaultProductFolderPath());
        try {
            validate(automationManagerHost, automationManagerPort, containerId, isFileUpload, zipFile, gitUrl);
            if (isFileUpload) {
                try {
                    zipFileName = zipFile.getOriginalFilename();
                    uploadZipFile(zipFile, productFolder);
                    NotificationManager.notifyFileUploaded(true);
                } catch (IOException e) {
                    NotificationManager.notifyFileUploaded(false);
                    throw new DependencyCheckScannerException("Error occurred while uploading zip file", e);
                }
            } else {
                try {
                    GitHandler.gitClone(gitUrl, gitUsername, gitPassword, ScannerProperties
                            .getDefaultProductFolderPath());
                    NotificationManager.notifyProductCloned(true);
                } catch (GitAPIException e) {
                    NotificationManager.notifyProductCloned(false);
                    throw new DependencyCheckScannerException("Error occurred while cloning", e);
                }
            }
            Observer mainScannerObserver = observe();
            DependencyCheckExecutor dependencyCheckExecutor = new DependencyCheckExecutor(isFileUpload, zipFileName,
                    gitUrl, gitUsername, gitPassword);
            dependencyCheckExecutor.addObserver(mainScannerObserver);
            new Thread(dependencyCheckExecutor).start();
        } catch (NotificationManagerException e) {
            LOGGER.error(e.toString());
        }
    }

    private void validate(String automationManagerHost, int automationManagerPort, String containerId, boolean
            isFileUpload, MultipartFile zipFile, String gitUrl) throws DependencyCheckScannerException {
        if (!configureNotificationManager(containerId, automationManagerHost, automationManagerPort)) {
            throw new DependencyCheckScannerException("Notification Manager not configured");
        }
        if (isFileUpload) {
            if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                throw new DependencyCheckScannerException("No zip file available");
            }
        } else {
            if (gitUrl == null) {
                throw new DependencyCheckScannerException("Git URL is not defined");
            }
        }
    }

    private void uploadZipFile(MultipartFile zipFile, File productFolder) throws IOException,
            NotificationManagerException {
        String zipFileName = zipFile.getOriginalFilename();
        if (productFolder.exists() || productFolder.mkdir()) {
            String fileUploadPath = ScannerProperties.getDefaultProductFolderPath() + File.separator + zipFileName;
            FileHandler.uploadFile(zipFile, fileUploadPath);
            LOGGER.info("File successfully uploaded");
            NotificationManager.notifyFileUploaded(true);
        }
    }

    private Observer observe() {
        return (o, arg) -> {
            try {
                if (new File(ScannerProperties.getReportsFolderPath() + Constants.ZIP_FILE_EXTENSION).exists()) {
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
     * @throws DependencyCheckScannerException
     */
    public void getReport(HttpServletResponse response) throws DependencyCheckScannerException {
        String reportsPath = ScannerProperties.getReportsFolderPath() + Constants.ZIP_FILE_EXTENSION;
        try {
            InputStream inputStream = new FileInputStream(reportsPath);
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
            LOGGER.info("Successfully written to output stream");
        } catch (IOException e) {
            throw new DependencyCheckScannerException("Error occurred while accessing reports file", e);
        }
    }
}
