/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased.zap;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.automation.manager.config.AutomationManagerProperties;
import org.wso2.security.tools.automation.manager.config.DynamicScannerProperties;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.containerbased.zap.ZapEntity;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.exception.DynamicScannerException;
import org.wso2.security.tools.automation.manager.handler.DockerHandler;
import org.wso2.security.tools.automation.manager.handler.HttpRequestHandler;
import org.wso2.security.tools.automation.manager.handler.HttpsRequestHandler;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased
        .AbstractContainerBasedDynamicScanner;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased.ContainerBasedDynamicScanner;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * The class {@code ZapScanner} implements the abstract class {@link AbstractContainerBasedDynamicScanner}.
 * <p>The main contract of this class is to start a ZAP docker container, and automate the ZAP scanning process by
 * calling {@link ZapClient} methods </p>
 */
public class ZapScanner extends AbstractContainerBasedDynamicScanner {
    private static final String HTTP_SCHEME = "http";
    private static final String HTTPS_SCHEME = "https";
    private static final String POST = "POST";
    private static final String CONTEXT_NAME = "context";
    private static final String SESSION_NAME_1 = "session1";
    private static final String SESSION_NAME_2 = "session2";
    private final static Logger LOGGER = LoggerFactory.getLogger(ZapScanner.class);
    private String contextId;
    private URI productUriRelativeToZap;

    public ZapScanner() {
        super(new ZapEntity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createContainer() throws DynamicScannerException {
        try {
            int port = ContainerBasedDynamicScanner.calculateDynamicScannerContainerPort(dynamicScannerEntity.getId());
            List<String> command = Arrays.asList("zap.sh", "-daemon", "-config", "api.disablekey=true", "-config",
                    "api.addrs.addr.name=.*", "-config", "api.addrs.addr.regex=true", "-port", String.valueOf(port),
                    "-host", "0.0.0.0");
            String containerId = DockerHandler.createContainer(DynamicScannerProperties.getZapDockerImage(), ipAddress,
                    String.valueOf(port), String.valueOf(port), command, null);
            if (containerId == null) {
                throw new DynamicScannerException("Error occurred while creating dynamic scanner container");
            }
            saveContainerData(containerId, port);
        } catch (InterruptedException | DockerCertificateException | DockerException e) {
            throw new DynamicScannerException("Error occurred while creating dynamic scanner container", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startScan(String productHostRelativeToScanner, String productHostRelativeToAutomationManager, int
            productPort) throws DynamicScannerException {
        try {
            ZapClient zapClient = new ZapClient(dynamicScannerEntity.getDockerIpAddress(), dynamicScannerEntity
                    .getHostPort(), HTTP_SCHEME);
            productUriRelativeToZap = new URIBuilder().setHost(productHostRelativeToScanner).setPort(productPort)
                    .setScheme(HTTPS_SCHEME).build();
            String site = productHostRelativeToScanner + ":" + productPort;
            Map<String, String> props = new HashMap<>();
            props.put("Content-Type", "text/plain");
            URI logoutUri = (new URIBuilder()).setHost(productHostRelativeToScanner).setPort(productPort).setScheme
                    ("https").setPath(DynamicScannerProperties.getWso2ProductManagementConsoleLogoutUrl()).build();

            createAndInitContext(zapClient);
            createAndInitSession(zapClient, productHostRelativeToAutomationManager, productPort, site, props,
                    SESSION_NAME_1);
            logoutFromWso2Server(productHostRelativeToAutomationManager, productPort, props);
            zapClient.excludeFromSpider(logoutUri.toString(), false);
            createAndInitSession(zapClient, productHostRelativeToAutomationManager, productPort, site, props,
                    SESSION_NAME_2);
            runSpider(zapClient);
            runAjaxSpider(zapClient);
            runActiveScan(zapClient);
            generateReport(zapClient);
        } catch (IOException | CertificateException | URISyntaxException | NoSuchAlgorithmException |
                KeyStoreException | AutomationManagerException | KeyManagementException | InterruptedException e) {
            dynamicScannerService.updateScanStatus(dynamicScannerEntity.getContainerId(), AutomationManagerProperties
                    .getStatusFailed(), 0);
            throw new DynamicScannerException("Exception occurs when starting dynamic scan", e);
        }
    }


    private void createAndInitContext(ZapClient zapClient) throws IOException, URISyntaxException {
        HttpResponse createNewContextResponse = zapClient.createNewContext(CONTEXT_NAME, false);
        contextId = extractJsonValue(createNewContextResponse, "contextId");
        zapClient.includeInContext(CONTEXT_NAME, "\\Q" + productUriRelativeToZap.toString() + "\\E.*", false);
    }

    private void createAndInitSession(ZapClient zapClient, String productHostRelativeToThis, int productPort, String
            site, Map<String, String> props, String sessionName) throws IOException, URISyntaxException,
            CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
            InterruptedException {
        HttpResponse createEmptySessionResponse = zapClient.createEmptySession(site, sessionName, false);

        String jSessionId = loginToWso2ServerAndGetSessionToken(productHostRelativeToThis, productPort, props);
        HttpResponse setSessionTokenValueResponse = zapClient.setSessionTokenValue(site, sessionName, "JSESSIONID",
                jSessionId, false);
    }

    private String loginToWso2ServerAndGetSessionToken(String productHostRelativeToThis, int productPort, Map<String,
            String> props) throws URISyntaxException, CertificateException, NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException, IOException {

        Map<String, Object> loginCredentials = new HashMap<>();
        loginCredentials.put(DynamicScannerProperties.getWso2ProductKeyUsername(), DynamicScannerProperties
                .getWso2ProductValueUsername());
        loginCredentials.put(DynamicScannerProperties.getWso2ProductKeyPassword(), DynamicScannerProperties
                .getWso2ProductValuePassword());

        URI loginUri = (new URIBuilder()).setHost(productHostRelativeToThis).setPort(productPort)
                .setScheme("https").setPath(DynamicScannerProperties.getWso2ProductManagementConsoleLoginUrl()).build();
        HttpsURLConnection httpsURLConnection = HttpsRequestHandler.sendRequest(loginUri.toString(), props,
                loginCredentials, POST);
        List<String> setCookieResponseList = HttpsRequestHandler.extractValueFromResponseHeader("Set-Cookie",
                httpsURLConnection);
        assert setCookieResponseList != null;
        String setCookieResponse = setCookieResponseList.get(0);
        return setCookieResponse.substring(setCookieResponse.indexOf("=") + 1, setCookieResponse.indexOf(";"));
    }

    private void logoutFromWso2Server(String productHostRelativeToThis, int productPort, Map<String, String> props)
            throws URISyntaxException, CertificateException, NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException, IOException {
        URI logoutUri = (new URIBuilder()).setHost(productHostRelativeToThis).setPort(productPort)
                .setScheme("https").setPath(DynamicScannerProperties.getWso2ProductManagementConsoleLogoutUrl())
                .build();
        HttpsRequestHandler.sendRequest(logoutUri.toString(), props,
                null, POST);
    }

    private void runSpider(ZapClient zapClient) {
        try {
            BufferedReader bufferedReader;
            ArrayList<String> spiderScanIds = new ArrayList<>();
            bufferedReader = new BufferedReader(new FileReader(urlListFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                HttpResponse spiderResponse = zapClient.spider(productUriRelativeToZap.toString() + line, "",
                        "", "", "", false);
                String scanId = extractJsonValue(spiderResponse, "scan");
                spiderScanIds.add(scanId);
            }
            for (String scanId : spiderScanIds) {
                HttpResponse spiderStatusResponse = zapClient.spiderStatus(scanId, false);
                LOGGER.info("Sending request to check spider status");
                while (Integer.parseInt(extractJsonValue(spiderStatusResponse, "status")) < 100) {
                    spiderStatusResponse = zapClient.spiderStatus(scanId, false);
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException | URISyntaxException | IOException e) {
            LOGGER.error("Error occurred while running spider scan", e);
        }
    }

    private void runAjaxSpider(ZapClient zapClient) throws IOException, URISyntaxException, InterruptedException {
        HttpResponse ajaxSpiderResponse = zapClient.ajaxSpider(productUriRelativeToZap.toString(), "",
                "", "", false);
        LOGGER.info("Starting Ajax spider: " + ajaxSpiderResponse);

        HttpResponse ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
        while (!extractJsonValue(ajaxSpiderStatusResponse, "status").equals("stopped")) {
            ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
            Thread.sleep(3000);
        }

    }

    private void runActiveScan(ZapClient zapClient) throws IOException, URISyntaxException, InterruptedException {
        int progress = 0;
        HttpResponse activeScanResponse = zapClient.activeScan(productUriRelativeToZap.toString(), "",
                "", "", "", "", contextId, false);
        String activeScanId = extractJsonValue(activeScanResponse, "scan");
        Thread.sleep(500);
        HttpResponse activeScanStatusResponse = zapClient.activeScanStatus(activeScanId, false);
        progress = Integer.parseInt(extractJsonValue(activeScanStatusResponse, "status"));
        while (progress < 100) {
            activeScanStatusResponse = zapClient.activeScanStatus(activeScanId, false);
            progress = Integer.parseInt(extractJsonValue(activeScanStatusResponse, "status"));
            dynamicScannerService.updateScanStatus(dynamicScannerEntity.getContainerId(),
                    AutomationManagerProperties.getStatusRunning(), progress);
            Thread.sleep(1000 * 60);
        }
        if (progress == 100) {
            dynamicScannerService.updateScanStatus(dynamicScannerEntity.getContainerId(),
                    AutomationManagerProperties.getStatusCompleted(), progress);
        }
    }

    private String extractJsonValue(HttpResponse httpResponse, String key) throws IOException {
        String jsonString = HttpRequestHandler.printResponse(httpResponse);
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getString(key);
    }

    private void generateReport(ZapClient zapClient) throws IOException, URISyntaxException,
            AutomationManagerException {
        HttpResponse generatedHtmlReport = zapClient.generateHtmlReport(false);
        String reportFilePath = fileUploadLocation + File.separator + DynamicScannerProperties.getZapReport();
        HttpRequestHandler.saveResponseToFile(generatedHtmlReport, new File(reportFilePath));
        dynamicScannerService.updateReportReady(dynamicScannerEntity.getContainerId(), true, reportFilePath);
    }
}
