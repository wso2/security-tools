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

HOME="/backups"
now="$(date +'%d_%m_%Y_%H')"
fileName="db_backup_$now".gz
backupFolder="$HOME/database_backup"
fullPathBackupFile="$backupFolder/$fileName"
logfile="$backupFolder/backup_log_$(date +'%Y_%m').txt"
vmsBackupTag="[VMS Backup Process]"

mysqlPassword=$(cat $HOME/scripts/config/mysqlPassword.conf)
mysqlUsername=$(cat $HOME/scripts/config/mysqlUsername.conf)
databaseName=$(cat $HOME/scripts/config/databaseName.conf)

echo $vmsBackupTag "$(date +'%d-%m-%Y %H:%M:%S') : Starting mysqldump" >> $logfile
mysqldump --user=$mysqlUsername --password=$mysqlpassword --default-character-set=utf8 $databaseName | gzip > $fullPathBackupFile
echo $vmsBackupTag "$(date +'%d-%m-%Y %H:%M:%S') : Finishing mysqldump" >> $logfile

find $backupFolder -name db_backup_* -mmin +$((60*1)) -exec rm {} \;
echo $vmsBackupTag "$(date +'%d-%m-%Y %H:%M:%S') : Deleted old backup dump files" >> $logfile

echo $vmsBackupTag "$(date +'%d-%m-%Y %H:%M:%S') : Call uploading backup script to google drive" >> $logfile
bash UploadDbBackupToDrive.sh $backupFolder $logfile $HOME $fileName
