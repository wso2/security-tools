#!/bin/sh

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
		sed -i -e "s/port=\"9443\"/port=\"9443\" proxyPort=\"443\"/g" $CATALINA_XML_PATH
		sed -i -e "s/port=\"9763\"/port=\"9763\" proxyPort=\"80\"/g" $CATALINA_XML_PATH

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

		#Admin Configuration (Admin application does not support SSO. Hence comenting this for now)
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

		if [[ $product == "wso2is"* ]]; then

			echo "$SCRIPT_TAG Configuration API Manager - SSO IDP in: $DYNAMIC_HOME/$product-AM-SSO"
			cp -R $DYNAMIC_HOME/$product $DYNAMIC_HOME/$product-AM-SSO

			CARBON_XML_PATH_IS_AM=$(find $DYNAMIC_HOME/$product-AM-SSO | grep "carbon.xml")
			echo "$SCRIPT_TAG carbon.xml path $CARBON_XML_PATH_IS_AM"

			USERMGT_XML_PATH_IS_AM=$(find $DYNAMIC_HOME/$product-AM-SSO | grep "user-mgt.xml")
			echo "$SCRIPT_TAG user-mgt.xml path $USERMGT_XML_PATH_IS_AM"

			CATALINA_XML_PATH_IS_AM=$(find $DYNAMIC_HOME/$product-AM-SSO | grep "catalina-server.xml")
			echo "$SCRIPT_TAG catalina-server.xml path $CATALINA_XML_PATH_IS_AM"

			echo "$SCRIPT_TAG Configuration API Manager - SSO IDP - Proxy Configuration"
			sed -i -e "s/<.*HostName>.*<\/HostName.*>/<HostName>$HOST_NAME_APIM<\/HostName>/g" $CARBON_XML_PATH_IS_AM
			sed -i -e "s/<.*MgtHostName>.*<\/MgtHostName.*>/<MgtHostName>$HOST_NAME_APIM<\/MgtHostName>/g" $CARBON_XML_PATH_IS_AM
			sed -i -e "s/port=\"9443\"/port=\"9443\" proxyPort=\"443\"/g" $CATALINA_XML_PATH_IS_AM
			sed -i -e "s/port=\"9763\"/port=\"9763\" proxyPort=\"80\"/g" $CATALINA_XML_PATH_IS_AM

			echo "$SCRIPT_TAG Configuration API Manager - SSO IDP - Offset Configuration (Offset is set to 100)"
			sed -i -e "s/<Offset>0<\/Offset>/<Offset>100<\/Offset>/g" $CARBON_XML_PATH_IS_AM

			echo "$SCRIPT_TAG Configuration API Manager - SSO IDP - Password and Compression Configuration"
			sed -i -e "s/<Password>admin<\/Password>/<Password>$ADMIN_PASSWORD<\/Password>/g" $USERMGT_XML_PATH_IS_AM
			sed -i -e "s/compression=\"on\"/compression=\"off\"/g" $CATALINA_XML_PATH_IS_AM

			sed -i -e "s/<UserStoreManager class=\"org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager\">/<!--UserStoreManager class=\"org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager\">/g" $USERMGT_XML_PATH_IS_AM
			tac $USERMGT_XML_PATH_IS_AM > $USERMGT_XML_PATH_IS_AM-tmp
			sed -i "0,/<\/UserStoreManager>/s//<\/UserStoreManager-->/" $USERMGT_XML_PATH_IS_AM-tmp
			tac $USERMGT_XML_PATH_IS_AM-tmp > $USERMGT_XML_PATH_IS_AM
			rm  $USERMGT_XML_PATH_IS_AM-tmp
			sed -i -e "s/<!--UserStoreManager class=\"org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager\">/<UserStoreManager class=\"org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager\">/g" $USERMGT_XML_PATH_IS_AM
			sed -i "0,/<\/UserStoreManager-->/s//<\/UserStoreManager>/" $USERMGT_XML_PATH_IS_AM

			echo "$SCRIPT_TAG Configuration API Manager - SSO IDP - Copying file based service providers"
			cp $HOME/scripts/am-sso-config/am-sp-*.xml $DYNAMIC_HOME/$product-AM-SSO/repository/conf/identity/service-providers

			echo "$SCRIPT_TAG Configuration API Manager - SSO IDP - Copying sso-idp-config"
			cp -rf $HOME/scripts/am-sso-config/am-sso-idp-config.xml $DYNAMIC_HOME/$product-AM-SSO/repository/conf/identity/sso-idp-config.xml

			echo "$SCRIPT_TAG Configuration API Manager - SSO IDP - Updating sso-idp-config with hostname"
			sed -i -e "s/localhost:9443/$HOST_NAME_APIM/g" $DYNAMIC_HOME/$product-AM-SSO/repository/conf/identity/sso-idp-config.xml

			echo "$SCRIPT_TAG Configuration API Manager - SSO IDP - Importing Certs"
			keytool -import -trustcacerts -alias is -file /etc/nginx/ssl/nginx-is.crt -keystore $DYNAMIC_HOME/$product-AM-SSO/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt
			keytool -import -trustcacerts -alias apim -file /etc/nginx/ssl/nginx.crt -keystore $DYNAMIC_HOME/$product-AM-SSO/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt

			CARBON_START_SCRIPT_PATH=$(find $DYNAMIC_HOME/$product-AM-SSO | grep "wso2server.sh")
			echo "$SCRIPT_TAG Starting WSO2 Server at $CARBON_START_SCRIPT_PATH"
			nohup bash $CARBON_START_SCRIPT_PATH >/dev/null 2>&1 &
		else
			echo "$SCRIPT_TAG [ERROR] Unknown product, skipping configuration"
			rm -rf $DYNAMIC_HOME/$product
			continue
			echo "$SCRIPT_TAG [ERROR] THIS SHOULD NOT PRINT"
		fi

		OFFSET=1

		echo "$SCRIPT_TAG Configuration Identity Server - Offset is: $OFFSET"

		echo "$SCRIPT_TAG Configuration Identity Server - Proxy Configuration"
		sed -i -e "s/<.*HostName>.*<\/HostName.*>/<HostName>$HOST_NAME_IS<\/HostName>/g" $CARBON_XML_PATH
		sed -i -e "s/<.*MgtHostName>.*<\/MgtHostName.*>/<MgtHostName>$HOST_NAME_IS<\/MgtHostName>/g" $CARBON_XML_PATH
		sed -i -e "s/port=\"9443\"/port=\"9443\" proxyPort=\"443\"/g" $CATALINA_XML_PATH
		sed -i -e "s/port=\"9763\"/port=\"9763\" proxyPort=\"80\"/g" $CATALINA_XML_PATH

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
		cp $HOME/scripts/is-sso-config/is-sp-*.xml $DYNAMIC_HOME/$product/repository/conf/identity/service-providers

		echo "$SCRIPT_TAG Configuration IS - SSO - Copying sso-idp-config"
		cp -rf $HOME/scripts/is-sso-config/is-sso-idp-config.xml $DYNAMIC_HOME/$product/repository/conf/identity/sso-idp-config.xml

		echo "$SCRIPT_TAG Configuration IS - SSO - Updating sso-idp-config with hostname"
		sed -i -e "s/localhost:9443/$HOST_NAME_IS/g" $DYNAMIC_HOME/$product/repository/conf/identity/sso-idp-config.xml
	fi

	echo "$SCRIPT_TAG Configuration WSO2 Server - Importing Certs"
	keytool -import -trustcacerts -alias is -file /etc/nginx/ssl/nginx-is.crt -keystore $DYNAMIC_HOME/$product/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt
	keytool -import -trustcacerts -alias apim -file /etc/nginx/ssl/nginx.crt -keystore $DYNAMIC_HOME/$product/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt

	echo "$SCRIPT_TAG Configuration WSO2 Server - Offset, Password and Compression Configuration"
	sed -i -e "s/<Offset>0<\/Offset>/<Offset>$OFFSET<\/Offset>/g" $CARBON_XML_PATH
	sed -i -e "s/<Password>admin<\/Password>/<Password>$ADMIN_PASSWORD<\/Password>/g" $USERMGT_XML_PATH
	sed -i -e "s/compression=\"on\"/compression=\"off\"/g" $CATALINA_XML_PATH

	CARBON_START_SCRIPT_PATH=$(find $DYNAMIC_HOME/$product | grep "wso2server.sh")
	echo "$SCRIPT_TAG Starting WSO2 Server at $CARBON_START_SCRIPT_PATH"
	nohup bash $CARBON_START_SCRIPT_PATH >/dev/null 2>&1 &

done

echo "$SCRIPT_TAG [END]"
