/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.core.model;

import java.util.List;
import java.util.Map;

/**
 * Model class to represent a container.
 */
public class Container {

    private String id;
    private boolean isRunning;
    private Map<Integer, Integer> portMappings;
    private List<String> commands;
    private List<String> envVariables;
    private Map<String, String> labels;

    public Container() {
    }

    public Container(String id, boolean isRunning, Map<Integer, Integer> portMappings,
                     List<String> commands, List<String> envVariables, Map<String, String> labels) {
        this.id = id;
        this.isRunning = isRunning;
        this.portMappings = portMappings;
        this.commands = commands;
        this.envVariables = envVariables;
        this.labels = labels;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public Map<Integer, Integer> getPortMappings() {
        return portMappings;
    }

    public void setPortMappings(Map<Integer, Integer> portMappings) {
        this.portMappings = portMappings;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public List<String> getEnvVariables() {
        return envVariables;
    }

    public void setEnvVariables(List<String> envVariables) {
        this.envVariables = envVariables;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}
