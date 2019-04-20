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
public class ScanManagerScansReponse {

    private List<ScanExternal> scanList;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
    private boolean isFirstPage;
    private boolean isLastPage;

    public ScanManagerScansReponse(List<ScanExternal> scanList, Integer totalPages, Integer currentPage,
                                   Integer pageSize, boolean hasNextPage, boolean hasPreviousPage,
                                   boolean isFirstPage, boolean isLastPage) {
        this.scanList = scanList;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.hasNextPage = hasNextPage;
        this.hasPreviousPage = hasPreviousPage;
        this.isFirstPage = isFirstPage;
        this.isLastPage = isLastPage;
    }

    public ScanManagerScansReponse() {
    }

    public List<ScanExternal> getScanList() {
        return scanList;
    }

    public void setScanList(List<ScanExternal> scanList) {
        this.scanList = scanList;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public void setFirstPage(boolean firstPage) {
        isFirstPage = firstPage;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public void setLastPage(boolean lastPage) {
        isLastPage = lastPage;
    }
}
