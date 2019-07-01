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
package org.wso2.security.tools.scanmanager.scanners.common.logging;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.message.Message;
import org.wso2.security.tools.scanmanager.scanners.common.model.CallbackLog;
import org.wso2.security.tools.scanmanager.scanners.common.util.CallbackUtil;

/**
 * Log appender to persist the logs in the scan manager.
 */
@Plugin(category = "Core", name = "CallbackLogAppender")
public class CallbackLogAppender extends AbstractAppender {

    private CallbackLogAppender(String name) {
        super(name, null, null);
    }

    @PluginFactory
    public static CallbackLogAppender createAppender(@PluginAttribute("name") String name) {
        return new CallbackLogAppender(name);
    }

    @Override
    public void append(LogEvent event) {
        Message message = event.getMessage();
        event.getLevel();
        if (message instanceof CallbackLog) {
            CallbackLog callbackLog = (CallbackLog) event.getMessage();

            CallbackUtil.persistScanLog(callbackLog.getJobId(), callbackLog.getMessage(), event.getLevel());
        }
    }
}
