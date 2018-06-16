/*
 *
 *   Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.security.tools.scanner.dependency.js.utils;

import org.apache.log4j.Logger;
import org.wso2.security.tools.scanner.dependency.js.constants.JSScannerConstants;
import org.wso2.security.tools.scanner.dependency.js.exception.FileHandlerException;

import java.io.File;
import java.util.Arrays;

/**
 * This class contains the common util methods.
 */
public class CommonUtils {
    private static final Logger log = Logger.getLogger(CommonUtils.class);

    /**
     * Create Directory
     *
     * @param dir Directory to be created
     */
    public static void createDirectory(File dir) throws FileHandlerException {
        if (!dir.exists()) {
            boolean isDirCreated = dir.mkdir();
            if (!isDirCreated) {
                throw new FileHandlerException(dir.getAbsolutePath() + "is not created");
            }
        } else {
            log.info("[JS_SEC_DAILY_SCAN] " + dir.getName() + " directory already exists");
        }
    }

    /**
     * Username and password are filled with random numbers. This is used to discard the char array elements
     * of username and password from memory dump and refill with random numbers. This is done for security purpose.
     *
     * @param username Username
     * @param password Password
     */
    public static void clearCredentialData(char[] username, char[] password) {
        Arrays.fill(username, JSScannerConstants.RANDOM_STRING.charAt(ConfigParser.getRandomNumber()));
        Arrays.fill(password, JSScannerConstants.RANDOM_STRING.charAt(ConfigParser.getRandomNumber()));
    }

    /**
     * Accesstoken is illed with random numbers. This is used to discard the char array elements
     * of accesstoken from memory dump and refill with random numbers.
     *
     * @param accessToken Accestoken
     */
    public static void clearAccssToken(char[] accessToken) {
        Arrays.fill(accessToken, JSScannerConstants.RANDOM_STRING.charAt(ConfigParser.getRandomNumber()));
    }

}
