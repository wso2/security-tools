/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanmanager.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.scanmanager.api.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.api.model.ScanRequest;
import org.wso2.security.tools.scanmanager.api.model.Scanner;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.wso2.security.tools.scanmanager.config.StartUpInit.scanManagerConfiguration;
import static org.wso2.security.tools.scanmanager.api.util.Constants.SCANNER_ID;
import static org.wso2.security.tools.scanmanager.api.util.ScanManagerUtils.addToQueue;
import static org.wso2.security.tools.scanmanager.api.util.ScanManagerUtils.generateScanId;
import static org.wso2.security.tools.scanmanager.api.util.ScanManagerUtils.uploadToFTP;

/**
 * Service layer methods to handle scanners
 */
@Service
public class ScannerService {

    /**
     * Adding the scan request to the scan queue.
     *
     * @param parameterMap
     * @param fileMap
     * @throws ScanManagerException
     */
    public void startScan(Map<String, String[]> parameterMap, Map<String, MultipartFile> fileMap)
            throws ScanManagerException {
        Map<String, String> fileIdAndNameMap = new HashMap<>();
        Map<String, String> convertedParamMap = new HashMap<>();

        String scanId = generateScanId();
        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setId(scanId);

        Iterator it = fileMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            MultipartFile multipartFile = (MultipartFile) pair.getValue();
            fileIdAndNameMap.put(pair.getKey().toString(), multipartFile.getOriginalFilename());

            uploadToFTP(multipartFile, File.separator + "files", scanId);
            it.remove(); // avoids a ConcurrentModificationException
        }

        Iterator paramIterator = parameterMap.entrySet().iterator();
        while (paramIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) paramIterator.next();
            String[] paramValue = (String[]) pair.getValue();
            convertedParamMap.put(pair.getKey().toString(), String.join(",", paramValue));
            paramIterator.remove();
        }

        scanRequest.setFileMap(fileIdAndNameMap);
        scanRequest.setParameterMap(convertedParamMap);

        //adding the scan to the queue
        addToQueue(convertedParamMap.get(SCANNER_ID), scanRequest);
    }

    /**
     * Get the list of scanners
     *
     * @return
     */
    public List<Scanner> getScanners() {
        return scanManagerConfiguration.getScanners();
    }
}