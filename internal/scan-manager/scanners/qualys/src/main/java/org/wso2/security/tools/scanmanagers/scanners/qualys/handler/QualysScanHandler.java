/*
 *
 *   Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanmanagers.scanners.qualys.handler;

import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.model.ScanStatus;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;
import org.wso2.security.tools.scanmanager.scanners.qualys.model.ScanContext;
import org.wso2.security.tools.scanmanager.scanners.qualys.utils.RequestBodyBuilder;
import org.wso2.security.tools.scanmanger.scanners.qualys.QualysScannerConstants;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * This class is responsible to handle the required  use cases of Qualys scanner.
 */
public class QualysScanHandler {

    //    private final Log log = LogFactory.getLog(QualysScanHandler.class);
    private QualysApiInvoker qualysApiInvoker;

    public QualysScanHandler(QualysApiInvoker qualysApiInvoker) {
        this.qualysApiInvoker = qualysApiInvoker;
    }

    //    public QualysApiInvoker getQualysApiInvoker() {
    //        return qualysApiInvoker;
    //    }

    /**
     * Prepare the scan before launching the scan. Main tasks are Adding the authentication scripts and crawling
     * scripts.
     *
     * @param fileMap Map that contains the file paths.
     * @param appID   Application ID
     * @param jobId   Job ID
     * @param appName Web Application Name
     * @param host    host url of qualys
     * @return Authentication script id
     * @throws ScannerException Error occurred while adding authentication scripts
     */
    public String prepareScan(String appID, String jobId, String appName, Map<String, List<String>> fileMap,
            String host) throws ScannerException {
        String authScriptId;
        // Purging Scan before launching the scan.
        try {
            if (qualysApiInvoker.purgeScan(host, appID)) {
                String message = "Application : " + appID + " is purged successfully ";
                CallbackUtil.persistScanLog(jobId, message, LogType.INFO);
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new ScannerException("Error occurred while purging the Application Scan : " + appID, e);
        }
        // Add authentication script to Qualys scanner..
        try {
            //Only one authentication script can be given per single scan.
            String addAuthRecordRequestBody = RequestBodyBuilder.buildAddAuthScriptRequestBody(appID,
                    fileMap.get(QualysScannerConstants.AUTHENTICATION_SCRIPTS).get(0));
            authScriptId = qualysApiInvoker.addAuthenticationScript(host, addAuthRecordRequestBody);
            String message = "Web Authentication Record is created :" + authScriptId;
            CallbackUtil.persistScanLog(jobId, message, LogType.INFO);
        } catch (TransformerException | IOException | ParserConfigurationException | SAXException e) {
            throw new ScannerException("Error occurred while adding the authentication scripts ", e);
        }
        // Update web application with added authentication script.
        try {
            String updateWebAppRequestBody = RequestBodyBuilder.buildUpdateWebAppRequestBody(appName, authScriptId);
            String updatedWebId = qualysApiInvoker.updateWebApp(host, updateWebAppRequestBody, appID);
            if (appID.equalsIgnoreCase(updatedWebId)) {
                String message = "Newly added Web Authentication  Record is added to web application : " + updatedWebId;
                CallbackUtil.persistScanLog(jobId, message, LogType.INFO);
            }
        } catch (ParserConfigurationException | TransformerException | SAXException | IOException e) {
            throw new ScannerException(
                    "Error occurred while updating the web app of Qualys with given authentication script", e);
        }
        return authScriptId;
    }

    /**
     * Launching the scan in qualys scan portal.
     *
     * @param scanContext Object that contains the scanner specific parameters.
     * @param host        host url of qualys
     * @return Scanner scan Id
     * @throws ScannerException Error occurred while launching the scan
     */
    public String launchScan(ScanContext scanContext, String host) throws ScannerException {
        String launchScanRequestBody;
        String scannerScanId;
        try {
            launchScanRequestBody = RequestBodyBuilder.buildLaunchScanRequestBody(scanContext);
            scannerScanId = qualysApiInvoker.launchScan(host, launchScanRequestBody);
            if (scannerScanId != null) {
                scanContext.setScannerScanId(scannerScanId);
                String message = "Qualys Scan for " + scanContext.getJobID() + " has successfully submitted : "
                        + scannerScanId;
                CallbackUtil.updateScanStatus(scanContext.getJobID(), ScanStatus.SUBMITTED, null, scannerScanId);
                CallbackUtil.persistScanLog(scanContext.getJobID(), message, LogType.INFO);
                StatusChecker statusChecker = new StatusChecker(qualysApiInvoker, scanContext,
                        scanContext.getInitialDelay(), scanContext.getSchedulerDelay());
                statusChecker.activateStatusChecker();
            }
        } catch (ParserConfigurationException | TransformerException | SAXException | IOException e) {
            throw new ScannerException("Error occurred while launching the scan for " + scanContext.getJobID(), e);
        }
        return scannerScanId;
    }

    /**
     * Cancelling Scan.
     *
     * @param host   host url
     * @param scanId scanId
     * @param jobId  JibID
     * @throws ScannerException Error occurred while cancelling scan.
     */
    public void cancelScan(String host, String scanId, String jobId) throws ScannerException {
        try {
            String status = qualysApiInvoker.retrieveStatus(host, scanId);
            if ((status.equalsIgnoreCase(QualysScannerConstants.RUNNING)) || status
                    .equalsIgnoreCase(QualysScannerConstants.SUBMITTED)) {
                if (qualysApiInvoker.cancelScan(host, scanId)) {
                    String message = "Scan id : " + scanId + " got cancelled as per request. ";
                    CallbackUtil.updateScanStatus(jobId, ScanStatus.CANCELED, null, scanId);
                    CallbackUtil.persistScanLog(jobId, message, LogType.INFO);
                } else {
                    String message = "Could not cancel scan : " + scanId;
                    CallbackUtil.updateScanStatus(jobId, ScanStatus.ERROR, null, scanId);
                    CallbackUtil.persistScanLog(jobId, message, LogType.ERROR);
                }
            } else {
                String message = "Could not find active scan for scanId : " + scanId;
                CallbackUtil.updateScanStatus(jobId, ScanStatus.ERROR, null, scanId);
                CallbackUtil.persistScanLog(jobId, message, LogType.ERROR);
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new ScannerException("Could not cancel scan : " + scanId, e);
        }
    }
}
