/*
 *
 *   Copyright (c) 2020, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanmanager.scanners.qualys.model;

import org.apache.logging.log4j.LogManager;
import org.wso2.security.tools.scanmanager.scanners.common.exception.ScannerException;
import org.wso2.security.tools.scanmanager.scanners.common.model.CallbackLog;
import org.wso2.security.tools.scanmanager.scanners.qualys.QualysScannerConstants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Model class to represent Crawling Script
 */
public class CrawlingScript extends SeleniumScript {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(CrawlingScript.class);

    // Starting URL of crawling script. It can be either URL or Regex.
    private String startingUrl;

    // To represent authentication requirement before initating crawling job
    private Boolean isRequredAuthentication;

    // To represent whether starting URL is a regex or not.
    private Boolean isStartingUrlRegex;

    public CrawlingScript(String scriptFileLocation, String jobId) throws ScannerException {
        super(scriptFileLocation, jobId);
        parseCrawlingScriptConfiguration(jobId);
    }

    public String getStartingUrl() {
        return startingUrl;
    }

    public void setStartingUrl(String startingUrl) {
        this.startingUrl = startingUrl;
    }

    public Boolean getRequredAuthentication() {
        return isRequredAuthentication;
    }

    public void setRequredAuthentication(Boolean requredAuthentication) {
        isRequredAuthentication = requredAuthentication;
    }

    public Boolean getStartingUrlRegex() {
        return isStartingUrlRegex;
    }

    public void setStartingUrlRegex(Boolean startingUrlRegex) {
        isStartingUrlRegex = startingUrlRegex;
    }

    /**
     * Parse crawling script related configurations.
     *
     * @param jobId jobId
     * @throws ScannerException error occurred while parsing crawling scrip configurations.
     */
    private void parseCrawlingScriptConfiguration(String jobId) throws ScannerException {
        XMLStreamReader streamReader = null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(this.getScriptFile());
            streamReader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            // In crawling script, related configuration are mentiond in a comment format by seprating ':' delimiter.
            // Eg : <!--startingUrl:https://\d*.\d*.\d*.\d*:\d*/foo/-->
            // These configuration are parsed using ':' delimiter
            while (streamReader.hasNext()) {
                if (streamReader.next() == XMLStreamConstants.COMMENT) {
                    if (isConfigurationsProvided()) {
                        break;
                    }
                    String comment = streamReader.getText();
                    String propertyKey = comment.trim().split(QualysScannerConstants.DELIMITER, 2)[0];
                    switch (propertyKey) {
                    case QualysScannerConstants.STARTING_URL:
                        this.setStartingUrl(comment.trim().split(QualysScannerConstants.DELIMITER)[1]);
                        break;
                    case QualysScannerConstants.REQUIRE_AUTHENTICATION:
                        this.setRequredAuthentication(
                                Boolean.parseBoolean(comment.trim().split(QualysScannerConstants.DELIMITER)[1]));
                        break;
                    case QualysScannerConstants.STARTING_URL_REGEX:
                        this.setStartingUrlRegex(
                                Boolean.parseBoolean(comment.trim().split(QualysScannerConstants.DELIMITER)[1]));
                        break;
                    default:
                        break;
                    }
                }
            }
            if (!isConfigurationsProvided()) {
                String message = "Crawling Script Setting configurations are not given properly.";
                log.error(new CallbackLog(jobId, message));
                throw new ScannerException(message);
            }
        } catch (FileNotFoundException | XMLStreamException e) {
            throw new ScannerException("Error while setting crawling script properties", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new ScannerException("Error while closing the file stream", e);
                }
            }
            if (streamReader != null) {
                try {
                    streamReader.close();
                } catch (XMLStreamException e) {
                    throw new ScannerException("Error while closing the XML stream", e);
                }
            }
        }
    }

    private boolean isConfigurationsProvided() {
        if (startingUrl != null && isRequredAuthentication != null && isStartingUrlRegex != null) {
            return true;
        } else {
            return false;
        }
    }
}
