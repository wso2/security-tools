package org.wso2.security.tools.scanmanager.model;
/**
 * Model class to represent a Scan status reques object coming from Scanner Side.
 */
public class ScanStatusUpdateObject {
    private Integer scanId;
    private String scanStatus;
    private String actualScannerId;

    public ScanStatusUpdateObject() {
    }

    public ScanStatusUpdateObject(Integer scanId, String scanStatus, String actualScannerId) {
        this.scanId = scanId;
        this.scanStatus = scanStatus;
        this.actualScannerId = actualScannerId;
    }

    public Integer getScanId() {
        return scanId;
    }

    public void setScanId(Integer scanId) {
        this.scanId = scanId;
    }

    public String getScanStatus() {
        return scanStatus;
    }

    public void setScanStatus(String scanStatus) {
        this.scanStatus = scanStatus;
    }

    public String getActualScannerId() {
        return actualScannerId;
    }

    public void setActualScannerId(String actualScannerId) {
        this.actualScannerId = actualScannerId;
    }
}
