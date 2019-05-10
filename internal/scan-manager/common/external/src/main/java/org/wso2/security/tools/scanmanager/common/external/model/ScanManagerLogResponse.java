/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.common.external.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class to represent scan manager log response.
 */
public class ScanManagerLogResponse extends ScanManagerPagedResponse {

    private List<Log> logs = new ArrayList<>();

    public ScanManagerLogResponse(List<Log> logs, Integer totalPages, Integer currentPage,
                                  Integer pageSize, boolean hasNextPage, boolean hasPreviousPage, boolean isFirstPage
            , boolean isLastPage) {
        super(totalPages, currentPage, pageSize, hasNextPage, hasPreviousPage, isFirstPage, isLastPage);
        this.logs = logs;
    }

    public ScanManagerLogResponse() {
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }
}
