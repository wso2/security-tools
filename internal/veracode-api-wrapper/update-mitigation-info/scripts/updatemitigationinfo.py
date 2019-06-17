#!/usr/bin/env python2

#
# Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

import argparse
import csv
import requests
import re
import sys

GET_BUILD_API = 'https://analysiscenter.veracode.com/api/5.0/getbuildlist.do'
UPDATE_MITIGATION_API = 'https://analysiscenter.veracode.com/api/updatemitigationinfo.do'
BUILD_REGEX = '(build build_id=\")(\d+)'
SUCCESS_RESPONSE_KEY = 'desc='
PREFIX = '[VERACODE API WRAPPER] '
ACTION_FALSEPOSITIVE = 'fp'
ACTION_APPDESIGN = 'appdesign'

print PREFIX, '- START'
parser = argparse.ArgumentParser(description='Veracode API wrapper to update mitigation comment.')

group = parser.add_mutually_exclusive_group(required=True)

group.add_argument('-a', action='store', dest='applicationID',
                    help='Veracode Application ID')

group.add_argument('-b', action='store', dest='buildID',
                    help='Build ID of the scan')

required = parser.add_argument_group('Required arguments')

required.add_argument('-u', type=str, action='store', dest='username', required=True,
                    help='Username')

required.add_argument('-cf', type=str, action='store', dest='csvFilePath', required=True,
                    help='CSV file path of feed back report')

required.add_argument('-of', type=str, action='store', dest='outputFilePath', required=True,
                    help='Output file path')

args = parser.parse_args()

password = raw_input("Enter password: ")

if (args.applicationID == None and args.buildID == None):
    print PREFIX, '-    Either Application ID or Build ID should be provided.'
    sys.exit()

# Get build id of particular application if build id is not provided.
if (args.buildID == None):
    print PREFIX, '- Retrieving the build ID of application ID : ', args.applicationID
    payload = {'app_id': args.applicationID}
    response = requests.post(GET_BUILD_API, params=payload, files=None, auth=(args.username, password))
    if response.status_code == 200:
        matchObj = re.search(BUILD_REGEX, response.text, re.M | re.I)
        if matchObj:
            args.buildID = matchObj.group(2)
            print PREFIX, "- Build ID of given application : " + args.buildID
        else:
            print PREFIX, "- Error occurred while retrieving build ID of given application."
            sys.exit()
    else:
        print PREFIX, "- Error occurred while invoking the API to retrieve build ID of given application. " \
                      "Response code :", response.status_code
        sys.exit()


# Update mitigation comment.
def updateComment(flowId, comment, action):
    updateCommentPayload = {'build_id': args.buildID, 'action': action, 'comment': comment, 'flaw_id_list': flowId}
    response = requests.post(UPDATE_MITIGATION_API, params=updateCommentPayload, files=None,
                             auth=(args.username, password))
    if response.status_code == 200:
        if SUCCESS_RESPONSE_KEY in response.text:
            output = "Flow ID : " + flowId + " mitigation info is updated success fully. "
        else:
            output = "Flow ID : " + flowId + "ERROR Response : " + response.text
    else:
        output = "Flow ID : " + flowId + "ERROR Response : " + response.text
    return output

print PREFIX, "- Reading the CSV entries from analysed report"

# Read CSV file and call api to update mitigation comment.
with open(args.csvFilePath, 'r') as csvfile:
    reader = csv.DictReader(csvfile)
    with open(args.outputFilePath, 'a') as outputFile:
        for row in reader:
            flowId = dict(row).get("issue_id")
            print PREFIX, "- Updating mitigation info for flow ID " + flowId
            if (dict(row).get("WSO2_resolution").lower() == 'False Positive'.lower()):
                action = ACTION_FALSEPOSITIVE
            else:
                action = ACTION_APPDESIGN
            comment = "Resolution: " + dict(row).get("WSO2_resolution") + '\n\n' + \
                      "Use case: " + dict(row).get("Use_Case") + '\n\n' + \
                      "Vulnerability Influence: " + dict(row).get("Vulnerability_Influence")
            output = updateComment(flowId, comment, action)
            outputFile.write('\n')
            outputFile.write(output)
            outputFile.write('\n')
            outputFile.write(comment)
            outputFile.write('\n')
            outputFile.write("--------------------------------------------------------------------------------------")
            outputFile.write('\n')
    print PREFIX, "- END"
