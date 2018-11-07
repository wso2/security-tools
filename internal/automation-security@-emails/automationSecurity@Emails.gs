var file = DriveApp.getFileById('place the file ID of the conf file here');
var csvFile = file.getBlob().getDataAsString();
var csvData = Utilities.parseCsv(csvFile);
userId = csvData[0].toString().split("= ")[1]
labelId = csvData[1].toString().split("= ")[1]
spreadsheetURL = csvData[2].toString().split("= ")[1]
defaultCC = csvData[3].toString().split("= ")[1]
token1 = csvData[4].toString().split("= ")[1]
reciever1 = csvData[5].toString().split("= ")[1]

function checkThreadList(){ //To check the thread list for new emails
  
  var response = Gmail.Users.Threads.list(userId, {labelIds: labelId}); //Replace with security@ label ID
  
  var spreadsheet = SpreadsheetApp.openByUrl(spreadsheetURL).getSheets()[0]; //Create a new spreadsheet and replace the url

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

    console.log(ticketMsg)
    console.log(mailMsg)
  }

}

function resetThreadId(ss, userId, newThreadIDs){ //To reset the speadsheet value after creating the ticket
  
   for(var i = 0; i < newThreadIDs.length; i++){
     
    var values = checkMail(userId, newThreadIDs[i]); 
    var ticketMsg = values.ticketMsg;
    var mailMsg = values.mailMsg;
    
    if(ticketMsg == "Jira ticket successfully created!"){ //If the ticket was successfully created replaces the ID in the spreadsheet with the newly created thread ID
      latestId = newThreadIDs[0];
      ss.getRange('A1').setValue(latestId);
    }
  }
  
  return {
        ticketMsg: ticketMsg, 
        mailMsg: mailMsg
    };  
}

function checkMail(userId, messageId){ //To gather information of the recieved email
  
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
    isWso2 = checkDomain(to, from, cc); //the reply email will only e sent to wso2 users
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

function checkDomain(to, from, cc){ //To check the domain of the recieved emails

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

function createIssue(subject, from, threadID, cc){ //Creates a JIRA ticket for each email recieved

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
  
  var url = "https://support-staging.wso2.com/jira/rest/api/2/issue/";
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
      var iurl = "https://support-staging.wso2.com/jira/browse/"+key;
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

function sendMail(CC, subject, issueUrl, threadId){ //Sends a reply email with the ticket URL
  
  var sender = userId; //Replace with the new Gmail user (from)
  var reciever = reciever1; //always security@ (to)
  var cc = CC.toString(); 
  var username = userId //Replace with the new Gmail user
  
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




