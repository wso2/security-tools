#!/usr/bin/env bash

# Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

##########################################################################
##   This script will set up the dynamic environment(Qualys) for      ##
##   scanning the WSO2 Identity Server which are in the HOME/products ##
##   folder.                                                          ##
##   Supported Carbon Kernel version : > or = 4.5.0                   ##
##   Supported WSO2 Identity Server version  : > or = 5.9.0           ##
##########################################################################

DYNAMIC_HOME="$HOME/env-dynamic/is"
PRODUCT_HOME="$HOME/products"
ADMIN_PASSWORD=$(head -n 1 $HOME/scripts/config/DynamicAdminUser.conf)
HOST_NAME_IS=$(head -n 1 $HOME/scripts/config/DynamicHostnameIS.conf)
SCRIPT_TAG="[SEC_AUTOMATION_BUILD_DYNAMIC_ENV_WSO2_IDENTITY_SERVER]"
NODE_IP = "{Node IP Value}"

echo "$SCRIPT_TAG [START]"

echo "$SCRIPT_TAG Existing WSO2 Identity Server instances"
ps -ef | grep java | grep org.wso2.carbon.bootstrap.Bootstrap | grep wso2is | tr -s ' ' | cut -d ' ' -f2 | paste -sd "," -

if [[ $(ps -ef | grep java | grep org.wso2.carbon.bootstrap.Bootstrap | grep wso2is | tr -s ' ' | cut -d ' ' -f2) ]]; then

	echo "$SCRIPT_TAG Killing existing WSO2 Identity server instances"
	kill -9 $(ps -ef | grep java | grep org.wso2.carbon.bootstrap.Bootstrap | grep wso2is | tr -s ' ' | cut -d ' ' -f2)

	echo "$SCRIPT_TAG Existing WSO2 Identity server instances after killing"
	ps -ef | grep java | grep org.wso2.carbon.bootstrap.Bootstrap | grep wso2is | tr -s ' ' | cut -d ' ' -f2 | paste -sd "," -

fi

echo "$SCRIPT_TAG Cleaning dynamic environment home: $DYNAMIC_HOME"
rm -rf $DYNAMIC_HOME

#echo "$SCRIPT_TAG Calling WUM update process"
#bash $HOME/scripts/WumUpdate.sh

cp -R $PRODUCT_HOME $DYNAMIC_HOME
echo "$SCRIPT_TAG Copied $PRODUCT_HOME to $DYNAMIC_HOME"
product=$(ls -l $DYNAMIC_HOME | tr -s ' ' | cut -d ' ' -f9 |  grep -v -e '^$')

echo "$SCRIPT_TAG Unzip pack: $product"
unzip $product.zip

#From Carbon kernel 4.5.0 onwards, deployment.toml is used as a central configuration file.
DEPLOYMENT_CONFIG_FILE_PATH=$(find $DYNAMIC_HOME/$product | grep "deployment.toml")
echo "$SCRIPT_TAG deployment.toml path $DEPLOYMENT_CONFIG_FILE_PATH"

if [[ $product == *"wso2is"* ]]; then

	OFFSET=1

	echo "$SCRIPT_TAG Configuration Identity Server - Offset is: $OFFSET"

	echo "$SCRIPT_TAG Configuration Identity Server - Proxy Configuration"
	sed -i -e "s/hostname = \"localhost\"/hostname = \"$HOST_NAME_IS\"/g" $DEPLOYMENT_CONFIG_FILE_PATH
	sed -i -e "s/node_ip = \"127.0.0.1\"/node_ip = \"$NODE_IP\"/g" $DEPLOYMENT_CONFIG_FILE_PATH

	echo "$SCRIPT_TAG Configuration WSO2 Server - Password, offset Configuration"
	sed -i -e "0,/password = \"admin\"/s/password = \"admin\"/password = \"$ADMIN_PASSWORD\"/g" $DEPLOYMENT_CONFIG_FILE_PATH
	sed -i -e "/node_ip = /a offset = 1" $DEPLOYMENT_CONFIG_FILE_PATH

	echo "$SCRIPT_TAG Remove userstore configuration"
	sed -i -e "s/type = \"read_write_ldap\"/type = \"database\"/g" $DEPLOYMENT_CONFIG_FILE_PATH
	sed -i -e "s/connection_url = \"ldap:\/\/localhost:\${Ports.EmbeddedLDAP.LDAPServerPort}\"/#connection_url = \"ldap:\/\/localhost:\${Ports.EmbeddedLDAP.LDAPServerPort}\"/g" $DEPLOYMENT_CONFIG_FILE_PATH
	sed -i -e "s/connection_name = \"uid=admin,ou=system\"/#connection_name = \"uid=admin,ou=system\"/g" $DEPLOYMENT_CONFIG_FILE_PATH
	sed -i -e "s/connection_password = \"admin\"/#connection_password = \"admin\"/g" $DEPLOYMENT_CONFIG_FILE_PATH
	sed -i -e "s/base_dn = \"dc=wso2,dc=org\"/#base_dn = \"dc=wso2,dc=org\"/g" $DEPLOYMENT_CONFIG_FILE_PATH

	#Carbon Authenticator Configuration
	echo "$SCRIPT_TAG Configuration IS - SSO for Management Console"
	cat <<END >> $DEPLOYMENT_CONFIG_FILE_PATH

[admin_console.authenticator.saml_sso_authenticator]
enable = true
identity_provider_sso_service_url = "https://$HOST_NAME_IS:443/samlsso"
assertion_consumer_service_url = "https://$HOST_NAME_IS:443/acs"

[transport.http]
proxyPort = 80
compression = "off"

[transport.https]
proxyPort = 443
compression="off"

END

	echo "$SCRIPT_TAG Configuration IS - SSO - Copying file based service providers"
	cp $HOME/scripts/wso2is-5.3.0-sso-config/is-sp-*.xml $DYNAMIC_HOME/$product/repository/conf/identity/service-providers

	echo "$SCRIPT_TAG Configuration IS - SSO - Copying sso-idp-config"
	cp -rf $HOME/scripts/wso2is-5.3.0-sso-config/is-sso-idp-config.xml $DYNAMIC_HOME/$product/repository/conf/identity/sso-idp-config.xml

	echo "$SCRIPT_TAG Configuration IS - SSO - Updating sso-idp-config with hostname"
	sed -i -e "s/localhost:9443/$HOST_NAME_IS:443/g" $DYNAMIC_HOME/$product/repository/conf/identity/sso-idp-config.xml

else
	echo "$SCRIPT_TAG [ERROR] Unknown product, skipping configuration"
	echo "$SCRIPT_TAG [ERROR] THIS SHOULD NOT PRINT"
fi

echo "$SCRIPT_TAG Configuration WSO2 Server - Importing Certs"
keytool -import -trustcacerts -alias is -file /etc/nginx/ssl/nginx-is.crt -keystore $DYNAMIC_HOME/$product/repository/resources/security/client-truststore.jks -storepass wso2carbon -noprompt

CARBON_START_SCRIPT_PATH=$(find $DYNAMIC_HOME/$product | grep "wso2server.sh")

echo "$SCRIPT_TAG Starting WSO2 Identity Server at $CARBON_START_SCRIPT_PATH"
nohup bash $CARBON_START_SCRIPT_PATH >/dev/null 2>&1 &

echo "$SCRIPT_TAG [END]"
