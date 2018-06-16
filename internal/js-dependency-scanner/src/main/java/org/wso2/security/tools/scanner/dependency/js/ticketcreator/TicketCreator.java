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

package org.wso2.security.tools.scanner.dependency.js.ticketcreator;

import org.wso2.security.tools.scanner.dependency.js.exception.TicketCreatorException;

import java.util.HashMap;

/**
 * Abstract Class for implementing issue creator API Endpoint.
 */
public abstract class TicketCreator {

    private char[] username;
    private char[] password;
    private String endPointURL;
    private HashMap<String, String> assigneeMapper;

    TicketCreator(char[] issueCreatorAPIUserName, char[] issueCreatorAPIPassWord,
                  String url) {
        username = issueCreatorAPIUserName.clone();
        password = issueCreatorAPIPassWord.clone();
        endPointURL = url;
    }

    public char[] getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }

    String getEndPointURL() {
        return endPointURL;
    }

    HashMap<String, String> getAssigneeMapper() {
        return assigneeMapper;
    }

    public void setAssigneeMapper(HashMap<String, String> assigneeMapper) {
        this.assigneeMapper = assigneeMapper;
    }

    /**
     * Handle API calls
     *
     * @param responseMapper Mapper which holds product name and it's scan response as string.
     * @param fileMapper     Mapper which holds product name and it's report file path.
     * @throws TicketCreatorException Exception occurred while creating JIRA ticket.
     */
    public abstract void handleTicketCreatorAPICall(HashMap<String, String> responseMapper, HashMap<String, String>
            fileMapper) throws TicketCreatorException;

}
