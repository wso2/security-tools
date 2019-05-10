#  /*
#  *  Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#  *
#  *  WSO2 Inc. licenses this file to you under the Apache License,
#  *  Version 2.0 (the "License"); you may not use this file except
#  *  in compliance with the License.
#  *  You may obtain a copy of the License at
#  *
#  *    http://www.apache.org/licenses/LICENSE-2.0
#  *
#  * Unless required by applicable law or agreed to in writing,
#  * software distributed under the License is distributed on an
#  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  * KIND, either express or implied.  See the License for the
#  * specific language governing permissions and limitations
#  * under the License.
#  */

import requests
import re
import csv
import argparse

GET_BUILD_API = 'https://analysiscenter.veracode.com/api/5.0/getbuildlist.do'
UPDATE_MITIGATION_API = 'https://analysiscenter.veracode.com/api/updatemitigationinfo.do'
BUILD_REGEX = '(build build_id=\")(\d+)'
SUCCESS_RESPONSE_KEY = 'desc='
PREFIX = '[VERACODE API WRAPPER] '
ACTION_FALSEPOSITIVE = 'fp'
ACTION_APPDESIGN = 'appdesign'

print PREFIX, '- START'
parser = argparse.ArgumentParser(description='Veracode API wrapper to update mitigation comment.')
parser.add_argument('-a', action='store', dest='applicationID',
                    help='Veracode Application ID')

parser.add_argument('-b', action='store', dest='buildID',
                    help='Build ID of the scan')

parser.add_argument('-u', type=str, action='store', dest='username',
                    help='Username')

parser.add_argument('-cf', type=str, action='store', dest='csvFilePath',
                    help='CSV file path of feed back report')

parser.add_argument('-of', type=str, action='store', dest='outputFilePath',
                    help='Output file path')

args = parser.parse_args()

password = raw_input("Enter password: ")

# Get build id of particular application if build id is not provided.
if (args.buildID == None):
    print PREFIX, '- Retrieving the build id of application id : ', args.applicationID
    payload = {'app_id': args.applicationID}
    response = requests.post(GET_BUILD_API, params=payload, files=None, auth=(args.username, password))
    matchObj = re.search(BUILD_REGEX, response.text, re.M | re.I)
    args.buildID = matchObj.group(2)
    print PREFIX, " Build Id of given application : " + args.buildID


# Update mitigation comment.
def updateComment(flowId, comment, action):
    updateCommentPayload = {'build_id': args.buildID, 'action': action, 'comment': comment, 'flaw_id_list': flowId}
    print updateCommentPayload
    response = requests.post(UPDATE_MITIGATION_API, params=updateCommentPayload, files=None,
                             auth=(args.username, password))
    if response.status_code == '200':
        if SUCCESS_RESPONSE_KEY not in response.text:
            output = "SUCCESS , Response : ", response.content
        else:
            output = "ERROR , Response : ", response.content
    else:
        output = "ERROR , Response : ", response.content
    writeFile(flowId, output)
    pass


def writeFile(flowId, output):
    file = open(args.outputFilePath, "a")
    file.write("Flow ID : " + flowId + " " + output)
    file.write("\n")
    file.close()


print PREFIX, "Reading the CSV entries from analysed report"

# Read CSV file and call api to update mitigation comment.
with open(args.csvFilePath, 'r') as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
        flowId = dict(row).get("issue_id")
        if (dict(row).get("WSO2_resolution") == 'False Positive'):
            action = ACTION_FALSEPOSITIVE
        else:
            action = ACTION_APPDESIGN
        comment = dict(row).get("WSO2_resolution") + "\n" + dict(row).get("Use_Case") + "\n" + dict(row).get(
            "Vulnerability_Influence")
        updateComment(flowId, comment, action)
