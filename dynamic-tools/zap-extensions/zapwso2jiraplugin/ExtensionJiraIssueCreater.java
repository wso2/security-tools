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

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.view.ZapMenuItem;

import javax.naming.AuthenticationException;
import javax.swing.ImageIcon;
import java.net.MalformedURLException;
import java.net.URL;

/*
 * An extension to create jira issues from alerts from the current session.
 * This class is defines the extension.
 */
public class ExtensionJiraIssueCreater extends ExtensionAdaptor {

    JiraRestClient jira = new JiraRestClient();

    private static final ImageIcon ICON = new ImageIcon(
            ExtensionJiraIssueCreater.class.getResource(IssueCreatorConstants.RESOURCE + "/cake.png"));
    private JiraIssueCreaterAPI api = null;
    private ZapMenuItem menuExample = null;
    private AbstractPanel statusPanel = null;
    private Logger log = Logger.getLogger(this.getClass());

    /**
     *
     */
    public ExtensionJiraIssueCreater() {
        super();
        initialize();
    }

    /**
     * @param name
     */
    public ExtensionJiraIssueCreater(String name) {
        super(name);
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setName(IssueCreatorConstants.NAME);
    }

    @Override public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        this.api = new JiraIssueCreaterAPI(this);
        API.getInstance().registerApiImplementor(api);
        log.info("access to jira issue creator is optained");

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
    createJiraIssues will be called when API action is executed
     * @param jiraBaseURL  is URL upto the jira Login window, in WSO2 it is upto <URL>/jira
     * @param jiraUserName  username of JIRA user
     * @param jiraPassword Password of the JIRA user
     * @param projectKey- is defined under every Jira project, when browsing an issue it will be the last parameter without the issue number
     * @param product - You can define a product name, which will be used in the summary to Identify the Issue clearly
     * @param issueLabel This is a custom feild defined in wso2 jira
     * @param filePath- This is the file path where ZAP generates its report. This includes file name too.
     * @return
     */

    public void createJiraIssues(String jiraBaseURL, String jiraUserName, String jiraPassword, String projectKey,
            String asssignee, String product, String issueLabel, String filePath) {

        String issue;
        String credentials = jiraUserName + ":" + jiraPassword;
        String auth = new String(Base64.encodeBase64(credentials.getBytes()));
        String BASE_URL = jiraBaseURL;
        String summary = "[JENKINS][" + product + "][DynamicScan-Nightly-Build-Report]";
        XmlDomParser xmlParser = new XmlDomParser();

        //Checking if there is any issues exists in the ZAP report generated after the scan
        boolean issueExist = xmlParser.isIssueExistsInReport();

        //Checking if there is any already available issues reported in the jira
        String issueKey = xmlParser.checkForIssueExistence(auth, BASE_URL, summary);

        String fileNewPath = xmlParser.reNameFile(product, filePath);

        if (issueExist) {
            try {
                if (issueKey.equals("")) {
                    String issueToBeCreated = xmlParser
                            .createNewTicket(projectKey, asssignee, issueLabel, summary, product);
                    issue = jira.invokePostMethod(auth, BASE_URL + "/rest/api/2/issue", issueToBeCreated);

                    JSONObject createdIssue = new JSONObject(issue);
                    issueKey = createdIssue.getString("key");
                    log.info("Issue is created under : " + issueKey);

                    jira.invokePostComment(auth, BASE_URL + "/rest/api/2/issue/" + issueKey + "/comment",
                            xmlParser.createComment());

                    jira.invokePutMethodWithFile(auth, BASE_URL + "/rest/api/2/issue/" + issueKey + "/attachments",
                            xmlParser.compressFile(fileNewPath));
                    log.info("Issue " + issueKey + " is updated with an attachment");
                } else {
                    jira.invokePostComment(auth, BASE_URL + "/rest/api/2/issue/" + issueKey + "/comment",
                            xmlParser.createComment());
                    log.info("Issue " + issueKey + " is updated with an attachment");

                    jira.invokePutMethodWithFile(auth, BASE_URL + "/rest/api/2/issue/" + issueKey + "/attachments",
                            xmlParser.compressFile(fileNewPath));
                }
            } catch (AuthenticationException e) {
                log.info("Authentication failed");
            }
        } else {
            log.info("There are no issues exists in the report");
        }
    }
}