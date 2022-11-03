import dns.resolver
import sys
import emailprotectionslib.dmarc as dmarclib
record_types = ['A', 'AAAA', 'NS', 'CNAME', 'MX', 'PTR', 'SOA', 'TXT']
try:
    domain =open(r"C:\Users\WSO2\Desktop\Outputs\Subdomain.txt").read().splitlines()
except IndexError:
    print('Syntax error - python3 dnsenum.py domainname')

for url in domain:
    for records in record_types:
        try:
            answer = dns.resolver.resolve(url, records)
            print(f'\n{records} Records')
            print('-' * 30)
            for server in answer:
                print(server.to_text())
                
        except dns.resolver.NXDOMAIN:
            print(f'{url} does not exist.')
            quit()

dmarc=dmarclib.DmarcRecord.from_domain(url)
print("\n DMARC Records")
print('-' * 30)
print(dmarc , '\n')