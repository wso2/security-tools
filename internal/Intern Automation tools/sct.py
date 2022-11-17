import whois
import csv
import gspread
import os
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow

#input url
url = input("enter the url: ")

outfile = csv.writer(open("output.csv", "w"))
outfile.writerow(['URL', 'Registrar', 'Email', 'Name', 'Country', 'City'])


if url is None:
	print ("Enter the valid url!!!")
	quit()

elif ():
	ip_list = open(url)
	for wi in ip_list:
		wi = wi.strip('\n') #Removing any new line character from the end of line
		wi = wi.strip('\r')
		w = whois.whois(wi)
#getting the variables to write on csv		
		registrar = w.registrar
		name = w.name
		country = w.country
		city = w.city
		email = w.emails
		outfile.writerow([url, registrar, email, name, country, city]) 

outfile.close()

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
content = open(r'output.csv').read()
gc.open_by_url('https://docs.google.com/spreadsheets/d/1O38T4_r2G1xO3OTRlF8AYLKCSCjEhokD-g6N_5b1TkM/edit#gid=0')
gc.import_csv('1O38T4_r2G1xO3OTRlF8AYLKCSCjEhokD-g6N_5b1TkM', content)