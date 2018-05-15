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

import org.wso2.security.tools.scanner.dependency.js.constants.RetireJSScannerConstants;
import org.wso2.security.tools.scanner.dependency.js.exception.ScanExecutorException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


/**
 * Executor class for Retire.js.
 */
public class RetireJsExecutor extends Executor {

    /**
     * Execute retire.js command against product directory.
     *
     * @param name     product name.
     * @param filePath path where the product directory is.
     * @return Result
     * @throws ScanExecutorException Exception occurred while performing scanning.
     */
    public String executeCommand(String name, String filePath) throws ScanExecutorException {
        StringBuilder stringBuilder;
        BufferedReader reader = null;
        try {
            File file = new File(filePath);
            //Execute scan command for retire.js
            ProcessBuilder processBuilder = new ProcessBuilder(RetireJSScannerConstants.RETIRE,
                    RetireJSScannerConstants.JS_COMMAND, RetireJSScannerConstants.OUTPUT_FORMAT,
                    RetireJSScannerConstants.JSON);
            processBuilder.directory(file);
            stringBuilder = new StringBuilder();
            Process p = processBuilder.start();
            String line;
            reader =
                    new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8));
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            throw new ScanExecutorException("Error occurred while performing Retire.js scan " +
                    "against " + name + " :", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                throw new ScanExecutorException("Error occurred while performing Retire.js scan " +
                        "against " + name + " :", e);
            }
        }
        return stringBuilder.toString();
    }

}
