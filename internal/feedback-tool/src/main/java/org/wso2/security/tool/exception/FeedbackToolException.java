package org.wso2.security.tool.exception;

/**
 *
 */
public class FeedbackToolException extends Exception {

    /**
     *
     * @param string
     */
    public FeedbackToolException(String string) {
        super(string);
    }


    public FeedbackToolException(String msg, Throwable e) {
        super(msg, e);
    }
}
