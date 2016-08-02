/*
 *
 *  * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.zaproxy.zap.extension.wso2jiraissuecreator;


public class IssueCreatorConstants {

    // The name is public so that other extensions can access it
    public static final String NAME= "ExtensionJiraIssueCreater";

    // The i18n prefix, by default the package name - defined in one place to make it easier
    // to copy and change this example
    public static final String PREFIX = "wso2jiraissuecreator";

    public static final String RESOURCE = "/org/zaproxy/zap/extension/wso2jiraissuecreator/resources";

    /** The action of creating new Jira issues. */
    public static final String ACTION_CREATE_JIRA_ISSUE = "createJiraIssues";

    public static final String ACTION_PARAM_HIGH = "high";
    public static final String ACTION_PARAM_MEDIUM = "medium";
    public static final String ACTION_PARAM_LOW = "low";

    public static final String ACTION_PARAM_BASEURL = "jiraBaseURL";
    public static final String ACTION_PARAM_JIRAUSERNAME = "jiraUserName";
    public static final String ACTION_PARAM_JIRAPASSWORD = "jiraPassword";

    /**
     * The mandatory parameter required for creating new Jira issues .
     */
    public static final String ACTION_PARAM_PROJECTKEY = "projectKey";
    public static final String ACTION_PARAM_ASSIGNEE = "assignee";

    public static final String  ACTION_PARAM_LABEL = "label";

    public static final String ACTION_PARAM_FILTER_BY_FILE_TYPE = "filterIssuesByResourceType";
}
