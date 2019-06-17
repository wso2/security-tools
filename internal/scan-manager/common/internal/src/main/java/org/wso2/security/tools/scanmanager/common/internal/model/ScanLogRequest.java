/*
 *  Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.security.tools.scanmanager.common.internal.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.wso2.security.tools.scanmanager.common.model.LogType;

import java.sql.Timestamp;

/**
 * Represents a scan log request coming from scanner services.
 */
public class ScanLogRequest {

    private String jobId;
    private LogType type;
    private String message;

    @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
    private Timestamp timestamp;

    public ScanLogRequest(String jobId, LogType type, String message, Timestamp timestamp) {
        this.jobId = jobId;
        this.type = type;
        this.message = message;
        if (timestamp != null) {
            this.timestamp = new Timestamp(timestamp.getTime());
        }
    }

    public ScanLogRequest() {
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public LogType getType() {
        return type;
    }

    public void setType(LogType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        if (timestamp != null) {
        return new Timestamp(timestamp.getTime());
        } else {
            return null;
        }
    }

    public void setTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
        this.timestamp = new Timestamp(timestamp.getTime());
        }
    }
}
