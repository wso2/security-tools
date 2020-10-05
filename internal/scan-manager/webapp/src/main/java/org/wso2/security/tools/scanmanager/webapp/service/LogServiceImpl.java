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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.wso2.security.tools.scanmanager.common.external.model.Log;
import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerLogResponse;
import org.wso2.security.tools.scanmanager.common.model.HTTPRequest;
import org.wso2.security.tools.scanmanager.common.model.LogType;
import org.wso2.security.tools.scanmanager.common.util.HTTPUtil;
import org.wso2.security.tools.scanmanager.webapp.config.ScanManagerWebappConfiguration;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.security.tools.scanmanager.webapp.util.Constants.JOB_ID_PARAM_NAME;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.PAGE_PARAM_NAME;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.PRE_JOB_ID_PREFIX;

/**
 * Log service implementation class.
 */
@Service
public class LogServiceImpl implements LogService {

    private static final Logger logger = LogManager.getLogger(LogServiceImpl.class);
    private Map<String, List<Log>> preparingScanLogs = new ConcurrentHashMap<>();

    @Override
    public ScanManagerLogResponse getLogs(String jobId, Integer pageNumber) throws ScanManagerWebappException {
        ScanManagerLogResponse scanManagerLogResponse = new ScanManagerLogResponse();
        List<NameValuePair> nameValuePairs = new ArrayList<>();

        try {
            if (jobId.startsWith(PRE_JOB_ID_PREFIX)) {
                if (preparingScanLogs.containsKey(jobId)) {
                    scanManagerLogResponse.setLogs(preparingScanLogs.get(jobId));
                }
            } else {
                if (pageNumber != null) {
                    nameValuePairs.add(new BasicNameValuePair(PAGE_PARAM_NAME, pageNumber.toString()));
                }
                nameValuePairs.add(new BasicNameValuePair(JOB_ID_PARAM_NAME, jobId));

                HTTPRequest getLogsRequest = new HTTPRequest(ScanManagerWebappConfiguration.getInstance()
                        .getLogURL("", nameValuePairs).toString(), null, null);
                ResponseEntity responseEntity = HTTPUtil.sendGET(getLogsRequest);
                if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
                    ObjectMapper mapper = new ObjectMapper();
                    Optional<Object> body = Optional.ofNullable(responseEntity.getBody());
                    if (body.isPresent()) {
                        scanManagerLogResponse = mapper.readValue(String.valueOf(body.get()),
                                ScanManagerLogResponse.class);
                    }

                } else {
                    throw new ScanManagerWebappException("Unable to get the scan logs for the job id: " + jobId);
                }
            }
        } catch (RestClientException | IOException e) {
            throw new ScanManagerWebappException("Unable to get the logs", e);
        }
        return scanManagerLogResponse;
    }

    @Override
    public void insertError(Scan scan, Throwable e) {
        logger.error("An error occurred", e);

        if (scan != null) {
            Log log = new Log(scan, LogType.ERROR, new Timestamp(System.currentTimeMillis()), getFullErrorMessage(e));
            if (preparingScanLogs.containsKey(scan.getJobId())) {
                preparingScanLogs.get(scan.getJobId()).add(log);
            } else {
                preparingScanLogs.put(scan.getJobId(), new ArrayList<>(Arrays.asList(log)));
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
            if (preparingScanLogs.containsKey(scan.getJobId())) {
                preparingScanLogs.get(scan.getJobId()).add(log);
            } else {
                preparingScanLogs.put(scan.getJobId(), new ArrayList<>(Arrays.asList(log)));
            }
        }
    }

    @Override
    public boolean removeLogsForPreparingScan(String preparingScanJobId) {
        if (preparingScanLogs.containsKey(preparingScanJobId)) {
            preparingScanLogs.remove(preparingScanJobId);
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
