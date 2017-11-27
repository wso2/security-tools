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
STATIC_HOME="$HOME/env-static"
PRODUCT_HOME="$HOME/products"
SCRIPT_TAG="[SEC_AUTOMATION_BUILD_STSTIC_ZIP]"

echo "$SCRIPT_TAG [START]"

echo "$SCRIPT_TAG Calling WUM update process"
bash $HOME/scripts/UpdateProducts.sh

echo "$SCRIPT_TAG Cleaning static environment home: $STATIC_HOME"
rm -rf $STATIC_HOME

cp -R $PRODUCT_HOME $STATIC_HOME
echo "$SCRIPT_TAG Copied $PRODUCT_HOME to $STATIC_HOME"

for product in $(cat $HOME/scripts/config/SupportedProductList.conf)
do
		echo "$SCRIPT_TAG Moving product to work folder: $STATIC_HOME/$product-work"
		mkdir $STATIC_HOME/$product-work

		for pattern in $(cat $HOME/scripts/config/StaticScanIncludeFileNamePatterns.conf)
		do
			echo "$SCRIPT_TAG Processing pattern: $pattern"
			if [[ $pattern == "regex/"* ]]; then
				actual_pattern=$(echo "$pattern" | cut -d'/' -f 2)
				echo "$SCRIPT_TAG Copying files that match the pattern: $actual_pattern"
				find $STATIC_HOME/$product -regex $actual_pattern -exec cp {} $STATIC_HOME/$product-work \;
			else
				echo "$SCRIPT_TAG Copying files that match the pattern: $pattern"
				find $STATIC_HOME/$product -name $pattern -exec cp {} $STATIC_HOME/$product-work \;
			fi
		done

		for filename in $(cat $HOME/scripts/config/StaticScanExcludeFileNames.conf)
		do
			echo "$SCRIPT_TAG Excluding $STATIC_HOME/$product-work/$filename"
			rm $STATIC_HOME/$product-work/$filename
		done

		echo "$SCRIPT_TAG Removing product folder: $STATIC_HOME/$product"
		rm -rf $STATIC_HOME/$product

		echo "$SCRIPT_TAG Creating the ZIP of scan artifacts"
		cd $STATIC_HOME
		zip -j -r -9 -q $product-scan.zip $product-work
		cd -

		echo "$SCRIPT_TAG Removing the ZIP source: $STATIC_HOME/$product-work"
		rm -rf $STATIC_HOME/$product-work

		echo "$SCRIPT_TAG Proceed with uploading: $STATIC_HOME/$product-scan.zip"

		VERACODE_APP_ID=-1
		if [[ $product == *"wso2am"* ]]; then
			VERACODE_APP_ID=328371
		elif [[ $product == *"wso2is"* ]]; then
			VERACODE_APP_ID=328089
		elif [[ $product == *"wso2iot"* ]]; then
			VERACODE_APP_ID=217912 #is master
		elif [[ $product == *"wso2das"* ]]; then
			VERACODE_APP_ID=218676 #carbon
		elif [[ $product == *"wso2ei"* ]]; then
			VERACODE_APP_ID=328373
		else
			echo "$SCRIPT_TAG [ERROR] Unknown product, skipping configuration"
			rm -rf $STATIC_HOME/$product
			continue
			echo "$SCRIPT_TAG [ERROR] THIS SHOULD NOT PRINT"
		fi

		if [[ $VERACODE_APP_ID > -1 ]]; then
			echo "$SCRIPT_TAG Submitting veracode scan for $STATIC_HOME/$product-scan.zip with app ID: $VERACODE_APP_ID."
			curl -q --progress-bar --compressed -u $(cat $HOME/scripts/config/VeracodeLogin.conf) https://analysiscenter.veracode.com/api/5.0/uploadfile.do -F "app_id=$VERACODE_APP_ID" -F "file=@$STATIC_HOME/$product-scan.zip"
			curl -q --progress-bar --compressed -u $(cat $HOME/scripts/config/VeracodeLogin.conf) https://analysiscenter.veracode.com/api/5.0/beginprescan.do -F "app_id=$VERACODE_APP_ID" -F "auto_scan=true"
		fi
done

echo "$SCRIPT_TAG [END]"
