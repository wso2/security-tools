/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.core.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Provides utility methods for HTTP request handling.
 */
public class HTTPUtil {

    /**
     * Send a POST Request.
     *
     * @param url            target URL
     * @param requestHeaders request headers map
     * @param requestParams  request params map
     * @return a response entity object
     * @throws RestClientException when an error occurs while initiating the request
     */
    public static ResponseEntity<String> sendPOST(String url, MultiValueMap<String, String> requestHeaders,
                                                  Map<String, Object> requestParams) throws RestClientException {
        HttpEntity<?> request = new HttpEntity<>(requestParams, requestHeaders);
        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.POST, request, String.class);
        return response;
    }

    /**
     * Send a GET request.
     *
     * @param url            target URL
     * @param requestHeaders request headers map
     * @param requestParams  request params map
     * @return a response entity object
     * @throws RestClientException when an error occurs while initiating the request
     */
    public static ResponseEntity<String> sendGET(String url, MultiValueMap<String, String> requestHeaders, Map<String
            , Object> requestParams) throws RestClientException {
        HttpEntity<?> request = new HttpEntity<>(requestParams, requestHeaders);
        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.GET, request, String.class);
        return response;
    }

    /**
     * Send a DELETE request.
     *
     * @param url            target URL
     * @param requestHeaders request headers map
     * @param requestParams  request params map
     * @return a response entity object
     * @throws RestClientException when an error occurs while initiating the request
     */
    public static ResponseEntity<String> sendDelete(String url, MultiValueMap<String, String> requestHeaders,
                                                    Map<String, Object> requestParams) throws RestClientException {
        HttpEntity<?> request = new HttpEntity<>(requestParams, requestHeaders);
        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.DELETE, request, String.class);
        return response;
    }
}
