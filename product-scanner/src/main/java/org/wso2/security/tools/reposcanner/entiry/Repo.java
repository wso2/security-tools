package org.wso2.security.tools.reposcanner.entiry;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ayoma on 4/11/17.
 */
@Entity
@Table(name = "REPO", indexes = {@Index(columnList = "REPO_NAME,TAG_NAME", name = "repoName_tagName_idx")})
public class Repo {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "repo_info_seq_gen")
    @SequenceGenerator(name = "repo_info_seq_gen", sequenceName = "REPO_INFO_SEQ")
    private Long id;

    @Column(name = "REPO_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private RepoType repoType;

    @Column(name = "USER", nullable = false)
    private String user;

    @Column(name = "REPO_NAME", nullable = false)
    private String repositoryName;

    @Column(name = "REPO_URL", nullable = false, length = 2048)
    private String repositoryUrl;

    @Column(name = "TAG_NAME", nullable = false)
    private String tagName;

    @Column(name = "TAG_ZIP", nullable = false, length = 2048)
    private String tagZip;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ADDED_DATE")
    private Date addedDate;

    public Repo(RepoType repoType, String user, String repositoryName, String repositoryUrl, String tagName, String tagZip, Date addedDate) {
        this.repoType = repoType;
        this.user = user;
        this.repositoryName = repositoryName;
        this.repositoryUrl = repositoryUrl;
        this.tagName = tagName;
        this.tagZip = tagZip;
        this.addedDate = addedDate;
    }

    public Repo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RepoType getRepoType() {
        return repoType;
    }

    public void setRepoType(RepoType repoType) {
        this.repoType = repoType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagZip() {
        return tagZip;
    }

    public void setTagZip(String tagZip) {
        this.tagZip = tagZip;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    @Override
    public String toString() {
        return "Repo{" +
                "id=" + id +
                ", repoType=" + repoType +
                ", user='" + user + '\'' +
                ", repositoryName='" + repositoryName + '\'' +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                ", tagName='" + tagName + '\'' +
                ", tagZip='" + tagZip + '\'' +
                ", addedDate=" + addedDate +
                '}';
    }
}
