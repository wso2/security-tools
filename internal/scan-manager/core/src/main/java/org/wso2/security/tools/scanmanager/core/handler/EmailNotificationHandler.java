/*
 *
 *   Copyright (c) 2020, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.security.tools.scanmanager.core.handler;

import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.core.config.ScanManagerConfiguration;
import org.wso2.security.tools.scanmanager.core.exception.ResourceNotFoundException;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.core.model.Email;
import org.wso2.security.tools.scanmanager.core.util.Util;

import java.util.Arrays;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * This class provides the implementation for the email notification operations.
 */
public class EmailNotificationHandler implements NotificationHandler {

    private static final String EMAIL_TEMPLATE = "emailTemplate.html";

    // Place holders used for email template.
    private static final String EMAIL_TEMPLATE_SCAN_TITLE_PLACEHOLDER = "{scanTitle}";
    private static final String EMAIL_TEMPLATE_JOBID_PLACEHOLDER = "{jobId}";
    private static final String EMAIL_TEMPLATE_PRODUCT_NAME_PLACEHOLDER = "{productName}";
    private static final String EMAIL_TEMPLATE_USER_NAME_PLACEHOLDER = "{launchedBy}";
    private static final String EMAIL_TEMPLATE_SCAN_TYPE_PLACEHOLDER = "{ScannerId}";
    private static final String EMAIL_TEMPLATE_SCAN_STATUS_PLACEHOLDER = "{scanStatus}";

    @Override
    public void sendNotification(Scan scan, String toAddress) throws ScanManagerException {

        // Set smtp configurations.
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", ScanManagerConfiguration.getInstance().getSmtpServerHost());
        props.setProperty("mail.smtp.port", String.valueOf(ScanManagerConfiguration.getInstance().getSmtpServerPort()));
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.ssl.trust", ScanManagerConfiguration.getInstance().getSmtpServerHost());
        props.setProperty("mail.debug", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(ScanManagerConfiguration.getInstance().getSmtpUserName(),
                        String.valueOf(ScanManagerConfiguration.getInstance().getSmtpPassword()));
            }
        });

        // Clear credential.
        Arrays.fill(ScanManagerConfiguration.getInstance().getSmtpPassword(), '0');
        try {
            Email email = assembleEmail(scan, toAddress);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(ScanManagerConfiguration.getInstance().getEmailFromaddress()));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));

            if (email.getCcList() != null && !email.getCcList().isEmpty()) {
                for (String ccAddress : email.getCcList()) {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress((String) ccAddress));
                }
            }
            message.setSubject(email.getSubject());
            message.setContent(email.getBody(), "text/html");
            Transport.send(message);
        } catch (MessagingException | ResourceNotFoundException e) {
            throw new ScanManagerException("Mail sending failed", e);
        }
    }

    /**
     * This method use to form complete email.
     *
     * @param scan object which represents scan
     * @param toAddress email address of scan launcher
     * @return email object
     * @throws ResourceNotFoundException error occurred if email template in not found
     */
    private Email assembleEmail(Scan scan, String toAddress) throws ResourceNotFoundException {
        Email email = new Email();
        email.setFromAddress(ScanManagerConfiguration.getInstance().getEmailFromaddress());
        email.setCcList(ScanManagerConfiguration.getInstance().getEmailCCaddress());
        email.setSubject(ScanManagerConfiguration.getInstance().getNotificationSubject().concat(" " + scan.getJobId()));
        email.setBody(buildEmailBody(scan, scan.getStatus().name(), toAddress));
        return email;
    }

    /**
     * This method is used to build email body. This method reads the email template file and replace the relevant
     * information from scan object.
     *
     * @param scan object which represents scan
     * @param status status of scan
     * @param toAddress email address of scan launcher
     * @return email body
     * @throws ResourceNotFoundException error occurred if email template file is not found
     */
    private String buildEmailBody(Scan scan, String status, String toAddress) throws ResourceNotFoundException {
        String htmlTemplate = Util.readHTMLEmailTemplate(EMAIL_TEMPLATE);
        String htmlwithContent = htmlTemplate.replace(EMAIL_TEMPLATE_SCAN_TITLE_PLACEHOLDER, scan.getName())
                .replace(EMAIL_TEMPLATE_JOBID_PLACEHOLDER, scan.getJobId())
                .replace(EMAIL_TEMPLATE_PRODUCT_NAME_PLACEHOLDER, scan.getProduct())
                .replace(EMAIL_TEMPLATE_USER_NAME_PLACEHOLDER, toAddress)
                .replace(EMAIL_TEMPLATE_SCAN_TYPE_PLACEHOLDER, scan.getScanner().getName())
                .replace(EMAIL_TEMPLATE_SCAN_STATUS_PLACEHOLDER, status);
        return htmlwithContent;
    }
}
