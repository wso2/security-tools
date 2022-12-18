import urllib.request as urllib2
import json 
import csv
import gspread
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
import requests
import os

domain = open(r'C:\Users\WSO2\Desktop\security-tools\internal\Intern Automation tools\subdomain.csv').read().splitlines()
url = 'http://ip-api.com/json/'
with open ('IP.csv','w',newline='') as f:
    row = 'Subdomains' , 'IP Address' , 'Status' , 'Status Code' , 'City' , 'Country'
    writer = csv.writer(f)
    writer.writerow(row)

    for PS in domain:
        response = urllib2.urlopen(url+PS,timeout=10)
        name = response.read()
        labs = json.loads(name)
        try:
            r = requests.get('https://'+PS)
            sat = r.status_code
        except:
            sat = ('Not Reachable')

        if labs['query'] == PS:
            row = PS,"Can't Resolve"

        else:
            row = PS , labs['query'] , labs['status'] , sat , labs['city'] , labs['country']
            print ('[+] ',PS,' :- ', row)
            writer = csv.writer(f)
            writer.writerow(row)

SCOPES = [
    'https://www.googleapis.com/auth/drive',
    'https://www.googleapis.com/auth/drive.file'
    ]
creds = None

if os.path.exists('token.json'):
    creds = Credentials.from_authorized_user_file('token.json', SCOPES)

if not creds or not creds.valid:
    if creds and creds.expired and creds.refresh_token:
        creds.refresh(Request())
    else:
        flow = InstalledAppFlow.from_client_secrets_file(
            'credentials.json', SCOPES)
        creds = flow.run_local_server(port=0)

    with open('token.json', 'w') as token:
        token.write(creds.to_json())

gc = gspread.authorize(creds)

#Read CSV file contents
content = open(r'C:\Users\WSO2\Desktop\security-tools\internal\Intern Automation tools\IP.csv').read()

gc.import_csv('1FxMFtcNpvaDSFv_LUMoNoKG2LD0hUhBivpkeaLMxoCI', content)