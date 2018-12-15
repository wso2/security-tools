/*
 *  Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 *
 */

package org.wso2.security.tools.scanner.handler;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.wso2.security.tools.scanner.config.ConfigurationReader;
import org.wso2.security.tools.scanner.exception.ScannerException;
import org.wso2.security.tools.scanner.utils.ScannerConstants;

import java.io.File;
import java.util.Collections;

/**
 * Utility class for handling Maven.
 */
public class MavenHandler {

    private MavenHandler() {
    }

    /**
     * Execute a maven command.
     *
     * @param pomFilePath  pom.xml file path
     * @param mavenCommand Maven command to execute
     * @throws ScannerException error during the construction of the command line
     *                          used to invoke Maven
     */
    public static void runMavenCommand(String pomFilePath, String mavenCommand)
            throws ScannerException {
        InvocationRequest request = new DefaultInvocationRequest();

        request.setPomFile(new File(pomFilePath));
        request.setGoals(Collections.singletonList(mavenCommand));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(ConfigurationReader.getConfigProperty(
                ScannerConstants.MVN_M2_HOME)));

        try {
            invoker.execute(request);
        } catch (MavenInvocationException e) {
            throw new ScannerException("Error occured while executing the maven command. ", e);
        }
    }
}
