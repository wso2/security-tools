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

package org.wso2.security.tools.scanner.dependency.js.constants;

/**
 * Constants related to creating an issue ticket in JIRA.
 */
public class TicketCreatorConstants {

    public static final String WSO2_JIRA_BASE_URL = "endpointurl";
    public static final String JIRA_REST_API_URL = "rest/api/2/issue";

    // Constants for assignees of particular product ticket
    public static final String IDENTITYSERVER = "imTicketAssignee";
    public static final String APIM = "apimTicketAssignee";
    public static final String INTEGRATION = "eiTicketAssignee";
    public static final String STREAMPROCESSOR = "spTicketAssignee";
    public static final String OPENBANKING = "obTicketAssignee";

    // Constants related to jira ticket
    public static final String TICKET_SUBJECT = "ticketSubject";
    public static final String PROJECT_KEY = "projectKey";
    public static final String ISSUELABEL = "issueLabel";
    public static final String ISSUE_TYPE = "issueType";
    public static final String DEFAULT_ASSIGNEE = "-1";

}
