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

import java.util.List;

/**
 * Model class to represent scans response for scan manager.
 */
public class ScanManagerScansResponse extends ScanManagerPagedResponse {

    private List<ScanExternal> scanList;

    public ScanManagerScansResponse(List<ScanExternal> scanList, Integer totalPages, Integer currentPage,
                                    Integer pageSize, boolean hasNextPage, boolean hasPreviousPage,
                                    boolean isFirstPage, boolean isLastPage) {
        super(totalPages, currentPage, pageSize, hasNextPage, hasPreviousPage, isFirstPage, isLastPage);
        this.scanList = scanList;
    }

    public ScanManagerScansResponse() {
    }

    public List<ScanExternal> getScanList() {
        return scanList;
    }

    public void setScanList(List<ScanExternal> scanList) {
        this.scanList = scanList;
    }
}
