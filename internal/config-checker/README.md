# Config Checker
This is a configuration validation tool for WSO2 products.

In WSO2 products there are configurations that are common to all products and specific to a product. Therefore this
tool uses a <application_directory>/common/properties/parent.xml file to hold the common configuration files and a
<application_directory>/product/properties/child.xml to hold product specific configuration files.This child.xml can be used to check configurations in a specific file.

Specify the files containing common configurations in parent.xml inside file tags. For example

    <file id="web.xml">
        <type>xml</type>
        <reference-path>/common/configurations/web-xml-ref.xml</reference-path>
    </file>
Here /common/configurations/web-xml-ref.xml holds the common configurations that should be included in a 'web.xml'.
This file contains tags that contain CDATA sections. The required configurations must be defined within those CDATA sections including its ancestor nodes. For example if we want to check the following configurationValue that resides in web.xml

    <context-param>
       <param-name>Owasp.CsrfGuard.Config</param-name>
       <param-value>repository/conf/security/Owasp.CsrfGuard.Carbon.properties</param-value>
    </context-param>
We should include it inside the web-xml-ref.xml as follows

    <config id="Owasp.CsrfGuard.Config">
        <![CDATA[
            <web-app id="WebApp">
                <context-param>
                    <param-name>Owasp.CsrfGuard.Config</param-name>
                    <param-value>repository/conf/security/Owasp.CsrfGuard.Carbon.properties</param-value>
                </context-param>
            </web-app>
          ]]>
    </config>
The "id" attribute in the config tag is used to uniquely identify the configurations.

Specify the files containing product specific configurations in child.xml. For example

    <file type="web.xml">
        <format>xml</format>
        <reference-path>/product/configurations/tomcat/web-xml-ref.xml</reference-path>
        <product-path>/repository/conf/tomcat/carbon/WEB-INF/web.xml</product-path>
        <exclude-configs>
            <config>bridgeservlet</config>
        </exclude-configs>
    </file>
Here /product/configurations/tomcat/web-xml-ref.xml file holds the product specific configurations for the specific
file in the product in /repository/conf/tomcat/carbon/WEB-INF/web.xml. Configurations defined in the child.xml overrides the configurations defined in parent.xml. If there are configurations that needs be excluded from common configurations, their configuration id can be defined inside tag.

The exclude-paths tag can be used to define specific configurations files that are needed to be excluded from the check. Add the relative path from the product directory to the file that needs to be excluded inside a tag. For example

    <exclude-paths>
        <path>/repository/deployment/server/webapps/oauth2/WEB-INF/web.xml</path>
    </exclude-paths>

## Build from the source

-Get a clone or download source from github

-Run the Maven command mvn clean install from the application root directory

-Extract target/config-checker-1.0-SNAPSHOT.zip and run config-checker-1.0-SNAPSHOT.jar with the following params.
`java -jar config-checker-1.0-SNAPSHOT.jar  -path {absolute path to the product pack without the trailing slash} -out {report generation format}`


For example `java -jar config-checker-1.0-SNAPSHOT.jar  -path /home/kasun/Downloads/wso2is-5.3.0 -out text`

