package org.wso2.security.tools.scanmanager.model;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * Model class to represent a Logs in the scan containers.
 */
@Entity
@Table(name = "LOG")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "SCAN_ID")
    private Integer scanId;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "TIMESTAMP")
    private Timestamp timeStamp;

    @Column(name = "MESSAGE")
    private String message;

    public Log() {
    }

    public Log(Integer scanId, String type, Timestamp timeStamp, String message) {
        this.scanId = scanId;
        this.type = type;
        this.timeStamp = timeStamp;
        this.message = message;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getScanId() {
        return scanId;
    }

    public void setScanId(Integer scanId) {
        this.scanId = scanId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
