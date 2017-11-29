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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Handler for email messages
 */
@Component
public class MailHandler {
    private final JavaMailSender mailSender;
    private Logger LOGGER = LoggerFactory.getLogger(MailHandler.class);

    @Autowired
    public MailHandler(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send an email with an attachment
     *
     * @param to                 To whom the email is sent
     * @param subject            Email subject
     * @param body               Email body
     * @param inputStream        Input stream of an attachment
     * @param attachmentFileName Attachment file name
     * @throws MessagingException Exceptions thrown by the Messaging classes
     * @throws IOException        Signals that an I/O exception of some sort has occurred
     */
    public void sendMail(String to, String subject, String body, InputStream inputStream, String
            attachmentFileName) throws MessagingException, IOException {
        LOGGER.info("Sending email");
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setText(body);
        mimeMessageHelper.addAttachment(attachmentFileName, new ByteArrayResource(IOUtils.toByteArray(inputStream)));
        mailSender.send(mimeMessageHelper.getMimeMessage());
    }
}
