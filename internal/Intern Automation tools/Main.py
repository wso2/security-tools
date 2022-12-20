import os

try:
    print("#" *30)
    print("Subdomain Enumeration")
    print("#" *30)
    os.system('python amass.py')
except KeyboardInterrupt:
    pass
try:
    print("#" *30)
    print("WHOIS Informations")
    print("#" *30)
    os.system('python ipinfo.py')
except KeyboardInterrupt:
    pass
try:
    print("#" *30)
    print("DNS check scan")
    print("#" *30)
    os.system('python DNScheck.py')
except KeyboardInterrupt:
    pass
try:
    print("#" *30) 
    print("PortScan Informations")
    print("#" *30)
    os.system('python port-scan.py')
except KeyboardInterrupt:
    pass
try:
    print("#" *30)
    print("Subdomain takeover attack")
    print("#" *30)
    os.system('python Subdomain_takeover.py')
except KeyboardInterrupt:
    pass
try:
    print("#" *30)
    print("Cross site scripting attack")
    print("#" *30)
    os.system('python XSS.py')
except KeyboardInterrupt:
    pass
try:
    print("#" *30)
    print("SQL Injection attack")
    print("#" *30)
    os.system('python SQLi.py')
except KeyboardInterrupt:
    pass
try:
    print("#" *30)
    print("PHP code injection attack")
    print("#" *30)
    os.system('python RCE.py')
except KeyboardInterrupt:
    quit()