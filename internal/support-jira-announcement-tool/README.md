# Support JIRA announcement tool

This tool is implemented to automate the JIRA ticket creation during monthly security announcements. There are two shell scripts and by running these scripts, first it obtains the list of projects in the JIRA portal and then iterates through all prjoects creating a ticket in each. 

Before creating the ticket, it first creates a sample ticket and asks your confirmation to proceed. If you are satisfied with the sample ticket created, you can press 'Y' to continue. If not the process will terminate. By running this tool, It also creates a log file with the status of ticket creation.

###  Configurations

In order to run the first script (get-projects.sh) :

	replace the username and password with your JIRA account credentials in the curl command.

In order to run the second script (send-announcements.sh) :

	replace the username and password with your JIRA account credentials in the curl command.

**Skip list file**

You should have the skip list file in order to run this. It should contain the project ids, project keys and project descriptions each separated by a comma. These projects will be skipped from the ticket creation process. 

**Ticket body content file**

This file contains the description of the ticket. It should be formatted in JSON format and should be saved as a .json file. Line breaks should be replaced with '\n' character. Please find the sample-ticket-body.json file. 

There is a separate script to format the ticket body with the following configurations. Run the script and provide the ticket body content file ( saved as a .json file) path when prompted. It will create the formatted ticket body file as 'announcement.json'.

### How to Run

Since this a script and to make it an executable file, run

```
chmod 777 get-projects.sh
```

To run the scripts from the current directory, run
```
./get-projects.sh
```

To create the sample ticket, run
```
Enter sample ticket key: <PROJECT KEY> (Example : SECURITYINTERNAL)
```


