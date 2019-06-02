#!/usr/bin/env bash

# Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

# ---------------------------------------------------------------
# --------------- Startup Script for Scan Manager ---------------
# ---------------------------------------------------------------

PARENT_PATH=$(cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P)
cd ${PARENT_PATH}

echo "Building Scan Manager"

cd ../
mvn clean install

echo "Building Scanner Images"
docker build --no-cache -f scanners/veracode/Dockerfile -t veracode .

echo "Starting Scan Manager"

cd core
nohup mvn tomcat7:run &
cd -

cd webapp
nohup java -jar target/scan-manager-webapp.war &
cd -
