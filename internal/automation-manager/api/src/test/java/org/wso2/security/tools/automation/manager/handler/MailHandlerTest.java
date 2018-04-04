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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.MessagingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link MailHandler}
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class MailHandlerTest {

    @MockBean
    private MailHandler mailHandler;

    @Test
    public void testSendMail() {
        String to = "x@xxx.com";
        String subject = "Test Mail";
        String body = "Test Body";
        String attachmentFileName = "Test File Name";
        InputStream inputStream;
        try {
            inputStream = new FileInputStream("/home/deshani/Documents/uploaded");
            mailHandler.sendMail(to, subject, body, inputStream, attachmentFileName);
        } catch (MessagingException | IOException e) {
            assertTrue(false);
        }
    }
}
