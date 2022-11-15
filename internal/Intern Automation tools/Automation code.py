#!/usr/bin/python3
import whois
import nmap
import sublist3r
import dns.resolver
import datetime 
import emailprotectionslib.dmarc as dmarclib
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError

SCOPES = [
    'https://www.googleapis.com/auth/drive',
    'https://www.googleapis.com/auth/drive.file'
    ]
SPREADSHEET_ID = '1_Q2_Bjnm8ERDYcggsMVCPPWCDGB-s4fCUDejJvIqnc8'
GET_IP_RANGE = 'results!A2:A160'
GET_DATE_RANGE = 'date!A1'

scanDateTime = datetime.datetime.now()
SCAN_DATE = scanDateTime.strftime("%x")

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

#subdoamin eumeration
print("----------------------------------------------------")
print("Subdomain Enumeration")
domain=input("Enter the Domain name : ")
subdomains = sublist3r.main(domain, 40, 'subdomains.txt', ports= None, silent=False, verbose= False, enable_bruteforce= False, engines=None)

#DNS records
record_types = ['A', 'AAAA', 'NS', 'CNAME', 'MX', 'PTR', 'SOA', 'TXT']
try:
    domain =open(r"C:\Users\WSO2\Desktop\security-tools\internal\Intern Automation tools\subdomain.txt").read().splitlines()
except IndexError:
    print('Syntax error - python3 dnsenum.py domainname')

for url in domain:
    for records in record_types:
        try:
            answer = dns.resolver.resolve(url, records)
            print(f'\n{records} Records')
            print('-' * 30)
            for server in answer:
                print(str(server))
        except dns.resolver.NoAnswer:
            pass
        except dns.resolver.NXDOMAIN:
            print(f'{url} does not exist.')
            quit()
        except KeyboardInterrupt:
            print('Quitting.')
            quit()

dmarc=dmarclib.DmarcRecord.from_domain(url)
print("\n DMARC Records")
print('-' * 30)
print(dmarc , '\n')

#Whois scan 
print("----------------------------------------------------")
print("WHOIS scan")
domain_info= whois.whois(domain)
for key, value in domain_info.items():
    print(key,':',value)


#Port scan
print("----------------------------------------------------")
scanner = nmap.PortScanner()
print("NMAP automation scan")
print ("nmap version:", scanner.nmap_version())

scanner.scan(arguments='-iL IP.txt')
for host in scanner.all_hosts():
    print('Host : %s (%s)' % (host, scanner[host].hostname()))
    print('State : {0}'.format(scanner[host].state()))
    print("open ports : ", scanner[host]['tcp'].keys())

print(scanner.csv())