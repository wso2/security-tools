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

PRODUCT_HOME="$HOME/products"
SCRIPT_TAG="[SEC_AUTOMATION_UPDATE_PRODUCTS]"

echo "$SCRIPT_TAG [START]"

echo "$SCRIPT_TAG Cleaning product home: ($PRODUCT_HOME)"
rm -rf $PRODUCT_HOME
mkdir $PRODUCT_HOME

for product in $(cat $HOME/scripts/config/SupportedProductList.conf)
do
	echo "$SCRIPT_TAG Adding $product to WUM"
	wum add -yv $product
done

echo "$SCRIPT_TAG Starting WUM update process"
wum update -v

for product in $(cat $HOME/scripts/config/SupportedProductList.conf)
do
	IFS='-' read -r -a namesplits <<< "$product"

	echo "$SCRIPT_TAG Listing versions of $product available in WUM directory"
	ls -ltr $HOME/.wum-wso2/products/${namesplits[0]}/${namesplits[1]} | tr -s ' ' | cut -d ' ' -f9 | grep -v -e '^$' | paste -sd "," -

	echo "$SCRIPT_TAG Latest version of $product available in WUM directory"
	ls -ltr $HOME/.wum-wso2/products/${namesplits[0]}/${namesplits[1]} | tr -s ' ' | cut -d ' ' -f9 | grep -v -e '^$' | tail -1

	latestZip=$(ls -ltr $HOME/.wum-wso2/products/${namesplits[0]}/${namesplits[1]} | tr -s ' ' | cut -d ' ' -f9 | grep -v -e '^$' | tail -1)
	unzip -q $HOME/.wum-wso2/products/${namesplits[0]}/${namesplits[1]}/$latestZip -d $PRODUCT_HOME
	echo "$SCRIPT_TAG Extracted ${namesplits[0]}/${namesplits[1]}/$latestZip to $PRODUCT_HOME"
done

echo "$SCRIPT_TAG [END]"
