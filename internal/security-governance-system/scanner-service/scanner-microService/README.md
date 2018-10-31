# Scanner Microservice API

This is a Spring Boot microservice API which exposes the security scanner tool's APIs.

This supports the following APIs.
1. Start scan using the product zip file.
2. Start the scan using the product's github location.
3. Get the scanner's status.
4. Stop a running scan.

This project contains interfaces which can be extended and  write to represent a scanner which support the above operations.
That custom scanner can be plugged using a configration file.


## Build
```
mvn clean install
```
