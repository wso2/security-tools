import subprocess
import os
import gspread
import os
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow


domain = open(r'C:\Users\WSO2\Desktop\security-tools\internal\Intern Automation tools\subdomain.csv').read().splitlines()
with open("subtake.csv","a") as output:
    for url in domain:
        print("[+] Scanning for the domain : https://"+url)
        subprocess.call("docker run --rm -it secsi/subzy --target=https://"+url, stdout=output) 


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
