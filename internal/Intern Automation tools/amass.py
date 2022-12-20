import subprocess
import os
import gspread
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow

domain = input("Enter the domain name : ")
with open('subdomain.csv','a') as output:
    print('Scanning for :- '+domain)
    subprocess.run(['docker', 'run', 'caffix/amass', 'enum' , '-max-dns-queries', '200' ,'-d', domain], shell=True, stdout=output)
    

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
content = open('subdomain.csv', 'r').read()

gc.import_csv('1EckNrafKPr6TMTnLorrqjIkwgwtFjAMT396pbvBLKAo', content)