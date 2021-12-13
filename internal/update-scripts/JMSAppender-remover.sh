#!/bin/bash
TIMESPTAMP=$(date "+%Y.%m.%d-%H.%M.%S")
echo "Running JMSAppender remover for Log4j v1 on $(pwd)"
echo "WSO2 JMSAppender remover for Log4j v1" | tee -a "log4j_jms_remover_$TIMESPTAMP.log"
JAR_FILES=$(find . -name '*.jar')
for JAR_FILE in $JAR_FILES
do
    echo "[-] Checking: $JAR_FILE" >> "log4j_jms_remover_$TIMESPTAMP.log"
    CONTAINS_JNDI_LOOKUP=$(unzip -l "$JAR_FILE" | grep "org/apache/log4j/net/JMSAppender.class")
    if [ -n "$CONTAINS_JNDI_LOOKUP" ]; then
        echo "Found org/apache/log4j/net/JMSAppender in: $JAR_FILE" >> "log4j_jms_remover_$TIMESPTAMP.log"
        echo "Removing org/apache/log4j/net/JMSAppender from $JAR_FILE" | tee -a "log4j_jms_remover_$TIMESPTAMP.log"
        zip -d "$JAR_FILE" org/apache/log4j/net/JMSAppender.class >>  "log4j_jms_remover_$TIMESPTAMP.log" 2>&1
    fi
done
