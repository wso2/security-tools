/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.automation.manager.handler;

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The class {@code DockerHandler} provides methods to handle Docker related operations
 *
 * @see com.spotify.docker.client
 */
@SuppressWarnings("unused")
public class DockerHandler {
    private static DockerClient dockerClient;

    /**
     * Get a client for interacting with dockerd
     *
     * @return docker client
     * @throws DockerCertificateException If docker certificate validation has failed
     */
    private static DockerClient getDockerClient() throws DockerCertificateException {
        if (dockerClient == null) {
            dockerClient = DefaultDockerClient.fromEnv().build();
        }
        return dockerClient;
    }

    /**
     * Pull Docker image
     *
     * @param imageName Image name to pull
     * @return a boolean value to indicate the operation success
     * @throws DockerCertificateException If docker certificate validation has failed
     * @throws DockerException
     * @throws InterruptedException
     */
    public static boolean pullImage(String imageName) throws DockerCertificateException, DockerException,
            InterruptedException {
        if (!checkIfImageIsAvailable(imageName)) {
            getDockerClient().pull(imageName);
        }
        return checkIfImageIsAvailable(imageName);
    }

    /**
     * Check if an image is available locally
     *
     * @param imageName Image name to be checked
     * @return a boolean to indicate the image is available
     * @throws DockerCertificateException
     * @throws DockerException
     * @throws InterruptedException
     */
    private static boolean checkIfImageIsAvailable(String imageName) throws DockerCertificateException,
            DockerException, InterruptedException {
        boolean imageAvailable = false;
        List<Image> images;
        images = getDockerClient().listImages();
        for (Image image : images) {
            ImmutableList<String> tags = image.repoTags();
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
     * Create a container and returns the id
     *
     * @param imageName            Docker image
     * @param ipAddress            Ip address to bind
     * @param containerPort        Container port to bind to a host port
     * @param hostPort             Host port to bind a container port
     * @param commands             Container startup command
     * @param environmentVariables Environment variables of the container
     * @return container id if the container is successfully created, or {@code null}
     * @throws DockerCertificateException
     * @throws DockerException
     * @throws InterruptedException
     */
    public static String createContainer(String imageName, String ipAddress, String containerPort, String hostPort,
                                         List<String> commands, String[] environmentVariables) throws
            DockerCertificateException, DockerException, InterruptedException {
        String[] ports = {containerPort, hostPort};
        HashMap<String, List<PortBinding>> portBindings = new HashMap<>();
        for (String port : ports) {
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.of(ipAddress, port));
            portBindings.put(port, hostPorts);
        }
        HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
        ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image(imageName).exposedPorts(ports)
                .cmd(commands)
                .env(environmentVariables)
                .build();
        return getDockerClient().createContainer(containerConfig).id();
    }

    /**
     * Start a container
     *
     * @param containerId Container id
     * @return Boolean value to indicate the container is started
     * @throws DockerCertificateException
     * @throws DockerException
     * @throws InterruptedException
     */
    public static boolean startContainer(String containerId) throws DockerCertificateException, DockerException,
            InterruptedException {
        getDockerClient().startContainer(containerId);
        return "running".equals(inspectContainer(containerId).state().status());
    }

    /**
     * Returns a {@link ContainerInfo} object to inspect a container
     *
     * @param containerId Container id
     * @return object containing container information
     * @throws DockerCertificateException
     * @throws DockerException
     * @throws InterruptedException
     */
    public static ContainerInfo inspectContainer(String containerId) throws DockerCertificateException,
            DockerException, InterruptedException {
        return getDockerClient().inspectContainer(containerId);
    }

    /**
     * Stop a running container
     *
     * @param containerId Container id
     * @throws DockerCertificateException
     * @throws DockerException
     * @throws InterruptedException
     */
    public static void killContainer(String containerId) throws DockerCertificateException, DockerException,
            InterruptedException {
        getDockerClient().killContainer(containerId);
    }

    /**
     * Remove a stopped container
     *
     * @param containerId Container id
     * @throws DockerCertificateException
     * @throws DockerException
     * @throws InterruptedException
     */
    public static void removeContainer(String containerId) throws DockerCertificateException, DockerException,
            InterruptedException {
        getDockerClient().removeContainer(containerId);
    }

    /**
     * Restarts a stopped container
     *
     * @param containerId Container id
     * @throws DockerCertificateException
     * @throws DockerException
     * @throws InterruptedException
     */
    public static void restartContainer(String containerId) throws DockerCertificateException, DockerException,
            InterruptedException {
        getDockerClient().restartContainer(containerId);
    }

    /**
     * Get container logs
     *
     * @param container_id Container id
     * @return Logs of a container
     * @throws DockerCertificateException
     * @throws DockerException
     * @throws InterruptedException
     */
    public static String getContainerLogs(String container_id) throws DockerCertificateException, DockerException,
            InterruptedException {
        final String logs;
        try (LogStream stream = getDockerClient().logs(container_id, DockerClient.LogsParam.stdout(),
                DockerClient.LogsParam.stderr())) {
            logs = stream.readFully();
            return logs;
        }
    }

    /**
     * Get a list of running containers
     *
     * @return a list of running containers
     * @throws DockerCertificateException
     * @throws DockerException
     * @throws InterruptedException
     */
    public static List<Container> getRunningContainersList() throws DockerCertificateException, DockerException,
            InterruptedException {
        return getDockerClient().listContainers();
    }

    /**
     * Get all containers list
     *
     * @return a list of all containers
     * @throws DockerCertificateException
     * @throws DockerException
     * @throws InterruptedException
     */
    public static List<Container> getAllContainersList() throws DockerCertificateException, DockerException,
            InterruptedException {
        return getDockerClient().listContainers(DockerClient.ListContainersParam.allContainers());
    }

    /**
     * Closes any and all underlying connections to docker, and release resources.
     *
     * @throws DockerCertificateException
     */
    public static void closeDockerClient() throws DockerCertificateException {
        getDockerClient().close();
    }

    /**
     * Copy files from a container
     *
     * @param containerId       Container Id
     * @param filePathToCopy    File path of host
     * @param destinationFile   Destination file path of the container
     * @param destinationFolder Destination folder path of the container
     * @throws IOException
     * @throws DockerCertificateException
     * @throws DockerException
     * @throws InterruptedException
     */
    public static void copyFilesFromContainer(String containerId, String filePathToCopy, String destinationFile,
                                              File destinationFolder) throws IOException, DockerCertificateException,
            DockerException, InterruptedException {
        try (InputStream inputStream = getDockerClient().archiveContainer(containerId, filePathToCopy);
             FileOutputStream outputStream = new FileOutputStream(new File(destinationFolder, FilenameUtils
                     .getName(destinationFile)))) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }

    /**
     * Copy files to a container
     *
     * @param inputStream Input stream to copy
     * @param containerId Container id
     * @param path        Destination path in container
     * @throws DockerCertificateException
     * @throws InterruptedException
     * @throws DockerException
     * @throws IOException
     */
    public static void copyFilesToContainer(InputStream inputStream, String containerId, String path) throws
            DockerCertificateException, InterruptedException, DockerException, IOException {
        getDockerClient().copyToContainer(inputStream, containerId, path);
    }
}
