/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.security.tool.util;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.security.tool.adapter.InputAdapter;
import org.wso2.security.tool.config.GetProperties;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * FileHandler -- This utility class consists of functionality to handle file related operations.
 *
 * @author Arshika Mohottige
 */
public class FileHandler {

    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);

    /**
     * Reads the relevant InputAdapter for the uploaded data file from the properties file and returns an instance
     * of the corresponding class.
     *
     * @param dataFileInfo The file details of the data file uploaded.
     * @return returns the relevant InputAdapter for the uploaded data file.
     */
    public static InputAdapter getAdapter(FileInfo dataFileInfo) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        String className;
        className = GetProperties.getClassName(FilenameUtils.getExtension(dataFileInfo.getFileName()));
        Class clazz = Class.forName(className);
        log.info("The InputAdapter class selected is: " + className);
        return (InputAdapter) clazz.newInstance();
    }

    /**
     * Saves the uploaded files by the client.
     * Temporarily saves the files uploaded by the clients in the system's tempdir.
     *
     * @param fileInputStream The uploaded file InputStream.
     * @param fileInfo        The details about the uploaded file.
     * @throws IOException
     */
    public static String saveUploadedFile(InputStream fileInputStream,
                                          FileInfo fileInfo) throws IOException {
        Files.copy(fileInputStream, Paths.get(System.getProperty("java.io.tmpdir"), fileInfo.getFileName()),
                StandardCopyOption.REPLACE_EXISTING);
        return Paths.get(System.getProperty("java.io.tmpdir"), fileInfo.getFileName()).toString();
    }

    /**
     * Writes the given html string to a .html file.
     *
     * @param htmlString     The html string after combining the JSON data with the template file.
     * @param outputFilePath The path where the output html file is created.
     * @return
     * @throws IOException
     */
    public static boolean writeToFile(String htmlString, String outputFilePath) throws IOException {
        boolean flag = false;
        if (htmlString == null) {
            return flag;
        }

        // Creating a new html file from the given output path
        File htmlFile = new File(outputFilePath);

        // Writing the given html string to the output .html file.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(htmlFile))) {
            writer.write(htmlString);
            flag = true;
        }
        return flag;
    }
}
