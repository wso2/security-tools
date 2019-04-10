/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.scanmanager.webapp.config.ScanManagerWebappConfiguration;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;
import org.wso2.security.tools.scanmanager.webapp.handlers.HttpRequestHandler;
import org.wso2.security.tools.scanmanager.webapp.model.Field;
import org.wso2.security.tools.scanmanager.webapp.model.Scan;
import org.wso2.security.tools.scanmanager.webapp.model.Scanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scan service that contains the scan business logic.
 */
@Service
public class ScanService {

    private static final Logger logger = LoggerFactory.getLogger(ScanService.class);

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String DISPLAY_NAME = "displayName";
    private static final String TYPE = "type";
    private static final String FIELDS = "fields";
    private static final String OWNER = "owner";
    private static final String STATUS = "status";
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";

    /**
     * Start a scan.
     *
     * @param multipartFileMultiValueMap
     * @param parameterMap
     * @return
     */
    public int startScan(Map<String, MultipartFile> multipartFileMultiValueMap, Map<String, String[]> parameterMap) {
        int responseStatus = -1;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httppost = new HttpPost(ScanManagerWebappConfiguration.getInstance().getScanURL());
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            for (Map.Entry<String, MultipartFile> entry : multipartFileMultiValueMap
                    .entrySet()) {
                multipartEntityBuilder.addBinaryBody(entry.getKey(), entry.getValue()
                        .getInputStream(), ContentType.MULTIPART_FORM_DATA, entry.getValue().getOriginalFilename());
            }
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                multipartEntityBuilder.addTextBody(entry.getKey(), String.join(",", entry.getValue()));
            }

            HttpEntity httpEntity = multipartEntityBuilder.build();
            httppost.setEntity(httpEntity);
            HttpResponse httpResponse = client.execute(httppost);
            responseStatus = httpResponse.getStatusLine().getStatusCode();
            if (responseStatus != HttpStatus.SC_OK) {
                logger.error("Error occurred while initiating the scan."
                        + httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (ScanManagerWebappException | IOException e) {
            logger.error("Unable to start the scan", e);
        }
        return responseStatus;
    }

    /**
     * Get the list of scanners.
     *
     * @return
     */
    public List<Scanner> getScanners() {
        MultiValueMap headerMap = new LinkedMultiValueMap();
        Map<String, Object> paramMap = new HashMap<>();
        List<Scanner> scannerList = new ArrayList<>();
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler();

        try {
            ResponseEntity<String> responseEntity = httpRequestHandler
                    .sendGET(ScanManagerWebappConfiguration.getInstance().getScannersURL().toString(), headerMap,
                            paramMap);

            if (responseEntity != null) {
                JSONArray responseArray = new JSONArray(responseEntity.getBody());
                for (int arrayIndex = 0; arrayIndex < responseArray.length(); arrayIndex++) {
                    JSONObject jsonObject = responseArray.getJSONObject(arrayIndex);
                    Scanner scanner = new Scanner();
                    scanner.setName(jsonObject.get(NAME).toString());
                    scanner.setId(jsonObject.get(ID).toString());
                    scanner.setType(jsonObject.get(TYPE).toString());

                    JSONArray fieldsArray = (JSONArray) jsonObject.get(FIELDS);
                    ArrayList<Field> fieldsList = new ArrayList<>();
                    for (int fieldsArrayIndex = 0; fieldsArrayIndex < fieldsArray.length(); fieldsArrayIndex++) {
                        JSONObject fieldObject = fieldsArray.getJSONObject(fieldsArrayIndex);
                        Field field = new Field(fieldObject.get(ID).toString(),
                                fieldObject.get(DISPLAY_NAME).toString(), fieldObject.get(TYPE).toString());
                        fieldsList.add(field);
                    }
                    scanner.setFields(fieldsList);
                    scannerList.add(scanner);
                }
            }
        } catch (ScanManagerWebappException | RestClientException | JSONException e) {
            logger.error("Unable to get the scanners", e);
        }
        return scannerList;
    }

    /**
     * Get the list of scans in the pool.
     *
     * @return
     */
    public List<Scan> getScans() {
        MultiValueMap headerMap = new LinkedMultiValueMap();
        Map<String, Object> paramMap = new HashMap<>();
        List<Scan> scanList = new ArrayList<>();
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler();

        try {
            ResponseEntity<String> responseEntity = httpRequestHandler
                    .sendGET(ScanManagerWebappConfiguration.getInstance().getScanURL().toString(), headerMap, paramMap);
            if (responseEntity != null) {
                JSONArray responseArray = new JSONArray(responseEntity.getBody());
                for (int arrayIndex = 0; arrayIndex < responseArray.length(); arrayIndex++) {
                    JSONObject jsonObject = responseArray.getJSONObject(arrayIndex);
                    Scan scan = new Scan();
                    scan.setId(jsonObject.get(ID).toString());
                    scan.setName(jsonObject.get(NAME).toString());
                    scan.setOwner(jsonObject.get(OWNER).toString());
                    scan.setStatus(jsonObject.get(STATUS).toString());
                    scanList.add(scan);
                }
            }
        } catch (ScanManagerWebappException | RestClientException | JSONException e) {
            logger.error("Unable to get the scans", e);
        }
        return scanList;
    }

    /**
     * Stop a scan.
     *
     * @param id
     * @return
     */
    public ResponseEntity stopScan(String id) {
        ResponseEntity<String> responseEntity = null;
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
        MultiValueMap headerMap = new LinkedMultiValueMap();
        Map<String, Object> paramMap = new HashMap<>();

        headerMap.add(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_HEADER_VALUE);
        paramMap.put(ID, id);
        try {
            responseEntity =
                    httpRequestHandler.sendPOST(ScanManagerWebappConfiguration.getInstance().getScanURL().toString(),
                            headerMap, paramMap);
        } catch (ScanManagerWebappException | RestClientException | JSONException e) {
            logger.error("Unable to stop the scan " + id, e);
        }
        if (responseEntity != null) {
            return responseEntity;
        } else {
            return new ResponseEntity(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
