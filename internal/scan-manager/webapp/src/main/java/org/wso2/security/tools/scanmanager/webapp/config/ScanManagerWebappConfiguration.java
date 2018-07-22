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

import org.apache.http.client.utils.URIBuilder;
import org.wso2.security.tools.scanmanager.webapp.util.Constants;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCANNERS_URI;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.SCANS_URI;

/**
 * Scan Manager webapp configuration model class
 */
public class ScanManagerWebappConfiguration {

    private char[] clientId;
    private char[] clientSecret;
    private String scanManagerHost;
    private String scanManagerPort;

    private static final ScanManagerWebappConfiguration scanManagerWebappConfiguration =
            new ScanManagerWebappConfiguration();

    private ScanManagerWebappConfiguration() {
    }

    public void init(Map<String, Object> configObjectMap) {
        this.clientId = (char[]) configObjectMap.get("clientId");
        this.clientSecret = (char[]) configObjectMap.get("clientSecret");
        this.scanManagerHost = (String) configObjectMap.get("scanManagerHost");
        this.scanManagerPort = (String) configObjectMap.get("scanManagerPort");
    }

    public static ScanManagerWebappConfiguration getInstance() {
        return scanManagerWebappConfiguration;
    }

    public char[] getClientId() {
        return clientId;
    }

    public char[] getClientSecret() {
        return clientSecret;
    }

    public String getScanManagerHost() {
        return scanManagerHost;
    }

    public int getScanManagerPort() throws NumberFormatException {
        return Integer.parseInt(scanManagerPort);
    }

    public URI getScanURL() throws ScanManagerWebappException {
        URI uri = null;
        try {
            uri = (new URIBuilder())
                    .setHost(scanManagerHost)
                    .setPort(Integer.parseInt(scanManagerPort))
                    .setScheme(Constants.SCHEME).setPath(SCANS_URI)
                    .build();
        } catch (URISyntaxException e) {
            throw new ScanManagerWebappException("Unable to build the start scan URL", e);
        }
        return uri;
    }


    public URI getScannersURL() throws ScanManagerWebappException {
        URI uri = null;
        try {
            uri = (new URIBuilder())
                    .setHost(scanManagerHost)
                    .setPort(Integer.parseInt(scanManagerPort))
                    .setScheme(Constants.SCHEME).setPath(SCANNERS_URI)
                    .build();
        } catch (URISyntaxException e) {
            throw new ScanManagerWebappException("Unable to build the scanners URL", e);
        }
        return uri;
    }
}
