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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.security.tools.scanner.dependency.js.constants.JSScannerConstants;
import org.wso2.security.tools.scanner.dependency.js.constants.TicketCreatorConstants;
import org.wso2.security.tools.scanner.dependency.js.exception.ConfigParserException;
import org.wso2.security.tools.scanner.dependency.js.exception.TicketCreatorException;
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
 * JIRA  Creator. This class responsible for essential functions for creating a JIRA ticket and
 * update existing tickets with newly scan reports.
 */
public class JIRATicketCreator extends TicketCreator {

    private static final Logger log = Logger.getLogger(JIRATicketCreator.class);
    private JIRARestClient jira;
    private static String ticketSubject;
    private static String projectKey;
    private static String issueLabel;
    private static String issueType;
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    public JIRATicketCreator(JIRARestClient jira, char[] username, char[] password, String url) {
        super(username, password, url);
        this.jira = jira;
    }

    public static void setTicketSubject(String ticketSubject) {
        JIRATicketCreator.ticketSubject = ticketSubject;
    }

    public static void setProjectKey(String projectKey) {
        JIRATicketCreator.projectKey = projectKey;
    }

    public static void setIssueLabel(String issueLabel) {
        JIRATicketCreator.issueLabel = issueLabel;
    }

    public static void setIssueType(String issueType) {
        JIRATicketCreator.issueType = issueType;
    }

    /**
     * This method is responsible to create JIRA ticket for every scan.
     *
     * @param responseMapper Mapper which holds product name and it's scan response as string.
     * @param fileMapper     Mapper which holds product name and it's report file path.
     * @throws TicketCreatorException Exception occurred while creating ticket.
     */
    @Override
    public void handleTicketCreatorAPICall(HashMap<String, String> responseMapper, HashMap<String, String> fileMapper)
            throws TicketCreatorException {
        try {
            ConfigParser.parseJIRATicketInfo();
        } catch (ConfigParserException e) {
            throw new TicketCreatorException("Unable to create JIRA ticket for all products.", e);
        }
        String credentials = new String(getUsername()) + ":" + new String(getPassword());
        String auth;
        auth = new String(Base64.encodeBase64(credentials.getBytes(UTF_8)), UTF_8);
        for (Map.Entry<String, String> entry : responseMapper.entrySet()) {
            if (isVulnerabilityExistInReport(entry.getValue())) {
                //Get the index of the last occurrence of the character ('-') in product name.
                int index = entry.getKey().lastIndexOf("-");
                String summary = entry.getKey().substring(0, index) + ":" + ticketSubject;
                try {
                    String ticketId = checkForTicketExistence(auth, summary, projectKey);
                    if (StringUtils.isBlank(ticketId)) {
                        createJIRATicket(entry.getKey(), fileMapper.get(entry.getKey()), auth, summary);
                    } else {
                        jira.invokePostComment(auth, getEndPointURL() +
                                TicketCreatorConstants.JIRA_REST_API_URL +
                                "/" + ticketId + "/comment", buildComment(entry.getKey()));
                        log.info("[JS_SEC_DAILY_SCAN] JIRA Comment added : " + buildComment(entry.getKey()));
                        jira.invokePutMethodWithFile(auth, getEndPointURL() +
                                TicketCreatorConstants.JIRA_REST_API_URL + "/" +
                                ticketId + "/attachments", fileMapper.get(entry.getKey()));
                    }
                } catch (AuthenticationException e) {
                    throw new TicketCreatorException("Failed to add comment for attached file in jira ticket." +
                            entry.getKey().substring(0, index), e);
                }
            }
        }
    }

    /**
     * Create JIRA Ticket.
     *
     * @param product  product name
     * @param filePath File path where the generated report is located. It will attached with ticket.
     * @param auth     Credential data which is encoded using base64.
     * @param summary  Summary of the JIRA issue ticket.
     * @throws TicketCreatorException exception occurred while creating issue ticket.
     */
    private void createJIRATicket(String product, String
            filePath, String auth, String summary) throws TicketCreatorException {
        String ticket;
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
            assignee = TicketCreatorConstants.DEFAULT_ASSIGNEE;
        }
        String jiraBaseUrl = getEndPointURL();
        String ticketToBeCreated = buildNewTicketData(product, assignee, summary);
        try {
            ticket = jira.invokePostMethod(auth, jiraBaseUrl + TicketCreatorConstants.JIRA_REST_API_URL,
                    ticketToBeCreated);
            JSONObject createdTicket = new JSONObject(ticket);
            String ticketId = createdTicket.getString("key");
            log.info("[JS_SEC_DAILY_SCAN] Issue ticket for " + product + " successfully created.");
            jira.invokePostComment(auth, jiraBaseUrl + TicketCreatorConstants.JIRA_REST_API_URL +
                    "/" + ticketId + "/comment", buildComment(product));
            log.info("[JS_SEC_DAILY_SCAN] JIRA Comment added ");
            jira.invokePutMethodWithFile(auth, jiraBaseUrl + TicketCreatorConstants.JIRA_REST_API_URL + "/" +
                    ticketId + "/attachments", filePath);
        } catch (AuthenticationException e) {
            throw new TicketCreatorException("Failed to authenticate JIRA : ", e);
        }
    }

    /**
     * Check whether the report includes any vulnerability.
     *
     * @param result result string.
     * @return True if vulnerability exists in report else return False.
     */
    private boolean isVulnerabilityExistInReport(String result) {
        JSONArray responseArray = new JSONArray(result);
        boolean isVulnerabilityExistInReport = false;
        if (responseArray.length() > 0) {
            isVulnerabilityExistInReport = true;
        }
        return isVulnerabilityExistInReport;
    }

    /**
     * Build new JIRA ticket data.
     *
     * @param product  product name.
     * @param assignee mail address of a person who is responsible for this ticket.
     * @param summary  summary of the JIRA Issue Ticket.
     * @return Created issue data.
     */
    private String buildNewTicketData(String product, String assignee, String summary) {
        String ticketDataToBeCreated = "{\"fields\": {\"project\": {\"key\":\"" + projectKey + "\"}," +
                "\"summary\":" + "\"" + summary + "\"" + ",  \"assignee\": {\"name\": \"" +
                assignee + "\"}," + "\"labels\":" + "[\"" + issueLabel + "\"]" + "," +
                "\"description\":" + "\"" + product + "\"" + "," +
                "\"issuetype\":{\"name\":\"" + issueType + "\"}}}";
        return ticketDataToBeCreated;
    }

    /**
     * Generating the comment that has to be added with the attachment
     *
     * @return comment that needs to be added when uploading a file
     */
    private String buildComment(String product) {
        String comment;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        comment = "{\"body\": \"This Report is generated for " + product + " on " + dateFormat.format(date) + ".\"}";
        return comment;
    }

    /**
     * Checking if there is any tickets already reported in the JIRA with the same summary.
     *
     * @param auth       Base64 encoded authorization parameters.
     * @param summary    JIRA heading which used to find the JIRA Ticket existance.
     * @param projectKey Key which indicate the project.
     * @return returning the JIRA Ticket key, if already jira is created else return an empty String.
     * @throws TicketCreatorException Exception occurred while creating ticket.
     */
    private String checkForTicketExistence(String auth, String summary, String projectKey)
            throws TicketCreatorException {
        String responseIssue;
        String key = "";
        JSONObject availableTicket;
        summary = summary.replace("[", "");
        summary = summary.replace("]", "");
        try {
            String ticketExistenceCheckUrl = getEndPointURL() +
                    "rest/api/2/search?jql=project+%3d+" + projectKey +
                    "+AND+summary+%7e+%22" + summary + "%22" + "&fields=" + "";
            responseIssue = jira.invokeGetMethod(auth, ticketExistenceCheckUrl);
            availableTicket = new JSONObject(responseIssue);
            if (availableTicket.getJSONArray("issues").length() != 0) {
                key = availableTicket.getJSONArray("issues").getJSONObject(0).getString("key");
            }
        } catch (AuthenticationException e) {
            throw new TicketCreatorException("Authentication failed", e);
        } catch (org.json.JSONException e) {
            throw new TicketCreatorException("Key is not already available. Creating the JIRA", e);
        }
        return key;
    }

}
