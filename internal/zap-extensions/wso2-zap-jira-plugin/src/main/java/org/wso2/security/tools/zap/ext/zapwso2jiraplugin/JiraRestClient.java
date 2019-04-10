
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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import javax.naming.AuthenticationException;
import java.io.File;
import java.io.IOException;

import org.apache.http.HttpStatus;

public class JiraRestClient {

    private static final Logger log = Logger.getRootLogger();


    /**
    * This method is for getting any Jira related details
    * @param auth -Base64 encoded value of Jirausername and JiraPassword
    * @param url- Jira Base URL
     * @return
     */
    public static String invokeGetMethod(String auth, String url)
            throws AuthenticationException, ClientHandlerException {

        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                .accept("application/json").get(ClientResponse.class);
        int statusCode = response.getStatus();
        if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthenticationException("Invalid Username or Password");
        } else if (statusCode == HttpStatus.SC_FORBIDDEN) {
            throw new AuthenticationException("Forbidden");
        }
        return response.getEntity(String.class);
    }

    /**
     * This method is for creating Jira related details
     * @param auth -Base64 encoded value of Jirausername and JiraPassword
     * @param url- Jira Base URL
     * @return
     */
    public static String invokePostMethod(String auth, String url, String data)
            throws AuthenticationException, ClientHandlerException {
        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                .accept("application/json").post(ClientResponse.class, data);
        int statusCode = response.getStatus();
        if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthenticationException("Invalid Username or Password");
        }
        return response.getEntity(String.class);
    }


    /**
     * This is is for modifying a ticket
     * @param auth -Base64 encoded value of Jirausername and JiraPassword
     * @param url- Jira Base URL
     * @param url- data need to be added as a comment
     * @return
     */

    public static String invokePutMethod(String auth, String url, String data)
            throws AuthenticationException, ClientHandlerException {
        Client client = Client.create();
        WebResource webResource = client.resource(url);
        WebResource.Builder builder = webResource.header("Authorization", "Basic " + auth);
        builder = builder.type("application/json");
        builder = builder.accept("application/json");
        ClientResponse response = builder.post(ClientResponse.class, data);
        int statusCode = response.getStatus();
        if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthenticationException("Invalid Username or Password");
        }
        return response.getEntity(String.class);
    }



    /**
     * This Method is for deleting Jira/attachments in ticket
     * @param auth -Base64 encoded value of Jirausername and JiraPassword
     * @param url- Jira Base URL
     * @return
     */

    public static void invokeDeleteMethod(String auth, String url)
            throws AuthenticationException, ClientHandlerException {
        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                .accept("application/json").delete(ClientResponse.class);
        int statusCode = response.getStatus();
        if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthenticationException("Invalid Username or Password");
        }
    }

    /*
    * This method is used for sending attachments to the JIRA
    * @param auth -Base64 encoded value of Jirausername and JiraPassword
    * @param url- Jira Base URL
    * @param path -This is the file path where ZAP generates its report. This includes file name too.
    * @return
     */
    public static void invokePutMethodWithFile(String auth, String url, String path) {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader(IssueCreatorConstants.ATLASSIAN_TOKEN, "nocheck");
        httppost.setHeader(IssueCreatorConstants.ATLASSIAN_AUTHORIZATION, "Basic " + auth);

        File fileToUpload = new File(path);
        FileBody fileBody = new FileBody(fileToUpload);

        HttpEntity entity = MultipartEntityBuilder.create().addPart("file", fileBody).build();

        httppost.setEntity(entity);
        CloseableHttpResponse response = null;

        try {
            response = httpclient.execute(httppost);
        } catch (Exception e) {
            log.error("File upload failed when involing the update method with file ");
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                log.error("Exception occured when closing the connection");
            }
        }
    }
}
