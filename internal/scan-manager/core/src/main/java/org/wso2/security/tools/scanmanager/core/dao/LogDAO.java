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
package org.wso2.security.tools.scanmanager.core.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.wso2.security.tools.scanmanager.common.external.model.Log;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;

/**
 * The class {@code LogDAO} is the DAO class that manage the persistence methods of the
 * Scan logs.
 */
@Repository
public interface LogDAO extends PagingAndSortingRepository<Log, Integer> {

    /**
     * Get logs by scan.
     *
     * @param scan scan details
     * @param pageable page information
     * @return list of logs for a given scan
     */
    public Page<Log> getByScanOrderByTimeStampDesc(Scan scan, Pageable pageable);
}
