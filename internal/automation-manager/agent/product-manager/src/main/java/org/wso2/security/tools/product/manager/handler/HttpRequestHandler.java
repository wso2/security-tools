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

package org.wso2.security.tools.product.manager.handler;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for HTTP request handling
 */
@SuppressWarnings({"unused"})
public class HttpRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestHandler.class);
    private static List<NameValuePair> urlParameters = new ArrayList<>();

    /**
     * Send HTTP GET request
     *
     * @param request Requested URI
     * @return HTTPResponse after executing the command
     */
    public static HttpResponse sendGetRequest(URI request) {
        try {
            HttpClientBuilder clientBuilder = HttpClients.custom();
            HttpClient httpClient = clientBuilder.setRetryHandler(new
                    DefaultHttpRequestRetryHandler(3, false)).build();
            HttpGet httpGetRequest = new HttpGet(request);
            return httpClient.execute(httpGetRequest);
        } catch (IOException e) {
            LOGGER.error("Error occurred while sending GET request to " + request.getPath(), e);
        }
        return null;
    }

    /**
     * Send HTTP POST request
     *
     * @param requestURI Requested URI
     * @return HTTPResponse after executing the command
     */
    public static HttpResponse sendPostRequest(String requestURI, ArrayList<NameValuePair> parameters) {
        try {
            HttpClientBuilder clientBuilder = HttpClients.custom();
            HttpClient httpClient = clientBuilder.setRetryHandler(new
                    DefaultHttpRequestRetryHandler(3, false)).build();
            HttpPost httpPostRequest = new HttpPost(requestURI);

            for (NameValuePair parameter : parameters) {
                urlParameters.add(new BasicNameValuePair(parameter.getName(), parameter.getValue()));
            }

            httpPostRequest.setEntity(new UrlEncodedFormEntity(urlParameters));
            return httpClient.execute(httpPostRequest);
        } catch (IOException e) {
            LOGGER.error("Error occurred while sending POST request to " + requestURI, e);
        }
        return null;
    }

    /**
     * Read HTTPResponse and returns a string
     *
     * @param response HTTPResponse
     * @return String of HTTPResponse
     */
    public static String printResponse(HttpResponse response) {
        try {
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            LOGGER.error("Error occurred while processing the HTTP response", e);
        }
        return null;
    }
}