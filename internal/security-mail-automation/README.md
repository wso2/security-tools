# Automating SECURITYINTERNAL JIRA ticket creation

 - This tool performs automation of SECURITYINTERNAL JIRA ticket creation from security@ emails. 
  - This tool was created using Google Apps scripts. Google Apps Script is a rapid application development platform that makes it fast and easy to create business applications that integrate with G Suite. 
  - Ths tool automatically creates a JIRA ticket whenever an email comes to security@ and replies the email with the ticket URL. 
  - This tool creates the ticket and sends the email in the following way described below.  
    1. Check for new emails by comparing the thread IDs which are saved in a spreadsheet.
    2. If there is a new email, get the necessary information from that email and creates a JIRA ticket.
    3. Auto-replies an email to security@ along with the ticket URL.
   - The script is configured with a time-driven trigger to check emails for every 10 minutes. A daily report will be generated with the execution success and failures and will be sent to platform-security@ email.

### Prerequisites

  - Enable Gmail API and Drive API in the Google Apps script (As well as in the  Google Cloud Platform API Dashboard).
  - Form a new label for the security@ emails.
  - Create a spreadsheet to store the thread IDs.
  
### How to run

The following configurations needs to be done before executing the script:

  - Change necessary information in the configuration file and upload it to the Google drive. See the sample conf file.
  - Change the File ID of the configuration file in the script. File ID is the ID shown in the shareable link of the file. (https://drive.google.com/open?id=1234567890qwertyuiopasdf)
  - Update the spreadsheet with the latest thread ID of the email which a JIRA ticket was last created. Thread IDs can be retrieved by using the Gmail API reference guide (https://developers.google.com/gmail/api/v1/reference/users/threads/list).
  - Install a time-driven trigger to run each 10 minutes. The trigger should invoke the checkThreadList function.

To check the logs of the executions go to G Suite Developer Hub (https://script.google.com/home) and check the 'My executions'.
