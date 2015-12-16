package org.zaproxy.zap.extension.jiraIssueCreater;


/**
 * Created by kasun on 10/28/15.
 */


import com.sun.jersey.core.util.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.extension.report.ReportGenerator;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zaproxy.zap.extension.XmlReporterExtension;
import org.zaproxy.zap.utils.XMLStringUtil;
import org.zaproxy.zap.view.ScanPanel;

import javax.naming.AuthenticationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class XmlDomParser{


    String createIssueData,summary,type,priority;
    String description="";
    private Logger log = Logger.getLogger(this.getClass());



    public String[] parseXmlDoc(String projectKey, String assignee, Boolean alertHigh, Boolean alertMedium, Boolean alertLow) {  //parse the xml document or file

        String[] returnIssueList;
        try {

            StringBuilder currentSession=new StringBuilder();
            String report=this.generate(currentSession);
            InputStream stream = new ByteArrayInputStream(report.getBytes(StandardCharsets.UTF_8));
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(stream);
            String[] dropIssues=this.dropIssueList(alertHigh, alertMedium, alertLow);//contains issue types to be dropped



            if(dropIssues.length!=0) { //if there are issues to be dropped

                String[] allIssues=createIssueList(doc, projectKey, assignee); //creates the issue list
                int allIssueCount=allIssues.length;
                int exportIssueCount = dropIssues.length;
                List<String> list = new ArrayList<>(Arrays.asList(allIssues));
                JSONObject jsonIssue;
                String currentPriority;


                for(int i = 0; i <allIssueCount;i++ ){ //for all issues
                    for(int j=0;j<exportIssueCount;j++){ //for the alert types to be dropped

                        jsonIssue=new JSONObject(allIssues[i]);
                        currentPriority=jsonIssue.getJSONObject("fields").getJSONObject("priority").getString("name"); // get the current priority
                        if(currentPriority.equals(dropIssues[j])){
                            list.remove(allIssues[i]);
                        }
                    }
                }

                returnIssueList=list.toArray(new String[list.size()]); //return the remaining issues

            }else{
                returnIssueList=createIssueList(doc, projectKey, assignee); //if no issues are dropped
            }

        } catch (ParserConfigurationException e) {
            returnIssueList=new String[0];
            log.error(e.getMessage(), e);
        } catch (SAXException e) {
            returnIssueList=new String[0];
            log.error(e.getMessage(), e);
        } catch (SessionNotFoundException e) {
            returnIssueList=new String[0];
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            returnIssueList=new String[0];
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            returnIssueList=new String[0];
            log.error(e.getMessage(), e);
        }
        return returnIssueList;
    }

    String[][] issueURLS;

    private String[] createIssueList(Document doc,String projectKey, String assignee) throws SessionNotFoundException{
        doc.getDocumentElement().normalize();
        NodeList session=doc.getElementsByTagName("alerts"); //to check wheter alerts exist
        String[] issueList;
        String tempIssueURLS;


        if(session.getLength()!=0) { // if alerts exist

            NodeList alertList = doc.getElementsByTagName("alertitem"); //alert items
            NodeList instances;
            issueList=new String[alertList.getLength()]; //initialize the array according to the number of alerts
            issueURLS=new String[alertList.getLength()][];


            for (int temp = 0; temp < alertList.getLength(); temp++) { //loop through alerts
                Node nNode = alertList.item(temp);
                Element alert = (Element) nNode;

                priority = StringEscapeUtils.escapeHtml(alert.getElementsByTagName("riskdesc").item(0).getTextContent().
                        substring(0, alert.getElementsByTagName("riskdesc").item(0).getTextContent().indexOf(" ")));

                instances = alert.getElementsByTagName("instance");
                issueURLS[temp]=new String[instances.getLength()];
                summary = StringEscapeUtils.escapeHtml(alert.getElementsByTagName("alert").item(0).getTextContent());
                description += StringEscapeUtils.escapeJava(alert.getElementsByTagName("desc").item(0).getTextContent() + "\n\n\n");
                description += StringEscapeUtils.escapeJava("| No of Instances | " + alert.getElementsByTagName("count").item(0).getTextContent() + " | \n");
                description += StringEscapeUtils.escapeJava("| Solution        | " + alert.getElementsByTagName("solution").item(0).getTextContent() + " | \n");
                description += StringEscapeUtils.escapeJava("| Reference       | " + alert.getElementsByTagName("reference").item(0).getTextContent() + " | \n");

                for (int i = 0; i < instances.getLength(); i++) { //loop through instances

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        tempIssueURLS=eElement.getElementsByTagName("uri").item(i).getTextContent();
                        description +=StringEscapeUtils.escapeHtml("| URL | " +tempIssueURLS+ " | \\n");
                        issueURLS[temp][i]=tempIssueURLS;
                    }

                    type = "Bug"; //issue type set to BUG

                }

                createIssueData = "{\"fields\": {\"project\": {\"key\":\"" + projectKey + "\"}," +
                        "\"summary\":" + "\"" + summary + "\"" + ",  \"assignee\": {\"name\": \"" + assignee + "\"}," +
                        "\"description\":" + "\"" + description + "\"" + "," +
                        "\"issuetype\":{\"name\":\"" + type + "\"},\"priority\":{\"name\":\"" + priority + "\"}}}";

                issueList[temp] = createIssueData;
                description = "";
            }
        }else{
            issueList=new String[0]; //initialize issueList to 0 if no session is found
            throw(new SessionNotFoundException("Session not Found"));
        }
        return issueList;
    }

    private String[] dropIssueList(Boolean high,Boolean medium, Boolean low){ //get the filters into an array

        if(high && medium && low){
           String[] priorities=new String[0];
            return priorities;

        }else if(high && medium){
            String[] priorities={"Low"};
            return priorities;

        }else if(high){
            String[] priorities={"Medium","Low"};
            return priorities;

        }else if(high && low){
            String[] priorities={"Medium"};
            return priorities;

        }else if(low && medium){
            String[] priorities={"High"};
            return priorities;

        }else if(medium){
            String[] priorities={"High","Low"};
            return priorities;

        }else if(low){
            String[] priorities={"High","Medium"};
            return priorities;

        }else{
            String[] priorities={"High","Medium","Low"};
            return priorities;
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

    private JSONObject getAllOpenIssues(String projectkey){
        JiraRestClient jiraRest=new JiraRestClient();
        String responseIssues;
        JSONObject allOpenIssues=null,tempJSON;
        try {
            String[] creds=this.loginUser();
            String auth=creds[1];
            String BASE_URL = creds[0];
            responseIssues=jiraRest.invokeGetMethod(auth, BASE_URL + "/rest/api/2/search?jql=project="+projectkey+"%20AND%20" +
                    "(status=%22Open%22OR%20status=%22In%20Progress%22%20OR%20status=%22To%20Do%22)" +
                    "+order+by+id&fields=key,summary,description,status&maxResults=1000");
            allOpenIssues=new JSONObject(responseIssues);
            int totalNumberOfIssues=Integer.parseInt(allOpenIssues.getString("total"));

            if(totalNumberOfIssues>1000){
                for (int start=1000;start<totalNumberOfIssues;start+=totalNumberOfIssues%1000){
                    responseIssues=jiraRest.invokeGetMethod(auth, BASE_URL + "/rest/api/2/search?jql=project="+projectkey+"%20AND%20" +
                            "(status=%22Open%22OR%20status=%22In%20Progress%22%20OR%20status=%22To%20Do%22)" +
                            "+order+by+id&fields=key,summary,description,status&startAt="+start+"&maxResults=1000");
                    tempJSON=new JSONObject(responseIssues);
                    allOpenIssues.append("issues",tempJSON.getJSONArray("issues"));
                }

            }


        } catch (IOException e) {
            log.error(e.getMessage(),e);
        } catch (AuthenticationException e) {
            log.error(e.getMessage(), e);
        }

        return allOpenIssues;

    }

    public void printAllOpenIssues(String projectKey){
        JSONObject issues=this.getAllOpenIssues(projectKey);
        JSONArray issueArray=issues.getJSONArray("issues");
        JSONObject tempIssue;
        for (int i=0;i<issueArray.length();i++){
            tempIssue=issueArray.getJSONObject(i);
            System.out.println(tempIssue.getJSONObject("fields").getString("description"));
            System.out.println("--------------------------hash--------------------------");
            System.out.println(tempIssue.getJSONObject("fields").getString("description").hashCode());

        }

    }


    public static String updateIssueID;
    public static JSONObject currentOpenIssue;

    public void updateExistingIssue(String issue,String auth, String BASE_URL,int currentIssueIndex) throws AuthenticationException {
        JSONObject currntIssue=new JSONObject(issue);
        JiraRestClient jira=new JiraRestClient();

        String currentOpenIssueDescription=currentOpenIssue.getJSONObject("fields").getString("description");
        String  currentDescription=currntIssue.getJSONObject("fields").getString("description");

        if(currentOpenIssueDescription.hashCode()!=currentDescription.hashCode()){ //if the descriptions are not the same

            String updatedDescription=StringEscapeUtils.escapeJava(this.updateIssueURLS(currentOpenIssueDescription, currentIssueIndex));
            currentOpenIssue.getJSONObject("fields").put("description", updatedDescription);

            String editIssueData = "{\"fields\": {\"description\":\"" + updatedDescription + "\"}}";

            jira.invokePutMethod(auth, BASE_URL + "/rest/api/2/issue/" + updateIssueID, editIssueData);
        }

    }

    private String updateIssueURLS(String description,int issueIndex){
        String updatedDescription=description;

        for(int i=0;i<issueURLS[issueIndex].length;i++) {
            if (!(description.toLowerCase().contains(issueURLS[issueIndex][i].toLowerCase()))) {
                updatedDescription += "| URL | " + issueURLS[issueIndex][i] + " | \n";
            }
        }

        return updatedDescription;
    }


    public boolean checkForIssueExistence(String issue, String projectKey){

        Boolean existance=false;
        JSONObject currentIssue=new JSONObject(issue);
        JSONObject allOpenIssues=this.getAllOpenIssues(projectKey);
//        System.out.println(allOpenIssues);

        if(allOpenIssues.getJSONArray("issues").length()!=0) {
            JSONArray issueArray = allOpenIssues.getJSONArray("issues");

            for (int i = 0; i < issueArray.length(); i++) {
                if (currentIssue.getJSONObject("fields").getString("summary").equals
                        (issueArray.getJSONObject(i).getJSONObject("fields").getString("summary"))) {
                    existance = true;
                    updateIssueID = issueArray.getJSONObject(i).getString("id");
                    currentOpenIssue = issueArray.getJSONObject(i);
                    break;
                }
            }
        }

        return existance;
    }



    // Methods copied and modified from package org.parosproxy.paros.extension.report; class ReportLastScan

    public String generate(StringBuilder report) throws Exception {
        report.append("<?xml version=\"1.0\"?>");
        report.append("<OWASPZAPReport version=\"").append(Constant.PROGRAM_VERSION).append("\" generated=\"").append(ReportGenerator.getCurrentDateTimeString()).append("\">\r\n");
        siteXML(report);
        report.append("</OWASPZAPReport>");
        return report.toString();
    }

    private void siteXML(StringBuilder report) {
        SiteMap siteMap = Model.getSingleton().getSession().getSiteTree();
        SiteNode root = (SiteNode) siteMap.getRoot();
        int siteNumber = root.getChildCount();
        for (int i = 0; i < siteNumber; i++) {
            SiteNode site = (SiteNode) root.getChildAt(i);
            String siteName = ScanPanel.cleanSiteName(site, true);
            String[] hostAndPort = siteName.split(":");
            boolean isSSL = (site.getNodeName().startsWith("https"));
            String siteStart = "<site name=\"" + XMLStringUtil.escapeControlChrs(site.getNodeName()) + "\"" +
                    " host=\"" + XMLStringUtil.escapeControlChrs(hostAndPort[0])+ "\""+
                    " port=\"" + XMLStringUtil.escapeControlChrs(hostAndPort[1])+ "\""+
                    " ssl=\"" + String.valueOf(isSSL) + "\"" +
                    ">";
            StringBuilder extensionsXML = getExtensionsXML(site);
            String siteEnd = "</site>";
            report.append(siteStart);
            report.append(extensionsXML);
            report.append(siteEnd);
        }
    }

    public StringBuilder getExtensionsXML(SiteNode site) {
        StringBuilder extensionXml = new StringBuilder();
        ExtensionLoader loader = Control.getSingleton().getExtensionLoader();
        int extensionCount = loader.getExtensionCount();
        for(int i=0; i<extensionCount; i++) {
            Extension extension = loader.getExtension(i);
            if(extension instanceof XmlReporterExtension) {
                extensionXml.append(((XmlReporterExtension)extension).getXml(site));
            }
        }
        return extensionXml;
    }

}