# Veracode API Wrapper - Update Mitigation Info

Calls the Veracode API to update the mitigation comment of a build ID. This is effectively used in updating the comment in Veracode which is maintained in CSV files.

---

## Prerequisite

### Prerequisites specific to Veracode 
- Veracode Subscription
- User role with "Mitigation" and "Mitigation and Comments"

### Prerequisites specific to program 
- <a href="https://github.com/wso2/security-tools/blob/master/internal/veracode-api-wrapper/updatemitigationinfo/requirements.txt">requirements.txt</a>

---

## Local Installation

```
$ virtualenv veracodeApiWrapper
$ . env/bin/activate
(veracodeApiWrapper) $ python updatemitigationinfo.py -h
```

---

### Help

```
usage: updatemitigationinfo.py [-h] [-a APPLICATIONID] [-b BUILDID]
                               [-u USERNAME] [-cf CSVFILEPATH]
                               [-of OUTPUTFILEPATH]

Veracode API wrapper to update mitigation comment.

optional arguments:
  -h, --help          show this help message and exit
  -a APPLICATIONID    Veracode Application ID
  -b BUILDID          Build ID of the scan
  -u USERNAME         Username
  -cf CSVFILEPATH     CSV file path of feed back report
  -of OUTPUTFILEPATH  Output file path`
```
  
---

### How it works

`updatemitigationinfo.py` reads the given CSV file path and construct a string for mitigation comment from the relevant columns in CSV file. Then it calls the Veracode API to update the mitigation comment.

---

### Note

This script is written to support the WSO2 Feedback Report of Veracode Scan.  
         
