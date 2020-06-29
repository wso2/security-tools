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

package org.wso2.security.tools.scanmanager.core.handler;

import org.wso2.security.tools.scanmanager.common.external.model.Scan;
import org.wso2.security.tools.scanmanager.core.exception.ScanManagerException;

/**
 * This class provides an interface to handle notification upon scan job completion related operations.
 */
public interface NotificationHandler {

    public void sendNotification(Scan scan, String toAddress) throws ScanManagerException;
}
