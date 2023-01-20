import requests
import os
from datetime import datetime
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build

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


 
def exp(url):
  try:
    expDate = str(dets["certs"][0]["notAfter"])
    exp_date = datetime.utcfromtimestamp(float(str(expDate)[:10])).strftime("%Y-%m-%d") 
    return exp_date
  except:
    return ("can't resolve")

def grade(url):
  try:
    grade = (dets['endpoints'][0]["grade"])
    return grade
  except:
    pass

def hasWarning(url):
  try:
    hasWarnins = (dets['endpoints'][0]["hasWarnings"])
    return hasWarnins
  except:
    pass

def Chain_issues(url):
  try:
    for data in dets["endpoints"]:
      chainIss = CHAIN_ISSUES[str(data["details"]["certChains"][0]["issues"])]
      return chainIss
  except:
    pass

def Forward_secrecy(url):
  try:
    for data in dets["endpoints"]:
      forSec = FORWARD_SECRECY[str(data["details"]["forwardSecrecy"])]
      return forSec
  except:
    pass

def heartbeat(url):
  try:
    for data in dets["endpoints"]:
      heartbeat = (data["details"]["heartbeat"])
      return heartbeat
  except:
    pass

def vulnBeat(url):
  try:
    for data in dets["endpoints"]:
      vulnBeast = (data["details"]["vulnBeast"])
      return vulnBeast
  except:
    pass

def drownVulnerable(url):
  try:
    for data in dets["endpoints"]:
      drownVulnerable = (data["details"]["drownVulnerable"])
      return drownVulnerable
  except:
    pass

def heartbleed(url):
  try:
    for data in dets["endpoints"]:
      heartbleed = (data["details"]["heartbleed"])
      return heartbleed
  except:
    pass

def freak(url):
  try:
    for data in dets["endpoints"]:
      freak = (data["details"]["freak"])
      return freak
  except:
      pass

def openSSLCCS(url):
  try:
    for data in dets["endpoints"]:
      openSslCcs = (False if data["details"]["openSslCcs"] == 1 else True)
      return openSslCcs
  except:
    pass
    
def openSSLLuckyMinus20(url):
  try:
    for data in dets["endpoints"]:
      openSSLLuckyMinus20 = (False if data["details"]["openSSLLuckyMinus20"] == 1 else True)
      return openSSLLuckyMinus20
  except:
    pass

def poodle(url):
  try:
    for data in dets["endpoints"]:
      poodle = (data["details"]["poodle"])
      return poodle
  except:
      pass

def poodleTls(url):
  try:
    for data in dets["endpoints"]:
      poodleTls = (False if data["details"]["poodleTls"] == 1 else True)
      return poodleTls
  except:
    pass

def supportRc4(url):
  try:
    for data in dets["endpoints"]:
      supportsRc4 = (data["details"]["supportsRc4"])
      return supportsRc4
  except:
    pass

def rc4withModern(url):
  try:
    for data in dets["endpoints"]:
      rc4WithModern = (data["details"]["rc4WithModern"])
      return rc4WithModern
  except:
    pass

def rc4only(url):
    try:
        for data in dets["endpoints"]:
            rc4Only = (data["details"]["rc4Only"])
            return rc4Only
    except:
        quit()


SCOPES = ['https://www.googleapis.com/auth/spreadsheets']

creds = Credentials.from_authorized_user_file('token.json', scopes=SCOPES)

# setup the Sheets API

service = build('sheets', 'v4', credentials=creds)

# Delete all the data from the sheet
spreadsheet_id = '1vcKk2KQ6zAJFblxmht78QFIQu77KkV4Bpug765P-EWg'
sheet_name = 'SSL healthcheck'
range_ = sheet_name + '!A2:Z'
request = service.spreadsheets().values().clear(spreadsheetId=spreadsheet_id, range=range_)
request.execute()

print(f'All data from {sheet_name} sheet has been deleted.')

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
    url = requests.get("https://api.ssllabs.com/api/v3/analyze?host="+host+"&publish=off&startNew=off&all=on&ignoreMismatch=on",timeout=30)
    if url.status_code == 200:
        # Parse the JSON response
        #rsp = ana.read()
        dets = url.json()
        # Access the desired information from the response object
        row = host,exp(url),grade(url),hasWarning(url),Chain_issues(url),Forward_secrecy(url),heartbeat(url),vulnBeat(url),drownVulnerable(url),heartbleed(url),freak(url),openSSLCCS(url),openSSLLuckyMinus20(url),poodle(url),poodleTls(url),supportRc4(url),rc4withModern(url),rc4only(url)
        print(row)

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

    else:
        print("[+]",host,": An error occurred:", url.status_code)