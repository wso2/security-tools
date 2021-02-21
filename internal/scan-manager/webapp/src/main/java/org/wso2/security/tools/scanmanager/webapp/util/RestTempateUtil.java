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

package org.wso2.security.tools.scanmanager.webapp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.wso2.security.scanmanager.common.exception.RetryExceededException;
import org.wso2.security.tools.scanmanager.common.model.HTTPRequest;
import org.wso2.security.tools.scanmanager.common.util.HTTPUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * TODO : Class level comment
 */
public class RestTempateUtil {
    private static final Log log = LogFactory.getLog(HTTPUtil.class);
    private static final int NO_OF_RETRY_LIMIT = 6;

    /**
     * Send a POST Request.
     *
     * @param httpRequest http request object
     * @return a response entity object
     * @throws RestClientException when an error occurs while initiating the request
     */
    public static ResponseEntity<String> sendPOST(HTTPRequest httpRequest) throws RestClientException {
        HttpEntity<?> request = new HttpEntity<>(httpRequest.getRequestParams(), httpRequest.getRequestHeaders());
        ResponseEntity<String> response = getRestTemplate()
                .exchange(httpRequest.getUrl(), HttpMethod.POST, request, String.class);
        return response;
    }

    /**
     * Send a GET request.
     *
     * @param httpRequest http request object
     * @return a response entity object
     * @throws RestClientException when an error occurs while initiating the request
     */
    public static ResponseEntity<String> sendGET(HTTPRequest httpRequest) throws RestClientException {
        HttpEntity<?> request = new HttpEntity<>(httpRequest.getRequestParams(), httpRequest.getRequestHeaders());
        ResponseEntity<String> response = getRestTemplate()
                .exchange(httpRequest.getUrl(), HttpMethod.GET, request, String.class);
        System.out.println(httpRequest.getUrl().toString());
        return response;
    }

    /**
     * Send a DELETE request.
     *
     * @param httpRequest http request object
     * @return a response entity object
     * @throws RestClientException when an error occurs while initiating the request
     */
    public static ResponseEntity<String> sendDELETE(HTTPRequest httpRequest) throws RestClientException {
        HttpEntity<?> request = new HttpEntity<>(httpRequest.getRequestParams(), httpRequest.getRequestHeaders());
        ResponseEntity<String> response = getRestTemplate()
                .exchange(httpRequest.getUrl(), HttpMethod.DELETE, request, String.class);
        return response;
    }

    /**
     * Common method to perform HTTP Post request.
     *
     * @param url               url
     * @param requestBody       http post request body
     * @param basicAuth         basic authentication base 64 encoded string
     * @param retryTimeInterval time interval to retry http request invocation
     * @return response response of HTTP Post Request
     * @throws IOException            error occurred while processing the http post request
     * @throws InterruptedException   error occurred while processing the http post request
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public static HttpResponse sendPOST(String url, String requestBody, char[] basicAuth, long retryTimeInterval)
            throws IOException, InterruptedException, RetryExceededException {
        HttpPost postRequest = new HttpPost(url);
        postRequest.addHeader("Authorization", "Basic " + new String(basicAuth));
        StringEntity entity;
        if (requestBody != null) {
            entity = new StringEntity(requestBody, ContentType.create("text/xml", Consts.UTF_8));
            postRequest.setEntity(entity);
        }
        return executeHTTPRequest(postRequest, retryTimeInterval);
    }

    /**
     * Perform a http get request.
     *
     * @param url               url
     * @param basicAuth         basic authentication base 64 encoded string
     * @param retryTimeInterval time interval to retry http request invocation
     * @return response
     * @throws IOException            error occurred while processing the http get request
     * @throws InterruptedException   error occurred while processing the http get request
     * @throws RetryExceededException error occurred while retrying http get request
     */
    public static HttpResponse sendGET(String url, char[] basicAuth, Long retryTimeInterval)
            throws IOException, InterruptedException, RetryExceededException {
        HttpResponse response;
        HttpGet getRequest = new HttpGet(url);
        getRequest.addHeader("Authorization", "Basic " + new String(basicAuth));
        getRequest.addHeader("Accept", "application/xml");
        response = executeHTTPRequest(getRequest, retryTimeInterval);
        return response;
    }

    /**
     * Execute provide request and retry if it is required.
     *
     * @param request           POST request or GET request
     * @param retryTimeInterval retry interval time
     * @return return http response if it is successful only
     * @throws IOException            error occurred while performing the request
     * @throws RetryExceededException Retry limit is exceeded
     * @throws InterruptedException   error occurred while waiting for retrying
     */
    private static HttpResponse executeHTTPRequest(HttpUriRequest request, Long retryTimeInterval)
            throws IOException, RetryExceededException, InterruptedException {
        int noOfRetry = 0;
        HttpResponse response;
        HttpClient client = HttpClientBuilder.create().build();
        response = client.execute(request);
        while (isRetryRequired(response)) {
            noOfRetry += 1;
            log.info("Endpoint is not currently available and will retry after " + retryTimeInterval + " Seconds");
            awaitForRetry(retryTimeInterval, noOfRetry);
            response = client.execute(request);
        }
        return response;
    }

    /**
     * Check whether retry required or not based on response code.
     *
     * @param response HTTP response
     * @return true if retrying http request required
     * @throws HttpResponseException http response exception
     */
    private static boolean isRetryRequired(HttpResponse response) throws HttpResponseException {
        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode == HttpStatus.OK.value()) {
            return false;
        } else if (HttpStatus.NOT_FOUND.value() == responseCode
                || HttpStatus.INTERNAL_SERVER_ERROR.value() == responseCode
                || HttpStatus.REQUEST_TIMEOUT.value() == responseCode
                || HttpStatus.SERVICE_UNAVAILABLE.value() == responseCode) {
            return true;
        } else {
            throw new HttpResponseException(response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase());
        }
    }

    /**
     * Wait to retry http request invocation.
     *
     * @param retryTimeInterval retry interval time
     * @param noOfRetry         no of retry
     * @throws InterruptedException   error occurred while waiting for retrying
     * @throws RetryExceededException Retry limit is exceeded
     */
    private static void awaitForRetry(Long retryTimeInterval, int noOfRetry)
            throws InterruptedException, RetryExceededException {
        if (noOfRetry < NO_OF_RETRY_LIMIT) {
            TimeUnit.MINUTES.sleep(retryTimeInterval);
        } else {
            throw new RetryExceededException(
                    "Unable to reach Endpoint. Retried for " + NO_OF_RETRY_LIMIT + " times with the the interval "
                            + retryTimeInterval.toString());
        }
    }

    private static RestTemplate getRestTemplate(){
        RestTemplate restTemplate = new RestTemplate();

        KeyStore keyStore;
        HttpComponentsClientHttpRequestFactory requestFactory = null;

        try {
            keyStore = KeyStore.getInstance("jks");
            ClassPathResource classPathResource = new ClassPathResource("<Key_Store");
            InputStream inputStream = classPathResource.getInputStream();
            keyStore.load(inputStream, "Key_Store_Password".toCharArray());

            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(new SSLContextBuilder()
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .loadKeyMaterial(keyStore, "Key_Store_Password".toCharArray()).build(),
                    NoopHostnameVerifier.INSTANCE);

            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory)
                    .setMaxConnTotal(Integer.valueOf(5))
                    .setMaxConnPerRoute(Integer.valueOf(5))
                    .build();

            requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            requestFactory.setReadTimeout(Integer.valueOf(10000));
            requestFactory.setConnectTimeout(Integer.valueOf(10000));

            restTemplate.setRequestFactory(requestFactory);
        } catch (Exception exception) {
            System.out.println("Exception Occured while creating restTemplate "+exception);
            exception.printStackTrace();
        }
        return restTemplate;
    }
}
