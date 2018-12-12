#!/bin/bash

#Enter the file path of the original ticket body
echo "Enter ticket body file path :"
read path

sed -E ':a;N;$!ba;s/\r{0,1}\n/\\n/g' "$path" > announcement-formatted.json

while IFS='' read -r line || [[ -n "$line" ]]; do
    firstchar=`echo ${line} | cut -c 1`
    lastchar=`echo ${line} | rev | cut -c 1`

    chrlen=${#line}

    if  [ "$firstchar" != '"' ] 
    then
    	text=${line:0:0}'"'${line:0}
    fi

    if  [ "$lastchar" != '"' ] 
    then
    	line=${text:0:chrlen+1}'"'${text:chrlen+1}
    fi

    echo $line > announcement.json
    
done < "announcement-formatted.json"

rm announcement-formatted.json

echo "File created as 'announcement.json'"

