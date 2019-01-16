# Automating Multiple Usage Detection

I have implemented the tool as an MSF4J microservice which exposes two endpoints to populate the database and to query the data in the database for a given method name. The method usages are extracted using the ASM Java Bytecode Manipulation and Analysis Framework. The application scans jar files and extracts the details about all the method invocations including the java util methods. All the extracted method references are stored in a database which was implemented as a mongodb database.
## Built With

* [WSO2 MSF4J](https://github.com/wso2/msf4j) - WSO2 Microservices Framework for Java (MSF4J)
* [MongoDB](https://www.mongodb.com) - MongoDB NoSQL database
* [ASM](https://asm.ow2.io/) -  Java bytecode manipulation and analysis framework. 

## Prerequisites 
 
* Install Oracle Java SE Development Kit (JDK) version 1.8: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html 
* Install Maven: https://maven.apache.org/install.html
* Install MongoDB : https://www.mongodb.com/

## How to Run

From this directory, run

```
mvn clean install
```

From the target directory, run

```
java -jar scan-tool-1.0.jar
```

## How to Test

To test the service you can use a simple html form or a rest client e.g. Postman, Advanced RestClient.




