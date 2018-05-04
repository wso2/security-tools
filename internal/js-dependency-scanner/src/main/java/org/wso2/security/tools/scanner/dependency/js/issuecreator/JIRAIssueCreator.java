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

package org.wso2.security.tools.scanner.dependency.js.issuecreator;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.security.tools.scanner.dependency.js.constants.IssueCreatorConstants;
import org.wso2.security.tools.scanner.dependency.js.constants.JSScannerConstants;
import org.wso2.security.tools.scanner.dependency.js.exception.ConfigParserException;
import org.wso2.security.tools.scanner.dependency.js.exception.IssueCreatorException;
import org.wso2.security.tools.scanner.dependency.js.utils.ConfigParser;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.naming.AuthenticationException;


/**
 * Jira Issue Creator
 */
public class JIRAIssueCreator extends IssueCreator {
    private static final Logger log = Logger.getLogger(JIRAIssueCreator.class);
    private JIRARestClient jira;
    private static String ticketSubject;
    private static String projectKey;
    private static String issueLabel;
    private static String issueType;
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    public JIRAIssueCreator(JIRARestClient jira, char[] username, char[] password) {
        super(username, password);
        this.jira = jira;
    }


    public static void setTicketSubject(String ticketSubject) {
        JIRAIssueCreator.ticketSubject = ticketSubject;
    }


    public static void setProjectKey(String projectKey) {
        JIRAIssueCreator.projectKey = projectKey;
    }


    public static void setIssueLabel(String issueLabel) {
        JIRAIssueCreator.issueLabel = issueLabel;
    }


    public static void setIssueType(String issueType) {
        JIRAIssueCreator.issueType = issueType;
    }

    @Override
    public void handleIssueCreatorAPICall(HashMap<String, String> responseMapper, HashMap<String, String> fileMapper)
            throws ConfigParserException, IssueCreatorException, AuthenticationException {
        ConfigParser.parseJIRATicketInfo();
        String credentials = new String(getUserName()) + ":" + new String(getPassWord());
        String auth;
        auth = new String(Base64.encodeBase64(credentials.getBytes(UTF_8)), UTF_8);
        for (Map.Entry<String, String> entry : responseMapper.entrySet()) {
            if (isVulnerabilityExistInReport(entry.getValue())) {
                String summary = entry.getKey() + ":" + ticketSubject;
                String issueKey = checkForIssueExistence(auth, summary, projectKey);
                if (StringUtils.isBlank(issueKey)) {
                    createJIRAIssue(entry.getKey(), fileMapper.get(entry.getKey()), auth, summary);
                } else {
                    jira.invokePostComment(auth, IssueCreatorConstants.WSO2_JIRA_BASE_URL +
                            IssueCreatorConstants.JIRA_REST_API_URL +
                            "/" + issueKey + "/comment", createComment(entry.getKey()));
                    log.info("[JS_SEC_DAILY_SCAN] JIRA Comment added : " + createComment(entry.getKey()));
                    jira.invokePutMethodWithFile(auth, IssueCreatorConstants.WSO2_JIRA_BASE_URL +
                            IssueCreatorConstants.JIRA_REST_API_URL + "/" +
                            issueKey + "/attachments", fileMapper.get(entry.getKey()));
                }
            }
        }

    }

    /**
     * Create JIRA Issue.
     *
     * @param product  product name
     * @param filePath File path where the generated report is located. It will attached with ticket.
     * @throws IssueCreatorException exception occurred while creating issue ticket.
     */
    private void createJIRAIssue(String product, String
            filePath, String auth, String summary) throws IssueCreatorException {
        String issue;
        String assignee = null;
        if (product.contains(JSScannerConstants.AM)) {
            assignee = this.getAssigneeMapper().get(JSScannerConstants.AM);
        } else if (product.contains(JSScannerConstants.INTEGRATION)) {
            assignee = this.getAssigneeMapper().get(JSScannerConstants.INTEGRATION);
        } else if (product.contains(JSScannerConstants.STREAMPROCESSOR)) {
            assignee = this.getAssigneeMapper().get(JSScannerConstants.STREAMPROCESSOR);
        } else if (product.contains(JSScannerConstants.IDENTITYSERVER)) {
            assignee = this.getAssigneeMapper().get(JSScannerConstants.IDENTITYSERVER);
        } else if (product.contains(JSScannerConstants.OB)) {
            assignee = this.getAssigneeMapper().get(JSScannerConstants.OB);
        }

        if (assignee == null) {
            assignee = IssueCreatorConstants.DEFAULT_ASSIGNEE;
        }

        String jiraBaseUrl = IssueCreatorConstants.WSO2_JIRA_BASE_URL;

        String issueTobeCreated = createNewTicket(product, assignee, summary);
        try {
            issue = jira.invokePostMethod(auth, jiraBaseUrl + IssueCreatorConstants.JIRA_REST_API_URL,
                    issueTobeCreated);
            log.info("[JS_SEC_DAILY_SCAN] Issue ticket for " + product + " successfully created. Please find issue " +
                    "detail : ");
            JSONObject createdIssue = new JSONObject(issue);
            String issueKey = createdIssue.getString("key");
            jira.invokePostComment(auth, jiraBaseUrl + IssueCreatorConstants.JIRA_REST_API_URL +
                            "/" +
                            issueKey + "/comment",
                    createComment(product));
            log.info("[JS_SEC_DAILY_SCAN] JIRA Comment added ");
            jira.invokePutMethodWithFile(auth, jiraBaseUrl + IssueCreatorConstants.JIRA_REST_API_URL + "/" +
                    issueKey + "/attachments", filePath);
        } catch (AuthenticationException e) {
            throw new IssueCreatorException("Failed to authenticate JIRA : " + e.getMessage());
        }
    }

    /**
     * Check whether the report includes any vulnerability.
     *
     * @param result result string.
     * @return True if vulnerability exists in report else return False.
     */
    private boolean isVulnerabilityExistInReport(String result) {
        JSONArray resposeArray = new JSONArray(result);
        boolean isVulnerabilityExistInReport = false;
        if (resposeArray.length() > 0) {
            isVulnerabilityExistInReport = true;
        }
        return isVulnerabilityExistInReport;
    }

    /**
     * Create JIRA new ticket.
     *
     * @param product  product name.
     * @param assignee mail address of a person who is responsible for this ticket.
     * @return Created issue data.
     */
    private String createNewTicket(String product, String assignee, String summary) {

        String createIssueData = "{\"fields\": {\"project\": {\"key\":\"" + projectKey + "\"}," +
                "\"summary\":" + "\"" + summary + "\"" + ",  \"assignee\": {\"name\": \"" +
                assignee + "\"}," + "\"labels\":" + "[\"" + issueLabel + "\"]" + "," +
                "\"description\":" + "\"" + product + "\"" + "," +
                "\"issuetype\":{\"name\":\"" + issueType + "\"}}}";
        return createIssueData;
    }

    /**
     * Generating the comment that has to be added with the attachment
     *
     * @return comment that needs to be added when uploading a file
     */
    private String createComment(String product) {

        String comment;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        comment = "{\"body\": \"This Report is generated for " + product + " on " + dateFormat.format(date) + ".\"}";
        return comment;
    }

    /**
     * Checking if there is any issues already reported in the Jira with the samw topic
     *
     * @param auth    Base64 encoded authorization paramters
     * @param summary JIRA heading which used to find the JIRA existance
     * @return returning the JIRA key, if already jira is created else return an empty String
     */

    private String checkForIssueExistence(String auth, String summary, String projectKey) {

        String responseIssuue;
        String key = "";
        JSONObject availableIssue = null;
        summary = summary.replace("[", "");
        summary = summary.replace("]", "");

        try {
            String issueExistenceCheckUrl = IssueCreatorConstants.WSO2_JIRA_BASE_URL +
                    "rest/api/2/search?jql=project+%3d+" + projectKey +
                    "+AND+text+%7e+%22" + summary + "%22" + "&fields=" + "";
            responseIssuue = jira.invokeGetMethod(auth, issueExistenceCheckUrl);
            availableIssue = new JSONObject(responseIssuue);

        } catch (AuthenticationException e) {
            log.error("Authentication failed", e);
        }

        try {
            assert availableIssue != null;
            if (availableIssue.getJSONArray("issues").length() != 0) {
                key = availableIssue.getJSONArray("issues").getJSONObject(0).getString("key");
            }
        } catch (org.json.JSONException e) {
            log.error("Key is not already available. Creating the Jira");
        }
        return key;
    }

}
