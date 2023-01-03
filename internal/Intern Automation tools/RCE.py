import os 
import requests
import csv
import gspread
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

creds = None

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

try:
    for i in range(174):
        url = str(*values[i])
        os.system('python dirsearch.py -e php -u '+url+ ' --exclude-status 201-999 --output=out.txt --format=plain')
    
    X = open('out.txt','r').read().splitlines()
    for i in X:
        payloads = open('payload_rce.txt').read().splitlines()
        csvfile= open('RCE.csv', 'w',newline='')
        row1 = 'File extention' , 'Payload' , 'Status Code' , 'Vulnerable or Not'
        writer = csv.writer(csvfile)
        writer.writerow(row1)

        for payload in payloads:
            request = requests.get(i+payload)
            print("The domain name : ",i)
            print("payload used : ",payload)
            r = request.status_code

            if r == 200:
                print("The subdomain is Vulnerable to the Remote code execution attack !!!!!")
                print("#"*30)
                row2 = i , payload , r , 'Vulnerable!!!'
                writer = csv.writer(csvfile)
                writer.writerow(row2)

            else:
                print("The domain is not vulnerable!")
                print("#"*30)

        row3 = i , 'All' , '404' ,'Not Vulnerable'
        writer = csv.writer(csvfile)
        writer.writerow(row3)

    csvfile.close()
except KeyboardInterrupt:
    print('Quiting')
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

#Read CSV file contents
content = open(r'C:\Users\WSO2\Desktop\security-tools\internal\Intern Automation tools\RCE.csv').read()
gc.open_by_url('https://docs.google.com/spreadsheets/d/1-yCtl9lKhMJYI2DvGyIZqbxUKkF_Vu0W8r7kL5cu-FI/edit#gid=277837716')
gc.import_csv('1-yCtl9lKhMJYI2DvGyIZqbxUKkF_Vu0W8r7kL5cu-FI', content)