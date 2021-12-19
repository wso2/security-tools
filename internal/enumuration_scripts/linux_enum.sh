
# Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# This is derived from : https://github.com/rebootuser/LinEnum

#last logged on user information
lastlogedonusrs=`lastlog 2>/dev/null |grep -v "Never" 2>/dev/null`
if [ ! -z "$lastlogedonusrs" ]; then
  echo -e "[-] Users that have previously logged onto the system:\n$lastlogedonusrs" 
  echo -e "\n" 
fi

#who else is logged on
loggedonusrs=`w 2>/dev/null`
if [ "$loggedonusrs" ]; then
  echo -e "[-] Who else is logged on:\n$loggedonusrs"
  echo -e "\n"
fi

#contents of /etc/passwd
readpasswd=`cat /etc/passwd 2>/dev/null`
if [ "$readpasswd" ]; then
  echo -e "[-] Contents of /etc/passwd:\n$readpasswd" 
  echo -e "\n"
fi

echo "[-] Log4shell vulnerable JARs"
JAR_FILES=$(find / -name '*.jar' 2>/dev/null)
for JAR_FILE in $JAR_FILES
do
  CONTAINS_JNDI_LOOKUP=$(unzip -l "$JAR_FILE" | grep "org/apache/logging/log4j/core/lookup/JndiLookup.class")
  if [ -n "$CONTAINS_JNDI_LOOKUP" ]; then
    echo "$JAR_FILE"
  fi
done
echo -e "\n"



superman=`grep -v -E "^#" /etc/passwd 2>/dev/null| awk -F: '$3 == 0 { print $1}' 2>/dev/null`
if [ "$superman" ]; then
  echo -e "[-] Super user account(s):\n$superman"
  echo -e "\n"
fi


LOCAL_USERS=$(cat /etc/passwd | grep -v "/usr/sbin/nologin\|/bin/false\|/bin/sync")
if [ ! -z "$LOCAL_USERS" ]; then
  echo "[-] Checking .ssh folders"
  for user in $LOCAL_USERS ; do
    LUSER_HOME=$(echo $user | cut -d':' -f6)
    if [[ -d "$LUSER_HOME/.ssh" && $(find "$LUSER_HOME/.ssh/" -type f) ]]; then
      echo -e "[+] Found .ssh in $LUSER_HOME"
      find "$LUSER_HOME/.ssh/" -type f
    fi
  done
  echo -e "\n"

  echo "[-] Checking history files"
  for user in $LOCAL_USERS; do
    LUSER_HOME=$(echo $user | cut -d':' -f6)
    if [ -f "$LUSER_HOME/.bash_history" ]; then
      echo "$ cat $LUSER_HOME/.bash_history"
      cat "$LUSER_HOME/.bash_history"
    fi

    if [ -f "$LUSER_HOME/.zsh_history" ]; then
      echo "$ cat $LUSER_HOME/.zsh_history"
      cat "$LUSER_HOME/.zsh_history"
    fi
  done
fi
echo -e "\n"


#looks for hidden files
if [ "$thorough" = "1" ]; then
  hiddenfiles=`find / -name ".*" -type f ! -path "/proc/*" ! -path "/sys/*" -exec ls -al {} \; 2>/dev/null`
  if [ "$hiddenfiles" ]; then
    echo -e "[-] Hidden files:\n$hiddenfiles"
    echo -e "\n"
  fi
fi

# Coverd from : https://github.com/Neo23x0/log4shell-detector
# LOG_DIRS=$(find / -name "logs" -o -name "log" -type d 2>/dev/null)
# if [ ! -z "$LOG_DIRS" ]; then
#   for dir in $LOG_DIRS; do
#     # grep_out=$(grep -irn -E "(?:\$(?:\((?:\(.*\)|.*)\)|\{.*})|[<>]\(.*\))" "$dir" 2>/dev/null)
#     # grep_out=$(grep -irn -E "jndi" "$dir" 2>/dev/null)
#     grep_out=$(grep -irn -E "(?:[\$|%24](?:\((?:\(.*\)|.*)\)|\{.*})|[<>]\(.*\)([?>]))" -E "jndi" "$dir" 2>/dev/null)
#     if [ ! -z "$grep_out" ]; then
#       echo "$dir >>>>>>>>>>>>>>>"
#       echo "$grep_out"
#     fi
#   done
#   echo -e "\n"
# fi


job_info()
{
	echo -e "### JOBS/TASKS ##########################################" 

	#are there any cron jobs configured
	cronjobs=`ls -la /etc/cron* 2>/dev/null`
	if [ "$cronjobs" ]; then
		  echo -e "[-] Cron jobs:\n$cronjobs" 
		    echo -e "\n"
	fi

	#can we manipulate these jobs in any way
	cronjobwwperms=`find /etc/cron* -perm -0002 -type f -exec ls -la {} \; -exec cat {} 2>/dev/null \;`
	if [ "$cronjobwwperms" ]; then
		  echo -e "[+] World-writable cron jobs and file contents:\n$cronjobwwperms" 
		    echo -e "\n"
	fi

	#contab contents
	crontabvalue=`cat /etc/crontab 2>/dev/null`
	if [ "$crontabvalue" ]; then
		  echo -e "[-] Crontab contents:\n$crontabvalue" 
		    echo -e "\n"
	fi

	crontabvar=`ls -la /var/spool/cron/crontabs 2>/dev/null`
	if [ "$crontabvar" ]; then
		  echo -e "[-] Anything interesting in /var/spool/cron/crontabs:\n$crontabvar" 
		    echo -e "\n"
	fi

	anacronjobs=`ls -la /etc/anacrontab 2>/dev/null; cat /etc/anacrontab 2>/dev/null`
	if [ "$anacronjobs" ]; then
		  echo -e "[-] Anacron jobs and associated file permissions:\n$anacronjobs" 
		    echo -e "\n"
	fi

	anacrontab=`ls -la /var/spool/anacron 2>/dev/null`
	if [ "$anacrontab" ]; then
		  echo -e "[-] When were jobs last executed (/var/spool/anacron contents):\n$anacrontab" 
		    echo -e "\n"
	fi

	#pull out account names from /etc/passwd and see if any users have associated cronjobs (priv command)
	cronother=`cut -d ":" -f 1 /etc/passwd | xargs -n1 crontab -l -u 2>/dev/null`
	if [ "$cronother" ]; then
		  echo -e "[-] Jobs held by all users:\n$cronother" 
		    echo -e "\n"
	fi

	# list systemd timers
	if [ "$thorough" = "1" ]; then
		  # include inactive timers in thorough mode
		    systemdtimers="$(systemctl list-timers --all 2>/dev/null)"
		      info=""
	      else
		        systemdtimers="$(systemctl list-timers 2>/dev/null |head -n -1 2>/dev/null)"
			  # replace the info in the output with a hint towards thorough mode
			    info="Enable thorough tests to see inactive timers"
	fi
	if [ "$systemdtimers" ]; then
		  echo -e "[-] Systemd timers:\n$systemdtimers\n$info"
		    echo -e "\n"
	fi

}

networking_info()
{
	echo -e "### NETWORKING  ##########################################" 

	#nic information
	nicinfo=`/sbin/ifconfig -a 2>/dev/null`
	if [ "$nicinfo" ]; then
		  echo -e "[-] Network and IP info:\n$nicinfo" 
		    echo -e "\n"
	fi

	#nic information (using ip)
	nicinfoip=`/sbin/ip a 2>/dev/null`
	if [ ! "$nicinfo" ] && [ "$nicinfoip" ]; then
		  echo -e "[-] Network and IP info:\n$nicinfoip" 
		    echo -e "\n"
	fi

	arpinfo=`arp -a 2>/dev/null`
	if [ "$arpinfo" ]; then
		  echo -e "[-] ARP history:\n$arpinfo" 
		    echo -e "\n"
	fi

	arpinfoip=`ip n 2>/dev/null`
	if [ ! "$arpinfo" ] && [ "$arpinfoip" ]; then
		  echo -e "[-] ARP history:\n$arpinfoip" 
		    echo -e "\n"
	fi

	#dns settings
	nsinfo=`grep "nameserver" /etc/resolv.conf 2>/dev/null`
	if [ "$nsinfo" ]; then
		  echo -e "[-] Nameserver(s):\n$nsinfo" 
		    echo -e "\n"
	fi

	nsinfosysd=`systemd-resolve --status 2>/dev/null`
	if [ "$nsinfosysd" ]; then
		  echo -e "[-] Nameserver(s):\n$nsinfosysd" 
		    echo -e "\n"
	fi

	#default route configuration
	defroute=`route 2>/dev/null | grep default`
	if [ "$defroute" ]; then
		  echo -e "[-] Default route:\n$defroute" 
		    echo -e "\n"
	fi

	#default route configuration
	defrouteip=`ip r 2>/dev/null | grep default`
	if [ ! "$defroute" ] && [ "$defrouteip" ]; then
		  echo -e "[-] Default route:\n$defrouteip" 
		    echo -e "\n"
	fi

	#listening TCP
	tcpservs=`netstat -ntpl 2>/dev/null`
	if [ "$tcpservs" ]; then
		  echo -e "[-] Listening TCP:\n$tcpservs" 
		    echo -e "\n"
	fi

	tcpservsip=`ss -t -l -n 2>/dev/null`
	if [ ! "$tcpservs" ] && [ "$tcpservsip" ]; then
		  echo -e "[-] Listening TCP:\n$tcpservsip" 
		    echo -e "\n"
	fi

	#listening UDP
	udpservs=`netstat -nupl 2>/dev/null`
	if [ "$udpservs" ]; then
		  echo -e "[-] Listening UDP:\n$udpservs" 
		    echo -e "\n"
	fi

	udpservsip=`ss -u -l -n 2>/dev/null`
	if [ ! "$udpservs" ] && [ "$udpservsip" ]; then
		  echo -e "[-] Listening UDP:\n$udpservsip" 
		    echo -e "\n"
	fi
}

services_info()
{
	echo -e "### SERVICES #############################################" 

	#running processes
	psaux=`ps aux 2>/dev/null`
	if [ "$psaux" ]; then
		  echo -e "[-] Running processes:\n$psaux" 
		    echo -e "\n"
	fi

	#lookup process binary path and permissisons
	procperm=`ps aux 2>/dev/null | awk '{print $11}'|xargs -r ls -la 2>/dev/null |awk '!x[$0]++' 2>/dev/null`
	if [ "$procperm" ]; then
		  echo -e "[-] Process binaries and associated permissions (from above list):\n$procperm" 
		    echo -e "\n"
	fi

	if [ "$export" ] && [ "$procperm" ]; then
		procpermbase=`ps aux 2>/dev/null | awk '{print $11}' | xargs -r ls 2>/dev/null | awk '!x[$0]++' 2>/dev/null`
		  mkdir $format/ps-export/ 2>/dev/null
		    for i in $procpermbase; do cp --parents $i $format/ps-export/; done 2>/dev/null
	fi

	#anything 'useful' in inetd.conf
	inetdread=`cat /etc/inetd.conf 2>/dev/null`
	if [ "$inetdread" ]; then
		  echo -e "[-] Contents of /etc/inetd.conf:\n$inetdread" 
		    echo -e "\n"
	fi

	if [ "$export" ] && [ "$inetdread" ]; then
		  mkdir $format/etc-export/ 2>/dev/null
		    cp /etc/inetd.conf $format/etc-export/inetd.conf 2>/dev/null
	fi

	#very 'rough' command to extract associated binaries from inetd.conf & show permisisons of each
	inetdbinperms=`awk '{print $7}' /etc/inetd.conf 2>/dev/null |xargs -r ls -la 2>/dev/null`
	if [ "$inetdbinperms" ]; then
		  echo -e "[-] The related inetd binary permissions:\n$inetdbinperms" 
		    echo -e "\n"
	fi

	xinetdread=`cat /etc/xinetd.conf 2>/dev/null`
	if [ "$xinetdread" ]; then
		  echo -e "[-] Contents of /etc/xinetd.conf:\n$xinetdread" 
		    echo -e "\n"
	fi

	if [ "$export" ] && [ "$xinetdread" ]; then
		  mkdir $format/etc-export/ 2>/dev/null
		    cp /etc/xinetd.conf $format/etc-export/xinetd.conf 2>/dev/null
	fi

	xinetdincd=`grep "/etc/xinetd.d" /etc/xinetd.conf 2>/dev/null`
	if [ "$xinetdincd" ]; then
		  echo -e "[-] /etc/xinetd.d is included in /etc/xinetd.conf - associated binary permissions are listed below:"; ls -la /etc/xinetd.d 2>/dev/null 
		    echo -e "\n"
	fi

	#very 'rough' command to extract associated binaries from xinetd.conf & show permisisons of each
	xinetdbinperms=`awk '{print $7}' /etc/xinetd.conf 2>/dev/null |xargs -r ls -la 2>/dev/null`
	if [ "$xinetdbinperms" ]; then
		  echo -e "[-] The related xinetd binary permissions:\n$xinetdbinperms" 
		    echo -e "\n"
	fi

	initdread=`ls -la /etc/init.d 2>/dev/null`
	if [ "$initdread" ]; then
		  echo -e "[-] /etc/init.d/ binary permissions:\n$initdread" 
		    echo -e "\n"
	fi

	#init.d files NOT belonging to root!
	initdperms=`find /etc/init.d/ \! -uid 0 -type f 2>/dev/null |xargs -r ls -la 2>/dev/null`
	if [ "$initdperms" ]; then
		  echo -e "[-] /etc/init.d/ files not belonging to root:\n$initdperms" 
		    echo -e "\n"
	fi

	rcdread=`ls -la /etc/rc.d/init.d 2>/dev/null`
	if [ "$rcdread" ]; then
		  echo -e "[-] /etc/rc.d/init.d binary permissions:\n$rcdread" 
		    echo -e "\n"
	fi

	#init.d files NOT belonging to root!
	rcdperms=`find /etc/rc.d/init.d \! -uid 0 -type f 2>/dev/null |xargs -r ls -la 2>/dev/null`
	if [ "$rcdperms" ]; then
		  echo -e "[-] /etc/rc.d/init.d files not belonging to root:\n$rcdperms" 
		    echo -e "\n"
	fi

	usrrcdread=`ls -la /usr/local/etc/rc.d 2>/dev/null`
	if [ "$usrrcdread" ]; then
		  echo -e "[-] /usr/local/etc/rc.d binary permissions:\n$usrrcdread" 
		    echo -e "\n"
	fi

	#rc.d files NOT belonging to root!
	usrrcdperms=`find /usr/local/etc/rc.d \! -uid 0 -type f 2>/dev/null |xargs -r ls -la 2>/dev/null`
	if [ "$usrrcdperms" ]; then
		  echo -e "[-] /usr/local/etc/rc.d files not belonging to root:\n$usrrcdperms" 
		    echo -e "\n"
	fi

	initread=`ls -la /etc/init/ 2>/dev/null`
	if [ "$initread" ]; then
		  echo -e "[-] /etc/init/ config file permissions:\n$initread"
		    echo -e "\n"
	fi

	# upstart scripts not belonging to root
	initperms=`find /etc/init \! -uid 0 -type f 2>/dev/null |xargs -r ls -la 2>/dev/null`
	if [ "$initperms" ]; then
		   echo -e "[-] /etc/init/ config files not belonging to root:\n$initperms"
		      echo -e "\n"
	fi

	systemdread=`ls -lthR /lib/systemd/ 2>/dev/null`
	if [ "$systemdread" ]; then
		  echo -e "[-] /lib/systemd/* config file permissions:\n$systemdread"
		    echo -e "\n"
	fi

	# systemd files not belonging to root
	systemdperms=`find /lib/systemd/ \! -uid 0 -type f 2>/dev/null |xargs -r ls -la 2>/dev/null`
	if [ "$systemdperms" ]; then
		   echo -e "[+] /lib/systemd/* config files not belonging to root:\n$systemdperms"
		      echo -e "\n"
	fi
}


job_info
networking_info
services_info

