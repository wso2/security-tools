/*
 *
 *   Copyright (c) 2021, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Main class for the Core Spring Boot application
 */
@SpringBootApplication
//@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
@EnableJpaRepositories("org.wso2.security.tools.scanmanager.core.dao")
@EntityScan("org.wso2.security.tools.scanmanager.common.external.model")
@PropertySource("classpath:application.properties")
//@PropertySource("file:config/application.properties")
@ComponentScan("org.wso2.security.tools.scanmanager")
public class Main {
    private static final Logger logger = Logger.getLogger(StartUpInit.class);

    public static void main(String[] args){
        logger.info("Scan Manager Core Starting");
        SpringApplication.run(Main.class, args);
    }
}