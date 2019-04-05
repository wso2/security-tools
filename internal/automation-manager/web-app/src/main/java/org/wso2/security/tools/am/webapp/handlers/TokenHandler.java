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

package org.wso2.security.tools.am.webapp.handlers;

import org.json.JSONObject;
import org.wso2.security.tools.am.webapp.config.GlobalProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

/**
 * Utility methods for handling access tokens using client credentials grant
 */
public class TokenHandler {

    private static String accessToken;

    /**
     * Generate access token by sending request to API Manager
     */
    public static void generateAccessToken() {
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("grant_type", "client_credentials");
            requestParams.put("client_id", GlobalProperties.getClientId());
            requestParams.put("client_secret", GlobalProperties.getClientSecret());
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Content-Type", "application/x-www-form-urlencoded");
            HttpsURLConnection urlConnection = HttpsRequestHandler.sendRequest(GlobalProperties.getAccessTokenUri(), requestHeaders, requestParams, "POST", null);
            InputStreamReader in = new InputStreamReader((InputStream) urlConnection.getContent());
            BufferedReader buff = new BufferedReader(in);
            String line = buff.readLine();
            JSONObject jsonObj = new JSONObject(line);
            accessToken = jsonObj.getString("access_token");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get generated access token
     * @return Access token
     */
    public static String getAccessToken() {
        return accessToken;
    }
}
