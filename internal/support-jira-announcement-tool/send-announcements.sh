#!/bin/bash

#********************************************************************************USAGE****************************************************************************************
#This is the second script that you have to run for the process.
#It takes 5 user inputs.
#@summary - provide the summary field for the ticket
#@ticket - provide the path of the ticket body content file. Ticket file should be a .json file with the body content in JSON format. Replace all new line chrarcters with '\n'
#@proj_file - provide the path where you saved the project list file
#@skip_file - provide the path of the skip list file. Skip list file should contain a format like -> Project ID,Project key
#@log_file - provide the file path in which you want to create the log file
#Replace the username and the password with your JIRA account credntials in the curl command.
#*****************************************************************************************************************************************************************************

#to get the summary field of the ticket
echo "Enter summary :"
read summary

#to get the ticket body content
echo "Enter ticket body file path :"
read ticket

#to get the file path of the project list
echo "Enter project list file path :"
read proj_file

#to get the file path of the skip list
echo "Enter skip list file path :"
read skip_file

#to get the file path of where to create the log file
echo "Enter log file path :"
read log_file

#compares the skip list & the project list and makes a filtered list
comm --nocheck-order -3 $proj_file $skip_file > filtered

while IFS="," read -r id key remainder
do
	data="{\"fields\": {\"project\": {\"id\": "$id"},\"summary\": \"$summary\",\"description\": "`cat $ticket`",\"issuetype\": {\"name\": \"Announcement\"}}}"
	res=$(curl -u username:password -X POST --data "${data}" -H "Content-Type:application/json" https://support-staging.wso2.com/jira/rest/api/2/issue/) #to create an issue
	
	#Checks whether the given file exists, if available it appends the log details to it. If not it creates a new file and adds the log details. 
	#The log file will contain a format like -> Project ID | Response | Ticket number	
	if [ -e "$log_file" ]
	then 
		echo "$id | $res | `echo $res | jq '.key'`" >> "$log_file"
	else
		touch "$log_file"
		echo "$id | $res | `echo $res | jq '.key'`" > "$log_file"
	fi

done < "filtered"


