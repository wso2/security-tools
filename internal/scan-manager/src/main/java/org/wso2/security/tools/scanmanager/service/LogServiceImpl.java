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
package org.wso2.security.tools.scanmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wso2.security.tools.scanmanager.dao.LogDAO;
import org.wso2.security.tools.scanmanager.model.Log;

/**
 * The class {@code LogService} is the service class that manage the method implementations of the Scan logs.
 */
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    LogDAO logDAO;

    @Override
    @Transactional
    public boolean persist(Log log) {
        return logDAO.persist(log);
    }
}
