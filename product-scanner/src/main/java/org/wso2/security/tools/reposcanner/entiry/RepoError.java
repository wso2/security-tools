package org.wso2.security.tools.reposcanner.entiry;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ayoma on 4/17/17.
 */
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
