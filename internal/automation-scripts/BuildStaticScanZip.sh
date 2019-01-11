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

########################################################################
##   This script will set up the static environment(Veracode) for     ##
##   scanning the products which are in the HOME/products folder.     ##
########################################################################

STATIC_HOME="$HOME/env-static"
PRODUCT_HOME="$HOME/products"
SCRIPT_TAG="[SEC_AUTOMATION_BUILD_STATIC_ZIP]"

echo "$SCRIPT_TAG [START]"

#echo "$SCRIPT_TAG Calling WUM update process"
#bash $HOME/scripts/UpdateProducts.sh

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
                        	#Find and Copy all the js,jsp and jaggery files to scan artifact
                        	if [[ $pattern == *".js"* ]] || [[ $pattern == *".jag"* ]] ;then
                        		for file in $(find $STATIC_HOME/$product -name $pattern)
                        		do
                            			#Create same directory structure for each file
                            			dir=$(dirname "$file")
                            			base_path=$STATIC_HOME/$product
                            			suffix_of_dir="${dir//$base_path/}"
                            			target_dir=$STATIC_HOME/$product-work$suffix_of_dir
                            			mkdir -p $target_dir
                            			cp $file $target_dir
                        		done
                    		else
                        		find $STATIC_HOME/$product -name $pattern -exec cp {} $STATIC_HOME/$product-work \;
                    		fi
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
		zip -r -9 -q $product-scan.zip $product-work
		cd -

		echo "$SCRIPT_TAG Removing the ZIP source: $STATIC_HOME/$product-work"
		rm -rf $STATIC_HOME/$product-work

		echo "$SCRIPT_TAG Proceed with uploading: $STATIC_HOME/$product-scan.zip"

		VERACODE_APP_ID=-1
		if [[ $product == *"wso2am"* ]]; then
			# <app app_id="328371" app_name="wso2am-2.1.0" policy_updated_date="2017-12-21T08&#x3a;47&#x3a;33-05&#x3a;00"/>
			VERACODE_APP_ID=328371
		elif [[ $product == *"wso2is"* ]]; then
			# <app app_id="328089" app_name="wso2is-5.3.0" policy_updated_date="2017-12-21T06&#x3a;51&#x3a;37-05&#x3a;00"/>
			VERACODE_APP_ID=328089
		elif [[ $product == *"wso2iot"* ]]; then
			# <app app_id="392456" app_name="Application 06" policy_updated_date="2018-01-02T21&#x3a;37&#x3a;15-05&#x3a;00"/>
			# VERACODE_APP_ID=392456 #is master
			echo "$SCRIPT_TAG IOTS Disabled"
		elif [[ $product == *"wso2das"* ]]; then
			# <app app_id="218676" app_name="veracode-carbon-data" policy_updated_date="2017-12-21T05&#x3a;13&#x3a;44-05&#x3a;00"/>
			# VERACODE_APP_ID=218676 #carbon
			echo "$SCRIPT_TAG DAS Disabled"
		elif [[ $product == *"wso2ei"* ]]; then
			# <app app_id="328373" app_name="Application 03" policy_updated_date="2017-12-21T04&#x3a;38&#x3a;33-05&#x3a;00"/>
			VERACODE_APP_ID=328373
		else
			echo "$SCRIPT_TAG [ERROR] Unknown product, skipping configuration"
			rm -rf $STATIC_HOME/$product
			continue
			echo "$SCRIPT_TAG [ERROR] THIS SHOULD NOT PRINT"
		fi

		timestamp=$(date -d "today" +"%Y-%m-%d-%H.%M.%S")
		if [[ $VERACODE_APP_ID > -1 ]]; then
			echo "$SCRIPT_TAG Removing previous builds"
			for build_id in $(curl -q --compressed -u $(cat $HOME/scripts/config/VeracodeLogin.conf) https://analysiscenter.veracode.com/api/5.0/getbuildlist.do -F "app_id=$VERACODE_APP_ID" | grep build_id | cut -d "\"" -f2); do
				echo "$SCRIPT_TAG Removing build ID $build_id"
				curl -q --progress-bar --compressed -u $(cat $HOME/scripts/config/VeracodeLogin.conf) https://analysiscenter.veracode.com/api/5.0/deletebuild.do -F "app_id=$VERACODE_APP_ID"
			done
			echo "$SCRIPT_TAG Submitting veracode scan for $STATIC_HOME/$product-scan.zip with app ID: $VERACODE_APP_ID."
			curl -q --progress-bar --compressed -u $(cat $HOME/scripts/config/VeracodeLogin.conf) https://analysiscenter.veracode.com/api/5.0/createbuild.do -F "app_id=$VERACODE_APP_ID" -F "version=AUTO_SCAN_$timestamp"
			curl -q --progress-bar --compressed -u $(cat $HOME/scripts/config/VeracodeLogin.conf) https://analysiscenter.veracode.com/api/5.0/uploadfile.do -F "app_id=$VERACODE_APP_ID" -F "file=@$STATIC_HOME/$product-scan.zip"
			curl -q --progress-bar --compressed -u $(cat $HOME/scripts/config/VeracodeLogin.conf) https://analysiscenter.veracode.com/api/5.0/beginprescan.do -F "app_id=$VERACODE_APP_ID" -F "auto_scan=true" -F "scan_all_nonfatal_top_level_modules=true"
		fi
done

echo "$SCRIPT_TAG [END]"
