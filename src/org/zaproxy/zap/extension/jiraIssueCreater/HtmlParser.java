package org.zaproxy.zap.extension.jiraIssueCreater;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

/**
 * Created by kausn on 10/22/15.
 */
public class HtmlParser {

    public Document ReadHtmldoc(String filePath) { //read the htl doc

        File input = new File(filePath);
        Document doc = null;
        try {
            doc = Jsoup.parse(input, "UTF-8", "");
        } catch (IOException e) {
            //e.printStackTrace();
        }

        return doc;

    }

    public String[] CreateIssueList(Document doc, String projectKey, String assignee) { //create issue list

        String[] issueList = new String[1000];
        String summary = null; // error name from the first column heading
        String description = ""; //error description
        String type = "Bug"; // type set to bug
        String priority = null; // available : Highest High Medium Low Lowest | used : High Medium Low
        String tableData;
        String createIssueData;
        Elements tables = doc.select("table");

        for (int j = 1; j < tables.size(); j++) {

            Element table = doc.select("table").get(j); //select the first table (skipping table 0 by setting j=1)
            Elements rows = table.select("tr"); //select all rows

            for (int i = 0; i < rows.size(); i++) {
                if (i == 0) { //track the alert level and summary from the first row
                    Element row = rows.get(i);
                    Elements cols = row.select("td");
                    String temp = cols.get(0).text();
                    priority = temp.substring(0, temp.indexOf(" "));
                    summary = cols.get(1).text();
//                    System.out.println(summary);

                } else if (i > 1) {
                    Element row = rows.get(i);
//                    System.out.println("row " +i+ row.html());
                    Elements cols = row.select("td");

                    if (cols.size() > 1 && !(cols.get(0).text().equals("Instances"))) {
//                        System.out.println("First column "+cols.get(0).text());
                        tableData = "|" + StringEscapeUtils.escapeHtml(cols.get(0).text()) + "|" + StringEscapeUtils.escapeHtml(cols.get(1).text()) + "|" + "\\n";
//                        tableData ="|";
                        description += tableData;
//                    System.out.println(description);
                    } else if (cols.get(0).text().equals("Instances")) {
                        break;
                    } else {
                        continue;
                    }
                }
            }
            //System.out.println(description);
            createIssueData = "{\"fields\": {\"project\": {\"key\":\"" + projectKey + "\"}," +
                    "\"summary\":" + "\"" + summary + "\"" + ",  \"assignee\": {\"name\": \""+assignee+"\"}," +
                    "\"description\":" + "\"" + description + "\"" + "," +
                    "\"issuetype\":{\"name\":\"" + type + "\"},\"priority\":{\"name\":\"" + priority + "\"}}}";

//
            //create and add the issues to the array from here
            description = "";
            issueList[j - 1] = createIssueData;
            issueList[999] = Integer.toString(j);


        }

//        for (int i=0; i<Integer.parseInt(issueList[999]);i++){
//            System.out.println("Issue " + i+ issueList[i]);
//        }


        return issueList;
    }
}
