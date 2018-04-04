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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.dependencycheck.scanner.Constants;
import org.wso2.security.tools.dependencycheck.scanner.NotificationManager;
import org.wso2.security.tools.dependencycheck.scanner.config.ScannerProperties;
import org.wso2.security.tools.dependencycheck.scanner.exception.NotificationManagerException;
import org.wso2.security.tools.dependencycheck.scanner.handler.FileHandler;
import org.wso2.security.tools.dependencycheck.scanner.handler.MavenHandler;

import java.io.File;
import java.io.IOException;

/**
 * This class provides methods to run DependencyCheck scan
 */
public class DependencyCheckScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyCheckScanner.class);

    /**
     * This method is to build the product with Dependency Check Maven command. Then find, rename and replace
     * the generated reports to a folder
     *
     * @throws MavenInvocationException
     * @throws IOException
     * @throws NotificationManagerException
     */
    public void runScan() throws MavenInvocationException, IOException, NotificationManagerException {
        File reportsFolder = new File(ScannerProperties.getReportsFolderPath());
        LOGGER.info("Dependency Check started");
        NotificationManager.notifyScanStatus(ScannerProperties.getScanStatusRunning());
        MavenHandler.runMavenCommand(DependencyCheckExecutor.getProductPath() + File.separator + Constants.POM_FILE,
                ScannerProperties.getDependencyCheckMavenCommand());
        if (reportsFolder.exists() || reportsFolder.mkdir()) {
            String reportsFolderPath = ScannerProperties.getReportsFolderPath() + File.separator + ScannerProperties
                    .getDependencyCheckReportsFolder();
            FileHandler.findFilesRenameAndMoveToFolder(DependencyCheckExecutor.getProductPath(), reportsFolderPath,
                    ScannerProperties.getDependencyCheckReportFile());
            File fileToZip = new File(ScannerProperties.getReportsFolderPath());
            String destinationZipFilePath = ScannerProperties.getReportsFolderPath() + Constants.ZIP_FILE_EXTENSION;
            FileHandler.zipFolder(fileToZip, fileToZip.getName(), destinationZipFilePath);
        }
    }
}
