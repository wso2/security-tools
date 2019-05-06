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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.scanmanager.common.external.model.ScanExternal;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerScansResponse;
import org.wso2.security.tools.scanmanager.webapp.config.ScanManagerWebappConfiguration;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;
import org.wso2.security.tools.scanmanager.webapp.util.FTPUtil;
import org.wso2.security.tools.scanmanager.webapp.util.HTTPUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.security.tools.scanmanager.webapp.util.Constants.PAGE_PARAM_NAME;

/**
 * Scan service implementation class.
 */
@Service
public class ScanServiceImpl implements ScanService {

    private static final String SCAN_REQUEST_FILE_MAP_ATTRIBUTE_NAME = "fileMap";
    private static final String SCAN_REQUEST_PROPERTY_MAP_ATTRIBUTE_NAME = "propertyMap";
    private static final String SCAN_REQUEST_SCAN_NAME_ATTRIBUTE_NAME = "scanName";
    private static final String SCAN_REQUEST_SCAN_DESCRIPTION_ATTRIBUTE_NAME = "scanDescription";
    private static final String SCAN_REQUEST_SCAN_TYPE_ATTRIBUTE_NAME = "scanType";
    private static final String SCAN_REQUEST_PRODUCT_NAME_ATTRIBUTE_NAME = "productName";
    private static final String SCAN_REQUEST_SCANNER_ID_ATTRIBUTE_NAME = "scannerId";

    @Override
    public int submitScan(Map<String, MultipartFile> fileMap, Map<String, String> parameterMap,
                          String scanDirectory)
            throws ScanManagerWebappException {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        Map<String, String> uploadedFileMap = FTPUtil.uploadFilesToFTP(scanDirectory, fileMap);

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put(SCAN_REQUEST_FILE_MAP_ATTRIBUTE_NAME, uploadedFileMap);
        requestParams.put(SCAN_REQUEST_PROPERTY_MAP_ATTRIBUTE_NAME, parameterMap);
        requestParams.put(SCAN_REQUEST_SCAN_NAME_ATTRIBUTE_NAME,
                parameterMap.get(SCAN_REQUEST_SCAN_NAME_ATTRIBUTE_NAME));
        requestParams.put(SCAN_REQUEST_SCAN_DESCRIPTION_ATTRIBUTE_NAME,
                parameterMap.get(SCAN_REQUEST_SCAN_DESCRIPTION_ATTRIBUTE_NAME));
        requestParams.put(SCAN_REQUEST_SCAN_TYPE_ATTRIBUTE_NAME,
                parameterMap.get(SCAN_REQUEST_SCAN_TYPE_ATTRIBUTE_NAME));
        requestParams.put(SCAN_REQUEST_PRODUCT_NAME_ATTRIBUTE_NAME,
                parameterMap.get(SCAN_REQUEST_PRODUCT_NAME_ATTRIBUTE_NAME));
        requestParams.put(SCAN_REQUEST_SCANNER_ID_ATTRIBUTE_NAME,
                parameterMap.get(SCAN_REQUEST_SCANNER_ID_ATTRIBUTE_NAME));

        ResponseEntity<String> responseEntity =
                HTTPUtil.sendPOST(ScanManagerWebappConfiguration.getInstance().getScanURL("",
                        nameValuePairs).toString(), null, requestParams);
        return responseEntity.getStatusCode().value();
    }

    @Override
    public ScanManagerScansResponse getScans(Integer pageNumber) throws ScanManagerWebappException {
        ScanManagerScansResponse scansResponse = null;
        List<NameValuePair> nameValuePairs = new ArrayList<>();

        try {
            if (pageNumber != null) {
                nameValuePairs.add(new BasicNameValuePair(PAGE_PARAM_NAME, pageNumber.toString()));
            }
            ResponseEntity<String> responseEntity =
                    HTTPUtil.sendGET(ScanManagerWebappConfiguration.getInstance()
                            .getScanURL("", nameValuePairs).toString(), null, null);
            ObjectMapper mapper = new ObjectMapper();
            scansResponse = mapper.readValue(responseEntity.getBody(), ScanManagerScansResponse.class);
        } catch (IOException e) {
            throw new ScanManagerWebappException("Unable to get the scans", e);
        }
        return scansResponse;
    }

    @Override
    public ScanExternal getScan(String jobId) throws ScanManagerWebappException {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        ScanExternal scan = null;
        try {
            ResponseEntity<String> responseEntity =
                    HTTPUtil.sendGET(ScanManagerWebappConfiguration.getInstance()
                            .getScanURL("/" + jobId, nameValuePairs).toString(), null, null);
            if (responseEntity != null) {
                ObjectMapper mapper = new ObjectMapper();
                scan = mapper.readValue(responseEntity.getBody(), ScanExternal.class);
            }
        } catch (IOException e) {
            throw new ScanManagerWebappException("Unable to get the scan details for the job id: " + jobId, e);
        }
        return scan;
    }

    @Override
    public void getScanReport(String reportPath, String outputFilePath) throws ScanManagerWebappException {
        FTPUtil.downloadFromFTP(reportPath, outputFilePath);
    }

    @Override
    public ResponseEntity stopScan(String id) throws ScanManagerWebappException {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        return HTTPUtil.sendDelete(ScanManagerWebappConfiguration.getInstance()
                .getScanURL(id, nameValuePairs).toString(), null, null);
    }
}
