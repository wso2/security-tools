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

package org.wso2.security.tools.automation.manager.entity.productmanager.containerbased;

import org.wso2.security.tools.automation.manager.entity.productmanager.ProductManagerEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * The class {@code ContainerBasedProductManagerEntity} extends {@code ProductManagerEntity} is an entity to
 * store container based product managers
 */
@SuppressWarnings("unused")
@Entity
public class ContainerBasedProductManagerEntity extends ProductManagerEntity {
    @Column(unique = true)
    private String containerId;
    private String ipAddress;
    private String dockerIpAddress;
    private int containerPort;
    private int hostPort;
    private boolean fileUploaded;
    private String fileUploadedTime;
    private boolean fileExtracted;
    private String fileExtractedTime;
    private boolean serverStarted;
    private String serverStartedTime;

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDockerIpAddress() {
        return dockerIpAddress;
    }

    public void setDockerIpAddress(String dockerIpAddress) {
        this.dockerIpAddress = dockerIpAddress;
    }

    public int getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(int containerPort) {
        this.containerPort = containerPort;
    }

    public int getHostPort() {
        return hostPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    public boolean isFileUploaded() {
        return fileUploaded;
    }

    public void setFileUploaded(boolean fileUploaded) {
        this.fileUploaded = fileUploaded;
    }

    public String getFileUploadedTime() {
        return fileUploadedTime;
    }

    public void setFileUploadedTime(String fileUploadedTime) {
        this.fileUploadedTime = fileUploadedTime;
    }

    public boolean isFileExtracted() {
        return fileExtracted;
    }

    public void setFileExtracted(boolean fileExtracted) {
        this.fileExtracted = fileExtracted;
    }

    public String getFileExtractedTime() {
        return fileExtractedTime;
    }

    public void setFileExtractedTime(String fileExtractedTime) {
        this.fileExtractedTime = fileExtractedTime;
    }

    public boolean isServerStarted() {
        return serverStarted;
    }

    public void setServerStarted(boolean serverStarted) {
        this.serverStarted = serverStarted;
    }

    public String getServerStartedTime() {
        return serverStartedTime;
    }

    public void setServerStartedTime(String serverStartedTime) {
        this.serverStartedTime = serverStartedTime;
    }
}
