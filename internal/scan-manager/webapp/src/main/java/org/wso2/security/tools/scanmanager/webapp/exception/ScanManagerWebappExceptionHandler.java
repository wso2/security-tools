/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.webapp.exception;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import static org.wso2.security.tools.scanmanager.webapp.util.Constants.DEFAULT_ERROR_PAGE_VIEW;

/**
 * Global Exception handler class.
 */
@ControllerAdvice
public class ScanManagerWebappExceptionHandler {

    private static final Logger logger = Logger.getLogger(ScanManagerWebappExceptionHandler.class);

    /**
     * Exception handler for type Exception.
     *
     * @param e exception
     * @return error message object
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView exceptionHandler(Exception e) {
        logger.error("An error occurred", e);
        return new ModelAndView(DEFAULT_ERROR_PAGE_VIEW);
    }
}
