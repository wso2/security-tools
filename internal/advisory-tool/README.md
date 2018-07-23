# Advisory Tool

This tool can be used to generate the Security Advisories for WSO2 products.



Note: This repository is under active development.


## Support
### Product Families
* WSO2 Carbon 4 Based Products


## Build
```
mvn clean install
```

## Usage

Extract the advisorytool-1.0-SNAPSHOT.zip


1. To generate the customer security advisory HTML
```
java -jar advisorytool-1.0-SNAPSHOT.jar -type customer -out html
```
<br/>

2. To generate the customer security advisory XML
```
java -jar advisorytool-1.0-SNAPSHOT.jar -type customer -out xml
```
<br/>

3. To generate the customer security advisory PDF
```
java -jar advisorytool-1.0-SNAPSHOT.jar -type customer -out pdf
```
<br/>

4. To generate the customer security advisory PDF from HTML
```
java -jar advisorytool-1.0-SNAPSHOT.jar -type customer -out html2pdf
```
<br/>

5. To generate the security advisory PDF from XML
```
java -jar advisorytool-1.0-SNAPSHOT.jar -type customer -out xml2pdf
```
<br/>

6. To generate the security advisory HTML from XML
```
java -jar advisorytool-1.0-SNAPSHOT.jar -type customer -out html2xml
```