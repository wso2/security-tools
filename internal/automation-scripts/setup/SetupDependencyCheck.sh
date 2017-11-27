#!/bin/sh

# Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

TOOL_HOME="$HOME/tools"
TOOL_VERSION="3.0.2"
TOOL_NAME="dependency-check-$TOOL_VERSION"
TOOL_DOWNLOAD_URL_FOLDER="http://dl.bintray.com/jeremy-long/owasp"
TOOL_DOWNLOAD_URL_FILE="dependency-check-$TOOL_VERSION-release.zip"
SCRIPT_TAG="[SEC_AUTOMATION_UPDATE_PRODUCTS]"

echo "$SCRIPT_TAG [START]"

# Create tools folder if not present
if [ ! -d ${TOOL_HOME} ]; then
  mkdir ${TOOL_HOME}
  echo "$SCRIPT_TAG Created ${TOOL_HOME}"
fi

# Create backup folder if not present
if [ ! -d "$TOOL_HOME/backup" ]; then
  mkdir "$TOOL_HOME/backup"
  echo "$SCRIPT_TAG Created $TOOL_HOME/backup"
fi

# Backup and cleanup the Dependency Check home folder
if [ -d "$TOOL_HOME/$TOOL_NAME" ]; then
  timestamp=$(date -d "today" +"%Y-%m-%d-%H.%M.%S")
  mv "$TOOL_HOME/$TOOL_NAME" "$TOOL_HOME/backup/$TOOL_NAME-$timestamp"
  echo "$SCRIPT_TAG Moved existing version to $TOOL_HOME/backup/$TOOL_NAME-$timestamp"
fi
mkdir "$TOOL_HOME/$TOOL_NAME"

# Cleanup the temp folder, just in case previous entry was present
rm -rf /tmp/$TOOL_NAME
mkdir /tmp/$TOOL_NAME

# Download Dependency Check and extract into tool folder
echo "$SCRIPT_TAG Downloading Dependency Check to /tmp/$TOOL_NAME"
wget -q --show-progress $TOOL_DOWNLOAD_URL_FOLDER/$TOOL_DOWNLOAD_URL_FILE -P /tmp/$TOOL_NAME
unzip -q /tmp/$TOOL_NAME/$TOOL_DOWNLOAD_URL_FILE -d  $TOOL_HOME/$TOOL_NAME
SUB_FOLDER_NAME=$(ls -l $TOOL_HOME/$TOOL_NAME | tr -s ' ' | cut -d ' ' -f9 | grep -v -e '^$')
mv $TOOL_HOME/$TOOL_NAME/$SUB_FOLDER_NAME/* $TOOL_HOME/$TOOL_NAME
rm -rf $TOOL_HOME/$TOOL_NAME/$SUB_FOLDER_NAME
echo "$SCRIPT_TAG Extracted Dependency Check to $TOOL_HOME/$TOOL_NAME"

# Clean the temp folder
rm -rf /tmp/$TOOL_NAME
echo "$SCRIPT_TAG Removed /tmp/$TOOL_NAME"

# Applying custom modifications
echo "$SCRIPT_TAG Downloading Security-Tools repo to /tmp/$TOOL_NAME"
wget -q --show-progress https://github.com/wso2/security-tools/archive/master.zip -P /tmp/$TOOL_NAME
unzip -q /tmp/$TOOL_NAME/master.zip -d /tmp/$TOOL_NAME
cd /tmp/$TOOL_NAME/security-tools-master/external/dependency-check-core-$TOOL_VERSION
mvn clean install -DskipTests=true

rm $TOOL_HOME/$TOOL_NAME/repo/org/owasp/dependency-check-core/$TOOL_VERSION/dependency-check-core-$TOOL_VERSION.jar
cp /tmp/$TOOL_NAME/security-tools-master/external/dependency-check-core-$TOOL_VERSION/target/dependency-check-core-$TOOL_VERSION.jar $TOOL_HOME/$TOOL_NAME/repo/org/owasp/dependency-check-core/$TOOL_VERSION/
echo "$SCRIPT_TAG Replaced modification in $TOOL_HOME/$TOOL_NAME/repo/org/owasp/dependency-check-core/$TOOL_VERSION/dependency-check-core-$TOOL_VERSION.jar"

# Clean the temp folder
rm -rf /tmp/$TOOL_NAME
echo "$SCRIPT_TAG Removed /tmp/$TOOL_NAME"

echo "$SCRIPT_TAG [END]"
