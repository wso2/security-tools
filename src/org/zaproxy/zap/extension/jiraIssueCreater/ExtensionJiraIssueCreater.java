/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.jiraIssueCreater;

import com.sun.jersey.core.util.Base64;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.view.ZapMenuItem;

import javax.naming.AuthenticationException;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/*
 * An extension to create jira issues from alerts from the current session.
 *
 * This class is defines the extension.
 */
public class ExtensionJiraIssueCreater extends ExtensionAdaptor {

    // The name is public so that other extensions can access it
    public static final String NAME = "ExtensionJiraIssueCreater";

    // The i18n prefix, by default the package name - defined in one place to make it easier
    // to copy and change this example
    protected static final String PREFIX = "jiraIssueCreater";

    private static final String RESOURCE = "/org/zaproxy/zap/extension/jiraIssueCreater/resources";

    private static final ImageIcon ICON = new ImageIcon(
            ExtensionJiraIssueCreater.class.getResource( RESOURCE + "/cake.png"));

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
     *
     */
    private void initialize() {
        this.setName(NAME);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        if (getView() != null) {
            // Register our top menu item, as long as we're not running as a daemon
            // Use one of the other methods to add to a different menu list
            extensionHook.getHookMenu().addReportMenuItem(getMenuExample());
            extensionHook.getHookView().addStatusPanel(getStatusPanel());
        }

        this.api=new JiraIssueCreaterAPI(this);
        API.getInstance().registerApiImplementor(api);

    }

    private AbstractPanel getStatusPanel() {
        if (statusPanel == null) {
            statusPanel = new AbstractPanel();
            statusPanel.setLayout(new CardLayout());
            statusPanel.setName(Constant.messages.getString(PREFIX + ".panel.title"));
            statusPanel.setIcon(ICON);
            JTextPane pane = new JTextPane();
            pane.setEditable(false);
            pane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            pane.setContentType("text/html");
            pane.setText(Constant.messages.getString(PREFIX + ".panel.msg"));
            statusPanel.add(pane);
        }
        return statusPanel;
    }

    private ZapMenuItem getMenuExample() {
        if (menuExample == null) {
            menuExample = new ZapMenuItem(PREFIX + ".topmenu.report.title");

            menuExample.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent ae) {

                    String zap_home = Constant.getZapHome();
                    Properties prop = new Properties();
                    InputStream input = null;
                    CredentialForm credFrm = new CredentialForm();
                    credFrm.setTitle("Credential Form ");


                    File cred_file = new File(zap_home + "/cred.properties");

                    if (cred_file.exists()) { //if file exists read from file

                        try {
                            input = new FileInputStream(zap_home + "/cred.properties");
                            prop.load(input);

                            if (input != null) {
                                JiraIssueCreaterForm create_issues = new JiraIssueCreaterForm();
                                create_issues.setTitle("Create Jira Issues");
                                create_issues.listJiraProjects();
                                create_issues.show();
                            }
                            input.close();

                        } catch (FileNotFoundException e) {
                            log.error(e.getMessage(), e);
                            View.getSingleton().showWarningDialog("Credential file not found !!");
                            credFrm.show();

                        } catch (IOException e) {
                            log.error(e.getMessage(), e);

                        } catch (AuthenticationException e) { //jira throws a capcha user has to log and try again
                            cred_file.delete();
                            log.error(e.getMessage(), e);
                            View.getSingleton().showWarningDialog("Wrong Credentials! Please login to your jira account and retry!!");

                        }

                    } else { //create credential file if not found

                        credFrm.show();

                    }

                }
            });
        }
        return menuExample;
    }



    @Override
    public String getAuthor() {
        return Constant.messages.getString(PREFIX+".author");
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString(PREFIX + ".desc");
    }

    @Override
    public URL getURL() {
        try {
            return new URL(Constant.ZAP_EXTENSIONS_PAGE);
        } catch (MalformedURLException e) {
            return null;
        }
    }


    /**
     * api methods
     * **/

    private static boolean stringToBool(String s) {
        if (s.equals("1"))
            return true;
        if (s.equals("0"))
            return false;
        throw new IllegalArgumentException(s+" is not a bool. Only 1 and 0 are.");
    }

    public void createJiraIssues(String jiraBaseURL, String jiraUserName, String jiraPassword,
                                 String projectKey,String asssignee, String high, String medium, String low){

        String project_key = projectKey;
        String issueList[];
        JiraRestClient jira = new JiraRestClient();
        int issueCount;
        String issue;


        try {

            String auth = new String(Base64.encode(jiraUserName+":"+jiraPassword));
            String BASE_URL = jiraBaseURL;


                XmlDomParser xmlParser = new XmlDomParser();
                if(high.equals("1")|| medium.equals("1")|| low.equals("1")) {
                    issueList = xmlParser.parseXmlDoc(project_key, asssignee,
                            stringToBool(high), stringToBool(medium), stringToBool(low)); // parse xml report with filters
                    issueCount = issueList.length; //get the issue count from the preset last index

                    if (issueCount != 0) { //proceed if the issue count is > 1
                        for (int i = 0; i < issueCount; i++) { //create Issues in jira

                            if(xmlParser.checkForIssueExistence(issueList[i],project_key)){ //update if the issue already exists
                                xmlParser.updateExistingIssue(issueList[i],auth,BASE_URL,i);
                            }else {                                             //create a new issue if not
                                issue = jira.invokePostMethod(auth, BASE_URL + "/rest/api/2/issue", issueList[i]);
                                System.out.println("Created Issue : "+issue);
                            }
                        }

                    } else { //abort if the issue count is = 0

                        System.out.println("No issues forund ");
                    }

                }else{

                    System.out.println("No alert levels to create issues !!");
                }



        } catch (AuthenticationException e) { //authentication faliure

            log.error(e.getMessage(), e);

        }

    }

    public String[] loginUser() throws IOException, AuthenticationException {

        Properties prop = new Properties();
        InputStream input = new FileInputStream(Constant.getZapHome() + "/cred.properties");
        prop.load(input);
        String[] auth=new String[2];


        if (!(prop.getProperty("jiraUrl").equals("")) && !(prop.getProperty("jiraUsername").equals(""))
                && !(prop.getProperty("jiraPass").equals(""))) {
            auth[0] = prop.getProperty("jiraUrl");
            auth[1] = new String(Base64.encode(prop.getProperty("jiraUsername") + ":" + prop.getProperty("jiraPass")));
        }else{
            throw (new AuthenticationException("Login Error !!"));
        }
        input.close();
        return auth;
    }


}