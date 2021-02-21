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
package org.wso2.security.tools.scanmanager.core.config;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.core.service.ScanEngineService;

import javax.annotation.PostConstruct;

/**
 * This class defines start up methods of the application.
 */
@Component
public class StartUpInit {

    private static final Logger logger = Logger.getLogger(StartUpInit.class);

    private ScanEngineService scanEngineService;

    @Autowired
    public StartUpInit(ScanEngineService scanEngineService) {
        this.scanEngineService = scanEngineService;
    }

    @PostConstruct
    public void init() {

        scanEngineService.beginPendingScans();

    }
}
