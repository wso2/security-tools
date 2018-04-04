/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.automation.manager.handler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * The class {@code FileHandler} is to handle file uploading.
 * <p>Since a container starts in a separate thread, Tomcat removes uploaded file which is in Tomcat temp directory.
 * Therefore, the file cannot be directly sent to the container. So that, instead of using Tomcat temp directory, a
 * custom location is used to upload a file. Once the tasks are completed, the uploaded files aew deleted
 */
@SuppressWarnings("unused")
public class FileHandler {

    /**
     * Upload a file to a given location
     *
     * @param file           File to be uploaded
     * @param fileUploadPath File upload path
     * @throws IOException in case of a problem or the connection was aborted
     */
    public static void uploadFile(MultipartFile file, String fileUploadPath) throws IOException {
        FileUtils.copyInputStreamToFile(file.getInputStream(), new File(fileUploadPath));
    }

    /**
     * Delete a file in a given location
     *
     * @param filePath File path
     * @return Boolean to indicate the file is deleted
     */
    public static boolean deleteUploadedFile(String filePath) {
        boolean fileDeleted = false;
        File file = new File(FilenameUtils.getName(filePath));
        if (file.exists()) {
            fileDeleted = file.delete();
        }
        return fileDeleted;
    }
}
