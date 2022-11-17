import sublist3r
import gspread
import os.path
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError

#subdoamin eumeration
print("----------------------------------------------------")
print("Subdomain Enumeration")
domain=input("Enter the Domain name : ")
subdomains = sublist3r.main(domain, 40, 'subdomains.csv', ports= None, silent=False, verbose= False, enable_bruteforce= False, engines=None)
print(subdomains)

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

gc.import_csv('', content)