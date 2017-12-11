#!/bin/bash

# Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

DYNAMIC_HOME="$HOME/env-dynamic"
PRODUCT_HOME="$HOME/products"
ADMIN_PASSWORD=$(head -n 1 $HOME/scripts/config/DynamicAdminUser.conf)
HOST_NAME_APIM=$(head -n 1 $HOME/scripts/config/DynamicHostnameAM.conf)
HOST_NAME_IS=$(head -n 1 $HOME/scripts/config/DynamicHostnameIS.conf)
HOST_NAME_DAS=$(head -n 1 $HOME/scripts/config/DynamicHostnameDAS.conf)
HOST_NAME_EI=$(head -n 1 $HOME/scripts/config/DynamicHostnameEI.conf)
HOST_NAME_IOT=$(head -n 1 $HOME/scripts/config/DynamicHostnameIOT.conf)
SCRIPT_TAG="[SEC_AUTOMATION_BUILD_DYNAMIC_ENV]"

echo "$SCRIPT_TAG [START]"

echo "$SCRIPT_TAG Existing WSO2 server instances"
ps -ef | grep java | grep org.wso2.carbon.bootstrap.Bootstrap | tr -s ' ' | cut -d ' ' -f2 | paste -sd "," -

if [[ $(ps -ef | grep java | grep org.wso2.carbon.bootstrap.Bootstrap | tr -s ' ' | cut -d ' ' -f2) ]]; then

	echo "$SCRIPT_TAG Killing existing WSO2 server instances"
	kill -9 $(ps -ef | grep java | grep org.wso2.carbon.bootstrap.Bootstrap | tr -s ' ' | cut -d ' ' -f2)

	echo "$SCRIPT_TAG Existing WSO2 server instances after killing"
	ps -ef | grep java | grep org.wso2.carbon.bootstrap.Bootstrap | tr -s ' ' | cut -d ' ' -f2 | paste -sd "," -

fi

echo "$SCRIPT_TAG Cleaning dynamic environment home: $DYNAMIC_HOME"
rm -rf $DYNAMIC_HOME

echo "$SCRIPT_TAG Calling WUM update process"
bash $HOME/scripts/UpdateProducts.sh

cp -R $PRODUCT_HOME $DYNAMIC_HOME
echo "$SCRIPT_TAG Copied $PRODUCT_HOME to $DYNAMIC_HOME"

for product in $(cat $HOME/scripts/config/SupportedProductList.conf)
do

	CARBON_XML_PATH=$(find $DYNAMIC_HOME/$product | grep "carbon.xml")
	echo "$SCRIPT_TAG carbon.xml path $CARBON_XML_PATH"

	USERMGT_XML_PATH=$(find $DYNAMIC_HOME/$product | grep "user-mgt.xml")
	echo "$SCRIPT_TAG user-mgt.xml path $USERMGT_XML_PATH"

	CATALINA_XML_PATH=$(find $DYNAMIC_HOME/$product | grep "catalina-server.xml")
	echo "$SCRIPT_TAG catalina-server.xml path $CATALINA_XML_PATH"

	if [[ $product == *"wso2am"* ]]; then

		OFFSET=0
		echo "$SCRIPT_TAG Configuration API Manager - Offset is: $OFFSET"

		#Proxy Configuration
		echo "$SCRIPT_TAG Configuration API Manager - Proxy Configuration"
		sed -i -e "s/<.*HostName>.*<\/HostName.*>/<HostName>$HOST_NAME_APIM<\/HostName>/g" $CARBON_XML_PATH
		sed -i -e "s/<.*MgtHostName>.*<\/MgtHostName.*>/<MgtHostName>$HOST_NAME_APIM<\/MgtHostName>/g" $CARBON_XML_PATH

		#Carbon Authenticator Configuration
		echo "$SCRIPT_TAG Configuration API Manager - SSO for Management Console"
		AUTHENTICATOR_XML=$(find $DYNAMIC_HOME/$product | grep "authenticators.xml")
		sed -i -e "s/Authenticator name=\"SAML2SSOAuthenticator\" disabled=\"true\"/Authenticator name=\"SAML2SSOAuthenticator\" disabled=\"false\"/g" $AUTHENTICATOR_XML
		sed -i -e "s/https:\/\/localhost:9443\/samlsso/https:\/\/$HOST_NAME_APIM:443\/samlsso/g" $AUTHENTICATOR_XML
		sed -i -e "s/https:\/\/localhost:9443\/acs/https:\/\/$HOST_NAME_APIM:443\/acs/g" $AUTHENTICATOR_XML

		#Publisher Configuration
		echo "$SCRIPT_TAG Configuration API Manager - SSO for Publisher"
		SITE_CONF="$DYNAMIC_HOME/$product/repository/deployment/server/jaggeryapps/publisher/site/conf/site.json"
		sed -i "0,/\"enabled\".*:.*false.*,/s//\"enabled\" : \"true\",/" $SITE_CONF
		sed -i -e "s/https:\/\/localhost:9443\/samlsso/https:\/\/$HOST_NAME_APIM:443\/samlsso/g" $SITE_CONF
		tac $SITE_CONF > $SITE_CONF-tmp
		sed -i "0,/\"enabled\".*:.*false.*,/s//\"enabled\" : \"true\",\/\//" $SITE_CONF-tmp
		tac $SITE_CONF-tmp > $SITE_CONF
		rm  $SITE_CONF-tmp
		sed -i -e "s/sample.proxydomain.com/$HOST_NAME_APIM/g" $SITE_CONF
		sed -i -e "s/\"context\".*:.*\"\"/\"context\":\"\/publisher\"/g" $SITE_CONF

		#Store Configuration
		echo "$SCRIPT_TAG Configuration API Manager - SSO for Store"
		SITE_CONF="$DYNAMIC_HOME/$product/repository/deployment/server/jaggeryapps/store/site/conf/site.json"
		sed -i "0,/\"enabled\".*:.*false.*,/s//\"enabled\" : \"true\",/" $SITE_CONF
		sed -i -e "s/https:\/\/localhost:9443\/samlsso/https:\/\/$HOST_NAME_APIM:443\/samlsso/g" $SITE_CONF
		tac $SITE_CONF > $SITE_CONF-tmp
		sed -i "0,/\"enabled\".*:.*false.*,/s//\"enabled\" : \"true\",\/\//" $SITE_CONF-tmp
		tac $SITE_CONF-tmp > $SITE_CONF
		rm  $SITE_CONF-tmp
		sed -i -e "s/sample.proxydomain.com/$HOST_NAME_APIM/g" $SITE_CONF
		sed -i -e "s/\"context\".*:.*\"\"/\"context\":\"\/store\"/g" $SITE_CONF

		#Admin Configuration (Admin application does not support SSO. Hence modifying app to auto-login)
		echo "$SCRIPT_TAG Configuration API Manager - Authentication Skip for Admin"
		LOGIN_JAG="$DYNAMIC_HOME/$product/repository/deployment/server/jaggeryapps/admin/site/themes/wso2/templates/user/login/template.jag"
		sed -i -e "s/id=\"pass\"/id=\"pass\" value=\"$ADMIN_PASSWORD\"/g" $LOGIN_JAG
		sed -i -e "s/id=\"username\"/id=\"username\" value=\"admin\"/g" $LOGIN_JAG
		sed -i -e "s/(\"Reset\")%><\/button>/(\"Reset\")%><\/button><script>\$(document).ready(function() { login(); });<\/script>/g" $LOGIN_JAG

		#echo "$SCRIPT_TAG Configuration API Manager - SSO for Admin"
		#SITE_CONF="$DYNAMIC_HOME/$product/repository/deployment/server/jaggeryapps/admin/site/conf/site.json"
		#sed -i "0,/\"enabled\".*:.*false.*,/s//\"enabled\" : \"true\",/" $SITE_CONF
		#sed -i -e "s/https:\/\/localhost:9443\/samlsso/https:\/\/$HOST_NAME_APIM:443\/samlsso/g" $SITE_CONF
		#tac $SITE_CONF > $SITE_CONF-tmp
		#sed -i "0,/\"enabled\".*:.*false.*,/s//\"enabled\" : \"true\",\/\//" $SITE_CONF-tmp
		#tac $SITE_CONF-tmp > $SITE_CONF
		#rm  $SITE_CONF-tmp
		#sed -i -e "s/sample.proxydomain.com/$HOST_NAME_APIM/g" $SITE_CONF
		#sed -i -e "s/\"context\".*:.*\"\"/\"context\":\"\/admin\"/g" $SITE_CONF

	elif [[ $product == *"wso2is"* ]]; then

    #
    # Start of Creating SSO IDP for WSO2 API Manager Setup
    #

		if [[ $product == *"wso2is-5.3.0"* ]]; then
			for sso_product in $(cat $HOME/scripts/config/SSOProductList.conf)
			do
				SSO_HOST_NAME=""
				SSO_OFFSET=0
				if [[ $sso_product == *"wso2am"* ]]; then
					SSO_HOST_NAME=$HOST_NAME_APIM
					SSO_OFFSET="100"
				elif [[ $sso_product == *"wso2das"* ]]; then
					SSO_HOST_NAME=$HOST_NAME_DAS
					SSO_OFFSET="101"
				else
					echo "$SCRIPT_TAG [ERROR] Unknown product in SSO configuration list, skipping configuration"
					continue
					echo "$SCRIPT_TAG [ERROR] THIS SHOULD NOT PRINT"
				fi

				echo "$SCRIPT_TAG Configuration $sso_product - SSO IDP in: $DYNAMIC_HOME/$product-$sso_product-SSO"
				cp -R $DYNAMIC_HOME/$product $DYNAMIC_HOME/$product-$sso_product-SSO

				CARBON_XML_PATH_IS_SSO=$(find $DYNAMIC_HOME/$product-$sso_product-SSO | grep "carbon.xml")
				echo "$SCRIPT_TAG carbon.xml path $CARBON_XML_PATH_IS_SSO"

				USERMGT_XML_PATH_IS_SSO=$(find $DYNAMIC_HOME/$product-$sso_product-SSO | grep "user-mgt.xml")
				echo "$SCRIPT_TAG user-mgt.xml path $USERMGT_XML_PATH_IS_SSO"

				CATALINA_XML_PATH_IS_SSO=$(find $DYNAMIC_HOME/$product-$sso_product-SSO | grep "catalina-server.xml")
				echo "$SCRIPT_TAG catalina-server.xml path $CATALINA_XML_PATH_IS_SSO"

				echo "$SCRIPT_TAG Configuration $sso_product - SSO IDP - Proxy Configuration"
				sed -i -e "s/<.*HostName>.*<\/HostName.*>/<HostName>$SSO_HOST_NAME<\/HostName>/g" $CARBON_XML_PATH_IS_SSO
				sed -i -e "s/<.*MgtHostName>.*<\/MgtHostName.*>/<MgtHostName>$SSO_HOST_NAME<\/MgtHostName>/g" $CARBON_XML_PATH_IS_SSO
				sed -i -e "s/port=\"9443\"/port=\"9443\" proxyPort=\"443\"/g" $CATALINA_XML_PATH_IS_SSO
				sed -i -e "s/port=\"9763\"/port=\"9763\" proxyPort=\"80\"/g" $CATALINA_XML_PATH_IS_SSO

				echo "$SCRIPT_TAG Configuration $sso_product - SSO IDP - Offset Configuration (Offset is set to 100)"
				sed -i -e "s/<Offset>.*<\/Offset>/<Offset>$SSO_OFFSET<\/Offset>/g" $CARBON_XML_PATH_IS_SSO

				echo "$SCRIPT_TAG Configuration $sso_product - SSO IDP - Password and Compression Configuration"
				sed -i -e "s/<Password>admin<\/Password>/<Password>$ADMIN_PASSWORD<\/Password>/g" $USERMGT_XML_PATH_IS_SSO
				sed -i -e "s/compression=\"on\"/compression=\"off\"/g" $CATALINA_XML_PATH_IS_SSO

				sed -i -e "s/<UserStoreManager class=\"org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager\">/<!--UserStoreManager class=\"org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager\">/g" $USERMGT_XML_PATH_IS_SSO
				tac $USERMGT_XML_PATH_IS_SSO > $USERMGT_XML_PATH_IS_SSO-tmp
				sed -i "0,/<\/UserStoreManager>/s//<\/UserStoreManager-->/" $USERMGT_XML_PATH_IS_SSO-tmp
				tac $USERMGT_XML_PATH_IS_SSO-tmp > $USERMGT_XML_PATH_IS_SSO
				rm  $USERMGT_XML_PATH_IS_SSO-tmp
				sed -i -e "s/<!--UserStoreManager class=\"org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager\">/<UserStoreManager class=\"org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager\">/g" $USERMGT_XML_PATH_IS_SSO
				sed -i "0,/<\/UserStoreManager-->/s//<\/UserStoreManager>/" $USERMGT_XML_PATH_IS_SSO

				echo "$SCRIPT_TAG Configuration $sso_product - SSO IDP - Copying file based service providers"
				cp $HOME/scripts/$sso_product-sso-config/*-sp-*.xml $DYNAMIC_HOME/$product-$sso_product-SSO/repository/conf/identity/service-providers

				echo "$SCRIPT_TAG Configuration $sso_product - SSO IDP - Copying sso-idp-config"
				cp -rf $HOME/scripts/$sso_product-sso-config/sso-idp-config.xml $DYNAMIC_HOME/$product-$sso_product-SSO/repository/conf/identity/sso-idp-config.xml

				echo "$SCRIPT_TAG Configuration $sso_product - SSO IDP - Updating sso-idp-config with hostname"
				sed -i -e "s/localhost:9443/$SSO_HOST_NAME/g" $DYNAMIC_HOME/$product-$sso_product-SSO/repository/conf/identity/sso-idp-config.xml

				echo "$SCRIPT_TAG Configuration $sso_product - SSO IDP - Importing Certs"
				keytool -import -trustcacerts -alias is -file /etc/nginx/ssl/nginx-is.crt -keystore $DYNAMIC_HOME/$product-$sso_product-SSO/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt
				keytool -import -trustcacerts -alias apim -file /etc/nginx/ssl/nginx.crt -keystore $DYNAMIC_HOME/$product-$sso_product-SSO/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt
				keytool -import -trustcacerts -alias das -file /etc/nginx/ssl/nginx-das.crt -keystore $DYNAMIC_HOME/$product-$sso_product-SSO/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt
				keytool -import -trustcacerts -alias ei -file /etc/nginx/ssl/nginx-ei.crt -keystore $DYNAMIC_HOME/$product-$sso_product-SSO/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt
				keytool -import -trustcacerts -alias iot -file /etc/nginx/ssl/nginx-iot.crt -keystore $DYNAMIC_HOME/$product-$sso_product-SSO/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt

				CARBON_START_SCRIPT_PATH=$(find $DYNAMIC_HOME/$product-$sso_product-SSO | grep "wso2server.sh")
				echo "$SCRIPT_TAG Starting WSO2 Server at $CARBON_START_SCRIPT_PATH"
				nohup bash $CARBON_START_SCRIPT_PATH >/dev/null 2>&1 &

				#Uncomment below line when IS 5.4.0 is ready and added to supported product list
				#continue
			done
		fi
    #
    # End of Creating SSO IDP for WSO2 API Manager Setup
    #

		OFFSET=1

		echo "$SCRIPT_TAG Configuration Identity Server - Offset is: $OFFSET"

		echo "$SCRIPT_TAG Configuration Identity Server - Proxy Configuration"
		sed -i -e "s/<.*HostName>.*<\/HostName.*>/<HostName>$HOST_NAME_IS<\/HostName>/g" $CARBON_XML_PATH
		sed -i -e "s/<.*MgtHostName>.*<\/MgtHostName.*>/<MgtHostName>$HOST_NAME_IS<\/MgtHostName>/g" $CARBON_XML_PATH

		sed -i -e "s/<UserStoreManager class=\"org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager\">/<!--UserStoreManager class=\"org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager\">/g" $USERMGT_XML_PATH
		tac $USERMGT_XML_PATH > $USERMGT_XML_PATH-tmp
		sed -i "0,/<\/UserStoreManager>/s//<\/UserStoreManager-->/" $USERMGT_XML_PATH-tmp
		tac $USERMGT_XML_PATH-tmp > $USERMGT_XML_PATH
		rm  $USERMGT_XML_PATH-tmp
		sed -i -e "s/<!--UserStoreManager class=\"org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager\">/<UserStoreManager class=\"org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager\">/g" $USERMGT_XML_PATH
		sed -i "0,/<\/UserStoreManager-->/s//<\/UserStoreManager>/" $USERMGT_XML_PATH

		#Carbon Authenticator Configuration
		echo "$SCRIPT_TAG Configuration IS - SSO for Management Console"
		AUTHENTICATOR_XML=$(find $DYNAMIC_HOME/$product | grep "authenticators.xml")
		sed -i -e "s/Authenticator name=\"SAML2SSOAuthenticator\" disabled=\"true\"/Authenticator name=\"SAML2SSOAuthenticator\" disabled=\"false\"/g" $AUTHENTICATOR_XML
		sed -i -e "s/https:\/\/localhost:9443\/samlsso/https:\/\/$HOST_NAME_IS:443\/samlsso/g" $AUTHENTICATOR_XML
		sed -i -e "s/https:\/\/localhost:9443\/acs/https:\/\/$HOST_NAME_IS:443\/acs/g" $AUTHENTICATOR_XML

		echo "$SCRIPT_TAG Configuration IS - SSO - Copying file based service providers"
		cp $HOME/scripts/wso2is-5.3.0-sso-config/is-sp-*.xml $DYNAMIC_HOME/$product/repository/conf/identity/service-providers

		echo "$SCRIPT_TAG Configuration IS - SSO - Copying sso-idp-config"
		cp -rf $HOME/scripts/wso2is-5.3.0-sso-config/is-sso-idp-config.xml $DYNAMIC_HOME/$product/repository/conf/identity/sso-idp-config.xml

		echo "$SCRIPT_TAG Configuration IS - SSO - Updating sso-idp-config with hostname"
		sed -i -e "s/localhost:9443/$HOST_NAME_IS/g" $DYNAMIC_HOME/$product/repository/conf/identity/sso-idp-config.xml

	elif [[ $product == *"wso2das"* ]]; then

		OFFSET=2

		echo "$SCRIPT_TAG Configuration DAS - Offset is: $OFFSET"

		#Proxy Configuration
		echo "$SCRIPT_TAG Configuration DAS - Proxy Configuration"
		sed -i -e "s/<.*HostName>.*<\/HostName.*>/<HostName>$HOST_NAME_DAS<\/HostName>/g" $CARBON_XML_PATH
		sed -i -e "s/<.*MgtHostName>.*<\/MgtHostName.*>/<MgtHostName>$HOST_NAME_DAS<\/MgtHostName>/g" $CARBON_XML_PATH

		#Carbon Authenticator Configuration
		echo "$SCRIPT_TAG Configuration DAS - SSO for Management Console"
		AUTHENTICATOR_XML=$(find $DYNAMIC_HOME/$product | grep "authenticators.xml")
		sed -i -e "s/Authenticator name=\"SAML2SSOAuthenticator\" disabled=\"true\"/Authenticator name=\"SAML2SSOAuthenticator\" disabled=\"false\"/g" $AUTHENTICATOR_XML
		sed -i -e "s/https:\/\/localhost:9443\/samlsso/https:\/\/$HOST_NAME_DAS:443\/samlsso/g" $AUTHENTICATOR_XML
		sed -i -e "s/https:\/\/localhost:9443\/acs/https:\/\/$HOST_NAME_DAS:443\/acs/g" $AUTHENTICATOR_XML

		#Portal Configuration
		echo "$SCRIPT_TAG Configuration DAS - SSO for Portal"
		SITE_CONF="$DYNAMIC_HOME/$product/repository/deployment/server/jaggeryapps/portal/configs/designer.json"
		sed -i "0,/\"activeMethod\".*:.*\"basic\"/s//\"activeMethod\":\"sso\"/" $SITE_CONF
		sed -i -e "s/localhost:9443/$HOST_NAME_DAS:443/g" $SITE_CONF
		sed -i -e "s/localhost:9444/$HOST_NAME_DAS:443/g" $SITE_CONF

		#ML Configuration to Skip Authentication
		echo "$SCRIPT_TAG Configuration DAS - Authentication Skip for ML"
		LOGIN_JAG="$DYNAMIC_HOME/$product/repository/deployment/server/jaggeryapps/ml/site/home/login.jag"
		sed -i -e "s/value=\"\" placeholder=\"your username\"/value=\"admin\" placeholder=\"your username\"/g" $LOGIN_JAG
		sed -i -e "s/value=\"\" placeholder=\"your password\"/value=\"$ADMIN_PASSWORD\" placeholder=\"your password\"/g" $LOGIN_JAG
		sed -i -e "s/\$(document).ready(function() {/\$(document).ready(function() {authenticate();/g" $LOGIN_JAG

	elif [[ $product == *"wso2ei"* ]]; then

		OFFSET=3

		echo "$SCRIPT_TAG Configuration EI - Offset is: $OFFSET"

		CARBON_XML_PATH="$DYNAMIC_HOME/$product/conf/carbon.xml"
		echo "$SCRIPT_TAG carbon.xml path $CARBON_XML_PATH"

		USERMGT_XML_PATH="$DYNAMIC_HOME/$product/conf/user-mgt.xml"
		echo "$SCRIPT_TAG user-mgt.xml path $USERMGT_XML_PATH"

		CATALINA_XML_PATH="$DYNAMIC_HOME/$product/conf/tomcat/catalina-server.xml"
		echo "$SCRIPT_TAG catalina-server.xml path $CATALINA_XML_PATH"

		#Proxy Configuration
		echo "$SCRIPT_TAG Configuration EI - Proxy Configuration"
		sed -i -e "s/<.*HostName>.*<\/HostName.*>/<HostName>$HOST_NAME_EI<\/HostName>/g" $CARBON_XML_PATH
		sed -i -e "s/<.*MgtHostName>.*<\/MgtHostName.*>/<MgtHostName>$HOST_NAME_EI<\/MgtHostName>/g" $CARBON_XML_PATH

	elif [[ $product == *"wso2iot"* ]]; then

		OFFSET=4

		echo "$SCRIPT_TAG Configuration IOT - Offset is: $OFFSET"

		CARBON_XML_PATH="$DYNAMIC_HOME/$product/conf/carbon.xml"
		echo "$SCRIPT_TAG carbon.xml path $CARBON_XML_PATH"

		USERMGT_XML_PATH="$DYNAMIC_HOME/$product/conf/user-mgt.xml"
		echo "$SCRIPT_TAG user-mgt.xml path $USERMGT_XML_PATH"

		CATALINA_XML_PATH="$DYNAMIC_HOME/$product/conf/tomcat/catalina-server.xml"
		echo "$SCRIPT_TAG catalina-server.xml path $CATALINA_XML_PATH"

		#Proxy Configuration
		echo "$SCRIPT_TAG Configuration EI - Proxy Configuration"
		sed -i -e "s/<.*HostName>.*<\/HostName.*>/<HostName>$HOST_NAME_IOT<\/HostName>/g" $CARBON_XML_PATH
		sed -i -e "s/<.*MgtHostName>.*<\/MgtHostName.*>/<MgtHostName>$HOST_NAME_IOT<\/MgtHostName>/g" $CARBON_XML_PATH

	else
		echo "$SCRIPT_TAG [ERROR] Unknown product, skipping configuration"
		rm -rf $DYNAMIC_HOME/$product
		continue
		echo "$SCRIPT_TAG [ERROR] THIS SHOULD NOT PRINT"
	fi

	sed -i -e "s/port=\"9443\"/port=\"9443\" proxyPort=\"443\"/g" $CATALINA_XML_PATH
	sed -i -e "s/port=\"9763\"/port=\"9763\" proxyPort=\"80\"/g" $CATALINA_XML_PATH

	echo "$SCRIPT_TAG Configuration WSO2 Server - Importing Certs"
	keytool -import -trustcacerts -alias is -file /etc/nginx/ssl/nginx-is.crt -keystore $DYNAMIC_HOME/$product/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt
	keytool -import -trustcacerts -alias apim -file /etc/nginx/ssl/nginx.crt -keystore $DYNAMIC_HOME/$product/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt
	keytool -import -trustcacerts -alias das -file /etc/nginx/ssl/nginx-das.crt -keystore $DYNAMIC_HOME/$product/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt
	keytool -import -trustcacerts -alias ei -file /etc/nginx/ssl/nginx-ei.crt -keystore $DYNAMIC_HOME/$product/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt
	keytool -import -trustcacerts -alias iot -file /etc/nginx/ssl/nginx-iot.crt -keystore $DYNAMIC_HOME/$product/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt

	echo "$SCRIPT_TAG Configuration WSO2 Server - Offset, Password and Compression Configuration"
	sed -i -e "s/<Offset>.*<\/Offset>/<Offset>$OFFSET<\/Offset>/g" $CARBON_XML_PATH
	sed -i -e "s/<Password>admin<\/Password>/<Password>$ADMIN_PASSWORD<\/Password>/g" $USERMGT_XML_PATH
	sed -i -e "s/compression=\"on\"/compression=\"off\"/g" $CATALINA_XML_PATH

	if [[ $product == *"wso2ei"* ]]; then
		CARBON_START_SCRIPT_PATH="$DYNAMIC_HOME/$product/bin/integrator.sh"
	elif [[ $product == *"wso2iot"* ]]; then
	  CARBON_START_SCRIPT_PATH="$DYNAMIC_HOME/$product/bin/iot-server.sh"
	else
		CARBON_START_SCRIPT_PATH=$(find $DYNAMIC_HOME/$product | grep "wso2server.sh")
	fi

	echo "$SCRIPT_TAG Starting WSO2 Server at $CARBON_START_SCRIPT_PATH"
	nohup bash $CARBON_START_SCRIPT_PATH >/dev/null 2>&1 &

done

echo "$SCRIPT_TAG [END]"
