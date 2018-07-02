# Automating Customer Feedback Generation 

This tool is implemented as a WSO2 MSF4J Microservice which exclusively supports functionality such as generating an output HTML or PDF files from a given handlebars template and the relevant data file; and generating an output PDF file from a given HTML file.

## Built With

* [WSO2 MSF4J](http://www.dropwizard.io/1.0.2/docs/) - WSO2 Microservices Framework for Java (MSF4J)
* [Handlebars.java](https://maven.apache.org/) - Handlebars.java is a Java port of Handlebars.js.
* [Handlebars.js](https://rometools.github.io/rome/) - Javascript template engine language. 

## Prerequisites 

* Install Oracle Java SE Development Kit (JDK) version 1.8: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html 
* Install Maven: https://maven.apache.org/install.html

## Configurations 

In order to add any new input adapters update the config.properties file with the corresponding file type extension and the path to the input adapter. Below example is given for the excel (.xlsx file type) input adapter. 

```
xlsx=org.wso2.security.tool.adapter.ExcelInputAdapter
```

## How to Run

From this directory, run

```
mvn clean install
```

From the target directory, run

```
java -jar feedback-tool-0.1-SNAPSHOT.jar
```
