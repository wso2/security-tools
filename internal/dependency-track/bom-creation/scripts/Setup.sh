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

##########################################################################
##  This script will used to create a environment to generate BOM file  ##
##  If you want to generate BOM file for the first time, this script 	##
##  needs to be executed.		      									##
##                                                                      ##
##  This script executes following sub tasks		      				##
##       01. Install customized CycloneDx Maven Plugin into local M2    ##
##           repository.                                                ##
##       02. Add m2 settings.xml file to configure WSO2 custom maven    ##
##           repositories.                                              ##
##########################################################################

SCRIPT_TAG="[BUILD_BOM_CREATION_ENV]"
BOM_CREATION_ENV_HOME="$HOME/env-bomCreation"

echo "$SCRIPT_TAG [START]"
echo "$SCRIPT_TAG Create directory to download resources to set up the environment"
mkdir $BOM_CREATION_ENV_HOME
cd $BOM_CREATION_ENV_HOME

# Download settings.xml
echo "$SCRIPT_TAG Download setting.xml file for local m2 repository"
wget https://github.com/wso2/security-tools/blob/master/internal/dependency-track/bom-creation/resources/settings.xml

# Add settings.xml in local .m2 repository
echo "$SCRIPT_TAG Add setting.xml file for local m2 repository"
cp $BOM_CREATION_ENV_HOME/settings.xml .m2/

#-------------------------------------------------------------------------
# Cyclone Dx is used to create a BOM file. By default, Cyclone Dx includes
# transitive dependencies of a project during BOM creation. Cyclone Dx
# is modified to exclude transitive dependency.
# Following lines used to install the modified CycloneDx maven plugin into
# local m2 repository
#-------------------------------------------------------------------------

echo "$SCRIPT_TAG Install CycloneDx maven plugin"
# Download cyclonedx-maven-plugin-1.4.1-INTERNAL.jar
wget https://github.com/wso2/security-tools/blob/master/external/cyclonedx-maven-plugin/distribution/cyclonedx-maven-plugin-1.4.1-INTERNAL.jar

# Download parent pom file of cyclonedx-maven-plugin-1.4.1-INTERNAL
wget https://github.com/wso2/security-tools/blob/master/external/cyclonedx-maven-plugin/cyclonedx-maven-plugin-1.4.1/pom.xml

# Install cyclonedx-maven-plugin-1.4.1-INTERNAL.jar into local m2 repository
mvn install:install-file -Dfile=$BOM_CREATION_ENV_HOME/cyclonedx-maven-plugin-1.4.1-INTERNAL.jar -DgroupId=org.cyclonedx -DartifactId=cyclonedx-maven-plugin -Dversion=1.4.1-INTERNAL -Dpackaging=maven-plugin -DpomFile=$BOM_CREATION_ENV_HOME/pom.xml

echo "$SCRIPT_TAG Successfully installed customized cyclonedx-maven-plugin into local M2 repository"

# Remove resources used to set up the environment.
rm -rf $BOM_CREATION_ENV_HOME
echo "$SCRIPT_TAG [END]"
