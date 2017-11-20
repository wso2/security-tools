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

package org.wso2.security.tools.reposcanner.entiry;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "REPO_ERROR")
public class RepoError {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "error_info_seq_gen")
    @SequenceGenerator(name = "error_info_seq_gen", sequenceName = "ERROR_INFO_SEQ")
    private Long id;

    @Column(name = "BUILD_CONFIG", length = 2048)
    private String buildConfigLocation;

    @Column(name = "ERROR_REASON")
    private String errorReason;

    @ManyToOne
    @JoinColumn(name = "REPO_INFO_ID", nullable = false)
    private Repo repo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ADDED_DATE")
    private Date addedDate;

    public RepoError(String buildConfigLocation, String errorReason, Repo repo, Date addedDate) {
        this.buildConfigLocation = buildConfigLocation;
        this.errorReason = errorReason;
        this.repo = repo;
        this.addedDate = addedDate;
    }

    public RepoError() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBuildConfigLocation() {
        return buildConfigLocation;
    }

    public void setBuildConfigLocation(String buildConfigLocation) {
        this.buildConfigLocation = buildConfigLocation;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    public Repo getRepo() {
        return repo;
    }

    public void setRepo(Repo repo) {
        this.repo = repo;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    @Override
    public String toString() {
        return "RepoError{" +
                "id=" + id +
                ", buildConfigLocation='" + buildConfigLocation + '\'' +
                ", errorReason='" + errorReason + '\'' +
                ", repo=" + repo +
                ", addedDate=" + addedDate +
                '}';
    }
}
