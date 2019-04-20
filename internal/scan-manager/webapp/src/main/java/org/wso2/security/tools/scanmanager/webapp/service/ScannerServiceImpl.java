/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanmanager.webapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;
import org.wso2.security.tools.scanmanager.webapp.config.ScanManagerWebappConfiguration;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;
import org.wso2.security.tools.scanmanager.webapp.util.HTTPUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Scanner service implementation class.
 */
@Service
public class ScannerServiceImpl implements ScannerService {

    /**
     * Get the list of scanners.
     *
     * @return a list of scanners for the given page number
     * @throws ScanManagerWebappException when an error occurs when getting the list of scanners
     */
    public List<Scanner> getScanners() throws ScanManagerWebappException {
        List<Scanner> scannerList = new ArrayList<>();
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        try {
            ResponseEntity<String> responseEntity = HTTPUtil.sendGET(ScanManagerWebappConfiguration.getInstance()
                    .getScannersURL("", nameValuePairs).toString(), null, null);
            if (responseEntity != null) {
                JSONArray responseArray = new JSONArray(responseEntity.getBody());
                for (int arrayIndex = 0; arrayIndex < responseArray.length(); arrayIndex++) {
                    JSONObject jsonObject = responseArray.getJSONObject(arrayIndex);
                    ObjectMapper mapper = new ObjectMapper();
                    Scanner scanner = mapper.readValue(jsonObject.toString(), Scanner.class);
                    scannerList.add(scanner);
                }
            }
        } catch (IOException e) {
            throw new ScanManagerWebappException("Unable to get the scanners", e);
        }
        return scannerList;
    }
}
