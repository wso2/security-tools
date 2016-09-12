/*
 *
 *  * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.zaproxy.zap.extension.zapwso2jiraplugin;

public class IssueCreatorConstants {

    // The name is public so that other extensions can access it
    public static final String NAME = "ExtensionJiraIssueCreater";

    // The i18n prefix, by default the package name - defined in one place to make it easier
    // to copy and change this example
    public static final String PREFIX = "zapwso2jiraplugin";

    public static final String RESOURCE = "/org/zaproxy/zap/extension/zapwso2jiraplugin/resources";

    /**
     * The action of creating new Jira issues.
     */
    public static final String ACTION_CREATE_JIRA_ISSUE = "createJiraIssues";

    //is defined under every Jira project, when browsing an issue it will be the last parameter without the issue number
    public static final String ACTION_PARAM_PROJECT_KEY = "projectKey";

    //is URL upto the jira Login window, in WSO2 it is upto <URL>/jira
    public static final String ACTION_PARAM_BASEURL = "jiraBaseURL";

    /**
     * The mandatory parameter required for creating new Jira issues .
     */

    public static final String ACTION_PARAM_JIRAUSERNAME = "jiraUserName";
    public static final String ACTION_PARAM_JIRAPASSWORD = "jiraPassword";

    public static final String ACTION_PARAM_ASSIGNEE = "assignee";

    //issueLabel This is a custom feild defined in wso2 jira
    public static final String ACTION_PARAM_LABEL = "label";

    //product - You can define a product name, which will be used in the summary to Identify the Issue clearly
    public static final String ACTION_PARAM_PRODUCT = "product";

    // filePath- This is the file path where ZAP generates its report. This includes file name too.
    public static final String ACTION_PARAM_PATH = "filePath";

    public static final String ACTION_PARAM_WORKSPACE="workspace";

    public static final String ACTION_PARAM_FOLDER="backupPlace";

}
