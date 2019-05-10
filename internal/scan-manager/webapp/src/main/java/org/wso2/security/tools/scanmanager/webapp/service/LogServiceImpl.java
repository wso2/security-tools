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

package org.wso2.security.tools.scanmanager.webapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.scanmanager.common.external.model.Log;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerLogResponse;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.webapp.config.ScanManagerWebappConfiguration;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;
import org.wso2.security.tools.scanmanager.webapp.util.HTTPUtil;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.security.tools.scanmanager.webapp.util.Constants.JOB_ID_PARAM_NAME;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.PAGE_PARAM_NAME;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.PRE_JOB_ID_PREFIX;

/**
 * Log service implementation class.
 */
@Service
public class LogServiceImpl implements LogService {

    private static final Logger logger = Logger.getLogger(LogServiceImpl.class);
    private Map<String, List<Log>> waitingScanLogs = new ConcurrentHashMap<>();

    @Override
    public ScanManagerLogResponse getLogs(String jobId, Integer pageNumber) throws ScanManagerWebappException {
        ScanManagerLogResponse scanManagerLogResponse = new ScanManagerLogResponse();
        List<NameValuePair> nameValuePairs = new ArrayList<>();

        try {
            if (jobId.startsWith(PRE_JOB_ID_PREFIX)) {
                if (waitingScanLogs.containsKey(jobId)) {
                    scanManagerLogResponse.setLogs(waitingScanLogs.get(jobId));
                }
            } else {
                if (pageNumber != null) {
                    nameValuePairs.add(new BasicNameValuePair(PAGE_PARAM_NAME, pageNumber.toString()));
                }
                nameValuePairs.add(new BasicNameValuePair(JOB_ID_PARAM_NAME, jobId));
                ResponseEntity<String> responseEntity =
                        HTTPUtil.sendGET(ScanManagerWebappConfiguration.getInstance()
                                .getLogURL("", nameValuePairs).toString(), null, null);
                ObjectMapper mapper = new ObjectMapper();
                scanManagerLogResponse = mapper.readValue(responseEntity.getBody(), ScanManagerLogResponse.class);
            }
        } catch (IOException e) {
            throw new ScanManagerWebappException("Unable to get the logs", e);
        }
        return scanManagerLogResponse;
    }

    @Override
    public void insertError(Scan scan, Throwable e) {
        logger.error("An error occurred", e);

        if (scan != null) {
        Log log = new Log(scan, LogType.ERROR, new Timestamp(System.currentTimeMillis()), getFullErrorMessage(e));
            if (waitingScanLogs.containsKey(scan.getJobId())) {
                waitingScanLogs.get(scan.getJobId()).add(log);
            } else {
                waitingScanLogs.put(scan.getJobId(), new ArrayList<>(Arrays.asList(log)));
            }
        }
    }

    @Override
    public void insert(Scan scan, LogType type, String message) {
        insert(scan, type, new Timestamp(System.currentTimeMillis()), message);
    }

    @Override
    public void insert(Scan scan, LogType type, Timestamp timestamp, String message) {
        switch (type) {
            case ERROR:
                logger.error(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
        }

        if (scan != null) {
            Log log = new Log(scan, type, new Timestamp(System.currentTimeMillis()), message);
            if (waitingScanLogs.containsKey(scan.getJobId())) {
                waitingScanLogs.get(scan.getJobId()).add(log);
            } else {
                waitingScanLogs.put(scan.getJobId(), new ArrayList<>(Arrays.asList(log)));
            }
        }
    }

    @Override
    public boolean removeLogsForWaitingScan(String waitingScanJobId) {
        if (waitingScanLogs.containsKey(waitingScanJobId)) {
            waitingScanLogs.remove(waitingScanJobId);
            return true;
        } else {
            return false;
        }
    }

    private String getFullErrorMessage(Throwable e) {
        if (e.getCause() == null) {
            return e.getMessage();
        }
        return e.getMessage() + "\n\nCaused by: " + getFullErrorMessage(e.getCause());
    }
}
