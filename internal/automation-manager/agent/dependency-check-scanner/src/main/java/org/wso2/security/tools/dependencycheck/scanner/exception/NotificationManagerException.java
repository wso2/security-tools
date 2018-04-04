/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.dependencycheck.scanner.exception;

/**
 * This class is to handle the exceptions thrown by
 * {@link org.wso2.security.tools.dependencycheck.scanner.NotificationManager}
 */
@SuppressWarnings({"unused"})
public class NotificationManagerException extends Exception {

    /**
     * Constructs a new exception with {@code null} as its detail message.
     */
    public NotificationManagerException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message Message for the exception
     */
    public NotificationManagerException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     */
    public NotificationManagerException(String message, Throwable e) {
        super(message, e);
    }
}
