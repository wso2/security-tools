/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.security.tools.automation.manager.handler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handler for HTTP requests
 * <p>HTTP requests are sent to the micro service APIs in containers</p>
 */
@SuppressWarnings("unused")
public class HttpRequestHandler {

    /**
     * Sends a HTTP GET request
     *
     * @param request Requested URI
     * @return HTTP response after executing the GET command
     * @throws IOException in case of a problem or the connection was aborted
     */
    public static HttpResponse sendGetRequest(URI request) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGetRequest = new HttpGet(request);
        return httpClient.execute(httpGetRequest);
    }

    /**
     * Sends a HTTP POST request
     *
     * @param request    Requested URI
     * @param parameters URL parameters
     * @return HTTP response after executing POST command
     * @throws IOException in case of a problem or the connection was aborted
     */
    public static HttpResponse sendPostRequest(URI request, ArrayList<NameValuePair> parameters) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        List<NameValuePair> urlParameters = new ArrayList<>();
        HttpPost httpPostRequest = new HttpPost(request);
        if (parameters != null) {
            for (NameValuePair parameter : parameters) {
                urlParameters.add(new BasicNameValuePair(parameter.getName(), parameter.getValue()));
            }
            httpPostRequest.setEntity(new UrlEncodedFormEntity(urlParameters));
        }
        return httpClient.execute(httpPostRequest);
    }

    /**
     * Sends a multipart POST request.
     * This method is to send files in POST request
     *
     * @param request  Requested URI
     * @param files    List of files to be sent over HTTP
     * @param textBody List of text parameters to be sent in body
     * @return HTTP response after  executing the request
     * @throws IOException in case of a problem or the connection was aborted
     */
    public static HttpResponse sendMultipartRequest(URI request, Map<String, File> files, Map<String, String>
            textBody) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();

        HttpPost uploadFile = new HttpPost(request);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        if (textBody != null) {
            for (Map.Entry<String, String> entry : textBody.entrySet()) {
                builder.addTextBody(entry.getKey(), entry.getValue(), ContentType.TEXT_PLAIN);
            }
        }
        // This attaches the file to the POST:
        if (files != null) {
            for (Map.Entry<String, File> entry : files.entrySet()) {
                InputStream inputStream = new FileInputStream(entry.getValue());
                builder.addBinaryBody(
                        entry.getKey(),
                        inputStream,
                        ContentType.APPLICATION_OCTET_STREAM,
                        entry.getValue().getName()
                );

            }
        }
        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        return httpClient.execute(uploadFile);
    }

    /**
     * Reads the HTTP response and return a {@code String}
     *
     * @param response HTTP response to be read
     * @return a string of HTTP response
     * @throws IOException in case of a problem or the connection was aborted
     */
    public static String printResponse(HttpResponse response) throws IOException {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
    }

    /**
     * Get the entity of a HTTP response and saved to a file.
     *
     * @param response        HTTP response
     * @param destinationFile Destination file path
     * @throws IOException in case of a problem or the connection was aborted
     */
    public static void saveResponseToFile(HttpResponse response, File destinationFile) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try (InputStream inputStream = entity.getContent();
                 FileOutputStream output = new FileOutputStream(destinationFile)) {
                int l;
                byte[] tmp = new byte[2048];
                while ((l = inputStream.read(tmp)) != -1) {
                    output.write(tmp, 0, l);
                }
            }
        }
    }
}
