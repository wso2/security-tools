var file = DriveApp.getFileById('place the file ID of the conf file here'); //Sensitive infomation are stored seperately in a conf file and stored in the Google drive.
var csvFile = file.getBlob().getDataAsString();
var csvData = Utilities.parseCsv(csvFile);
userId = csvData[0].toString().split("= ")[1]
labelId = csvData[1].toString().split("= ")[1]
spreadsheetURL = csvData[2].toString().split("= ")[1]
defaultCC = csvData[3].toString().split("= ")[1]
token1 = csvData[4].toString().split("= ")[1]
reciever1 = csvData[5].toString().split("= ")[1]

/**
* checkThreadList() -- This function checks the thread list by checking the security@ label and  
*			creates an array of newly recieved email thread IDs.
*/

function checkThreadList(){
  
  var response = Gmail.Users.Threads.list(userId, {labelIds: labelId}); 
  
  var spreadsheet = SpreadsheetApp.openByUrl(spreadsheetURL).getSheets()[0]; 

  var data = spreadsheet.getDataRange().getValues();
  var latestId = data[0][0]; //Thread ID of the email thread that a ticket was last successfully created
  
  var newThreadIDs = new Array(); //an array to store newly added thread ids

  for each(var thread in response.threads){
    
    if(thread["id"] == latestId){ //skips the process if the threadID is equal to the latest ID
      console.log("No new mails");
      break;
    } 
    else{
      newThreadIDs.push(thread["id"]); 
    }  
  }

  if(newThreadIDs.length > 0){
    var val = resetThreadId(spreadsheet, userId, newThreadIDs) 
    var ticketMsg = val.ticketMsg;
    var mailMsg = val.mailMsg;

    console.log(ticketMsg) //Logging the success and failures of the process to the console
    console.log(mailMsg)
  }

}

/**
* resetThreadId() -- Resets the thread ID which is stored in the spreadsheet by the thread ID of the email which a ticket was last created.
*
*@param ss The spreadsheet which hold the thread IDs
*@param userId Email address of the user
*@param newThreadIDs Array of newly added thread IDs
*
*@return returns the success message of the ticket creation and the success message of the auto-reply email		
*/

function resetThreadId(ss, userId, newThreadIDs){ 
  
   for(var i = 0; i < newThreadIDs.length; i++){
     
    var values = checkMail(userId, newThreadIDs[i]); 
    var ticketMsg = values.ticketMsg;
    var mailMsg = values.mailMsg;
    
    if(ticketMsg == "Jira ticket successfully created!"){ 
      latestId = newThreadIDs[0];
      ss.getRange('A1').setValue(latestId);
    }
  }
  
  return {
        ticketMsg: ticketMsg, 
        mailMsg: mailMsg
    };  
}

/**
* checkMail() -- Gathers necessary information of an email such as Subject, To, From and CC
*
*@param userId Email address of the user
*@param messageId Message ID of the email message
*
*@return returns the success message of the ticket creation and the success message of the auto-reply email
*/

function checkMail(userId, messageId){
  
  var headers = Gmail.Users.Messages.get(userId, messageId).payload.headers; 
  
  for each (var obj in headers){
    if(obj["name"] == "Subject")
      var subject = obj["value"];
    if(obj["name"] == "To")
      var to = obj["value"]
    if(obj["name"] == "From")
      var from = obj["value"];
    if(obj["name"] == "Cc")
      var cc = obj["value"];
  }
     
  var isWso2 = new Array(); 
  
  if(cc != null){
    isWso2 = checkDomain(to, from, cc); 
    var values = createIssue(subject, from, messageId, isWso2); 
    var ticketMsg = values.ticketMsg;
    var mailMsg = values.mailMsg;
    
    return {
        ticketMsg: ticketMsg, 
        mailMsg: mailMsg
    };
  }
  else{
    cc = defaultCC; //if the CC section is null, sets a default cc to security@
    var values = createIssue(subject, from, messageId, cc);
    var ticketMsg = values.ticketMsg;
    var mailMsg = values.mailMsg;
    
    return {
        ticketMsg: ticketMsg, 
        mailMsg: mailMsg
    };
  }
  
}

/*
* checkDomain() -- To check the domain of the recieved emails
*
*@param to Reciever of the email
*@param from Sender of the email
*@param cc  Array containing any CC participants of the email
*
*@return returns an array of wso2 employees by removing the users that are out of the wso2 domain
*
*/

function checkDomain(to, from, cc){ 

  var tempArray = new Array();
  var isWso2 = new Array();
  var isCustomer = new Array();
  
  var str = to + "," + from + "," + cc;
  
  tempArray = str.split(",");
  
  for each (var item in tempArray){

    var check = item.split('<').pop().split('>')[0]

   var regExp = new RegExp("^[a-zA-Z0-9_.+-]+@(?:(?:[a-zA-Z0-9-]+\.)?[a-zA-Z]+\.)?(wso2)\.com$");
    
    if(regExp.test(check) == true){ //checks whther the domain is equal to wso2.com 
      isWso2.push(item);
    }
    else{
      isCustomer.push(item);
    }
  }
  return isWso2;
}

/*
* createIssue() -- Creates a JIRA issue ticket for each email recieved by stating the reporter, timestamp and the subject of the email
*
*@param subject Subject of the email to set as the summary of the ticket
*@param from Reciever of the email to fill the description
*@param threadID Thread ID of the email
*@param cc  Array containing any CC participants of the email
*
*@return returns the success message of the ticket creation and the success message of the auto-reply email
*/

function createIssue(subject, from, threadID, cc){

  if(subject == null){ //if the subject is null 
    subject = "Security Vulnerability";
  }
  var reporter = userId 
  var username = userId
  var token = token1;
  var encCred = Utilities.base64Encode(username + ":" + token);
  
  var bodyData = {
    "fields": {
      "project": {
        "key": "SECURITYINTERNAL"
      },
      "summary": subject,
      "issuetype": {
        "name": "Bug"
      },
      "reporter": {
        "name": reporter
      },
      "description": "Reporter: "+ from +" \nDate: "+ Utilities.formatDate(new Date(), "IST", "yyyy-MM-dd'/'HH:mm:ss") + "\nReference: Please find the \""+ subject +"\" in Security@." 
    }
  };
  
  var url = "https://WSO2_JIRA_DOMAIN/jira/rest/api/2/issue/";
  var payload = JSON.stringify(bodyData);

  var headers = { "Accept":"application/json", 
              "Content-Type":"application/json", 
              "Authorization":"Basic " + encCred,
         };

  var options = { "method":"POST",
              "contentType" : "application/json",
              "headers": headers,
              "payload" : payload
           };
  
 var response = UrlFetchApp.fetch(url, options);
 
  if(response.getResponseCode() == 201){
    
    var res = response.getContentText();
    var JSONres = JSON.parse(res);
    
    var key = JSONres["key"]

    if(key != null){
      var iurl = "https://WSO2_JIRA_DOMAIN/jira/browse/"+key;
    }
    
    var returnMessage = sendMail(cc, subject, iurl, threadID); 
    
    return {
        ticketMsg: "Jira ticket successfully created!", 
        mailMsg: returnMessage
    };
  }
  else{
    
    var err = JSON.parse(response.getContentText());
    
    return {
      ticketMsg: "Error when creating the ticket!"+err, 
      mailMsg: "Could not send the reply due to ticket creation failure!"
    };
  
  }

}

/*
* sendMail() -- After creating the ticket sends nd auto reply email to the same thread along with the ticket URL
*
*@param CC Array containing any CC participants of the email
*@param subject Subject of the email
*@param issueURL URL of the ticket created 
*@param threadId Thread ID of the email
*
*@return returns the success message of the auto-reply email
*
*/

function sendMail(CC, subject, issueUrl, threadId){ 
  
  var sender = userId; // (from)
  var reciever = reciever1; // (to)
  var cc = CC.toString(); 
  var username = userId 
  
  var message = "From: "+ sender +"\nTo: "+ reciever +"\nSubject: "+ subject +"\nCc: "+ cc +"\nDate: "+ Utilities.formatDate(new Date(), "GMT+1", "dd/MM/yyyy") +" \nMessage-ID: sentFromGAPI \n\nA JIRA issue has been created with the following URL - "+ issueUrl;
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

  var Requesturl = "https://www.googleapis.com/gmail/v1/users/"+username+"/messages/send";
 
 var RequestArguments = { "headers": {"Authorization": 'Bearer ' + ScriptApp.getOAuthToken()},
  "method": "post",
  "contentType": "application/json",
  "payload": payload,
  "muteHttpExceptions":true
 };
  
 var response = UrlFetchApp.fetch(Requesturl,RequestArguments);
 
  if(response.getResponseCode() == 200){
    return "Mail sent successfully!";
  }
  else{
    var err = JSON.parse(response.getContentText());
    return "Error when sending the mail!"+response;
  }
 
}




