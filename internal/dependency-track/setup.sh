#!/bin/bash

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


######################################################################
##   This script will set up the environment for Dependency Track   ##
######################################################################

# ---------------
# Update Packages
# ---------------

sudo apt update -y
sudo apt upgrade -y

# -------------
# Install MySQL
# -------------

sudo apt install -y mysql-server

read -p "Enter Password for MySQL user 'dependency-track': "  dependency_track_password
sudo mysql <<END
  CREATE DATABASE dependency_track;
  USE dependency_track;
  CREATE USER 'dependency-track'@'localhost' IDENTIFIED BY '$dependency_track_password';
  GRANT ALL PRIVILEGES ON dependency_track.* TO 'dependency-track'@'localhost';
  FLUSH PRIVILEGES;
END

# -------------
# Install Nginx
# -------------

sudo apt install -y nginx

# ---------------------
# Add Application Users
# ---------------------	

sudo useradd dependency-track

# --------------
# Setup Firewall
# --------------

sudo ufw allow 'Nginx HTTP'
sudo ufw allow 'Nginx HTTPS'
sudo ufw allow 'Nginx Full'
sudo ufw allow 'OpenSSH'
sudo ufw deny 8080/tcp
sudo ufw status verbose
sudo ufw enable

# -----------------------------
# Install SDKMan, JDK and Maven
# -----------------------------

sudo su -

export SDKMAN_DIR="/usr/local/sdkman" && curl -s "https://get.sdkman.io" | bash - 

cat <<END >> /home/dependency-track/.bashrc
export SDKMAN_DIR="/usr/local/sdkman"
[[ -s "/usr/local/sdkman/bin/sdkman-init.sh" ]] && source "/usr/local/sdkman/bin/sdkman-init.sh"
END

source ~/.bashrc

sdk install java 8.0.212-amzn
sdk install maven 3.6.1

# -----------------------------
# Change SQL Mode
# -----------------------------

sudo cat <<END >> /home/dependency-track/etc/mysql/my.cnf
[mysqld]
sql_mode="ANSI_QUOTES,STRICT_TRANS_TABLES,ONLY_FULL_GROUP_BY,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"
END

sudo service mysql restart

# Switch user to dependency-track.
sudo su dependency-track
cd /home/dependency-track

# -----------------------------
# Get MySQL connector
# -----------------------------

mkdir libs
cd libs
wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar
cd -

# --------------------------------------------------------
# Get latest release of Dependency Track embedded war file
# --------------------------------------------------------

mkdir artifact
cd artifact
asset_type=dependency-track-embedded.war
# Download only embedded war asset.
downloadUrl = $(curl -s https://api.github.com/repos/DependencyTrack/dependency-track/releases/latest | jq -r ".assets[] | select(.name | test(\"${asset_type}\")) | .browser_download_url")
wget $downloadUrl
cd -

# ---------------------------------------------------
# Get application.properties file
# ---------------------------------------------------

mkdir config
cd config
wget https://raw.githubusercontent.com/wso2/security-tools/master/internal/dependency-track/config/application.properties
cd -

# ---------------------------------------------------
# Configure Dependency Track database as MySQL
# ---------------------------------------------------

urlData='jdbc:mysql:\/\/localhost:3306\/dependency_track?autoReconnect=true\&useSSL=false'
driver='com.mysql.cj.jdbc.Driver'
driver_path='\/home\/dependency-track\/libs\/mysql-connector-java-8.0.16.jar'
username='dependency-track'

sed -i "s/alpine.database.url=/alpine.database.url=$urlData/" /home/dependency-track/config/application.properties
sed -i "s/alpine.database.driver=/alpine.database.driver=$driver/" /home/dependency-track/config/application.properties
sed -i "s/alpine.database.driver.path=/alpine.database.driver.path=$driver_path/" /home/dependency-track/config/application.properties
sed -i "s/alpine.database.username=/alpine.database.username=$username/" /home/dependency-track/config/application.properties
sed -i "s/alpine.database.password=/alpine.database.password=$dependency_track_password/" /home/dependency-track/config/application.properties

# ---------------------------------------------------
# Start Dependency Track server in nohub mode
# ---------------------------------------------------

cd artifact
nohup java -Dalpine.application.properties=/home/dependency-track/config/application.properties -Xmx4G -jar dependency-track-embedded.war >/dev/null 2>&1 &

echo "Deploying Dependency Track..."

# -------------------------------------------------------------
# Wait until Dependency Track run Database creation scripts
# -------------------------------------------------------------

echo "Waiting for some seconds until Dependency Check runs Database creation script."
sleep 60

read -n 1 -r -s -p "If Database creation is successfully completed, Press any key to continue..."

# ---------------------------------------------------
# Alter privileges to Dependency Track Database
# ---------------------------------------------------

sudo mysql -u dependency-track -p
REVOKE ALL PRIVILEGES ON dependency_track.* TO 'dependency-track'@'localhost';
GRANT DELETE,UPDATE,SELECT,INSERT ON dependency_track.* TO 'dependency-track'@'localhost';
FLUSH PRIVILEGES;

# ---------------------------------------------------
# Configure SSL Certs
# ---------------------------------------------------

openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout /etc/ssl/private/nginx-selfsigned.key -out /etc/ssl/certs/nginx-selfsigned.crt
openssl dhparam -out /etc/ssl/certs/dhparam.pem 2048

# ---------------------------------------------------
# Configure Nginx
# ---------------------------------------------------

cat <<END > /etc/nginx/sites-enabled/dt
server {
  listen 80;
  listen [::]:80;
  
  server_name dt.private.wso2.com;
  
  return 301 https://dt.private.wso2.com$request_uri;
}

server {
  listen 443 ssl;

  server_name dt.private.wso2.com;
  
  # RSA certificat
  ssl_certificate /etc/ssl/certs/nginx-selfsigned.crt;
  ssl_certificate_key /etc/ssl/private/nginx-selfsigned.key;
  
  ssl_protocols TLSv1.2;
  ssl_prefer_server_ciphers on;
  ssl_ciphers "EECDH+AESGCM:EDH+AESGCM:AES256+EECDH:AES256+EDH";
  ssl_ecdh_curve secp384r1;
  ssl_session_cache shared:SSL:10m;
  ssl_session_tickets off;
  ssl_stapling on;
  ssl_stapling_verify on;

  add_header X-Frame-Options DENY;
  add_header X-Content-Type-Options nosniff;

  ssl_dhparam /etc/ssl/certs/dhparam.pem;

  location / {
    proxy_pass http://localhost:8080/;
  }
}
END

nginx -s stop
nginx

exit
