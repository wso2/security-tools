/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.scanmanager.config;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.wso2.security.tools.scanmanager.api.exception.ScanManagerException;

import javax.annotation.PostConstruct;

/**
 * This class defines start up methods of the application
 */
@Component
public class StartUpInit {

    private static final Logger logger = Logger.getLogger(StartUpInit.class);

    public static ScanManagerConfiguration scanManagerConfiguration = null;

    @PostConstruct
    public void init() {
        try {
            scanManagerConfiguration = ScanMangerConfigurationBuilder.getConfiguration();
            if (scanManagerConfiguration == null) {
                throw new ScanManagerException("Error occurred while reading the" +
                        " scan manager configuration");
            }
        } catch (ScanManagerException e) {
            logger.error("Error occurred while initializing", e);
        }
    }
}
