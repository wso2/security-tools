/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.security.tools.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.exception.ConfigCheckerException;
import org.wso2.security.tools.model.report.ConfigElement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generates a text scan report.
 */
public class TextReportGenerator implements ReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(TextReportGenerator.class);
    public static final String OUTPUT_FILE_NAME = "report.text";

    @Override
    public void generateReport(HashMap<String, List<ConfigElement>> configElementsResultMap,
                               String outputPath) throws ConfigCheckerException {

        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;
        try {
            log.info("Generating Text Scan Report.");

            fileOutputStream = new FileOutputStream(outputPath + File.separator + OUTPUT_FILE_NAME);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));

            Iterator iterator = configElementsResultMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry) iterator.next();
                bufferedWriter.write("File: " + pair.getKey() + " \n\n");

                List<ConfigElement> configElementList = (List<ConfigElement>) pair.getValue();
                for (ConfigElement configElement : configElementList) {
                    bufferedWriter.write(configElement.getId() + " : " + configElement.getResult() + "\n");
                }
                iterator.remove();
                bufferedWriter.write("\n\n");
            }
            log.info("Text Scan Report generation completed.");
        } catch (IOException e) {
            throw new ConfigCheckerException("Error occurred while writing to " + outputPath
                    + File.separator + OUTPUT_FILE_NAME, e);
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                throw new ConfigCheckerException("Error occurred while closing the File Writer and Buffered Writer", e);
            }
        }
    }
}
