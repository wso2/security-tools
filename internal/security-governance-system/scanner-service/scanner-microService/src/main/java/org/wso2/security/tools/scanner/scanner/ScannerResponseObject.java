package org.wso2.security.tools.scanner.scanner;

/**
 * Class to represent the object that goes with the POST request.
 */
public class ScannerResponseObject {
    private String scanID;
    private String isSuccessful;

    public String getScanID() {
        return scanID;
    }

    public void setScanID(String scanID) {
        this.scanID = scanID;
    }

    public String getIsSuccessful() {
        return isSuccessful;
    }

    public void setIsSuccessful(String isSuccessful) {
        this.isSuccessful = isSuccessful;
    }
}
