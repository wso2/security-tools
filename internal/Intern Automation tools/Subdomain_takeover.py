import subprocess
import os
import gspread
import os
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build


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

domain = value(SCOPES, GET_SUBDOMAIN_RANGE, getData)

with open("subtake.csv","a") as output:
    for i in range(4):
        url = str(*domain[i])
        print("[+] Scanning for the domain : https://"+url)
        try:
            subprocess.call("docker run --rm -it secsi/subzy --target=https://"+url, stdout=output) 
        except:
            print('User stoped the scan')
            quit()


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

# Read CSV file contents
content = open('subtake.csv', 'r').read()

gc.import_csv('16tTozWZ9w8hKzo_awHK9qeup-NezIvNeitbTuSg_LYc', content)
