import subprocess
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build

domain = open(r"ENTER THE FILE NAME THAT CONTAINS THE LOGIN PAGES").read().splitlines()

for url in domain:
    print("[+] Scanning for the login page :",url)
    x = subprocess.call("docker run --rm -it -v /tmp/sqlmap:/root/.sqlmap/ paoloo/sqlmap --url "+url+" --batch --data 'username=\'test@test@\'&password=\'test\'\' '")
    
    if x == 0:
        ans = "Subdomain is not Vulnerable to SQL injection "
        print(ans)
    else:
        ans = "Subdomain is Vulnerable to SQL injection"
        print(ans)

    # Set up the Sheets API client
    scopes = ['https://www.googleapis.com/auth/spreadsheets']
    creds = Credentials.from_authorized_user_file('token.json', scopes)
    service = build('sheets', 'v4', credentials=creds)
    data = (url , ans)
    # Define the data to be inserted
    values = [data]

    # Insert the data into the sheet
    result = service.spreadsheets().values().append(
        spreadsheetId='1vcKk2KQ6zAJFblxmht78QFIQu77KkV4Bpug765P-EWg',
        range='SQLi!A2',
        valueInputOption='RAW',
        insertDataOption='INSERT_ROWS',
        body={'values': values}
    ).execute()