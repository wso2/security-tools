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

DEPENDENCY_HOME="$HOME/env-dependency-check"
PRODUCT_HOME="$HOME/products"
LOG_HOME="$HOME/outputs"
OUTPUT_HOME="$HOME/outputs"
TOOL_SCRIPT="$HOME/tools/dependency-check-3.0.1/bin/dependency-check.sh"
SCRIPT_TAG="[SEC_AUTOMATION_DEPENDENCY_CHECK]"

DEBUG=true

echo "$SCRIPT_TAG [START]"

# Downloading exclusion and hint files
rm -rf $HOME/tools/dependency-check-resources
echo "$SCRIPT_TAG Downloading hints.xml from security-tools repo"
wget -q --show-progress https://raw.githubusercontent.com/wso2/security-tools/master/external/dependency-check-resources/wso2-hints.xml -P ~/tools/dependency-check-resources
echo "$SCRIPT_TAG Downloading suppressions.xml from security-tools repo"
wget -q --show-progress https://raw.githubusercontent.com/wso2/security-tools/master/external/dependency-check-resources/wso2-suppressions.xml -P ~/tools/dependency-check-resources

echo "$SCRIPT_TAG Cleaning Dependency Check environment home: $DEPENDENCY_HOME"
rm -rf $DEPENDENCY_HOME

echo "$SCRIPT_TAG Calling WUM update process"
bash $HOME/scripts/UpdateProducts.sh

cp -R $PRODUCT_HOME $DEPENDENCY_HOME
echo "$SCRIPT_TAG Copied $PRODUCT_HOME/$product to $DEPENDENCY_HOME"

timestamp=$(date -d "today" +"%Y-%m-%d-%H.%M.%S")
for product in $(ls -l $DEPENDENCY_HOME | tr -s ' ' | cut -d ' ' -f9 |  grep -v -e '^$'); do
	date=$(date -d "today" +"%Y-%m-%d")
	if [ ! -d "$OUTPUT_HOME/$date" ]; then
		mkdir -p $OUTPUT_HOME/$date
		echo "$SCRIPT_TAG Created $OUTPUT_HOME/$date"
	fi
	if [ ! -d "mkdir $OUTPUT_HOME/$date/$product-$timestamp" ]; then
		mkdir -p $OUTPUT_HOME/$date/$product-$timestamp
		echo "$SCRIPT_TAG Created $LOG_HOME/$date/$product-$timestamp"
	fi

	if [ ! -d "mkdir $LOG_HOME/$date" ]; then
		mkdir -p $LOG_HOME/$date
		echo "$SCRIPT_TAG Created $LOG_HOME/$date"
	fi

	echo "$SCRIPT_TAG Starting Dependency Check for: $PRODUCT_HOME/$product"

	if [[ DEBUG == true ]]; then
		read -r -d '' TOOL_ARG <<- EOM
		-l $LOG_HOME/$date/$product-dependency-check-$timestamp.log
		-o $OUTPUT_HOME/$date/$product-$timestamp
		--project $product
		-s $PRODUCT_HOME/$product
		--suppression $HOME/tools/dependency-check-resources/wso2-suppressions.xml
		--hints $HOME/tools/dependency-check-resources/wso2-hints.xml
		-f ALL
		EOM
	else
		read -r -d '' TOOL_ARG <<- EOM
		-o $OUTPUT_HOME/$date/$product-$timestamp
		--project $product
		-s $PRODUCT_HOME/$product
		--suppression $HOME/tools/dependency-check-resources/wso2-suppressions.xml
		--hints $HOME/tools/dependency-check-resources/wso2-hints.xml
		-f ALL
		EOM
	fi

	$TOOL_SCRIPT $TOOL_ARG
	echo "$SCRIPT_TAG Ending Dependency Check for: $PRODUCT_HOME/$product"
done

echo "$SCRIPT_TAG [END]"
