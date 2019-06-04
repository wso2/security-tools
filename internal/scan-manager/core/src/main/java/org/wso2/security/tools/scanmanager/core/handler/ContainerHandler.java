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
package org.wso2.security.tools.scanmanager.core.handler;

import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.core.model.Container;

import java.util.List;
import java.util.Map;

/**
 * This class provides an interface to handle container related operations.
 */
public interface ContainerHandler {

    /**
     * Create a container.
     *
     * @param imageName            Container image
     * @param ipAddress            IP address to bind
     * @param containerPort        Container port to bind to a host port
     * @param labels               Container labels
     * @param commands             Container startup commands
     * @param environmentVariables Environment variables of the container
     * @return container info object if the container is successfully created
     * @throws ScanManagerException when an error occurs while creating a container
     */
    public Container create(String imageName, String ipAddress, Integer containerPort,
                            Map<String, String> labels, List<String> commands,
                            String[] environmentVariables) throws ScanManagerException;

    /**
     * Start a container.
     *
     * @param containerId Container id
     * @throws ScanManagerException when an error occurs while starting the container
     */
    public void start(String containerId) throws ScanManagerException;

    /**
     * Stop and remove a container.
     *
     * @param containerId container id of the container to be cleaned
     * @throws ScanManagerException when an error occurs while cleaning the container
     */
    public void clean(String containerId) throws ScanManagerException;

    /**
     * Inspect a container.
     *
     * @param containerId Container id
     * @return container information object
     * @throws ScanManagerException when an error occurs while inspecting the container
     */
    public Container inspect(String containerId) throws ScanManagerException;

    /**
     * Get the list of existing containers.
     *
     * @return a list of existing containers
     * @throws ScanManagerException when an error occurs while getting the list of containers
     */
    public List<Container> list() throws ScanManagerException;
}
