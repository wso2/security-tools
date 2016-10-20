
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.security.tools.zap.ext.zapwso2jiraplugin;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.api.API;

import javax.naming.AuthenticationException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/*
 * An extension to create jira issues from alerts from the current session.
 * This class is defines the extension.
 */
public class JiraIssueCreatorExtension extends ExtensionAdaptor {

    JiraContentHandler updateJiraAttachments = new JiraContentHandler();
    private JiraIssueCreatorAPI jiraIssueCreatorAPI = null;
    private static final Logger log = Logger.getRootLogger();

    public JiraIssueCreatorExtension() {
        super();
        initialize();
    }

    /**
     * @param name
     */
    public JiraIssueCreatorExtension(String name) {
        super(name);
    }

    private void initialize() {
        this.setName(IssueCreatorConstants.NAME);
    }

    @Override public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        this.jiraIssueCreatorAPI = new JiraIssueCreatorAPI(this);
        API.getInstance().registerApiImplementor(jiraIssueCreatorAPI);
    }

    @Override public String getAuthor() {
        return Constant.messages.getString(IssueCreatorConstants.PREFIX + ".author");
    }

    @Override public String getDescription() {
        return Constant.messages.getString(IssueCreatorConstants.PREFIX + ".desc");
    }

    @Override public URL getURL() {
        try {
            return new URL(Constant.ZAP_EXTENSIONS_PAGE);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * createJiraIssues will be called when API action is executed
     *
     * @param jiraBaseURL  is URL upto the jira Login window, in WSO2 it is upto <URL>/jira
     * @param jiraUserName username of JIRA user
     * @param jiraPassword Password of the JIRA user
     * @param projectKey-  is defined under every Jira project, when browsing an issue it will be the last parameter without the issue number
     * @param product      - You can define a product name, which will be used in the summary to Identify the Issue clearly
     * @param issueLabel   This is a custom feild defined in wso2 jira
     * @param filePath-    This is the file path where ZAP generates its report. This includes file name too.
     * @return
     */

    public void createJiraIssues(String jiraBaseURL, String jiraUserName, String jiraPassword, String projectKey,
            String asssignee, String product, String issueLabel, String filePath, String workspace,
            String backupFolder) {


        FileHandleUtill fileHandleUtill=new FileHandleUtill();

        String issue;
        String credentials = jiraUserName + ":" + jiraPassword;
        String auth = new String(Base64.encodeBase64(credentials.getBytes()));
        String BASE_URL = jiraBaseURL;

        String version = MavenBuildReceiver.getPomVersion(new File(workspace + "/pom.xml"));

        //String summary = "[JENKINS][" + product +"-"+version+"][DynamicScan-Nightly-Build-Report]";

        String pattern = "[JENKINS][%s-%s][DynamicScan-Nightly-Build-Report]";

        String summary = String.format(pattern, product, version);

        JiraTaskHandler jiraTaskHandler = new JiraTaskHandler();
        JiraContentHandler jiraContentHandler=new JiraContentHandler();

        //Checking if there is any issues exists in the ZAP report generated after the scan
        boolean issueExist = jiraContentHandler.isIssueExistsInReport();

        //Checking if there is any already available issues reported in the jira
        if (issueExist) {
            String fileNewPath = fileHandleUtill.renameFile(product, filePath);
            String issueKey = jiraContentHandler.getIssueKeyIfExists(auth, BASE_URL, summary, projectKey);
            try {
                if (StringUtils.isBlank(issueKey)) {
                    String issueToBeCreated = jiraTaskHandler
                            .createNewTicket(projectKey, asssignee, issueLabel, summary, product);
                    issue = new JiraRestClient()
                            .invokePostMethod(auth, BASE_URL + IssueCreatorConstants.ACCESS_JIRA_ISSUES_ENDPOINT,
                                    issueToBeCreated);

                    JSONObject createdIssue = new JSONObject(issue);
                    issueKey = createdIssue.getString("key");

                } else {

                    updateJiraAttachments.modifyJiraContents(auth, BASE_URL, issueKey);

                }
            } catch (AuthenticationException e) {
                log.warn("Authenticated details provided are not valid");
            }

            try {
                new JiraRestClient().invokePutMethod(auth,
                        BASE_URL + IssueCreatorConstants.ACCESS_JIRA_ISSUES_ENDPOINT + issueKey + "/comment",
                        jiraTaskHandler.createComment());

                new JiraRestClient().invokePutMethodWithFile(auth,
                        BASE_URL + IssueCreatorConstants.ACCESS_JIRA_ISSUES_ENDPOINT + issueKey + "/attachments",
                        fileHandleUtill.compressFile(fileNewPath));

            } catch (AuthenticationException e) {
                log.warn("Authenticated details provided are not valid");
            }

            if (!(" ".equals(backupFolder)))
                fileHandleUtill.moveAttachmentToBackupFolder(backupFolder, fileHandleUtill.compressFile(fileNewPath));
            else
                log.warn("Back up location is unspecified");

        } else {
            log.info("There are no issues exists in the report");
        }
    }
}