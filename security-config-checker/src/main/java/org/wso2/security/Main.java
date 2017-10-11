/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.common.ExceptionSingleton;
import org.wso2.security.common.Util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.wso2.security.common.Constants.CHILD_PROPERTIES_FILE_PATH;
import static org.wso2.security.common.Constants.OUTPUT_FILE_PATH;
import static org.wso2.security.common.Constants.PARENT_PROPERTIES_FILE_PATH;
import static org.wso2.security.common.Constants.PRESENT_DIRECTORY;

/**
 * This is the main class.
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;
        try {
            Util.setProductPath(args[0]);
            Util.setApplicationPath(Paths.get(PRESENT_DIRECTORY).toAbsolutePath().normalize().toString());

            fileOutputStream = new FileOutputStream(Util.getApplicationPath() + OUTPUT_FILE_PATH);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));

            Util.setBufferedWriter(bufferedWriter);

            Util.getBufferedWriter().write("-----SECURITY CONFIGURATION CHECKER------");
            log.info("SECURITY CONFIGURATION CHECK started.");

            File childPropertiesFile = new File(Util.getApplicationPath() +
                    CHILD_PROPERTIES_FILE_PATH);
            File parentPropertiesFile = new File(Util.getApplicationPath() +
                    PARENT_PROPERTIES_FILE_PATH);

            Util.validateConfigurations(childPropertiesFile, parentPropertiesFile);

            if (ExceptionSingleton.getInstance().getHasErrors()) {
                log.info("SECURITY CONFIGURATION CHECK completed with Errors.");
            } else {
                log.info("SECURITY CONFIGURATION CHECK completed successfully.");
            }
        }  catch (IOException e) {
            log.error("IOException occurred while writing to result.text", e);
        } finally {

            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                ExceptionSingleton.getInstance().setHasErrors(true);
                log.error("IOException occurred while closing the File Writer and Buffered Writer", e);
            }
        }
    }
}
