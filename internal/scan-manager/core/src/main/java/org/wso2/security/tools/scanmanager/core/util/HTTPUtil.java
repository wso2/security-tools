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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.wso2.security.tools.scanmanager.core.model.HTTPRequest;

/**
 * Provides utility methods for HTTP request handling.
 */
public class HTTPUtil {

    /**
     * Send a POST Request.
     *
     * @param httpRequest http request object
     * @return a response entity object
     * @throws RestClientException when an error occurs while initiating the request
     */
    public static ResponseEntity<String> sendPOST(HTTPRequest httpRequest) throws RestClientException {
        HttpEntity<?> request = new HttpEntity<>(httpRequest.getRequestParams(), httpRequest.getRequestHeaders());
        ResponseEntity<String> response = new RestTemplate().exchange(httpRequest.getUrl(), HttpMethod.POST, request,
                String.class);
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
        ResponseEntity<String> response = new RestTemplate().exchange(httpRequest.getUrl(), HttpMethod.GET, request,
                String.class);
        return response;
    }

    /**
     * Send a DELETE request.
     *
     * @param httpRequest http request object
     * @return a response entity object
     * @throws RestClientException when an error occurs while initiating the request
     */
    public static ResponseEntity<String> sendDelete(HTTPRequest httpRequest) throws RestClientException {
        HttpEntity<?> request = new HttpEntity<>(httpRequest.getRequestParams(), httpRequest.getRequestHeaders());
        ResponseEntity<String> response = new RestTemplate().exchange(httpRequest.getUrl(), HttpMethod.DELETE, request,
                String.class);
        return response;
    }
}
