/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.security.tool.exception;

/**
 * This class is exclusively for handling exceptions in this project.
 */
public class FeedbackToolException extends Exception {

    /**
     * Constructor to create a new FeedbackToolException with the specified detail message.
     *
     * @param string The detail message of the exception.
     */
    public FeedbackToolException(String string) {
        super(string);
    }

    /**
     * Constructor to create a new exception with the specified detail message and cause.
     *
     * @param message The detail message of the exception.
     * @param e       The cause of the exception.
     */
    public FeedbackToolException(String message, Throwable e) {
        super(message, e);
    }
}
