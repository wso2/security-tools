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
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.PortBinding;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;
import org.wso2.security.tools.scanmanager.core.model.ContainerInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class {@code DockerHandlerImpl} provides the implementation for the DockerHandler interface.
 */
public class DockerHandlerImpl implements DockerHandler {

    private static final String CONTAINER_STATUS_RUNNING = "running";

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

    private Integer generateHostPort() throws ScanManagerException {
        List<Integer> allocatedHostPorts = findAllocatedHostPorts();
        Integer newHostPort = 9080;
        do {
            newHostPort++;
        } while (allocatedHostPorts.contains(newHostPort));
        return newHostPort;
    }

    private List<Integer> findAllocatedHostPorts() throws ScanManagerException {
        List<Integer> allocatedHostPorts = new ArrayList<>();
        try (DockerClient dockerClient = getNewDockerClient()) {
            for (Container container : dockerClient.listContainers()) {
                for (Container.PortMapping portMapping : container.ports()) {
                    if (!allocatedHostPorts.contains(portMapping.publicPort())) {
                        allocatedHostPorts.add(portMapping.publicPort());
                    }
                }
            }
        } catch (DockerCertificateException | InterruptedException | DockerException e) {
            throw new ScanManagerException("Unable to identify the allocated ports", e);
        }
        return allocatedHostPorts;
    }

    public ContainerInfo createContainer(String imageName, String ipAddress, Integer containerPort,
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
            return new ContainerInfo(containerCreation.id(), portMapping, commands,
                    Arrays.asList(environmentVariables), labels);
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new ScanManagerException("Error occurred while creating the docker container", e);
        }
    }

    public boolean startContainer(String containerId) throws ScanManagerException {
        try (DockerClient dockerClient = getNewDockerClient()) {
            dockerClient.startContainer(containerId);
            return CONTAINER_STATUS_RUNNING.equals(inspectContainer(dockerClient, containerId).state().status());
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new ScanManagerException("Error occurred while starting the docker container with container id: " +
                    containerId, e);
        }

    }

    private com.spotify.docker.client.messages.ContainerInfo inspectContainer(DockerClient dockerClient,
                                                                              String containerId)
            throws DockerException, InterruptedException {
        return dockerClient.inspectContainer(containerId);
    }

    public void killContainer(String containerId) throws ScanManagerException {
        try (DockerClient dockerClient = getNewDockerClient()) {
            dockerClient.killContainer(containerId);
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new ScanManagerException("Error occurred while killing the docker container with container id: " +
                    containerId, e);
        }
    }

    public void removeContainer(String containerId) throws ScanManagerException {
        try (DockerClient dockerClient = getNewDockerClient()) {
            dockerClient.removeContainer(containerId);
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new ScanManagerException("Error occurred while removing the docker container with container id: " +
                    containerId, e);
        }
    }

    public List<ContainerInfo> getContainersList() throws ScanManagerException {
        List<ContainerInfo> runningContainers = new ArrayList<>();
        try (DockerClient dockerClient = getNewDockerClient()) {
            for (Container container : dockerClient.listContainers()) {

                Map<Integer, Integer> portMappings = new HashMap<>();
                for (Container.PortMapping portMapping : container.ports()) {
                    portMappings.put(portMapping.privatePort(), portMapping.publicPort());
                }
                ContainerInfo containerInfo = new ContainerInfo(container.id());
                containerInfo.setPortMappings(portMappings);
                containerInfo.setLabels(container.labels());
                runningContainers.add(containerInfo);
            }
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new ScanManagerException("Error occurred while getting the running containers list", e);
        }
        return runningContainers;
    }
}
