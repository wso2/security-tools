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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.wso2.security.tools.scanmanager.common.external.model.ScanManagerLogResponse;
import org.wso2.security.tools.scanmanager.webapp.config.ScanManagerWebappConfiguration;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;
import org.wso2.security.tools.scanmanager.webapp.util.HTTPUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.security.tools.scanmanager.webapp.util.Constants.JOB_ID_PARAM_NAME;
import static org.wso2.security.tools.scanmanager.webapp.util.Constants.PAGE_PARAM_NAME;

/**
 *  Log service implementation class.
 */
@Service
public class LogServiceImpl implements LogService {

    @Override
    public ScanManagerLogResponse getLogs(String jobId, Integer pageNumber) throws ScanManagerWebappException {
        ScanManagerLogResponse scanManagerLogResponse = null;
        List<NameValuePair> nameValuePairs = new ArrayList<>();

        try {
            if (pageNumber != null) {
                nameValuePairs.add(new BasicNameValuePair(PAGE_PARAM_NAME, pageNumber.toString()));
            }
            nameValuePairs.add(new BasicNameValuePair(JOB_ID_PARAM_NAME, jobId));
            ResponseEntity<String> responseEntity =
                    HTTPUtil.sendGET(ScanManagerWebappConfiguration.getInstance()
                            .getLogURL("", nameValuePairs).toString(), null, null);
            ObjectMapper mapper = new ObjectMapper();
            scanManagerLogResponse = mapper.readValue(responseEntity.getBody(), ScanManagerLogResponse.class);
        } catch (IOException e) {
            throw new ScanManagerWebappException("Unable to get the logs", e);
        }
        return scanManagerLogResponse;
    }
}
