#!/bin/bash

#******************************************USAGE******************************************************
#This is the first script that you have to run for the process.
#It gets all the created projects from jira to a file called "output.json".
#Then it creates another file called "project" with the project ids and the corresponding project keys.
#Project file contains a format like this -> Project ID,Project key,Project description
#Replace the username and the password with your JIRA account credntials in the curl command.
#*****************************************************************************************************

curl -D- -u 'username:password' -X GET -H "Content-Type: application/json" https://WSO2_JIRA_DOMAIN/jira/rest/api/2/project/ -o output.json #curl command to get all projects and to create a file called output.json

#pretty print the content of output.json to all_projects.json file
cat output.json | python -m json.tool > all_projects.json

count=`jq length ./all_projects.json`

for (( c=0; c< $count ; c++ ))
do  
   jq '.['$c'] | .id' all_projects.json >> id #creates a file called id with all project ids

   jq '.['$c'] | .key' all_projects.json >> key #creates a file called key with all project keys

   jq '.['$c'] | .projectCategory.name' all_projects.json >> project_status #creates a file called project_status with the statuses

done

paste -d "," id key project_status > project #creates a file called project with id, key and project_status separated by a comma

