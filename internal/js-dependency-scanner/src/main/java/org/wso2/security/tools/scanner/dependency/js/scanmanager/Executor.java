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

package org.wso2.security.tools.scanner.dependency.js.scanmanager;

import org.apache.log4j.Logger;
import org.wso2.security.tools.scanner.dependency.js.exception.ScanExecutorException;

import java.util.HashMap;
import java.util.Map;

/**
 * Main entry point to execute scanning process. This class execute scanner commands and generate the result which
 * contains vulnerabilities related to JS Libraries.
 */
public abstract class Executor {

    private static final Logger log = Logger.getLogger(Executor.class);

    /**
     * Start perform security scan using particular security tool against products.
     *
     * @param productFileMapper Mapper which holds product and it's product directory path.
     * @return Mapper which holds product and scan response
     */
    public HashMap<String, String> startScanner(HashMap<String, String> productFileMapper) {
        //Map to store scan results for each product.
        HashMap<String, String> productResponseMapper = new HashMap<>();
        for (Map.Entry<String, String> entry : productFileMapper.entrySet()) {
            //Execute scan
            String response;
            try {
                log.info("[JS_SEC_DAILY_SCAN]  " + "Start security scan for : " + entry.getKey());
                response = executeCommand(entry.getKey(), entry.getValue());
                if (response != null) {
                    productResponseMapper.put(entry.getKey(), response);
                    log.info("[JS_SEC_DAILY_SCAN]  " + "End security scan for : " + entry.getKey());
                }
            } catch (ScanExecutorException e) {
                log.error(e);
            }
        }
        return productResponseMapper;
    }

    /**
     * Abstract method to implement particular security tool's scan command.
     *
     * @param name     product name.
     * @param filePath path where the product directory is.
     * @return scan response.
     * @throws ScanExecutorException Error occurred while performing scan process.
     */
    abstract String executeCommand(String name, String filePath) throws ScanExecutorException;

}
