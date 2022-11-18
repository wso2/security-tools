import dns.resolver
import requests


subdomains = open (r"C:\Users\WSO2\Desktop\security-tools\internal\Intern Automation tools\SubdomainX.txt")
for subdomain in subdomains:
        #Make a DNS query for each subdomain looking for a CNAME record
        try:
            resolver = dns.resolver.Resolver()
            dnsAnswer = resolver.query(subdomain.strip(), 'CNAME')
            print ("query: ") + str(dnsAnswer.qname)
            for cname in dnsAnswer:
                if "\\009" in str(cname.target)[:-1]:
                    newcname = str(cname.target)[:-1].replace("\\009","")
                else:
                    newcname = str(cname.target)[:-1]
                #Check if the CNAME is a github.io page
                if "github.io" in newcname:
                    print ("Github page found. " + newcname)
                    try:
                        githubresponse = requests.get("https://"+newcname)
                        if githubresponse.status_code == 404:
                            print ("Github page not found. Possible for subdomain takeover")
                            continue
                        else:
                            print("not vulnerable")
                    except:
                        print("not vulnerable")
        except dns.resolver.NXDOMAIN:
            print ("No DNS record for " + subdomain.strip())