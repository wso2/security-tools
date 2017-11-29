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

package org.wso2.security.tools.automation.manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.Properties;

/**
 * {@code AppConfiguration} class extends {@code WebMvcConfigurerAdapter} contains required application configurations.
 * The {@code WebMvcConfigurerAdapter} is for configuring Spring MVC,the replacement of the xml file loaded by the
 * DispatcherServlet for configuring Spring MVC.
 * The {@code WebMvcConfigurerAdapter} gives us a chance to override resources and the default handler.
 * Also, {@code JavaMailSender} to send emails, and {@code Docket} for generating Swagger are configured
 *
 * @see WebMvcConfigurerAdapter
 */
@PropertySource("classpath:application.properties")
@Configuration
@EnableRetry
@EnableSwagger2
@EnableWebMvc
public class AppConfiguration extends WebMvcConfigurerAdapter {

    @Value("${spring.mail.host}")
    private String SMTP_HOST;

    @Value("${spring.mail.port}")
    private int SMTP_PORT;

    @Value("${spring.mail.username}")
    private String SMTP_USERNAME;

    @Value("${spring.mail.password}")
    private String SMTP_PASSWORD;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean SMTP_AUTH = true;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean SMTP_STARTTLS_ENABLE = true;

    @Value("${spring.mail.properties.mail.debug}")
    private boolean SMTP_DEBUG = true;

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    /**
     * Defines configurations for {@link JavaMailSender}
     *
     * @return {@link JavaMailSender}
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(SMTP_HOST);
        mailSender.setPort(SMTP_PORT);
        mailSender.setUsername(SMTP_USERNAME);
        mailSender.setPassword(SMTP_PASSWORD);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", SMTP_AUTH);
        props.put("mail.smtp.starttls.enable", SMTP_STARTTLS_ENABLE);
        props.put("mail.debug", SMTP_DEBUG);
        return mailSender;
    }

    /**
     * Defines configurations for swagger
     *
     * @return {@link Docket}
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Automation Manager REST API",
                "Automation Manager API is managed by WSO2 Platform Security Team",
                "v1.0",
                "Terms of service",
                new Contact("WSO2 Platform Security Team", "http://wso2.com/security", "security@wso2.com"),
                "License of API", "API license URL", Collections.emptyList());
    }

    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        return multipartResolver;
    }
}
