
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

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.naming.AuthenticationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.util.SecurityManager;

public class JiraTaskHandler {

    String createIssueData, type;
    String description = "";
    JiraRestClient jiraRest;

    private static final Logger log = Logger.getRootLogger();

    /**
     * Creating new Jira issue Json object String is generated from here
     *
     * @param projectKey JIRA project key, under which ticket needs to be created
     * @param assignee   to whom ticket need to be assigned
     * @param issueLabel custom object which used to identify the project
     * @param summary    JIRA heading
     * @param product    product name
     * @return Json String that needs to be sent to the JIRA
     */
    public String createNewTicket(String projectKey, String assignee, String issueLabel, String summary,
            String product) {

        description = product;
        type = "Bug";

        createIssueData = "{\"fields\": {\"project\": {\"key\":\"" + projectKey + "\"}," +
                "\"summary\":" + "\"" + summary + "\"" + ",  \"assignee\": {\"name\": \"" + assignee + "\"},"
                + "\"customfield_10464\": [{\"value\": \"" + issueLabel + "\"}]," +
                "\"description\":" + "\"" + description + "\"" + "," +
                "\"issuetype\":{\"name\":\"" + type + "\"}}}";

        return createIssueData;
    }

    /**
     * Generating the comment that has to be added with the attachment
     *
     * @return comment that needs to be added when uploading a file
     */
    public String createComment() {

        String comment;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        comment = "{\"body\": \"This Report is generated on " + dateFormat.format(date).toString() + ".\"}";

        return comment;
    }

}