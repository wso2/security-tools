## Security tool to automating the scan related to vulnerabilities of Java Script Libraries used in products.

*This tool performs security scans for weekly release of every WSO2 products. The weekly release of products
downloaded from respective github repository. 

*This tool currently uses Retire.js as a JS Security Scan tool. Retire.js is an open source tool
which helps to detect JS libraries with known vulnerabilities.

*This tool generate a report for each product which includes retire.js result as json format.

*This tool delivers the by fallowing ways : 
        1. Upload reports to git repo : 
        2. If the report contains any known vulnerability, Create issue ticket. Currently it supports JIRA.
        3. Integrate with VMS.
        
* Before you begin : 

    1. Install Oracle Java SE Development Kit (JDK) version 1.7* or 1.8 and set the JAVA_HOME environment variable.

    2. Install Maven

    3. Clone : https://github.com/wso2/security-tools
    
    4. Github credential details. (Username, password, github access token)
    
    5. JIRA credential details. (Username, password)
    
    
Note : Following configurations need to be done before executing this scan tool. 

   1. There are separate configurations for each product. Please check those configurations.
   2. Please provide github credential information in githubconfig.properties.
   3. Please provide github accesstoken information in githubaccesstoken.properties.
   4. Please provide jira (any issue ticket manager) credential information in issuecreatorconfig.properties.
   5. Please provide jira ticket default ticket parameter values in jiraticketinfo.properties.
   6. Please check supported product list.
    
* How to use this tool?

    1. After providing above configuration details, Go to the root directory of this tool.
    2. Run this command.
`            mvn clean install
`    
    3. Run this command.
`             java -jar target/JsSecurityScanner-1.0-SNAPSHOT.jar 
`