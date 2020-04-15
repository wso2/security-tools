/*
 *
 *   Copyright (c) 2020, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.security.tools.scanmanager.scanners.qualys.model;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Class to represent the WebAppAuth method for a scan.
 */
public interface WebAppAuth {

    /**
     * Build request body to add authentication method.
     * @param appID application ID
     * @return request body in XML format
     * @throws TransformerException error occurred while building secure string writer
     * @throws ParserConfigurationException error occurred while parsing
     * @throws IOException error occurred while performing any file operations
     */
    String buildAuthRequestBody(String appID) throws TransformerException, ParserConfigurationException, IOException;

}
