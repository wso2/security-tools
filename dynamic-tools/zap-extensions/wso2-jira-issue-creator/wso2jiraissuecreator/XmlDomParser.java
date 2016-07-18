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

import com.sun.jersey.core.util.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.parosproxy.paros.Constant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.naming.AuthenticationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XmlDomParser {

    String createIssueData, summary, type;
    String description = "";
    public static String updateIssueID;
    public static JSONObject currentOpenIssue;
    public static JSONObject allOpenIssues;
    JiraRestClient jiraRest;
    String[] creds;
    String auth;
    String BASE_URL;

    public XmlDomParser(){
        jiraRest = new JiraRestClient();

        try {
            creds = this.loginUser();
            auth = creds[1];
            BASE_URL = creds[0];
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

    }

    private Logger log = Logger.getLogger(this.getClass());

    public String[] parseXmlDoc(String projectKey, String assignee, Boolean alertHigh, Boolean alertMedium,
            Boolean alertLow, String issuelabel,Boolean filterIssuesByResourceType) {  //parse the xml document or file

        this.allOpenIssues=this.getAllOpenIssues(projectKey);
        log.info("all open issues are updated");
        String[] returnIssueList;
        try {

            StringBuilder currentSession = new StringBuilder();
            String report = LastScanReport.getInstance().generate(currentSession);
            InputStream stream = new ByteArrayInputStream(report.getBytes(StandardCharsets.UTF_8));
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(stream);
            String[] dropIssues = this
                    .dropIssueList(alertHigh, alertMedium, alertLow);//contains issue types to be dropped

            if (dropIssues.length != 0) { //if there are issues to be dropped

                String[] allIssues = createIssueList(doc, projectKey, assignee,issuelabel,
                        filterIssuesByResourceType); //creates the issue list
                int allIssueCount = allIssues.length;
                int exportIssueCount = dropIssues.length;
                List<String> list = new ArrayList<>(Arrays.asList(allIssues));
                JSONObject jsonIssue;
                String currentPriority;

                for (int i = 0; i < allIssueCount; i++) { //for all issues
                    for (int j = 0; j < exportIssueCount; j++) { //for the alert types to be dropped
                        jsonIssue = new JSONObject(allIssues[i]);
                        currentPriority = jsonIssue.getJSONObject("fields").getJSONObject("priority")
                                .getString("name"); // get the current priority
                        if (currentPriority.equals(dropIssues[j])) {
                            list.remove(allIssues[i]);
                        }
                    }
                }

                returnIssueList = list.toArray(new String[list.size()]); //return the remaining issues

            } else {
                returnIssueList = createIssueList(doc, projectKey, assignee,issuelabel,
                        filterIssuesByResourceType); //if no issues are dropped
            }

        } catch (ParserConfigurationException e) {
            returnIssueList = new String[0];
            log.error(e.getMessage(), e);
        } catch (SAXException e) {
            returnIssueList = new String[0];
            log.error(e.getMessage(), e);
        } catch (SessionNotFoundException e) {
            returnIssueList = new String[0];
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            returnIssueList = new String[0];
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            returnIssueList = new String[0];
            log.error(e.getMessage(), e);
        }
        return returnIssueList;
    }

    String[][] issueURLS; //publicly declared array to check url's against  existing url's in a issue (during update)

    private String[] createIssueList(Document doc, String projectKey, String assignee,String issueLabel,
            Boolean filterIssuesByResourceType) throws SessionNotFoundException {

        doc.getDocumentElement().normalize();
        NodeList session = doc.getElementsByTagName("alerts"); //to check wheter alerts exist
        String[] issueList;
        String tempIssueURLS;
        ArrayList<String> urlList = new ArrayList<>();

        if (session.getLength() != 0) { // if alerts exist

            NodeList alertList = doc.getElementsByTagName("alertitem"); //alert items
            NodeList instances;
            issueList = new String[alertList.getLength()]; //initialize the array according to the number of alerts
            issueURLS = new String[alertList.getLength()][];

            for (int temp = 0; temp < alertList.getLength(); temp++) { //loop through alerts
                Node nNode = alertList.item(temp);
                Element alert = (Element) nNode;

                instances = alert.getElementsByTagName("instance");
                issueURLS[temp] = new String[instances.getLength()];
                summary = StringEscapeUtils.escapeHtml(alert.getElementsByTagName("alert").item(0).getTextContent());
                description += StringEscapeUtils
                        .escapeJava(alert.getElementsByTagName("desc").item(0).getTextContent() + "\n\n\n");
                description += StringEscapeUtils.escapeJava(
                        "| No of Instances | " + alert.getElementsByTagName("count").item(0).getTextContent()
                                + " | \n");
                description += StringEscapeUtils.escapeJava(
                        "| Solution        | " + alert.getElementsByTagName("solution").item(0).getTextContent()
                                + " | \n");
                description += StringEscapeUtils.escapeJava(
                        "| Reference       | " + alert.getElementsByTagName("reference").item(0).getTextContent()
                                + " | \n");
                description=description.replace("<p>+","\n\n");
                log.info("Project description :"+description);

                for (int i = 0; i < instances.getLength(); i++) { //loop through instances

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;
                        tempIssueURLS = eElement.getElementsByTagName("uri").item(i).getTextContent();
                        issueURLS[temp][i] = tempIssueURLS;

                        if (!filterIssuesByResourceType) {
                            description += StringEscapeUtils.escapeHtml("| URL | " + tempIssueURLS + " | \\n");
                        } else {
                            urlList.add(tempIssueURLS);
                        }

                    }

                    type = "Bug"; //issue type set to BUG
                }

                if (filterIssuesByResourceType) {
                    description += sortIssueURLS(urlList);
                }

                createIssueData = "{\"fields\": {\"project\": {\"key\":\"" + projectKey + "\"}," +
                        "\"summary\":" + "\"" + summary + "\"" + ",  \"assignee\": {\"name\": \"" + assignee + "\"},"
                        + "\"customfield_10464\": [{\"value\": \"" + issueLabel + "\"}]," +
                        "\"description\":" + "\"" + description + "\"" + "," +
                        "\"issuetype\":{\"name\":\"" + type + "\"}}}";

                issueList[temp] = createIssueData;
                description = "";
            }
        } else {
            issueList = new String[0]; //initialize issueList to 0 if no session is found
            throw (new SessionNotFoundException("Session not Found"));
        }
        return issueList;
    }

    private String sortIssueURLS(List<String> urlsOfCurrentIssue) { //filter issues by files
        String urlsSortedByResourceName = "\\n", extension = "", currentURL;
        LinkedHashMap<String, List<String>> map = new LinkedHashMap<String, List<String>>();

        List<String> jspFiles = new ArrayList<String>();
        List<String> htmlFiles = new ArrayList<String>();
        List<String> cssFiles = new ArrayList<String>();
        List<String> jsFiles = new ArrayList<String>();
        List<String> xmlFiles = new ArrayList<String>();
        List<String> jsonFiles = new ArrayList<String>();
        List<String> otherFiles = new ArrayList<String>();

        for (int i = 0; i < urlsOfCurrentIssue.size(); i++) {
            currentURL = urlsOfCurrentIssue.get(i);
            extension = FilenameUtils.getExtension(currentURL);
            //            System.out.println(extension);
            switch (extension) {
            case "jsp":
                jspFiles.add(currentURL);
                break;

            case "html":
                htmlFiles.add(currentURL);
                break;

            case "css":
                cssFiles.add(currentURL);
                break;

            case "js":
                jsFiles.add(currentURL);
                break;

            case "xml":
                xmlFiles.add(currentURL);
                break;

            case "json":
                jsonFiles.add(currentURL);
                break;

            default:
                otherFiles.add(currentURL);
                break;

            }
        }

        map.put("jsp", jspFiles);
        map.put("html", htmlFiles);
        map.put("js", jsFiles);
        map.put("xml", xmlFiles);
        map.put("css", cssFiles);
        map.put("json", jsonFiles);
        map.put("other", otherFiles);

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            List<String> URLS = entry.getValue();
            for (int i = 0; i < URLS.size(); i++) {
                urlsSortedByResourceName += StringEscapeUtils.escapeHtml("| URL | " + URLS.get(i) + " | \\n");
            }
            urlsSortedByResourceName += "\\n";
        }
        return urlsSortedByResourceName;
    }

    private String[] dropIssueList(Boolean high, Boolean medium, Boolean low) { //get the filters into an array

        if (high && medium && low) {
            String[] priorities = new String[0];
            return priorities;

        } else if (high && medium) {
            String[] priorities = { "Low" };
            return priorities;

        } else if (high) {
            String[] priorities = { "Medium", "Low" };
            return priorities;

        } else if (high && low) {
            String[] priorities = { "Medium" };
            return priorities;

        } else if (low && medium) {
            String[] priorities = { "High" };
            return priorities;

        } else if (medium) {
            String[] priorities = { "High", "Low" };
            return priorities;

        } else if (low) {
            String[] priorities = { "High", "Medium" };
            return priorities;

        } else {
            String[] priorities = { "High", "Medium", "Low" };
            return priorities;
        }

    }

    public String[] loginUser() throws IOException, AuthenticationException {

        Properties prop = new Properties();
        InputStream input = new FileInputStream(Constant.getZapHome() + "/cred.properties");
        prop.load(input);
        String[] auth = new String[2];

        if (!(prop.getProperty("jiraUrl").equals("")) && !(prop.getProperty("jiraUsername").equals("")) && !(prop
                .getProperty("jiraPass").equals(""))) {
            auth[0] = prop.getProperty("jiraUrl");
            auth[1] = new String(Base64.encode(prop.getProperty("jiraUsername") + ":" + prop.getProperty("jiraPass")));
        } else {
            throw (new AuthenticationException("Login Error !!"));
        }
        input.close();
        return auth;
    }

    private JSONObject getAllOpenIssues(String projectkey) {
        String responseIssues;
        JSONObject allOpenIssues = null, tempJSON;
        try {
            responseIssues = jiraRest
                    .invokeGetMethod(auth, BASE_URL + "/rest/api/2/search?jql=project=" + projectkey + "%20AND%20" +
                            "(status=%22Open%22OR%20status=%22In%20Progress%22%20)" +
                            "+order+by+id&fields=key,summary,description,status&maxResults=10");

            allOpenIssues = new JSONObject(responseIssues);

            log.info("done invoking the 'invokemethod' in the XmlDomParser");

            int totalNumberOfIssues = Integer.parseInt(allOpenIssues.get("total").toString());

            log.info("total number of all open issues " + allOpenIssues);

            if (totalNumberOfIssues > 1000) {
                for (int start = 1000; start < totalNumberOfIssues; start += totalNumberOfIssues % 1000) {
                    responseIssues = jiraRest.invokeGetMethod(auth,
                            BASE_URL + "/rest/api/2/search?jql=project=" + projectkey + "%20AND%20" +
                                    "(status=%22Open%22OR%20status=%22In%20Progress%22%20)" +
                                    "+order+by+id&fields=key,summary,description,status&startAt=" + start
                                    + "&maxResults=1000");
                    tempJSON = new JSONObject(responseIssues);
                    allOpenIssues.append("issues", tempJSON.getJSONArray("issues"));
                }

            }
        }
        catch (AuthenticationException e){

        }



        return allOpenIssues;

    }

    public void printAllOpenIssues(String projectKey) {
        JSONObject issues = this.getAllOpenIssues(projectKey);
        JSONArray issueArray = issues.getJSONArray("issues");
        JSONObject tempIssue;
        for (int i = 0; i < issueArray.length(); i++) {
            tempIssue = issueArray.getJSONObject(i);
            System.out.println(tempIssue.getJSONObject("fields").getString("description"));
            System.out.println("--------------------------hash--------------------------");
            System.out.println(tempIssue.getJSONObject("fields").getString("description").hashCode());

        }

    }

    public void updateExistingIssue(String issue, String auth, String BASE_URL, int currentIssueIndex)
            throws AuthenticationException {

        log.info("working in update existing issue");
        JSONObject currntIssue = new JSONObject(issue);
        JiraRestClient jira = new JiraRestClient();

        String currentOpenIssueDescription = currentOpenIssue.getJSONObject("fields").getString("description");
        String currentDescription = currntIssue.getJSONObject("fields").getString("description");

        if (currentOpenIssueDescription.hashCode() != currentDescription
                .hashCode()) { //if the descriptions are not the same

            String updatedDescription = StringEscapeUtils
                    .escapeJava(this.updateIssueURLS(currentOpenIssueDescription, currentIssueIndex));
            currentOpenIssue.getJSONObject("fields").put("description", updatedDescription);

            String editIssueData = "{\"fields\": {\"description\":\"" + updatedDescription + "\"}}";

            jira.invokePutMethod(auth, BASE_URL + "/rest/api/2/issue/" + updateIssueID, editIssueData);
        }

    }

    private String updateIssueURLS(String description, int issueIndex) {
        String updatedDescription = description;

        for (int i = 0; i < issueURLS[issueIndex].length; i++) {
            if (!(description.toLowerCase()
                    .contains(issueURLS[issueIndex][i].toLowerCase()))) { //if the current url is not in the URL table
                updatedDescription += "| URL | " + issueURLS[issueIndex][i] + " | \n";
            }
        }

        return updatedDescription;
    }

    public boolean checkForIssueExistence(String issue, String projectKey) { //checks for an open issue

        Boolean existance = false;
        JSONObject currentIssue = new JSONObject(issue);
        //        System.out.println(allOpenIssues);

        log.info("Project key passed to the checkfor existing issue is  "+ projectKey);
        log.info("number og open issues are "+allOpenIssues.length());

        if (allOpenIssues.getJSONArray("issues").length() != 0) {
            JSONArray issueArray = allOpenIssues.getJSONArray("issues");

            for (int i = 0; i < issueArray.length(); i++) {
                if (currentIssue.getJSONObject("fields").getString("summary")
                        .equals(issueArray.getJSONObject(i).getJSONObject("fields").getString("summary"))) {
                    existance = true;
                    updateIssueID = issueArray.getJSONObject(i).getString("id");
                    currentOpenIssue = issueArray.getJSONObject(i);
                    break;
                }
            }
        }

        return existance;
    }
}