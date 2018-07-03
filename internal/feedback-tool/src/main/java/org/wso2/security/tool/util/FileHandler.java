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
import org.wso2.security.tool.exception.FeedbackToolException;

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
 */
public class FileHandler {

    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);

    /**
     * Reads the relevant InputAdapter for the uploaded data file from the properties file and returns an instance
     * of the corresponding class.
     *
     * @param dataFileInfo The file details of the data file uploaded.
     * @return returns the relevant InputAdapter for the uploaded data file.
     * @throws FeedbackToolException If an Exception is thrown inside the method implementation.
     */
    public static InputAdapter getAdapter(FileInfo dataFileInfo) throws FeedbackToolException {
        String className;
        className = new GetProperties().readProperty(FilenameUtils.getExtension(dataFileInfo.getFileName()));
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new FeedbackToolException("ClassNotFoundException was thrown while obtaining obtaining " +
                    "the InputAdapter", e);
        }
        try {
            return (InputAdapter) clazz.newInstance();
        } catch (InstantiationException e) {
            throw new FeedbackToolException("InstantiationException was thrown while obtaining obtaining " +
                    "the InputAdapter", e);
        } catch (IllegalAccessException e) {
            throw new FeedbackToolException("IllegalAccessException was thrown while obtaining obtaining " +
                    "the InputAdapter", e);
        }
    }

    /**
     * Saves the uploaded files by the client.
     * Temporarily saves the files uploaded by the clients in the system's tempdir.
     *
     * @param fileInputStream The uploaded file InputStream.
     * @param fileInfo        The details about the uploaded file.
     * @throws FeedbackToolException If an Exception is thrown inside the method implementation.
     */
    public static String saveUploadedFile(InputStream fileInputStream,
                                          FileInfo fileInfo) throws FeedbackToolException {
        try {
            Files.copy(fileInputStream, Paths.get(System.getProperty("java.io.tmpdir"), fileInfo.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FeedbackToolException("IOException was thrown while saving the file: "
                    + fileInfo.getFileName(), e);
        }
        return Paths.get(System.getProperty("java.io.tmpdir"), fileInfo.getFileName()).toString();
    }

    /**
     * Writes the given HTML string to a .html file.
     *
     * @param htmlString     The HTML string after combining the JSON data with the template file.
     * @param outputFilePath The path where the output HTML file is created.
     * @return returns false if the htmlString is null and true upon successful execution.
     * @throws FeedbackToolException If an Exception is thrown inside the method implementation.
     */
    public static boolean writeToFile(String htmlString, String outputFilePath) throws FeedbackToolException {
        boolean flag = false;
        if (htmlString == null) {
            return flag;
        }

        // Creating a new HTML file from the given output path
        File htmlFile = new File(outputFilePath);

        // Writing the given HTML string to the output .html file.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(htmlFile))) {
            writer.write(htmlString);
            flag = true;
        } catch (IOException e) {
            throw new FeedbackToolException("IOException was thrown while writing to the HTML file", e);
        }
        return flag;
    }

}
