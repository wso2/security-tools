# Security Config Checker
This is a security configuration validation tool for WSO2 products.

In WSO2 products there are security configurations that are common to all products and specific to a product. Therefore this tool uses a <application_directory>/Common/properties/parent.xml file to hold the common configuration files and a <application_directory>/Product/properties/child.xml to hold product specific configuration files.This child.xml can be used to check configurations in a specific file.

Specify the files containing common configurations in parent.xml inside file tags. For example

    <file id="web.xml">
        <type>xml</type>
        <reference-path>/Common/configurations/web-xml-ref.xml</reference-path>
    </file>
Here /Common/configurations/web-xml-ref.xml holds the common security configuration that should be included in a 'web.xml'. This file contains tags that contain CDATA sections. The required configurations must be defined within those CDATA sections including its ancestor nodes. For example if we want to check the following configuration that resides in web.xml

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
The "id" attribute in the config tag is used to uniquely identify the configuration.

Specify the files containing product specific configurations in child.xml. For example

    <file type="web.xml">
        <format>xml</format>
        <reference-path>/Product/configurations/tomcat/web-xml-ref.xml</reference-path>
        <product-path>/repository/conf/tomcat/carbon/WEB-INF/web.xml</product-path>
        <exclude-configs>
            <config>bridgeservlet</config>
        </exclude-configs>
    </file>
Here /Product/configurations/tomcat/web-xml-ref.xml file holds the product specific configurations for the specific file in the product in /repository/conf/tomcat/carbon/WEB-INF/web.xml. Configurations defined in the child.xml overrides the configurations defined in parent.xml. If there are configurations that needs be excluded from common configurations, their configuration id can be defined inside tag.

The exclude-paths tag can be used to define specific configurations files that are needed to be excluded from the check. Add the relative path from the product directory to the file that needs to be excluded inside a tag. For example

    <exclude-paths>
        <path>/repository/deployment/server/webapps/oauth2/WEB-INF/web.xml</path>
    </exclude-paths>

## Build from the source

-Get a clone or download source from github

-Run the Maven command mvn clean install from the application root directory

-Execute run.sh by giving the path of the product package that needs to checked as a parameter

For example `./run.sh -p /home/user/wso2is-5.3.0`

