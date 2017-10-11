/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.reposcanner.artifact;

import org.apache.log4j.Logger;
import org.apache.maven.shared.invoker.*;
import org.wso2.security.tools.reposcanner.AppConfig;
import org.wso2.security.tools.reposcanner.entiry.Repo;
import org.wso2.security.tools.reposcanner.entiry.RepoArtifact;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MavenArtifactInfoGenerator implements ArtifactInfoGenerator {
    private static Logger log = Logger.getLogger(MavenArtifactInfoGenerator.class.getName());

    @Override
    public RepoArtifact getArtifact(String consoleTag, Repo repo, File baseFolder, File configFile) throws MavenInvocationException {
        String path = configFile.getAbsolutePath().substring(baseFolder.getAbsolutePath().length(), configFile.getAbsolutePath().length());

        log.info(consoleTag + "Calling MavenID and FinalName identification process for path: " + path);

        String id = runMavenExpression(consoleTag, configFile, "-Dexpression=project.id");
        String finalName = runMavenExpression(consoleTag, configFile, "-Dexpression=project.build.finalName");

        if (id != null && id.split(":").length == 4) {
            log.info(consoleTag + "MavenID for path \"" + path + "\" is " + id);
            log.info(consoleTag + "FinalName for path \"" + path + "\" is " + finalName);

            path = path.substring(path.indexOf(File.separator, 1), path.length());
            path = path.replace("pom.xml", "");

            RepoArtifact mavenInfo = new RepoArtifact(repo, path, id, finalName);
            return mavenInfo;
        } else {
            throw new IllegalArgumentException("Invalid or incomplete MavenID (" + id + ") for path: " + path);
        }
    }

    private String runMavenExpression(String consoleTag, File baseFile, String expression) throws MavenInvocationException {
        Properties props = System.getProperties();

        props.setProperty("maven.home", AppConfig.getMavenHome());

        MavenIdInvocationOutputHandler handler = new MavenIdInvocationOutputHandler(consoleTag);
        InvocationRequest request = new DefaultInvocationRequest();
        request.setOutputHandler(handler);
        request.setPomFile(baseFile);
        request.setGoals(Arrays.asList("org.apache.maven.plugins:maven-help-plugin:evaluate", expression));

        Invoker invoker = new DefaultInvoker();
        invoker.execute(request);

        return handler.getMavenId();
    }

    private static class MavenIdInvocationOutputHandler implements InvocationOutputHandler {
        private String mavenId;
        private String consoleTag;

        public MavenIdInvocationOutputHandler(String consoleTag) {
            this.consoleTag = consoleTag;
        }

        public void consumeLine(String s) {
            if (AppConfig.isDebug()) {
                log.debug(consoleTag + "[MavenInvocation] " + s);
            }

            List<String> mavenOutputSkipPatterns = AppConfig.getMavenOutputSkipPatterns();
            boolean isConsumable = true;
            for (String mavenOutputSkipPattern : mavenOutputSkipPatterns) {
                if (s.startsWith(mavenOutputSkipPattern)) {
                    isConsumable = false;
                }
            }
            if (isConsumable && !(s.contains("null object") || s.contains("invalid expression"))) {
                mavenId = s;
            }
        }

        public String getMavenId() {
            return mavenId;
        }

        public void setMavenId(String mavenId) {
            this.mavenId = mavenId;
        }
    }
}
