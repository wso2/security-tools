
#!/bin/bash

accountype="HOSTED"
backupfolder="$HOME/script/database_backup"

now="$(date +'%d_%m_%Y_%H')"
file="db_backup_$now".gz

client_id=$(cat $HOME/script/config/googleClientId.conf)
client_secret=$(cat $HOME/script/config/googleClientSecret.conf)
refresh_token=$(cat $HOME/script/config/googleRefreshToken.conf)
grant_type=refresh_token

logfile="$backupfolder/"drive_upload_backup_log_"$(date +'%Y_%m')".txt
echo "Called the token endpoint to get a nes access token at $(date +'%d-%m-%Y %H:%M:%S')" >> "$logfile"


token=$(curl --request POST --data "$client_id&$client_secret&$refresh_token&grant_type=$grant_type" https://accounts.google.com/o/oauth2/token | sed -n '/ *"access_token" *: *"/ { s///; s/".*//; p; }')

echo "access token generation was successfull...." >> "$logfile"

echo "Uploading to drive started at $(date +'%d-%m-%Y %H:%M:%S')" >> "$logfile"

curl -X POST -L -H "Authorization: Bearer $token" -F "metadata={name : '$file'};type=application/json;charset=UTF-8" F "file=$file;type=application/zip" "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"

echo "Uploading to drive finished at $(date +'%d-%m-%Y %H:%M:%S')" >> "$logfile"

echo "*****************************" >> "$logfile"

