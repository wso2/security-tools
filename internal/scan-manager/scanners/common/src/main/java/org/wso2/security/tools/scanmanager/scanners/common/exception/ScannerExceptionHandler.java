/*
 *  Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.security.tools.scanmanager.scanners.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.wso2.security.tools.scanmanager.common.ErrorMessage;

/**
 * Global exception handler class for Scanner exceptions.
 */
@ControllerAdvice
public class ScannerExceptionHandler {

    /**
     * Exception handler for type Exception.
     *
     * @param e
     * @return Response Entity with error details
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorMessage> exceptionHandler(Exception e) {
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error has occurred. " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception handler for type InvalidRequestException.
     *
     * @param e
     * @return Response Entity with error details
     */
    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<ErrorMessage> invalidRequestExceptionHandler(Exception e) {
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                "Bad request. " + e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
