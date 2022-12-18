import subprocess


domain = open(r"C:\Users\WSO2\Desktop\security-tools\internal\Intern Automation tools\login_portals.txt").read().splitlines()

for url in domain:
    subprocess.call("docker run --rm -it -v /tmp/sqlmap:/root/.sqlmap/ paoloo/sqlmap --url "+url+" --data 'username=\'test@test@\'&password=\'test\'\'")