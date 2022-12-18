import subprocess

domain = open(r'C:\Users\WSO2\Desktop\security-tools\internal\Intern Automation tools\subdomain.csv')
for url in domain:
    print("Scanning for the XSS in domain : "+url)
    subprocess.call('python pwnxss.py -u https://'+url)
    print("Scan completed for the domain : "+url)
    print("*"*70)
    print("*"*70)

