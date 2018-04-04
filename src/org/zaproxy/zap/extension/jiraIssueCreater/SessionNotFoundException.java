package org.zaproxy.zap.extension.jiraIssueCreater;

/**
 * Created by kausn on 11/19/15.
 */
public class SessionNotFoundException extends Exception {

    public SessionNotFoundException(String message){
        super(message);
    }
}
