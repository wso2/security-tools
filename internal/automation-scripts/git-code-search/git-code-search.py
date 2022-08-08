# !/usr/bin/env python

# Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

from time import sleep
import requests
import os

gitToken = os.environ['TOKEN']

# Calls the GitHub search api with the given query params
def get_data(num):
  try:
     # url = "https://api.github.com/search/code?q=myjino.com&page="+str(num)+"&per_page=50"
    url = "https://api.github.com/search/code?q=<SEARCH_PHRASE>&page="+str(num)+"&per_page=50"

    headers = {
    'Authorization': 'Token ' + gitToken
    }

    response = requests.request("GET", url, headers=headers)
    return response
  except requests.HTTPError as e:
    print("Error occured. Try again later!")
    exit()

# Prints the repo_owner/repo name and file_path to a csv file
def print_items(filename,res):
  data = res.json()
  
  with open(filename, "w") as outfile:
    for item in data['items']:
      line = item['repository']['full_name'] + "," + item['path'] + "\n"
      outfile.write(line)

def main():
  for i in range(1,2):
    response = get_data(i)
    file = "results-page"+ str(i) +".csv" 
    print_items(file,response)
    sleep(60)

if __name__ == "__main__":
    main()

