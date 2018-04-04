/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.am.webapp.entity;

/**
 * The class {@link User} is the model to store user data
 */
public class User {

    private String email;

    /**
     *
     * @return Email of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set email of the user
     * @param email Email address
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
