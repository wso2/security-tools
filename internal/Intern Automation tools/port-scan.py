import os.path
import nmap
import datetime
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
GET_IP_RANGE = 'results!A2:A6'
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

# Scan a given IP address using nmap scanner
def scan(ip):
    try:
        report = []
        scanner = nmap.PortScanner()
        res = scanner.scan(hosts=ip, arguments='-A -T3')
        ports = scanner[ip]['tcp'].keys()
        print("Ports found: " + str(len(ports)))

        for port in ports:
            state = scanner[ip]['tcp'][port]['state']
            service = scanner[ip]['tcp'][port]['name']
            if state == 'open' or state == 'closed' or state == 'filtered':
                report.append(str(port))
                report.append(' ')
                report.append(str(service))
                report.append(' ')
                report.append(str(state))
                report.append('\n')
            else:
                report.append('All ports are filtered')
                report.append('\n')
        return report
    except Exception as e:
        print("Cannot scan IP. ", e.__class__, " occurred.")
        report.append('All ports are filtered')
        report.append('\n')
        return report

# Formats a string array
def formatArray(arr):
    arrLen = len(arr)

    if arrLen==0:
        return []

    arr.pop(arrLen-1)
    fmtArr = ''.join(arr)

    return fmtArr

# Checks for prevously scanned ports and compare with current scanned ports results
def checkIfExist(service, arr, i, lastScanDate):
    status = "Changed since " + lastScanDate + " scan"
    sheetRange = "'results'!B"+str(i+2)
    sheet = service.spreadsheets()
    result = sheet.values().get(spreadsheetId=SPREADSHEET_ID,
                                range=sheetRange,
                                valueRenderOption='UNFORMATTED_VALUE').execute()
    upValues = result.get('values', [])

    for row in upValues:
        st = row[0]
        if st==arr:
            status = "Not changed since " + lastScanDate + " scan"
        else:
            status = "Changed since " + lastScanDate + " scan"
    
    return status

# Update Google sheets with given range and data
def updateData(service, response, i, clm):
    sheetRange = "'results'!" + clm + str(i)
    values =[[response]]
    body = {
        'range': sheetRange,
        'majorDimension': 'ROWS',
        'values': values
    }

    try:
        result = service.spreadsheets().values().update(
                                spreadsheetId=SPREADSHEET_ID,
                                range=sheetRange,
                                valueInputOption="USER_ENTERED", responseValueRenderOption="FORMATTED_VALUE", body=body).execute()
        values = result.get('values', [])
        return result
    except HttpError as err:
        print("Cannot update sheet. ", err, " occurred.")
        quit()

# Updates the last scanned date in Google Sheets
def updateScanDate(service):
    values =[[str(SCAN_DATE)]]
    body = {
        'range': GET_DATE_RANGE,
        'majorDimension': 'ROWS',
        'values': values
    }

    try:
        result = service.spreadsheets().values().update(
                                spreadsheetId=SPREADSHEET_ID,
                                range=GET_DATE_RANGE,
                                valueInputOption="USER_ENTERED", responseValueRenderOption="FORMATTED_VALUE", body=body).execute()
        values = result.get('values', [])
        return result
    except HttpError as err:
        print("Cannot update sheet. ", err, " occurred.")
        quit()


def main():
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

    try:
        service = build('sheets', 'v4', credentials=creds)
        dateValues = getData(service, GET_DATE_RANGE)
        lastScanDate = dateValues[0][0]

        values = getData(service, GET_IP_RANGE)
        ipLen = len(values)

        if ipLen == None:
            print("No IPs found on the Google Sheet")
            quit()

        for i in range(ipLen):
            ip = values[i][0]
            print("Scanning IP: " + str(ip))

            response = scan(ip)
            arr = formatArray(response)
            status = checkIfExist(service, arr, i, lastScanDate)           
            res = updateData(service, arr, i+2, "B")
            resStatus = updateData(service, status, i+2, "C")
        
        updateScanDate(service) 

    except HttpError as err:
        print("Cannot connect to Google Sheets. ", err ," occured")
        quit()


if __name__ == '__main__':
    main()