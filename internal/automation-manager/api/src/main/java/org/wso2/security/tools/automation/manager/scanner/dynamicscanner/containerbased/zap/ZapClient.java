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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The class {@code ZapClient} is a client to communicate with ZAP API
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ZapClient {

    private static final String SAVE_SESSION = "/JSON/core/action/newSession/";
    private static final String REMOVE_SESSION = "/JSON/httpSessions/action/removeSession/";
    private static final String CREATE_EMPTY_SESSION = "/JSON/httpSessions/action/createEmptySession/";
    private static final String SET_SESSION_TOKEN_VALUE = "/JSON/httpSessions/action/setSessionTokenValue/";
    private static final String EXCLUDE_FROM_SCAN = "/JSON/spider/action/excludeFromScan/";
    private static final String NEW_CONTEXT = "/JSON/context/action/newContext/";
    private static final String INCLUDE_IN_CONTEXT = "/JSON/context/action/includeInContext/";
    private static final String SPIDER_SCAN = "/JSON/spider/action/scan/";
    private static final String SPIDER_STATUS = "/JSON/spider/view/status/";
    private static final String AJAX_SPIDER_SCAN = "/JSON/ajaxSpider/action/scan/";
    private static final String AJAX_SPIDER_STATUS = "/JSON/ajaxSpider/view/status/";
    private static final String ACTIVE_SCAN = "/JSON/ascan/action/scan/";
    private static final String ACTIVE_SCAN_STATUS = "/JSON/ascan/view/status/";
    private static final String HTML_REPORT = "/OTHER/core/other/htmlreport/";
    private final static String GET = "GET";
    private final static String POST = "POST";
    private final HttpClient httpClient;
    private final String host;
    private final int port;
    private final String scheme;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * Initialize the ZAP client
     *
     * @param host   ZAP API host
     * @param port   ZAP API port
     * @param scheme Scheme (eg: HTTP, HTTPS)
     */
    public ZapClient(String host, int port, String scheme) {
        httpClient = HttpClientBuilder.create().build();
        this.host = host;
        this.port = port;
        this.scheme = scheme;
        LOGGER.trace("ZapClient is initialized");
    }

    /**
     * Save a session
     *
     * @param name      Session name
     * @param overwrite Overwrite value
     * @param post      Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse saveSession(String name, boolean overwrite, Boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme)
                .setPath(SAVE_SESSION)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("name", name)
                .addParameter("overwrite", overwrite ? "true" : "false")
                .build();
        LOGGER.trace("Sending request to save session");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Remove a session
     *
     * @param site    Site that session belongs to
     * @param session Session name
     * @param post    Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse removeSession(String site, String session, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme)
                .setPath(REMOVE_SESSION)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("site", site)
                .addParameter("session", session)
                .build();
        LOGGER.trace("Sending request to remove session");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Create new empty session
     *
     * @param site    Site that a session needs to be created
     * @param session Session name
     * @param post    Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse createEmptySession(String site, String session, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme)
                .setPath(CREATE_EMPTY_SESSION)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("site", site)
                .addParameter("session", session)
                .build();
        LOGGER.trace("Sending request to create empty session");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Set session token value
     *
     * @param site         Site that a session token needs to be added
     * @param session      Session name
     * @param sessionToken Name of the session token
     * @param tokenValue   Value of the session token
     * @param post         Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse setSessionTokenValue(String site, String session, String sessionToken, String tokenValue,
                                             boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme)
                .setPath(SET_SESSION_TOKEN_VALUE)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("site", site)
                .addParameter("session", session)
                .addParameter("sessionToken", sessionToken)
                .addParameter("tokenValue", tokenValue)
                .build();
        LOGGER.trace("Sending request to set session token value");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Exclude a URL from spider. This method is used to exclude logout URL from spider. If a logout URL is hit, the
     * session will stop
     *
     * @param regex Regular expression of URL
     * @param post  Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse excludeFromSpider(String regex, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme)
                .setPath(EXCLUDE_FROM_SCAN)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("regex", regex)
                .build();
        LOGGER.trace("Sending request to exclude URL from spider");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Create a new context
     *
     * @param contextName Context name
     * @param post        Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse createNewContext(String contextName, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(NEW_CONTEXT)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("contextName", contextName)
                .build();
        LOGGER.trace("Sending request to create new context");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Include URLs in a context
     *
     * @param contextName Context name
     * @param regex       Regular expression of URL
     * @param post        Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse includeInContext(String contextName, String regex, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme)
                .setPath(INCLUDE_IN_CONTEXT)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("contextName", contextName)
                .addParameter("regex", regex)
                .build();
        LOGGER.trace("Sending request to include in context");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Run spider
     *
     * @param url         URL to start spider scan
     * @param maxChildren Maximum number of children
     * @param recurse     Recurse
     * @param contextName Context name
     * @param subtreeOnly Indicate to scan sub tree only
     * @param post        Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse spider(String url, String maxChildren, String recurse, String contextName, String subtreeOnly,
                               boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(scheme).setPath(SPIDER_SCAN)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("url", url)
                .addParameter("maxChildren", maxChildren)
                .addParameter("recurse", recurse)
                .addParameter("contextName", contextName)
                .addParameter("subtreeOnly", subtreeOnly)
                .build();
        LOGGER.trace("Sending request to run spider");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Gets spider status
     *
     * @param scanId Scan id of the spider scan
     * @param post   Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse spiderStatus(String scanId, boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(SPIDER_STATUS)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("scanId", scanId)
                .build();
        LOGGER.trace("Sending request to check spider status");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Run AJAX spider as a percentage
     *
     * @param url         URL to start AJAX spider
     * @param inScope     Indicates whether in scope
     * @param contextName Context name
     * @param subtreeOnly Indicates to scan sub tree only
     * @param post        Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse ajaxSpider(String url, String inScope, String contextName, String subtreeOnly, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme)
                .setPath(AJAX_SPIDER_SCAN)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("url", url)
                .addParameter("inScope", inScope)
                .addParameter("contextName", contextName)
                .addParameter("subtreeOnly", subtreeOnly)
                .build();
        LOGGER.trace("Sending request to run ajax spider");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Get AJAX spider status (eg: running, stopped)
     *
     * @param post Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse ajaxSpiderStatus(boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme)
                .setPath(AJAX_SPIDER_STATUS)
                .addParameter("formMethod", post ? POST : GET)
                .build();
        LOGGER.trace("Sending request to check ajax spider status");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Run active scan
     *
     * @param url            URL to start active scan
     * @param recurse        Indicates whether to recurse
     * @param inScopeOnly    Indicate whether in scope only
     * @param scanPolicyName If there is any scan policy
     * @param method         Method type
     * @param postData       Post data
     * @param contextId      Context Id
     * @param post           Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse activeScan(String url, String recurse, String inScopeOnly, String scanPolicyName, String method,
                                   String postData, String contextId, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(ACTIVE_SCAN)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("url", url)
                .addParameter("recurse", recurse)
                .addParameter("inScopeOnly", inScopeOnly)
                .addParameter("scanPolicyName", scanPolicyName)
                .addParameter("method", method)
                .addParameter("postData", postData)
                .addParameter("contextId", contextId)
                .build();
        LOGGER.trace("Sending request to run active scan");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Check active scan status
     *
     * @param scanId Scan id
     * @param post   Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse activeScanStatus(String scanId, boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme)
                .setPath(ACTIVE_SCAN_STATUS)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("scanId", scanId)
                .build();
        LOGGER.trace("Sending request to check active scan status");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    /**
     * Generate HTML report
     *
     * @param post Boolean value to indicate request method (GET or POST)
     * @return HTTP response after executing the HTTP command
     * @throws IOException        If cannot communicate with server
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a
     *                            URI reference
     */
    public HttpResponse generateHtmlReport(boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(HTML_REPORT)
                .addParameter("formMethod", post ? POST : GET)
                .build();
        LOGGER.trace("Sending request to generate html report");
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }
}
