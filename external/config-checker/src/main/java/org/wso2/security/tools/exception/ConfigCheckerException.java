package org.wso2.security.tools.exception;

/**
 * Exception wrapper class for Config Checker.
 */
public class ConfigCheckerException extends Exception {

    public ConfigCheckerException(String message, Throwable e) {
        super(message);
    }

    public ConfigCheckerException(String message) {
        super(message);
    }
}
