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

package org.wso2.security.tools.automation.manager.entity.staticscanner.containerbased;

import org.wso2.security.tools.automation.manager.entity.staticscanner.StaticScannerEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * The abstract class {@code ContainerBasedStaticScannerEntity} extends {@code StaticScannerEntity} is an entity to
 * store container based static scanners
 */
@SuppressWarnings("unused")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class ContainerBasedStaticScannerEntity extends StaticScannerEntity {
    @Column(unique = true)
    private String containerId;
    private String ipAddress;
    private String dockerIpAddress;
    private int containerPort;
    private int hostPort;
    private String contextPath;
    private boolean isProductAvailable;
    private boolean fileUploaded;
    private String fileUploadedTime;
    private boolean fileExtracted;
    private String fileExtractedTime;
    private boolean productCloned;
    private String productClonedTime;
    private String scanStatus;
    private String scanStatusTime;
    private boolean reportReady;
    private String reportReadyTime;
    private boolean reportSent;
    private String reportSentTime;

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

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public boolean isProductAvailable() {
        return isProductAvailable;
    }

    public void setProductAvailable(boolean productAvailable) {
        this.isProductAvailable = productAvailable;
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

    public boolean isProductCloned() {
        return productCloned;
    }

    public void setProductCloned(boolean productCloned) {
        this.productCloned = productCloned;
    }

    public String getProductClonedTime() {
        return productClonedTime;
    }

    public void setProductClonedTime(String productClonedTime) {
        this.productClonedTime = productClonedTime;
    }

    public String getScanStatus() {
        return scanStatus;
    }

    public void setScanStatus(String scanStatus) {
        this.scanStatus = scanStatus;
    }

    public String getScanStatusTime() {
        return scanStatusTime;
    }

    public void setScanStatusTime(String scanStatusTime) {
        this.scanStatusTime = scanStatusTime;
    }

    public boolean isReportReady() {
        return reportReady;
    }

    public void setReportReady(boolean reportReady) {
        this.reportReady = reportReady;
    }

    public String getReportReadyTime() {
        return reportReadyTime;
    }

    public void setReportReadyTime(String reportReadyTime) {
        this.reportReadyTime = reportReadyTime;
    }

    public boolean isReportSent() {
        return reportSent;
    }

    public void setReportSent(boolean reportSent) {
        this.reportSent = reportSent;
    }

    public String getReportSentTime() {
        return reportSentTime;
    }

    public void setReportSentTime(String reportSentTime) {
        this.reportSentTime = reportSentTime;
    }
}
