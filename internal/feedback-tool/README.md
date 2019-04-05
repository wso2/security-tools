# Automating Customer Feedback Generation 

This tool is implemented as a WSO2 MSF4J Microservice which exclusively supports functionality such as generating an output HTML or PDF files from a given handlebars template and the relevant data file; and generating an output PDF file from a given HTML file.

## Built With

* [WSO2 MSF4J](https://github.com/wso2/msf4j) - WSO2 Microservices Framework for Java (MSF4J)
* [Handlebars.java](https://github.com/jknack/handlebars.java) - Handlebars.java is a Java port of Handlebars.js.
* [Handlebars.js](https://handlebarsjs.com/) - Javascript template engine language. 

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
java -jar feedback-tool-1.0.jar
```

## How to Test

To test the service you can use a simple html form like below or a rest client e.g. Postman, Advanced RestClient.

```html
<form method="post" action="http://localhost:9090/security-feedback/generate-pdf" enctype="multipart/form-data">
    <table>
        <tr>
    	    <td>Template:</td>
    	    <td><input type="file" name="hbs" /></td>
        </tr>
        <tr>
    	    <td>Data</td>
    	    <td><input type="file" name="data" /></td>
        </tr>
        <tr>
    	    <td colspan="1"><input name="submit" type="submit" value="Submit" /></td>
        </tr>
    </table>
</form>
```


