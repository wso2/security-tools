# SecurityTools - RepoScanner

This tool can be used to create a database of artifacts available in given set of source repositories. Gathered information will include:
* Repository Type
* Repository Name
* Repository URL
* Tag Name
* Tag Download URL (ZIP)
* Artifact ID
* Group ID
* Packaging 
* Version
* Maven Final Name
* Path of Artifact within Repository 

Note: This repository is under active development. 

## Support
### Repositories
* GitHub
### Build Configuration
* Maven
### Storage
* JDBC

## Build
```
mvn clean install 
```
## Usage 
Note: Passwords and OAuth2 Tokens are requested as command line inputs.
```
-------------------------------------------------
-----                                       -----
-----          Repository Scanner           -----
-----                                       -----
-------------------------------------------------
Usage: Repo Scanner [options]
  Options:
    -git.oauth2
      OAuth token used to access GitHub
    -git.users
      Comma separated list of GitHub user accounts to scan
    -maven.home
      Maven home (if environment variables are not set)
    -storage
      Storage used in storing final results (Options: JDBC) (Default: JDBC)
    -jdbc.driver
      Database driver class (Default: com.mysql.jdbc.Driver)
    -jdbc.url
      Database connection URL (Default: jdbc:mysql://localhost/RepoScanner)
    -jdbc.username
      Database username (Default: root)
    -jdbc.password
      Database password
    -jdbc.dialect
      Database Hibernate dialect (Default: org.hibernate.dialect.MySQLDialect)
    -verbose, -v
      Verbose output
      Default: false
    -debug, -d
      Verbose + Debug output for debugging requirements
      Default: false
    --help, -help, -?

    -jdbc.create
      Drop and create JDBC tables
      Default: false
    -rescan
      Rescan repo-tag combinaions even if they are already indexed. (Default:
      false)
      Default: false
    -downloadMaster
      Download master branches of all repositories locally
      Default: false
    -downloadTags
      Download all tags of all repositories locally
      Default: false
    -skipScan
      Skip scanning process. Usable when -downloadMaster and -downloadTag
      options are required without scanning.
      Default: false
```
## Usage Examples
Scan GIT all repositories from "wso2" and "wso2-extensions" GitHub account
```
java -jar RepoScanner-1.0-SNAPSHOT.jar -git.oauth2 -jdbc.password -jdbc.username MySQLUser -git.users wso2,wso2-extensions
```

Skip the scanning process and download source code from master branches of all repositories from "wso2" and "wso2-extensions" GitHub account.
```
java -jar RepoScanner-1.0-SNAPSHOT.jar -git.oauth2 -jdbc.password -jdbc.username MySQLUser -git.users wso2,wso2-extensions -skipScan -downloadMaster
```

Skip the scanning process and download source code from master branches and tags of all repositories from "wso2" and "wso2-extensions" GitHub account.
```
java -jar RepoScanner-1.0-SNAPSHOT.jar -git.oauth2 -jdbc.password -jdbc.username MySQLUser -git.users wso2,wso2-extensions -skipScan -downloadMaster -downloadTags
```