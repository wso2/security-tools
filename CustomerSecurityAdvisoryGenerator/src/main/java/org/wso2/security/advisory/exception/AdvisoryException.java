package org.wso2.security.advisory.exception;

/**
 * Used to wrap the actual exception with a pdf creation.
 */
public class AdvisoryException extends Exception {

    public AdvisoryException(String msg) {
        super(msg);
    }

    public AdvisoryException(String msg, Throwable e) {
        super(msg, e);
    }
}
