// Sensitive infomation are stored seperately in a conf file and stored in the Google drive.
var configFile = DriveApp.getFileById('place the file ID of the conf file here');
var configFileContent = file.getBlob().getDataAsString();
var parsedConfig = Utilities.parseCsv(configFileContent, '\n');

// TODO: Need to improve to not depend on the order of configs.
var userId = parsedConfig[0].toString().split("= ")[1];
var labelId = parsedConfig[1].toString().split("= ")[1];
var spreadsheetURL = parsedConfig[2].toString().split("= ")[1];
var defaultCC = parsedConfig[3].toString().split("= ")[1];
var jiraUsername = parsedConfig[4].toString().split("= ")[1];
var jiraPassword = parsedConfig[5].toString().split("= ")[1];
var recieverEmailAddress = parsedConfig[6].toString().split("= ")[1];
var jiraDomain = parsedConfig[7].toString().split("= ")[1];
var jiraProject = parsedConfig[8].toString().split("= ")[1];

/**
* This function is the main function of the process. It checks the thread list by checking
* the security@ label and creates an array of newly recieved email thread IDs. It then
* calls the necessary functions to flow through the process.
*/
function checkThreadList() {
  var emailThreadList = Gmail.Users.Threads.list(userId, {labelIds: labelId});
  var lastThreadIdSpreadsheet = SpreadsheetApp.openByUrl(spreadsheetURL).getSheets()[0];

  // Thread ID of the email thread that a ticket was last successfully created
  var latestId = spreadsheet.getRange("A1").getValue();

  // An array to store newly added thread ids
  var newThreadIds = new Array();
  for each (var thread in emailThreadList.threads) {
    if (thread["id"] == latestId) {
      // Skips the process if the threadID is equal to the latest ID
      console.log("No new mails");
      break;
    } else {
      console.log("New mail thread found: " + thread["id"]);
      newThreadIds.push(thread["id"]);
    }
  }

  if (newThreadIds.length > 0) {
    for (var i=0; i < newThreadIds.length; i++) {
      // Gets the necessary information from the mail
      var info = checkMailInfo(userId, newThreadIds[i]);

      // Creates a JIRA issue ticket
      var ticketUrl = createIssue(info.subject, info.from, newThreadIds[i], info.cc);

      if (ticketUrl != null) {
        // Sends an auto reply notification to the same email thread along with the ticket Url
        if (sendNotification(info.cc.toString(), info.subject, ticketUrl, newThreadIds[i])) {
          // Resets the threadId after ticket creation and auto reply
          updateLastProcessedThreadId(spreadsheet, newThreadIds[i]);
        } else {
          // TODO: What to do if email sending fails.
        }
      } else {
        // TODO: What to do if ticket creation fails.
      }
    }
  }
}

/**
* Gathers necessary information of an email such as Subject, To, From and CC
*
* @param userId Email address of the user
* @param messageId Message ID of the email message
*
* @return returns an object containing the subject, from and cc of a selected email
*/
function checkMailInfo(userId, messageId) {
  var headers = Gmail.Users.Messages.get(userId, messageId).payload.headers;
  var isWso2 = new Array();

  for each (var obj in headers) {
    if (obj["name"] == "Subject") {
      var subject = obj["value"];
    } else if (obj["name"] == "To") {
      var to = obj["value"];
    } else if (obj["name"] == "From") {
      var from = obj["value"];
    }

    if (obj["name"] == "Cc") {
      var cc = obj["value"];
      var mailInfo = Object.create(null, {
        subject:{
          value: subject
        },
        from:{
          value: from
        },
        cc:{
          value: isWso2
        }
      });
      return mailInfo;
    } else {
      cc = defaultCC; // If the CC section is null, sets a default cc to security@
      var mailInfo = Object.create(null, {
        subject:{
          value: subject
        },
        from:{
          value: from
        },
        cc:{
          value: cc
        }
      });
      return mailInfo;
    }
  }
}

/**
* Creates a JIRA issue ticket for each email recieved by stating the reporter,
* timestamp and the subject of the email
*
* @param subject Subject of the email to set as the summary of the ticket
* @param from Reciever of the email to fill the description
* @param threadID Thread ID of the email
* @param cc Array containing any CC participants of the email
*
* @return returns the URL of the issue ticket created
*/
function createIssue(subject, from, threadId) {
  var isSubjectEmpty = false;
  if (subject == null || subject == "") { //if the subject is null
    subject = "Security Vulnerability";
    isSubjectEmpty = true;
  }
  var reporter = jiraUsername;
  var encCred = Utilities.base64Encode(jiraUsername + ":" + jiraPassword);
  var url = "https://" + jiraDomain + "/jira/rest/api/2/issue/";

  var timestamp = Utilities.formatDate(new Date(), "IST", "yyyy-MM-dd'/'HH:mm:ss");
  var threadInfo;
  if (isSubjectEmpty) {
    threadInfo = "Please find the mail thread with ID \"" + threadId + "\" in Security@.";
  } else {
    threadInfo = "Please find the \"" + subject + "\" in Security@."
  }
  var bodyData = {
    "fields": {
      "project": {
        "key": jiraProject
      },
      "summary": subject,
      "issuetype": {
        "name": "Bug"
      },
      "reporter": {
        "name": reporter
      },
      "description": "Reporter: " + from + " \nDate: " + timestamp + "\nReference: " + threadInfo
    }
  };
  var payload = JSON.stringify(bodyData);

  var headers = {
    "Accept": "application/json",
    "Content-Type": "application/json",
    "Authorization": "Basic " + encCred,
  };
  var options = {
    "method":"POST",
    "contentType" : "application/json",
    "headers": headers,
    "payload" : payload
  };

  var response = UrlFetchApp.fetch(url, options);

  if (response.getResponseCode() == 201) {
    var textResponse = response.getContentText();
    var jsonResponse = JSON.parse(textResponse);

    var key = jsonResponse["key"];
    if (key != null || key == "") {
      var iurl = "https://" + jiraDomain + "/jira/browse/" + key;
      console.log("Jira ticket successfully created!");
      return iurl;
    } else {
      console.log("Jira ticket creation failed. Jira key is empty.");
    }
  } else {
    var err = JSON.parse(response.getContentText());
    console.log("Jira ticket creation failed with error: " + err);
  }
  return null;
}

/**
* After creating the ticket sends nd auto reply email to the same thread along with the ticket URL
*
* @param CC Array containing any CC participants of the email
* @param subject Subject of the email
* @param issueURL URL of the ticket created
* @param threadId Thread ID of the email
*
* @return returns the response of the auto reply email
*/
function sendNotification(cc, subject, issueUrl, threadId) {
  var sender = userId; // (from)
  var reciever = recieverEmailAddress; // (to)
  var username = userId;

  var timestamp = Utilities.formatDate(new Date(), "GMT+1", "dd/MM/yyyy");
  var message = "From: " + sender +
    "\nTo: " + reciever +
    "\nSubject: " + subject +
    "\nCc: " + cc +
    "\nDate: " + timestamp +
    "\nMessage-ID: sentFromGAPI" +
    "\n\nA JIRA issue has been created with the URL: " + issueUrl;
  var encodedmessage = Utilities.base64EncodeWebSafe(message);

  var resource = {
    "raw": encodedmessage,
    "payload": {
      "headers": [
        {
          "name": "to",
          "value": reciever
        },
        {
          "name": "from",
          "value": sender
        },
        {
          "name": "subject",
          "value": subject
        },
        {
          "name": "cc",
          "value": cc
        }
      ],
      "mimeType": "text/plain"
    },
    "threadId": threadId
  }
  var payload = JSON.stringify(resource);

  var requestUrl = "https://www.googleapis.com/gmail/v1/users/" + username + "/messages/send";
  var requestArguments = {
    "headers": {"Authorization": "Bearer " + ScriptApp.getOAuthToken()},
    "method": "post",
    "contentType": "application/json",
    "payload": payload,
    "muteHttpExceptions":true
  };
  var response = UrlFetchApp.fetch(requestUrl, requestArguments);

  if (response.getResponseCode() == 200) {
    console.log("Mail sent successfully!");
    return true;
  } else {
    var err = JSON.parse(response.getContentText());
    console.log("Mail sending failed with error: " + err);
  }
  return false;
}

/**
* Update the thread ID which is stored in the spreadsheet to the thread ID of the email which a
* ticket was last created.
*
* @param ss The spreadsheet which hold the thread IDs
* @param newThreadIDs Array of newly added thread IDs
*/
function updateLastProcessedThreadId(spreadsheet, threadId) {
  spreadsheet.getRange('A1').setValue(threadId);
}

/**
* To check the domain of the recieved emails
*
* @param to Reciever of the email
* @param from Sender of the email
* @param cc Array containing any CC participants of the email
*
* @return returns an array of wso2 employees by removing the users that are out of the wso2 domain
*/
function checkDomain(to, from, cc) {
  var wso2Emails = new Array();
  var externalEmails = new Array();

  var allAddresses = to + "," + from + "," + cc;
  var tempArray = allAddresses.split(",");

  for each (var item in tempArray) {
   var check = item.split('<').pop().split('>')[0];
   var regExp = new RegExp("^[a-zA-Z0-9_.+-]+@(?:(?:[a-zA-Z0-9-]+\.)?[a-zA-Z]+\.)?(wso2)\.com$");

    if (regExp.test(check) == true) {
      // Checks whther the domain is equal to wso2.com
      wso2Emails.push(item);
    } else {
      externalEmails.push(item);
    }
  }
  return wso2Emails;
}
