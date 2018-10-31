# Veracode Scanner Microservice API

This is a Spring Boot microservice API which exposes the Veracode static Security Scanner tool's APIs.

This supports the following APIs.
1. Start scan using the product zip file.
2. Start the scan using the product's github location.
3. Get the scanner's status.
4. Stop a running scan.


## Build
```
mvn clean install
```