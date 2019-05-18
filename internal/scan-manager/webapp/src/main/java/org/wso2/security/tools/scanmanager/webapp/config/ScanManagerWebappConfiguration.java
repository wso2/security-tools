/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.security.tools.scanmanager.webapp.config;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.bouncycastle.util.Arrays;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;
import org.wso2.security.tools.scanmanager.webapp.util.Constants;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.wso2.security.tools.scanmanager.webapp.util.Constants.LOGS_URI;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCANNERS_URI;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCANS_URI;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.URL_SEPARATOR;

/**
 * Scan Manager webapp configuration model class.
 */
public class ScanManagerWebappConfiguration {

    private String scanManagerHost;
    private Integer scanManagerPort;
    private String ftpHost;
    private Integer ftpPort;
    private String ftpUsername;
    private char[] ftpPassword;
    private String ftpBasePath;

    private static final String SCAN_MANAGER_HOST_KEY = "scanManagerHost";
    private static final String SCAN_MANAGER_PORT_KEY = "scanManagerPort";
    private static final String FTP_HOST_KEY = "ftpHost";
    private static final String FTP_PORT_KEY = "ftpPort";
    private static final String FTP_USERNAME_KEY = "ftpUsername";
    private static final String FTP_PASSWORD_KEY = "ftpPassword";
    private static final String FTP_BASE_PATH = "ftpBasePath";

    private static final ScanManagerWebappConfiguration scanManagerWebappConfiguration =
            new ScanManagerWebappConfiguration();

    private ScanManagerWebappConfiguration() {
    }

    public void init(Map<String, Object> configObjectMap) throws ScanManagerWebappException {
        String scanManagerHost = (String) configObjectMap.get(SCAN_MANAGER_HOST_KEY);
        Integer scanManagerPort = (Integer) configObjectMap.get(SCAN_MANAGER_PORT_KEY);
        String ftpHost = (String) configObjectMap.get(FTP_HOST_KEY);
        Integer ftpPort = (Integer) configObjectMap.get(FTP_PORT_KEY);
        String ftpUsername = (String) configObjectMap.get(FTP_USERNAME_KEY);
        String ftpBasePath = (String) configObjectMap.get(FTP_BASE_PATH);

        if (scanManagerHost != null) {
            this.scanManagerHost = scanManagerHost;
        } else {
            throw new ScanManagerWebappException("Unable to get the scan manager host configuration");
        }
        if (scanManagerPort != null) {
            this.scanManagerPort = scanManagerPort;
        } else {
            throw new ScanManagerWebappException("Unable to get the scan manager port configuration");
        }
        if (ftpHost != null) {
            this.ftpHost = ftpHost;
        } else {
            throw new ScanManagerWebappException("Unable to get the FTP host configuration");
        }
        if (ftpPort != null) {
            this.ftpPort = ftpPort;
        } else {
            throw new ScanManagerWebappException("Unable to get the FTP port configuration");
        }
        if (ftpUsername != null) {
            this.ftpUsername = ftpUsername;
        } else {
            throw new ScanManagerWebappException("Unable to get the FTP server username configuration");
        }
        if (configObjectMap.get(FTP_PASSWORD_KEY) != null) {
            this.ftpPassword = ((String) configObjectMap.get(FTP_PASSWORD_KEY)).toCharArray();
        } else {
            throw new ScanManagerWebappException("Unable to get the FTP server password configuration");
        }
        if (ftpBasePath != null) {
            this.ftpBasePath = ftpBasePath;
        } else {
            throw new ScanManagerWebappException("Unable to get the FTP server base path configuration");
        }
    }

    public static ScanManagerWebappConfiguration getInstance() {
        return scanManagerWebappConfiguration;
    }

    public String getScanManagerHost() {
        return scanManagerHost;
    }

    public int getScanManagerPort() throws NumberFormatException {
        return scanManagerPort;
    }

    public String getFtpHost() {
        return ftpHost;
    }

    public Integer getFtpPort() {
        return ftpPort;
    }

    public String getFtpUsername() {
        return ftpUsername;
    }

    public char[] getFtpPassword() {
        return Arrays.copyOf(ftpPassword, ftpPassword.length);
    }

    public String getFtpBasePath() {
        return ftpBasePath;
    }

    /**
     * Building the URL to get the scans from scan manager API.
     *
     * @param path           additional params that needs to be appended to the scans path (e.g. path params)
     * @param nameValuePairs request param names and their values
     * @return scan manager scans URI
     * @throws ScanManagerWebappException when an error occurs when building the scan manager scans URI
     */
    public URI getScanURL(String path, List<NameValuePair> nameValuePairs) throws ScanManagerWebappException {
        URI uri = null;
        try {
            uri = buildURI(SCANS_URI, path, nameValuePairs);
        } catch (URISyntaxException e) {
            throw new ScanManagerWebappException("Unable to build the scans URL", e);
        }
        return uri;
    }

    /**
     * Building the URL to get the logs for a particular scan from scan manager API.
     *
     * @param path           additional params that needs to be appended to the logs path (e.g. path params)
     * @param nameValuePairs request param names and their values
     * @return scan manager logs URI
     * @throws ScanManagerWebappException when an error occurs when building the scan manager logs URI
     */
    public URI getLogURL(String path, List<NameValuePair> nameValuePairs) throws ScanManagerWebappException {
        URI uri = null;
        try {
            uri = buildURI(LOGS_URI, path, nameValuePairs);
        } catch (URISyntaxException e) {
            throw new ScanManagerWebappException("Unable to build the logs URL", e);
        }
        return uri;
    }

    /**
     * Building the URL to get all the scanners from scan manager API.
     *
     * @param path           additional params that needs to be appended to the scanners path (e.g. path params)
     * @param nameValuePairs request param names and their values
     * @return scan manager scanners URI
     * @throws ScanManagerWebappException when an error occurs when building the scan manager scanners URI
     */
    public URI getScannersURL(String path, List<NameValuePair> nameValuePairs) throws ScanManagerWebappException {
        URI uri = null;
        try {
            uri = buildURI(SCANNERS_URI, path, nameValuePairs);
        } catch (URISyntaxException e) {
            throw new ScanManagerWebappException("Unable to build the scanners URL", e);
        }
        return uri;
    }

    private URI buildURI(String basePath, String path, List<NameValuePair> nameValuePairs) throws URISyntaxException {
        URI uri = null;
        uri = (new URIBuilder())
                .setHost(scanManagerHost)
                .setPort(scanManagerPort)
                .setScheme(Constants.SCHEME)
                .setPath(basePath + URL_SEPARATOR + path)
                .addParameters(nameValuePairs)
                .build();
        return uri;
    }
}
