import os
from google.oauth2.credentials import Credentials
from googleapiclient.discovery import build
import csv

SCOPES = ['https://www.googleapis.com/auth/spreadsheets']

creds = Credentials.from_authorized_user_file('token.json', scopes=SCOPES)

# setup the Sheets API

service = build('sheets', 'v4', credentials=creds)

# Delete all the data from the sheet
spreadsheet_id = '1vcKk2KQ6zAJFblxmht78QFIQu77KkV4Bpug765P-EWg'
sheet_name = 'httpx'
range_ = sheet_name + '!A:Z'
request = service.spreadsheets().values().clear(spreadsheetId=spreadsheet_id, range=range_)
request.execute()

print(f'All data from {sheet_name} sheet has been deleted.')


output = os.popen("type .\subdomain.csv | docker run -i projectdiscovery/httpx -sc -csv > out.csv")

# read the CSV file
with open('out.csv', 'r') as f:
    reader = csv.reader(f)
    data = [row for row in reader]

# Build the credentials object
scopes = ['https://www.googleapis.com/auth/spreadsheets']
creds = Credentials.from_authorized_user_file('token.json', scopes)

# Build the service
sheets_service = build('sheets', 'v4', credentials=creds)

# Define the spreadsheet and worksheet
spreadsheet_id = '1vcKk2KQ6zAJFblxmht78QFIQu77KkV4Bpug765P-EWg'
sheet_name = 'httpx'

# Clear the sheet
request = sheets_service.spreadsheets().values().clear(spreadsheetId=spreadsheet_id, range=sheet_name)
request.execute()

# Add the data to the sheet
request = sheets_service.spreadsheets().values().append(spreadsheetId=spreadsheet_id, range=sheet_name, valueInputOption='RAW', insertDataOption='INSERT_ROWS', body={'values': data})
request.execute()
