#!/bin/sh

now="$(date +'%d_%m_%Y_%H')"
filename="db_backup_$now".gz
backupfolder="$HOME/database_backup"
fullpathbackupfile="$backupfolder/$filename"
logfile="$backupfolder/backup_log_$(date +'%Y_%m').txt"

mysqlpassword=$(cat $HOME/script/config/mysqlPassword.conf)

echo "mysqldump started at $(date +'%d-%m-%Y %H:%M:%S')" >> "$logfile"
mysqldump --user=mysqlusername --password=mysqlpassword --default-character-set=utf8 DefectDojoPR | gzip > "$fullpathbackupfile"
echo "mysqldump finished at $(date +'%d-%m-%Y %H:%M:%S')" >> "$logfile"

find "$backupfolder" -name db_backup_* -mtime +8 -exec rm {} \;
echo "Deleted old files" >> "$logfile"

echo "Operation finished at $(date +'%d-%m-%Y %H:%M:%S')" >> "$logfile"
echo " " >> "$logfile"
echo " " >> "$logfile"

