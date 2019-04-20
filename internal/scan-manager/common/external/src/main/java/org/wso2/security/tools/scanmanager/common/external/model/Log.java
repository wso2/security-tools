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
package org.wso2.security.tools.scanmanager.common.external.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.wso2.security.tools.scanmanager.common.model.LogType;

import java.math.BigInteger;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
    @JsonIgnore
    private BigInteger id;

    @ManyToOne
    @JoinColumn(name = "JOB_ID")
    @JsonIgnore
    private Scan scan;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private LogType type;

    @Column(name = "TIME_CREATED")
    private Timestamp timeStamp;

    @Column(name = "MESSAGE")
    private String message;

    public Log(Scan scan, LogType type, Timestamp timeStamp, String message) {
        this.scan = scan;
        this.type = type;
        this.message = message;
        if (timeStamp != null) {
            this.timeStamp = new Timestamp(timeStamp.getTime());
        }
    }

    public Log() {
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public LogType getType() {
        return type;
    }

    public void setType(LogType type) {
        this.type = type;
    }

    public Timestamp getTimeStamp() {
        if (timeStamp != null) {
            return new Timestamp(timeStamp.getTime());
        } else {
            return null;
        }
    }

    public void setTimeStamp(Timestamp timeStamp) {
        if (timeStamp != null) {
            this.timeStamp = new Timestamp(timeStamp.getTime());
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
