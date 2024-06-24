#!/bin/bash

# Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

TIMESPTAMP=$(date "+%Y.%m.%d-%H.%M.%S")
echo "Running JMSAppender remover for Log4j v1 on $(pwd)"
echo "WSO2 JMSAppender remover for Log4j v1" | tee -a "log4j_jms_remover_$TIMESPTAMP.log"
JAR_FILES=$(find . -name '*.jar')
for JAR_FILE in $JAR_FILES
do
    echo "[-] Checking: $JAR_FILE" >> "log4j_jms_remover_$TIMESPTAMP.log"
    CONTAINS_TARGET=$(unzip -l "$JAR_FILE" | grep "org/apache/log4j/net/JMSAppender.class")
    if [ -n "$CONTAINS_TARGET" ]; then
        echo "Found org/apache/log4j/net/JMSAppender in: $JAR_FILE" >> "log4j_jms_remover_$TIMESPTAMP.log"
        echo "Removing org/apache/log4j/net/JMSAppender from $JAR_FILE" | tee -a "log4j_jms_remover_$TIMESPTAMP.log"
        zip -d "$JAR_FILE" org/apache/log4j/net/JMSAppender.class >>  "log4j_jms_remover_$TIMESPTAMP.log" 2>&1
    fi
done
echo "Execution completed"
