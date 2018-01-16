#!/bin/bash

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

#######################################################################
##   This script will run the dynamic(Qualys) and static(Veracode)   ##
##         scans for weekly released products in every week.         ##
#######################################################################

LOG_HOME="$HOME/outputs"

date=$(date -d "today" +"%Y-%m-%d")
if [ ! -d "$LOG_HOME/$date" ]; then
	mkdir -p $LOG_HOME/$date
fi

timestamp=$(date -d "today" +"%Y-%m-%d-%H.%M.%S")

echo "$SCRIPT_TAG Calling Github product download process"
bash $HOME/scripts/DownloadProductsZip.sh 2>&1 | tee -a $LOG_HOME/$date/product-zip-download-$timestamp.log

echo "$SCRIPT_TAG Ending Github product download process"
bash $HOME/scripts/BuildDynamicScanEnv.sh 2>&1 | tee -a $LOG_HOME/$date/weekly-scan-dynamic-$timestamp.log
bash $HOME/scripts/BuildStaticScanZip.sh 2>&1 | tee -a $LOG_HOME/$date/weekly-scan-static-$timestamp.log
