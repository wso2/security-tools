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
package org.wso2.security.tools.scanmanager.scanners.common.model;

import org.apache.logging.log4j.message.Message;

/**
 * Model to represent the logging event message.
 */
public class CallbackLog implements Message {
    private String jobId;
    private String msg;
    private static final Object[] NULL_OBJECT = {};
    private static final long serialVersionUID = 2L;

    public CallbackLog(String jobId, String msg) {
        this.msg = msg;
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getMessage() {
        return msg;
    }

    public void setMessage(String msg) {
        this.msg = msg;
    }

    @Override
    public String getFormattedMessage() {
        return msg;
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public Object[] getParameters() {
        return NULL_OBJECT;
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }
}
