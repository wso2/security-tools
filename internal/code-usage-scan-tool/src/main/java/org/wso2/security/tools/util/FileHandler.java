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

package org.wso2.security.tools.util;

import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.security.tools.exception.ScanToolException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * FileHandler -- This utility class consists of functionality to handle file related operations.
 */
public class FileHandler {

    public static String saveUploadedFile(InputStream fileInputStream,
                                          FileInfo fileInfo) throws ScanToolException {
        try {
            Files.copy(fileInputStream, Paths.get(System.getProperty("java.io.tmpdir"), fileInfo.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ScanToolException("IOException was thrown while saving the file: "
                    + fileInfo.getFileName(), e);
        }
        return Paths.get(System.getProperty("java.io.tmpdir"), fileInfo.getFileName()).toString();
    }
}
