import requests
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build
import requests
import os

# setup the Sheets API
SCOPES = ['https://www.googleapis.com/auth/spreadsheets']

creds = Credentials.from_authorized_user_file('token.json', scopes=SCOPES)

# setup the Sheets API

service = build('sheets', 'v4', credentials=creds)

# Delete all the data from the sheet
spreadsheet_id = '1vcKk2KQ6zAJFblxmht78QFIQu77KkV4Bpug765P-EWg'
sheet_name = 'Whois'
range_ = sheet_name + '!A2:Z'
request = service.spreadsheets().values().clear(spreadsheetId=spreadsheet_id, range=range_)
request.execute()

print(f'Previous scan results from {sheet_name} sheet has been deleted.')

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

def values(SCOPES):
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
    return service

values = getData(service, GET_SUBDOMAIN_RANGE)
domain = values
url = 'http://ip-api.com/json/'
try:
    for i in range(174):
        PS = str(*domain[i])
        response = requests.get(url+PS,timeout=20)
        labs = response.json()
        sat = response.status_code

        if labs['query'] == PS:
            row = PS,"Can't Resolve"

        else:
            row = PS , labs['query'] , labs['status'] , sat , labs['city'] , labs['country']
            print ('[+] ',PS,' :- ', row)

            # Set up the Sheets API client
            scopes = ['https://www.googleapis.com/auth/spreadsheets']
            creds = Credentials.from_authorized_user_file('token.json', scopes)
            service = build('sheets', 'v4', credentials=creds)

            # Define the data to be inserted
            value = [row]

            # Insert the data into the sheet
            result = service.spreadsheets().values().append(
            spreadsheetId='1vcKk2KQ6zAJFblxmht78QFIQu77KkV4Bpug765P-EWg',
            range='Whois!A2',
            valueInputOption='RAW',
            insertDataOption='INSERT_ROWS',
            body={'values': value}
            ).execute()
except KeyError:
    print(KeyError)
except KeyboardInterrupt:
    print("Quitting")