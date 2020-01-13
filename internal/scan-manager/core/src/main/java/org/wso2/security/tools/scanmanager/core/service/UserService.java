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
package org.wso2.security.tools.scanmanager.core.service;

import org.wso2.security.tools.scanmanager.common.external.model.User;

/**
 * The user service class that manage the methods of the Users.
 */
public interface UserService {

    /**
     * Insert a user entity.
     *
     * @param user user details
     * @return the user
     */
    public User insert(User user);

    /**
     * Get user for a given username.
     *
     * @param username username of the user
     * @return the user for the given username
     */
    public User getByUsername(String username);

    /**
     * Get user for a given username.
     *
     * @param id user id of the user
     * @return the user for the given username
     */
    public User getById(String id);
}
