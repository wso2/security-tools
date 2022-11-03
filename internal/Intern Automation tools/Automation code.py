#!/usr/bin/python3
import whois
import nmap
import sublist3r
import dns.resolver

print("----------------------------------------------------")
print("Subdomain Enumeration")
domain=input("Enter the Domain name : ")
subdomains = sublist3r.main(domain, 40, 'subdomains.txt', ports= None, silent=False, verbose= False, enable_bruteforce= False, engines=None)

print("----------------------------------------------------")
print("IP address")
d = open("subdomains.txt",'r').read().splitlines()
for domain in d:
    try:
        result = dns.resolver.resolve(domain, 'A')
        for val in result:
            print(domain ,' : ', val.to_text())
            f = open("IP.txt", "a")
            f.write(val.to_text())
            f.write('\n')
            f.close()
          
    except:
        print(domain, " : can't resolve")
        f = open("IP.txt", "a")
        f.write("can't resolve") 
        f.write('\n')
        f.close()

print("----------------------------------------------------")
print("WHOIS scan")
domain_info= whois.whois(domain)
for key, value in domain_info.items():
    print(key,':',value)

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