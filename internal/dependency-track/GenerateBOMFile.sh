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

read -p "Enter URL to download source ZIP file: " download_url
read -p "Enter output folder name : " output_dir

temp_dir=$(mktemp -d)
cd $temp_dir
wget $download_url

# --------------------------------------
# Unzip source
# --------------------------------------

zip_file_name=$(ls )
mkdir src
unzip $temp_dir/$zip_file_name -d $temp_dir/src

# -------------------
# Generate BOM file
# -------------------

cd $temp_dir/src
src_file_name=$(ls )
cd $temp_dir/src/$src_file_name
mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
echo "BOM file is successfully generate."

# -------------------------------------------------
# Move generated BOM file to given output directory
# -------------------------------------------------

mv $temp_dir/src/$src_file_name/target/bom.xml $output_dir
echo "BOM file is moved to given output folder path : "$output_dir

# ------------------------
# Delete Cloned Repository
# ------------------------

rm -rf $temp_dir

echo "Downloaded assets are deleted."

exit
