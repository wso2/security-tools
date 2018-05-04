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

package org.wso2.security.tools.scanner.dependency.js.reportpublisher;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.wso2.security.tools.scanner.dependency.js.exception.FileHandlerException;

import java.io.IOException;
import java.util.HashMap;

/**
 * Abstract class to upload report to end point. It has the common methods for all endpoints where reports can be
 * uploaded.
 */
public abstract class ReportUploader {
    private HashMap<String, String> reportFileMapper;

    /**
     * Publish report to any endpoint. currently it supports to github.
     *
     * @param productResponseMapper Mapper for product and scan result.
     * @throws GitAPIException      Exception occurred during github API call.
     * @throws IOException          IO Exception.
     * @throws FileHandlerException Exception occurred during report generation.
     */
    public abstract void publishReport(HashMap<String, String> productResponseMapper) throws GitAPIException,
            IOException, FileHandlerException;

    public HashMap<String, String> getReportFileMapper() {
        return reportFileMapper;
    }

    void setReportFileMapper(HashMap<String, String> reportFileMapper) {
        this.reportFileMapper = reportFileMapper;
    }

}
