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
##   This script will generate a master BOM file for given    ##
##   product pack or jar.                                     ##
##   BOM Environment should be created using Setup.sh script  ##
##   before executing this script.                            ##
################################################################

SCRIPT_TAG="[DEPENDENCY_TRACK_GENERATE_BOM_FILE]"

echo "$SCRIPT_TAG [START]"

read -p "Enter file path to generate bom file (Eg: Product pack zip file) : " file_path
read -p "Enter output directory to create BOM file: " output_dir

temp_dir=$(mktemp -d)
cp -R $file_path $temp_dir
cd $temp_dir

# --------------------------------------
# Unzip given file
# --------------------------------------

echo "$SCRIPT_TAG Unzip given file"

file_name=$(ls )
mkdir working_dir
unzip -qq $temp_dir/$file_name -d $temp_dir/working_dir

echo "$SCRIPT_TAG given file is successfully extracted"

cd $temp_dir/working_dir
src_file_name=$(ls )
cd $temp_dir/working_dir/$src_file_name

echo "$SCRIPT_TAG Unzip *.war and *.jar files"

# Unzip war files
for WAR_FILE in $(find . -name *.war)
do
	jar -xf $WAR_FILE
	echo "$SCRIPT_TAG $WAR_FILE is extracted"
done

# Unzip jar files
for JAR_FILE in $(find . -name *.jar)
do
	jar -xf $JAR_FILE
	echo "$SCRIPT_TAG $JAR_FILE is extracted"
done

# cyclonedx-maven-plugin:1.4.1-INTERNAL created by making customization to exclude transitive
# dependencies of third party dependency while executing cyclonedx plugin to generate bom file.
# Following command is used to execute bom file creation using modified cyclonedx plugin.
# This command will traverse all the pom.xml files of given file and execute within that directory.
echo "$SCRIPT_TAG $JAR_FILE create BOM files for each pom.xml files"
find . -name pom.xml -execdir mvn -T 4 org.cyclonedx:cyclonedx-maven-plugin:1.4.1-INTERNAL:makeAggregateBom \;

# Aggregate all bom file to master bom file
echo "$SCRIPT_TAG $JAR_FILE aggregate created bom files and create master bom file"
find -name "bom.xml" >> bomFileList.txt
head -n 3 $(head -n 1 bomFileList.txt) >> $output_dir/bom.xml
for file in $(cat $temp_dir/working_dir/$src_file_name/bomFileList.txt)
do 
	echo -------------------------------------------------------------
	echo $file
	# Retrieve all components in bom.xml
	sed -n '/<component /,/<\/component/p' $file >> $output_dir/bom.xml
done
tail -n 2 $(head -n 1 bomFileList.txt) >> $output_dir/bom.xml

echo "$SCRIPT_TAG BOM file is successfully generated in $output_dir"
echo "$SCRIPT_TAG [END]"
