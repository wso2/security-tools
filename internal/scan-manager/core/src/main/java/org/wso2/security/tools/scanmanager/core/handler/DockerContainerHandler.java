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

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.PortBinding;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.core.model.ScanManagerContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides the implementation for the Docker containers.
 */
public class DockerContainerHandler implements ContainerHandler {

    private static final int SECONDS_TO_WAIT_BEFORE_KILLING_CONTAINER = 5;
    private static final int CONTAINER_HOST_PORT_MINIMUM_VALUE = 20000;

    private DockerClient getNewDockerClient() throws DockerCertificateException {
        return DefaultDockerClient.fromEnv().build();
    }

    private boolean pullImage(DockerClient dockerClient, String imageName) throws DockerException,
            InterruptedException {
        if (!checkIfImageIsAvailable(dockerClient, imageName)) {
            dockerClient.pull(imageName);
        }
        return checkIfImageIsAvailable(dockerClient, imageName);
    }

    private boolean checkIfImageIsAvailable(DockerClient dockerClient, String imageName) throws DockerException,
            InterruptedException {
        boolean imageAvailable = false;
        List<Image> images;
        images = dockerClient.listImages();
        for (Image image : images) {
            List<String> tags = image.repoTags();
            if (tags != null) {
                for (String tag : tags) {
                    if (imageName.equals(tag)) {
                        imageAvailable = true;
                    }
                }
            }
        }
        return imageAvailable;
    }

    /**
     * Generates a host port by checking the ports used by existing containers.
     *
     * @return port
     * @throws ScanManagerException when an error occurs when checking the ports used by existing containers
     */
    private Integer generateHostPort() throws ScanManagerException {
        List<Integer> allocatedHostPorts = findAllocatedHostPorts();
        Integer newHostPort = CONTAINER_HOST_PORT_MINIMUM_VALUE;
        do {
            newHostPort++;
        } while (allocatedHostPorts.contains(newHostPort));
        return newHostPort;
    }

    private List<Integer> findAllocatedHostPorts() throws ScanManagerException {
        List<Integer> allocatedHostPorts = new ArrayList<>();
        try (DockerClient dockerClient = getNewDockerClient()) {
            dockerClient.listContainers()
                    .forEach(container -> container.ports()
                            .forEach(portMapping -> allocatedHostPorts.add(portMapping.publicPort())));
        } catch (DockerCertificateException | InterruptedException | DockerException e) {
            throw new ScanManagerException("Unable to identify the allocated ports", e);
        }
        return allocatedHostPorts;
    }

    @Override
    public ScanManagerContainer createContainer(String imageName, String ipAddress, Integer containerPort,
                                                Map<String, String> labels, List<String> commands,
                                                String[] environmentVariables) throws ScanManagerException {
        try (DockerClient dockerClient = getNewDockerClient()) {
            String[] exposedPorts = {containerPort.toString()};
            HashMap<String, List<PortBinding>> portBindings = new HashMap<>();

            Integer hostPort = generateHostPort();
            Map<Integer, Integer> portMapping = new HashMap<>();
            portMapping.put(containerPort, hostPort);

            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.hostPort(hostPort.toString()));
            portBindings.put(containerPort.toString(), hostPorts);
            HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

            //pull the image from the docker registry.
            pullImage(dockerClient, imageName);

            ContainerConfig containerConfig = ContainerConfig.builder()
                    .hostConfig(hostConfig)
                    .image(imageName)
                    .exposedPorts(exposedPorts)
                    .cmd(commands)
                    .labels(labels)
                    .env(environmentVariables)
                    .build();
            ContainerCreation containerCreation = dockerClient.createContainer(containerConfig);
            return new ScanManagerContainer(containerCreation.id(), false, portMapping, commands,
                    Arrays.asList(environmentVariables), labels);
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new ScanManagerException("Error occurred while creating the docker container", e);
        }
    }

    @Override
    public void startContainer(String containerId) throws ScanManagerException {
        try (DockerClient dockerClient = getNewDockerClient()) {
            dockerClient.startContainer(containerId);
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new ScanManagerException("Error occurred while starting the docker container with container id: " +
                    containerId, e);
        }
    }

    @Override
    public ScanManagerContainer inspectContainer(String containerId) throws ScanManagerException {
        ContainerInfo containerInfo = null;

        try (DockerClient dockerClient = getNewDockerClient()) {
            containerInfo = dockerClient.inspectContainer(containerId);
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new ScanManagerException("Error occurred while starting the docker container with container id: " +
                    containerId, e);
        }

        //extracting data from the docker container and populating scan manager container
        if (containerInfo != null) {
            Map<Integer, Integer> portMappings = new HashMap<>();
            for (Map.Entry<String, List<PortBinding>> entry : containerInfo.hostConfig().portBindings().entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    portMappings.put(Integer.parseInt(entry.getKey()),
                            Integer.parseInt(entry.getValue().get(0).hostPort()));
                }
            }
            return new ScanManagerContainer(containerInfo.id(), containerInfo.state().running(), portMappings,
                    containerInfo.config().cmd(), containerInfo.config().env(), containerInfo.config().labels());
        } else {
            return null;
        }
    }

    @Override
    public void cleanContainer(String containerId) throws ScanManagerException {
        try (DockerClient dockerClient = getNewDockerClient()) {
            if (dockerClient.inspectContainer(containerId).state().running()) {
                dockerClient.stopContainer(containerId, SECONDS_TO_WAIT_BEFORE_KILLING_CONTAINER);
            }
            dockerClient.removeContainer(containerId);
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new ScanManagerException("Error occurred while cleaning the docker container with container id: " +
                    containerId, e);
        }
    }

    @Override
    public List<ScanManagerContainer> getContainersList() throws ScanManagerException {
        List<ScanManagerContainer> containers = new ArrayList<>();
        try (DockerClient dockerClient = getNewDockerClient()) {
            for (Container container : dockerClient.listContainers()) {

                Map<Integer, Integer> portMappings = new HashMap<>();
                for (Container.PortMapping portMapping : container.ports()) {
                    portMappings.put(portMapping.privatePort(), portMapping.publicPort());
                }
                ContainerInfo containerInfo = dockerClient.inspectContainer(container.id());
                ScanManagerContainer scanManagerContainer = new ScanManagerContainer(container.id(),
                        containerInfo.state().running(), portMappings, containerInfo.config().cmd(),
                        containerInfo.config().env(), container.labels());
                containers.add(scanManagerContainer);
            }
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new ScanManagerException("Error occurred while getting the running containers list", e);
        }
        return containers;
    }
}
