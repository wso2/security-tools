/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.security.web.entity;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;



/**
 * Component class for having tha attributes which needed to be added by the developer when filling the form
 */
@Entity
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Integer id;

    private String componantName;

    private Date uploadedDate;

    private int issuesReported;

    private int falsePositives;

    private int truePositives;

    private String developerStatus;

    private String securityteamStatus;

    private String developer;

    private String developerEmail;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getComponantName() {
        return componantName;
    }

    public void setComponantName(String componantName) {
        this.componantName = componantName;
    }

    public Date getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(Date uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public int getIssuesReported() {
        return issuesReported;
    }

    public void setIssuesReported(int issuesReported) {
        this.issuesReported = issuesReported;
    }

    public int getFalsePositives() {
        return falsePositives;
    }

    public void setFalsePositives(int falsePositives) {
        this.falsePositives = falsePositives;
    }

    public int getTruePositives() {
        return truePositives;
    }

    public void setTruePositives(int truePositives) {
        this.truePositives = truePositives;
    }

    public String getDeveloperStatus() {
        return developerStatus;
    }

    public void setDeveloperStatus(String developerStatus) {
        this.developerStatus = developerStatus;
    }

    public String getSecurityteamStatus() {
        return securityteamStatus;
    }

    public void setSecurityteamStatus(String securityteamStatus) {
        this.securityteamStatus = securityteamStatus;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getDeveloperEmail() {
        return developerEmail;
    }

    public void setDeveloperEmail(String developerEmail) {
        this.developerEmail = developerEmail;
    }
}
