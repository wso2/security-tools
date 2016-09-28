/*
 *
 *  * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.security.tools.zap.ext.zapwso2jiraplugin;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class FileHandleUtill {

    private static final Logger log = Logger.getRootLogger();

    public void moveAttachmentToBackupFolder(String path, String filePath) {

        File source = new File(filePath);
        File dest = new File(path.trim());

        try {
            FileUtils.copyFileToDirectory(source, dest);
        } catch (IOException e) {
            log.error("File not found in the specified path");
        }
    }
}
