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

package org.wso2.security.tools.am.webapp.service;

import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.am.webapp.config.GlobalProperties;
import org.wso2.security.tools.am.webapp.handlers.HttpsRequestHandler;
import org.wso2.security.tools.am.webapp.handlers.TokenHandler;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Service layer methods to handle general methods
 */
@Service
public class MainService {

    /**
     * Get JSON array of scans done by a user
     *
     * @param userId User id
     * @return Json array of scanners
     */
    public JSONArray[] getMyScanners(String userId) {
        String accessToken = TokenHandler.getAccessToken();
        JSONArray[] scannersArray = new JSONArray[2];
        int i = 0;
        while (i < 10) {
            try {
                JSONArray staticScanners = sendRequestToGetScanners(userId, accessToken, GlobalProperties
                        .getGetStaticScanners());
                JSONArray dynamicScanners = sendRequestToGetScanners(userId, accessToken, GlobalProperties
                        .getGetDynamicScanners());
                scannersArray[0] = staticScanners;
                scannersArray[1] = dynamicScanners;
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
                TokenHandler.generateAccessToken();
                accessToken = TokenHandler.getAccessToken();
                i += 1;
            }
        }
        return scannersArray;
    }

    private JSONArray sendRequestToGetScanners(String userId, String accessToken, String path) throws IOException,
            URISyntaxException {
        URI uriToGetStaticScanners = (new URIBuilder()).setHost(GlobalProperties.getAutomationManagerHost()).setPort
                (GlobalProperties.getAutomationManagerPort()).setScheme("https").setPath(path).addParameter("userId",
                userId).build();
        HttpsURLConnection httpsURLConnection = HttpsRequestHandler.sendRequest(uriToGetStaticScanners.toString(),
                null, null, "GET", accessToken);
        JSONArray scanners = null;
        if (httpsURLConnection.getResponseCode() == HttpStatus.SC_OK) {
            String jsonString = HttpsRequestHandler.getResponseAsString(httpsURLConnection);
            scanners = new JSONArray(jsonString);
        }
        return scanners;
    }
}
