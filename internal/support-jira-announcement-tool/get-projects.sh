#!/bin/bash

#******************************************USAGE******************************************************
#This is the first script that you have to run for the process.
#It gets all the created projects from jira to a file called "output.json".
#Then it creates another file called "project" with the project ids and the corresponding project keys.
#Project file contains a format like this -> Project ID,Project key
#Replace the username and the password with your JIRA account credntials in the curl command.
#*****************************************************************************************************

curl -D- -u 'username:password' -X GET -H "Content-Type: application/json" https://WSO2_JIRA_DOMAIN/jira/rest/api/2/project/ -o output.json #curl command to get all projects and to create a file called output.json

#pretty print the content of output.json to all_projects.json file
cat output.json | python -m json.tool > all_projects.json

jq '.[] | .id' output.json > id #creates a file called id with all project ids

jq '.[] | .key' output.json > key #cerates a file called key with all project keys

jq '.[] | .projectCategory.name' output.json > project_status #cerates a file called project_status with the statuses


paste -d "," id key project_status > project #creates a file called project with id and key separated by a comma


