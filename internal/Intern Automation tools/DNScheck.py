import emailprotectionslib.dmarc as dmarclib
import csv
import dns.resolver
import gspread
import os
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build

def Arecord(url):
    try:
        ARec = dns.resolver.resolve(url,'A')
        for server1 in ARec:
            return str(server1)
    except dns.resolver.NoAnswer:
        return("Can't Resolve")
        pass
    except dns.resolver.NXDOMAIN:
        pass
    except dns.resolver.Timeout:
        pass
    except KeyboardInterrupt:
        print('Quitting.')
        quit()

def AAAArecord(url):
    try:
        AAAARec = dns.resolver.resolve(url,'AAAA')
        for server2 in AAAARec:
            return str(server2)
    except dns.resolver.NoAnswer:
        return("Can't Resolve")
        pass
    except dns.resolver.NXDOMAIN:
        pass
    except dns.resolver.Timeout:
        pass
    except KeyboardInterrupt:
        print('Quitting.')
        quit()
    
def NSrecord(url):
    try:
        answer3 = dns.resolver.resolve(url,'NS')
        for server3 in answer3:
            return str(server3)
    except dns.resolver.NoAnswer:
        return("Can't Resolve")
        pass
    except dns.resolver.NXDOMAIN:
        pass
    except dns.resolver.Timeout:
        pass
    except KeyboardInterrupt:
        print('Quitting.')
        quit()

def CNAME(url):
    try:
        answer4 = dns.resolver.resolve(url,'CNAME')
        for server4 in answer4:
            return str(server4)
    except dns.resolver.NoAnswer:
        return("Can't Resolve")
        pass
    except dns.resolver.NXDOMAIN:
        pass
    except dns.resolver.Timeout:
        pass
    except KeyboardInterrupt:
        print('Quitting.')
        quit()

def MXRecord(url):
    try:
        answer5 = dns.resolver.resolve(url,'MX')
        for server5 in answer5:
            return str(server5)
    except dns.resolver.NoAnswer:
        return("Can't Resolve")
        pass
    except dns.resolver.NXDOMAIN:
        pass
    except dns.resolver.Timeout:
        pass
    except KeyboardInterrupt:
        print('Quitting.')
        quit()

def TXTRecord(url):
    try:
        answer6 = dns.resolver.resolve(url,'TXT')
        for server6 in answer6:
            return str(server6)
    except dns.resolver.NoAnswer:
        return("Can't Resolve")
        pass
    except dns.resolver.NXDOMAIN:
        pass
    except dns.resolver.Timeout:
        pass
    except KeyboardInterrupt:
        print('Quitting.')
        quit()

def dmarcRecord(url):
    try:
        dmarc = dmarclib.DmarcRecord.from_domain(url)
        return str(dmarc.record)
    except KeyboardInterrupt:
        print("Can't Resolve")
        quit()

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

values = value(SCOPES, GET_SUBDOMAIN_RANGE, getData)

filename = open(r"C:\Users\WSO2\Desktop\security-tools\internal\Intern Automation tools\IP.csv")
file = csv.DictReader(filename)

IP = []
status = []
status_code = []
city = []
country = []
url_list = []
A_list = []
AAAA_list=[]
NS_list=[]
CNAME_list=[]
MXRecord_list=[]
TXTRecord_list=[]
dmarc_list=[]

for col in file:
    IP.append(col['IP Address'])
    status.append(col['Status'])
    status_code.append(col['Status Code'])
    city.append(col['City'])
    country.append(col['Country'])


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
        flow = InstalledAppFlow.from_client_secrets_file('credentials.json', SCOPES)
        creds = flow.run_local_server(port=0)

    with open('token.json', 'w') as token: 
        token.write(creds.to_json())

gc = gspread.authorize(creds)

csvfile= open('file.csv', 'w',newline='')
row  = 'Subdomain' , 'IP Address' , 'Status' , 'Status Code' , 'City' , 'Country' , 'A Records' , 'AAAA Records' , 'NS Record' , 'CNAME' , 'MX Record' , 'TXT Record' , 'DMARC Record'
writer = csv.writer(csvfile)
writer.writerow(row)
list = values
for i in range(4):
    url=str(*list[i])
    data = [Arecord(url),AAAArecord(url),NSrecord(url),CNAME(url),MXRecord(url),TXTRecord(url),dmarcRecord(url)]
    print('[+] Scanning for the domain ',url, ':- ',data)
    #data_list.append(data)
    url_list.append(url)
    A_list.append(Arecord(url))
    AAAA_list.append(AAAArecord(url))
    NS_list.append(NSrecord(url))
    CNAME_list.append(CNAME(url))
    MXRecord_list.append(MXRecord(url))
    TXTRecord_list.append(TXTRecord(url))
    dmarc_list.append(dmarcRecord(url))

#print data_list
writer = csv.writer(csvfile, dialect='excel')
writer.writerows(zip(url_list, IP , status , status_code , city , country , A_list, AAAA_list, NS_list, CNAME_list, MXRecord_list , TXTRecord_list , dmarc_list))
csvfile.close()

#Read CSV file contents
content = open(r'file.csv').read()
gc.open_by_url('https://docs.google.com/spreadsheets/d/1vcKk2KQ6zAJFblxmht78QFIQu77KkV4Bpug765P-EWg/edit#gid=0')
gc.import_csv('1vcKk2KQ6zAJFblxmht78QFIQu77KkV4Bpug765P-EWg', content)
