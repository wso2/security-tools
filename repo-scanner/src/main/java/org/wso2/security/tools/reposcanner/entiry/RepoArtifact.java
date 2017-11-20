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
@Table(name = "REPO_ARTIFACT", indexes = {@Index(columnList = "PATH", name = "artifact_path_idx")})
public class RepoArtifact {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "artifact_info_seq_gen")
    @SequenceGenerator(name = "artifact_info_seq_gen", sequenceName = "ARTIFACT_INFO_SEQ")
    private Long id;

    @Column(name = "PATH", nullable = false, length = 2048)
    private String path;

    @Column(name = "GROUP_ID", nullable = false)
    private String groupId;

    @Column(name = "ARTIFACT_ID", nullable = false)
    private String artifactId;

    @Column(name = "PACKAGING", nullable = false)
    private String packaging;

    @Column(name = "VERSION", nullable = false)
    private String version;

    @Column(name = "FINAL_NAME")
    private String finalName;

    @ManyToOne
    @JoinColumn(name = "REPO_INFO_ID", nullable = false)
    private Repo repo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ADDED_DATE")
    private Date addedDate;

    public RepoArtifact(Repo repo, String path, String mavenId, String finalName) {
        String[] mavenIdParts = mavenId.split(":");
        this.path = path;
        this.groupId = mavenIdParts[0];
        this.artifactId = mavenIdParts[1];
        this.packaging = mavenIdParts[2];
        this.version = mavenIdParts[3];
        this.repo = repo;
        this.addedDate = new Date();
        this.finalName = finalName;
    }

    public RepoArtifact() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFinalName() {
        return finalName;
    }

    public void setFinalName(String finalName) {
        this.finalName = finalName;
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
        return "RepoArtifact{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", packaging='" + packaging + '\'' +
                ", version='" + version + '\'' +
                ", finalName='" + finalName + '\'' +
                ", repo=" + repo +
                ", addedDate=" + addedDate +
                '}';
    }
}
