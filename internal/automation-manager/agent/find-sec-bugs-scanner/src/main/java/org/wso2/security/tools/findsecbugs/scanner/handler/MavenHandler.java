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

package org.wso2.security.tools.findsecbugs.scanner.handler;

import org.apache.maven.shared.invoker.*;
import org.wso2.security.tools.findsecbugs.scanner.scanner.FindSecBugsScannerExecutor;

import java.io.File;
import java.util.Collections;

/**
 * Utility methods for handling Maven
 */
@SuppressWarnings({"unused"})
public class MavenHandler {

    private static final String MVN_COMMAND_M2_HOME = "M2_HOME";

    /**
     * Execute a maven command
     *
     * @param pomFilePath  pom.xml file path
     * @param mavenCommand Maven command to execute
     * @throws MavenInvocationException Signals an error during the construction of the command line used to invoke
     *                                  Maven
     */
    public static void runMavenCommand(String pomFilePath, String mavenCommand) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(pomFilePath));
        request.setBaseDirectory(new File(FindSecBugsScannerExecutor.getProductPath()));
        request.setGoals(Collections.singletonList(mavenCommand));
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(System.getenv(MVN_COMMAND_M2_HOME)));
        invoker.execute(request);
    }
}
