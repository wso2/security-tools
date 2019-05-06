/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.core.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.wso2.security.tools.scanmanager.common.model.ErrorMessage;

/**
 * Global Exception handler class.
 */
@ControllerAdvice
public class ScanManagerExceptionHandler {

    private static final Logger logger = Logger.getLogger(ScanManagerExceptionHandler.class);

    /**
     * Exception handler for type Exception.
     *
     * @param e exception
     * @return error message object
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorMessage> exceptionHandler(Exception e) {
        logger.error("An error occurred", e);
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception handler for type HttpRequestMethodNotSupportedException.
     *
     * @param e exception
     * @return error message object
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorMessage> httpRequestMethodNotSupportedHandler(HttpRequestMethodNotSupportedException e) {
        logger.error("Http request method not supported", e);
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.METHOD_NOT_ALLOWED.value(),
                HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase()), HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Exception handler for type HttpMessageNotReadableException.
     *
     * @param e exception
     * @return error message object
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorMessage> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        logger.error("HTTP message not readable", e);
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "HTTP message not readable"),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Exception handler for type HttpMediaTypeNotSupportedException.
     *
     * @param e exception
     * @return error message object
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ErrorMessage> httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        logger.error("HTTP media type not supported", e);
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase()), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Exception handler for type InvalidRequestException.
     *
     * @param e exception
     * @return error message object
     */
    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<ErrorMessage> invalidRequestExceptionHandler(InvalidRequestException e) {
        logger.error("Invalid request", e);
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Exception handler for type HttpMessageConversionException.
     *
     * @param e exception
     * @return error message object
     */
    @ExceptionHandler(HttpMessageConversionException.class)
    ResponseEntity<ErrorMessage> httpMessageConversionException(HttpMessageConversionException e) {
        logger.error("HTTP message conversion error", e);
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "HTTP message conversion error"),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Exception handler for type JsonMappingException.
     *
     * @param e exception
     * @return error message object
     */
    @ExceptionHandler(JsonMappingException.class)
    ResponseEntity<ErrorMessage> jsonMappingExceptionHandler(JsonMappingException e) {
        logger.error("JSON mapping error", e);
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Exception handler for type ResourceNotFoundException.
     *
     * @param e exception
     * @return error message object
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorMessage> resourceNotFoundExceptionHandler(ResourceNotFoundException e) {
        logger.error("Resource not found", e);
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.NOT_FOUND.value(), e.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    /**
     * Exception handler for type ScanManagerException.
     *
     * @param e exception
     * @return error message object
     */
    @ExceptionHandler(ScanManagerException.class)
    ResponseEntity<ErrorMessage> scanManagerExceptionHandler(ScanManagerException e) {
        logger.error("An error occurred", e);
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception handler for type InvalidFormatException.
     *
     * @param e exception
     * @return error message object
     */
    @ExceptionHandler(InvalidFormatException.class)
    ResponseEntity<ErrorMessage> invalidFormatException(InvalidFormatException e) {
        logger.error("Invalid format error", e);
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                "Invalid format error"), HttpStatus.BAD_REQUEST);
    }

    /**
     * Exception handler for type MissingServletRequestParameterException.
     *
     * @param e exception
     * @return Response Entity with error details
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorMessage> missingServletRequestParameterException(MissingServletRequestParameterException e) {
        logger.error("Missing request parameter", e);
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
