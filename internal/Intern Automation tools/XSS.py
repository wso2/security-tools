import subprocess
import os

domain = open(r'subdomain.csv').read().splitlines()
for url in domain:
    print("Scanning for the XSS in domain : "+url)
    subprocess.call('python pwnxss.py -u https://'+url)
    print("Scan completed for the domain : "+url)
    print("*"*70)
    print("*"*70)

