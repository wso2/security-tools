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

import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.api.*;


public class JiraIssueCreaterAPI extends ApiImplementor {

    private static final Logger log = Logger.getLogger(JiraIssueCreaterAPI.class);

    private ExtensionJiraIssueCreater extension;

    public JiraIssueCreaterAPI(ExtensionJiraIssueCreater extension) {
        super();
        this.extension = extension;

        this.addApiAction(new ApiAction(IssueCreatorConstants.ACTION_CREATE_JIRA_ISSUE,
                new String[] { IssueCreatorConstants.ACTION_PARAM_BASEURL,
                        IssueCreatorConstants.ACTION_PARAM_JIRAUSERNAME,
                        IssueCreatorConstants.ACTION_PARAM_JIRAPASSWORD, IssueCreatorConstants.ACTION_PARAM_PROJECTKEY,
                        IssueCreatorConstants.ACTION_PARAM_ASSIGNEE, IssueCreatorConstants.ACTION_PARAM_HIGH,
                        IssueCreatorConstants.ACTION_PARAM_MEDIUM, IssueCreatorConstants.ACTION_PARAM_LOW,
                        IssueCreatorConstants.ACTION_PARAM_LABEL },
                new String[] { IssueCreatorConstants.ACTION_PARAM_FILTER_BY_FILE_TYPE }));
    }

    @Override public String getPrefix() {
        return IssueCreatorConstants.PREFIX;
    }

    @Override public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {

        if (log.isDebugEnabled()) {
            log.debug("Request for handleApiAction: " + name + " (params: " + params.toString() + ")");
        }

        switch (name) {
        case IssueCreatorConstants.ACTION_CREATE_JIRA_ISSUE:
            extension.createJiraIssues(params.getString(IssueCreatorConstants.ACTION_PARAM_BASEURL),
                    params.getString(IssueCreatorConstants.ACTION_PARAM_JIRAUSERNAME),
                    params.getString(IssueCreatorConstants.ACTION_PARAM_JIRAPASSWORD),
                    params.getString(IssueCreatorConstants.ACTION_PARAM_PROJECTKEY),
                    params.getString(IssueCreatorConstants.ACTION_PARAM_ASSIGNEE),
                    params.getString(IssueCreatorConstants.ACTION_PARAM_HIGH),
                    params.getString(IssueCreatorConstants.ACTION_PARAM_MEDIUM),
                    params.getString(IssueCreatorConstants.ACTION_PARAM_LOW),
                    params.getString(IssueCreatorConstants.ACTION_PARAM_LABEL ),
                    params.getString(IssueCreatorConstants.ACTION_PARAM_FILTER_BY_FILE_TYPE));
        }
        return new ApiResponseElement(name, params.toString());
    }
}
