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

##################################################################
##  This script will download the zip file of the products      ##
##  which are weekly released and unzip to common folder.       ##
##################################################################

PRODUCT_HOME="$HOME/products"
PRODUCT_ZIP_HOME="$HOME/productZips"
SCRIPT_TAG="[SEC_AUTOMATION_UPDATE_PRODUCTS]"
URL_PREFIX="https://api.github.com/repos/wso2/"
URL_SUFIX="releases?"

echo "$SCRIPT_TAG [START]"

echo "$SCRIPT_TAG Cleaning previous Product Downloads"
rm -rf $PRODUCT_ZIP_HOME
mkdir $PRODUCT_ZIP_HOME

echo "$SCRIPT_TAG Cleaning product home: ($PRODUCT_HOME)"
rm -rf $PRODUCT_HOME
mkdir $PRODUCT_HOME

for product in $(cat $HOME/scripts/config/WeeklyReleasedSupportedProductList.conf)
do
    
    echo "$SCRIPT_TAG Get the latest product url released for the $(cat $HOME/scripts/config/GitHubAppToken.conf) product"
    zipUrl=$(curl -s "$URL_PREFIX$product/$URL_SUFIX$(cat $HOME/scripts/config/GitHubAppToken.conf)" | grep "browser_download_url" | head -1 | cut -d "\"" -f 4)

    echo "$SCRIPT_TAG Download the latest product released for the $(cat $HOME/scripts/config/GitHubAppToken.conf) product"
    wget -q --show-progress $zipUrl -P $PRODUCT_ZIP_HOME

	latestZip=$(ls -ltr $PRODUCT_ZIP_HOME/*.zip | tr -s ' ' | cut -d ' ' -f9 | grep -v -e '^$' | tail -1)
	unzip -q $latestZip -d $PRODUCT_HOME
	echo "$SCRIPT_TAG Extracted ${namesplits[0]}/${namesplits[1]}/$latestZip to $PRODUCT_HOME"

done

echo "$SCRIPT_TAG [END]"