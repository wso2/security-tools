/*
 *
 *   Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanner.dependency.js.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.wso2.security.tools.scanner.dependency.js.exception.ApiInvokerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.apache.http.protocol.HTTP.USER_AGENT;

/**
 * This class is responsible to connect API endpoints which are commonly used in this tool.
 */
public class CommonApiInvoker {

    private static final Logger log = Logger.getLogger(CommonApiInvoker.class);


    private CommonApiInvoker() {
    }

    public static void setGitToken(char[] gitToken) {
        CommonApiInvoker.gitToken = gitToken.clone();
    }

    public static char[] getGitToken() {
        return gitToken;
    }

    //github API access-token
    private static char[] gitToken;

    /**
     * Connect GitHub API
     *
     * @param url URL of API call.
     * @return Response String.
     * @throws ApiInvokerException Exception occurred while API Call.
     */
    public static String connectGitAPI(String url) throws ApiInvokerException {
        HttpResponse response;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        // add request header
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("content-type", "application/json");

        /*
         * api request need to be authorized to get high api request per hour
         * Note: For unauthorized user request limit is 60 per hour
         * If authorized user request limit is 5000
         * But the private github access token expired once commit done, Need to get new access token
         */
        request.addHeader("Authorization", "Bearer " + new String(gitToken));
        StringBuffer result;
        BufferedReader bufferedReader = null;
        try {
            response = client.execute(request);
            bufferedReader = new BufferedReader(new
                    InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            result = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            throw new ApiInvokerException("Failed to connect Git API endpoint " + url, e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.error("Unable to close stream : ", e);
                }
            }

        }
        return result.toString();
    }

}
