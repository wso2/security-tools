import urllib.request as urllib2
import json
import os
from datetime import datetime
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build
import requests

CHAIN_ISSUES = {
    "0": "none",
    "1": "unused",
    "2": "incomplete chain",
    "4": "chain contains unrelated or duplicate certificates",
    "8": "the certificates form a chain (trusted or not) but incorrect order",
    "16": "contains a self-signed root certificate",
    "32": "the certificates form a chain but cannot be validated",
}

FORWARD_SECRECY = {
    "1": "With some browsers WEAK",
    "2": "With modern browsers",
    "4": "Yes (with most browsers) ROBUST",
}

SCOPES = [
    'https://www.googleapis.com/auth/drive',
    'https://www.googleapis.com/auth/drive.file'
    ]
SPREADSHEET_ID = '1EckNrafKPr6TMTnLorrqjIkwgwtFjAMT396pbvBLKAo'
GET_SUBDOMAIN_RANGE = 'results!A1:A174'

# Retrieves data from Google Sheet with any given range
def getData(service, range):
    sheet = service.spreadsheets()
    result = sheet.values().get(spreadsheetId=SPREADSHEET_ID,
                                range=range,
                                valueRenderOption='FORMATTED_VALUE').execute()
    values = result.get('values', [])

    if not values:
        print('Cannot retreive data. No data found.')
        quit()

    return values

def value(SCOPES, GET_SUBDOMAIN_RANGE, getData):
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

    service = build('sheets', 'v4', credentials=creds)
    values = getData(service, GET_SUBDOMAIN_RANGE)
    return values

values = value(SCOPES, GET_SUBDOMAIN_RANGE, getData)

list = values
for i in range(174):
  host=str(*list[i])
  ana = requests.get("https://api.ssllabs.com/api/v3/analyze?host="+host+"&publish=off&startNew=off&all=on&ignoreMismatch=on",timeout=25)
  with open ('ssl.csv','w',newline='') as f:
    if ana.status_code == 200:
      # Parse the JSON response
      #rsp = ana.read()
      dets = ana.json()
      # Access the desired information from the response object
      try:
        expDate = str(dets["certs"][0]["notAfter"]) 
        expiration_date = datetime.utcfromtimestamp(float(str(expDate)[:10])).strftime("%Y/%m/%d")
        grade = (dets['endpoints'][0]["grade"])
        hasWarnins = (dets['endpoints'][0]["hasWarnings"])
        for data in dets["endpoints"]:
          chainIss = CHAIN_ISSUES[str(data["details"]["certChains"][0]["issues"])]
          forSec = FORWARD_SECRECY[str(data["details"]["forwardSecrecy"])]
          heartbeat = (data["details"]["heartbeat"])
          vulnBeast = (data["details"]["vulnBeast"])
          drownVulnerable = (data["details"]["drownVulnerable"])
          heartbleed = (data["details"]["heartbleed"])
          freak = (data["details"]["freak"])
          openSslCcs = (False if data["details"]["openSslCcs"] == 1 else True)
          openSSLLuckyMinus20 = (False if data["details"]["openSSLLuckyMinus20"] == 1 else True)
          poodle = (data["details"]["poodle"])
          poodleTls = (False if data["details"]["poodleTls"] == 1 else True)
          supportsRc4 = (data["details"]["supportsRc4"])
          rc4WithModern = (data["details"]["rc4WithModern"])
          rc4Only = (data["details"]["rc4Only"])
          #protocols = ["details"][0]["protocols"]
          #for protocol in protocols:
            #pro = ("TLS",protocol["version"])

        
        row = host,grade,hasWarnins,expiration_date,chainIss,forSec,heartbeat,vulnBeast,drownVulnerable,heartbleed,freak,openSslCcs,openSSLLuckyMinus20,poodle,poodleTls,supportsRc4,rc4WithModern,rc4Only
        print("[+] ",row)
        # Set up the Sheets API client
        scopes = ['https://www.googleapis.com/auth/spreadsheets']
        creds = Credentials.from_authorized_user_file('token.json', scopes)
        service = build('sheets', 'v4', credentials=creds)
        # Insert the data into the sheet
        values = [row]
        result = service.spreadsheets().values().append(
        spreadsheetId='1vcKk2KQ6zAJFblxmht78QFIQu77KkV4Bpug765P-EWg',
        range='SSL healthcheck!A2',
        valueInputOption='RAW',
        insertDataOption='INSERT_ROWS',
        body={'values': values}
        ).execute()
      except:
        print("[+] ",host, 'error')
    else:  
      print("[+]",host,": An error occurred:", ana.status_code)