#!/bin/bash

# Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

backupFolder=$1
logfile=$2
HOME=$3
fileName=$4
vmsBackupTag="[VMS Backup Process]"

echo "backupFolder $backupFolder"
echo "file $fileName"

client_id=$(cat $HOME/scripts/config/googleClientId.conf)
client_secret=$(cat $HOME/scripts/config/googleClientSecret.conf)
refresh_token=$(cat $HOME/scripts/config/googleRefreshToken.conf)
googleDriveFolderId=$(cat $HOME/scripts/config/googleDriveFolderId.conf)
grant_type=refresh_token

echo $vmsBackupTag "$(date +'%d-%m-%Y %H:%M:%S') : Calling the token endpoint to get a access token" >> "$logfile"
token=$(curl --request POST --data "client_id=$client_id&client_secret=$client_secret&refresh_token=$refresh_token&grant_type=$grant_type" https://accounts.google.com/o/oauth2/token | sed -n '/ *"access_token" *: *"/ { s///; s/".*//; p; }')
echo $vmsBackupTag "$(date +'%d-%m-%Y %H:%M:%S') : Access token generation was successful" >> "$logfile"

echo $token

echo $vmsBackupTag "$(date +'%d-%m-%Y %H:%M:%S') : Uploading to drive started" >> "$logfile"
curl -X POST -L -H "Authorization: Bearer $token" -F "metadata={name : '$fileName', parents : ['$googleDriveFolderId']};type=application/json;charset=UTF-8" F "file=$backupFolder/$fileName;type=application/zip" "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
echo $vmsBackupTag "$(date +'%d-%m-%Y %H:%M:%S') : Uploading to drive finished" >> "$logfile"

echo $vmsBackupTag "$(date +'%d-%m-%Y %H:%M:%S') : Operation is finished" >> "$logfile"
echo " " >> "$logfile"
