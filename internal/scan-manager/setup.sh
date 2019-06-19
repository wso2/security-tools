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

# ---------------
# Update Packages
# ---------------

sudo apt update -y
sudo apt upgrade -y

# -------------
# Install MySQL
# -------------

sudo apt install -y mysql-server

read -p "Enter Password for MySQL user 'scan-manager-core': "  scan_manager_core_password
sudo mysql <<END
  CREATE DATABASE scan_manager;
  USE scan_manager;
  CREATE USER 'scan-manager-core'@'localhost' IDENTIFIED BY '$scan_manager_core_password';
  GRANT DELETE,UPDATE,SELECT,INSERT ON scan_manager.* TO 'scan-manager-core'@'localhost';
  FLUSH PRIVILEGES;
END

# -----------------
# Install Docker CE
# -----------------

sudo apt-get install -y apt-transport-https ca-certificates \
    curl gnupg-agent software-properties-common

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

sudo add-apt-repository -y \
"deb [arch=amd64] https://download.docker.com/linux/ubuntu \
$(lsb_release -cs) \
stable"

sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io

sudo groupadd docker
sudo usermod -aG docker $USER
sudo systemctl enable docker

# -------------
# Install Nginx
# -------------
sudo apt install -y nginx

# ----------
# Setup SFTP
# ----------
sudo useradd -m scan-manager-sftp -g sftp
sudo mkdir -p /home/sftp/scan-manager-sftp

sudo chown root:root /home/sftp
sudo chown scan-manager-sftp:sftp /home/sftp/scan-manager-sftp

sudo cat <<END >> /etc/ssh/sshd_config
    Match User scan-manager-sftp
        ChrootDirectory /home/sftp
        X11Forwarding no
        AllowTcpForwarding no
        ForceCommand internal-sftp
        PasswordAuthentication yes
END

sudo service sshd restart

# ---------------------
# Add Application Users
# ---------------------
sudo useradd scan-manager

# --------------
# Setup Firewall
# --------------
sudo ufw allow 'Nginx HTTP'
sudo ufw allow 'Nginx HTTPS'
sudo ufw allow 'Nginx Full'
sudo ufw allow 'OpenSSH'
# To be removed after Nginx setup is complete
sudo ufw allow 8080/tcp
# Allow access to scan-manager-core from docker containers
sudo ufw allow in on docker0 to any port 8081 proto tcp
sudo ufw status verbose
sudo ufw enable


# -----------------------------
# Install SDKMan, JDK and Maven
# -----------------------------
sudo su -

export SDKMAN_DIR="/usr/local/sdkman" && curl -s "https://get.sdkman.io" | bash - 

cat <<END >> /home/scan-manager/.bashrc
export SDKMAN_DIR="/usr/local/sdkman"
[[ -s "/usr/local/sdkman/bin/sdkman-init.sh" ]] && source "/usr/local/sdkman/bin/sdkman-init.sh"
END

source ~/.bashrc

sdk install java 8.0.212-amzn
sdk install maven 3.6.1

exit
