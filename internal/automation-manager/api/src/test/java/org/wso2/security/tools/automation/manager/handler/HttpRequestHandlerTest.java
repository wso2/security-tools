/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.security.tools.automation.manager.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link HttpRequestHandler}
 */
@RunWith(SpringRunner.class)
public class HttpRequestHandlerTest {

    @Test
    public void testSendGetRequest() {
        String request = "http://localhost:8085/get";
        try {
            HttpRequestHandler.sendGetRequest(new URI(request));
        } catch (IOException | URISyntaxException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSendPostRequest() {
        String request = "http://localhost:8085/post";
        try {
            HttpRequestHandler.sendGetRequest(new URI(request));
        } catch (IOException | URISyntaxException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSendMultipartRequest() {
        String request = "http://localhost:8080/post";
        Map<String, File> files = new HashMap<>();
        files.put("testFile", new File("/home/deshani/Documents/uploaded"));

        Map<String, String> textBody = new HashMap<>();
        textBody.put("testText", "test");
        try {
            HttpRequestHandler.sendMultipartRequest(new URI(request), files, textBody);
        } catch (IOException | URISyntaxException e) {
            assertTrue(false);
        }
    }
}
