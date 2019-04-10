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

now="$(date +'%d_%m_%Y_%H')"
filename="db_backup_$now".gz
backupfolder="$HOME/database_backup"
fullpathbackupfile="$backupfolder/$filename"
logfile="$backupfolder/backup_log_$(date +'%Y_%m').txt"
vmsbackuptag="[VMS Backup Process]"

mysqlpassword=$(cat $HOME/scripts/config/MysqlPassword.conf)
mysqlusername=$(cat $HOME/scripts/config/MysqlUsername.conf)

echo $vmsbackuptag "$(date +'%d-%m-%Y %H:%M:%S') : Starting mysqldump" >> $logfile
mysqldump --user=$mysqlusername --password=$mysqlpassword --default-character-set=utf8 DefectDojoPR | gzip > $fullpathbackupfile
echo $vmsbackuptag "$(date +'%d-%m-%Y %H:%M:%S') : Fnishing mysqldump" >> $logfile

find $backupfolder -name db_backup_* -mmin +$((60*1)) -exec rm {} \;
echo $vmsbackuptag "$(date +'%d-%m-%Y %H:%M:%S') : Deleted old backup dump files" >> $logfile

echo $vmsbackuptag "$(date +'%d-%m-%Y %H:%M:%S') : Call uploading backup script to google drive" >> $logfile
bash UploadDbBackupToDrive.sh $backupfolder $fullpathbackupfile $logfile


