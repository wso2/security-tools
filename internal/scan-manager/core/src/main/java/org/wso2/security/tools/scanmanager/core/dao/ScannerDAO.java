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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wso2.security.tools.scanmanager.common.external.model.Scanner;

import java.util.List;

/**
 * The the DAO class that manage the persistence methods of the scanners.
 */
@Repository
public interface ScannerDAO extends JpaRepository<Scanner, String> {

    /**
     * Remove a scanner by id.
     *
     * @param scannerId scanner id of the scanner to be removed
     * @return updated row count
     */
    public Integer removeById(String scannerId);

    /**
     * Get all scanners.
     *
     * @return a list of scanners
     */
    public List<Scanner> findAll();

    /**
     * Get scanner by id.
     *
     * @param scannerId scanner id
     * @return scanner object for the given scanner id
     */
    public Scanner getScannerById(String scannerId);
}
