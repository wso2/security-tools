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
import org.wso2.security.tools.scanmanager.common.external.model.ScannerField;

import java.util.List;

/**
 * The DAO class that manage the persistence methods of the scanner fields.
 */
@Repository
public interface ScannerFieldDAO extends JpaRepository<ScannerField, Integer> {

    /**
     * Get scanner fields for a given scanner.
     *
     * @param scanner scanner object
     * @return list of scanner fields for the given scanner
     */
    public List<ScannerField> getByScanner(Scanner scanner);

    /**
     * Get scanner field by scanner id, field id and field type.
     *
     * @param scanner scanner object
     * @param fieldId field id
     * @param type    field type
     * @return scanner field object for the given scanner, field id and type
     */
    public ScannerField getByScannerAndFieldIdAndType(Scanner scanner, String fieldId, String type);
}
