#!/usr/bin/env bash

# Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


################################################################
##   This script will generate a BOM file for given product   ##
################################################################

# ------------------------------------
# Download source code
# ------------------------------------

read -p "Enter git source code download url to create BOM File : " download_url

mkdir $HOME/workingDir
cd $HOME/workingDir
wget $download_url

# --------------------------------------
# Unzip source
# --------------------------------------

zip_file_name=$(ls )
mkdir sourceDir
unzip $HOME/workingDir/$zip_file_name -d $HOME/workingDir/sourceDir
rm -rf $HOME/workingDir/$zip_file_name

# -------------------
# Generate BOM file
# -------------------
cd $HOME/workingDir/sourceDir
file_name=$(ls )
cd $HOME/workingDir/sourceDir/$file_name
mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom

echo "BOM file is successfully generated in " $($HOME/workingDir/sourceDir/$file_name/target)
echo "Please upload the BOM file to Dependency Track."

nautilus $HOME/workingDir/sourceDir/$file_name/target

sleep 60
read -n 1 -r -s -p "If you have uploaded BOM file to Dependency Track successfully, Press any key to continue..."

# ------------------------
# Delete Cloned Repository
# ------------------------

rm -rf $HOME/workingDir/

echo "Cloned repository is successfully deleted."

exit
