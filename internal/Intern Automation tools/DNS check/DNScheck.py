import emailprotectionslib.dmarc as dmarclib
import csv
import dns.resolver
import pandas as pd
import gspread
import os
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow

def Arecord(url):
    try:
        ARec = dns.resolver.resolve(url,'A')
        for server1 in ARec:
            return server1
    except dns.resolver.NoAnswer:
        pass
    except dns.resolver.NXDOMAIN:
        print(f'{url} does not exist.')
        quit()
    except KeyboardInterrupt:
        return('Quitting.')
        quit()

def AAAArecord(url):
    try:
        AAAARec = dns.resolver.resolve(url,'AAAA')
        for server2 in AAAARec:
            print(str(server2))
            return server2
    except dns.resolver.NoAnswer:
        pass
    except dns.resolver.NXDOMAIN:
        print(f'{url} does not exist.')
        quit()
    except KeyboardInterrupt:
        return('Quitting.')
        quit()
    
def NSrecord(url):
    try:
        answer3 = dns.resolver.resolve(url,'NS')
        for server3 in answer3:
            print(str(server3))
            return server3
    except dns.resolver.NoAnswer:
        pass
    except dns.resolver.NXDOMAIN:
        print(f'{url} does not exist.')
        quit()
    except KeyboardInterrupt:
        return('Quitting.')
        quit()

def CNAME(url):
    try:
        answer4 = dns.resolver.resolve(url,'CNAME')
        for server4 in answer4:
            return server4
    except dns.resolver.NoAnswer:
        pass
    except dns.resolver.NXDOMAIN:
        print(f'{url} does not exist.')
        quit()
    except KeyboardInterrupt:
        return('Quitting.')
        quit()

def MXRecord(url):
    try:
        answer5 = dns.resolver.resolve(url,'MX')
        for server5 in answer5:
            print(str(server5))
            return server5
    except dns.resolver.NoAnswer:
        pass
    except dns.resolver.NXDOMAIN:
        print(f'{url} does not exist.')
        quit()
    except KeyboardInterrupt:
        return('Quitting.')
        quit()


def dmarcRecord(url: str):
    try:
        dmarc=dmarclib.DmarcRecord.from_domain(url)
        return dmarc
    except KeyboardInterrupt:
        print('Quitting.')
        quit()
  
url_list = []
A_list = []
AAAA_list=[]
NS_list=[]
CNAME_list=[]
MXRecord_list=[]
#dmarc_list=[]


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


csvfile= open('file.csv', 'w')
list = open(r"C:\Users\WSO2\Desktop\security-tools\internal\Intern Automation tools\DNS check\Subdomain.txt").read().splitlines()
for url in list:
    print ((url), Arecord(url), AAAArecord(url),NSrecord(url),CNAME(url),MXRecord(url))
    data = [url,Arecord(url),AAAArecord(url),NSrecord(url),CNAME(url),MXRecord(url)]
    #data_list.append(data)
    url_list.append(url)
    A_list.append(Arecord(url))
    AAAA_list.append(AAAArecord(url))
    NS_list.append(NSrecord(url))
    CNAME_list.append(CNAME(url))
    MXRecord_list.append(MXRecord(url))
    #dmarc_list.append(dmarcRecord(url))

#print data_list
writer = csv.writer(csvfile, dialect='excel')
writer.writerows(zip(url_list, A_list, AAAA_list, NS_list, CNAME_list, MXRecord_list))
csvfile.close()

#Read CSV file contents
content = open(r'file.csv').read()
gc.open_by_url('https://docs.google.com/spreadsheets/d/1vcKk2KQ6zAJFblxmht78QFIQu77KkV4Bpug765P-EWg/edit#gid=0')
gc.import_csv('1vcKk2KQ6zAJFblxmht78QFIQu77KkV4Bpug765P-EWg', content)
