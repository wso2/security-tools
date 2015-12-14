package org.zaproxy.zap.extension.jiraIssueCreater;

import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.api.*;

/**
 * Created by kausn on 11/30/15.
 */
public class JiraIssueCreaterAPI extends ApiImplementor {

    private static final Logger log = Logger.getLogger(JiraIssueCreaterAPI.class);



    /** The action of creating new Jira issues. */
    private static final String ACTION_CREATE_JIRA_ISSUE = "createJiraIssues";




    /** The mandatory parameter required for creating new Jira issues . */
    private static final String ACTION_PARAM_PROJECTKEY = "projectKey";
    private static final String ACTION_PARAM_ASSIGNEE = "assignee";

    private static final String ACTION_PARAM_BASEURL = "jiraBaseURL";
    private static final String ACTION_PARAM_JIRAUSERNAME = "jiraUserName";
    private static final String ACTION_PARAM_JIRAPASSWORD = "jiraPassword";


    private static final String ACTION_PARAM_HIGH = "high";
    private static final String ACTION_PARAM_MEDIUM = "medium";
    private static final String ACTION_PARAM_LOW = "low";


    private static final String PREFIX = "jiraIssueCreater";
    private ExtensionJiraIssueCreater extension;

    public JiraIssueCreaterAPI(ExtensionJiraIssueCreater extension){
        super();
        this.extension=extension;

        this.addApiAction(new ApiAction(ACTION_CREATE_JIRA_ISSUE, new String[] { ACTION_PARAM_BASEURL,ACTION_PARAM_JIRAUSERNAME,
                ACTION_PARAM_JIRAPASSWORD, ACTION_PARAM_PROJECTKEY,ACTION_PARAM_ASSIGNEE,
                ACTION_PARAM_HIGH,ACTION_PARAM_MEDIUM,ACTION_PARAM_LOW }));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {

        if (log.isDebugEnabled()) {
            log.debug("Request for handleApiAction: " + name + " (params: " + params.toString() + ")");
        }

        switch (name) {
            case ACTION_CREATE_JIRA_ISSUE:
                extension.createJiraIssues(params.getString(ACTION_PARAM_BASEURL),params.getString(ACTION_PARAM_JIRAUSERNAME),
                        params.getString(ACTION_PARAM_JIRAPASSWORD),params.getString(ACTION_PARAM_PROJECTKEY), params.getString(ACTION_PARAM_ASSIGNEE),
                        params.getString(ACTION_PARAM_HIGH), params.getString(ACTION_PARAM_MEDIUM), params.getString(ACTION_PARAM_LOW));
        }
        return new ApiResponseElement(name,params.toString());
    }
}
