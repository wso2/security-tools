package org.wso2.security.advisory.beans;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is used to populate the Product object which is affected by patches, from the data received from the PMT API.
 */
public class Product {

    private Date releaseDate;
    private String platformVersion;
    private String kernelVersion;

    @SerializedName("product-code")
    private String productCode;

    @SerializedName("product-name")
    private String productName;

    @SerializedName("product-versions")
    private ArrayList<Version> versionList = new ArrayList<>();

    public Product() {

    }

    public Product(String productCode, String productName, ArrayList<Version> versionList) {
        this.productCode = productCode;
        this.productName = productName;
        this.versionList = versionList;
    }

    public Product(String productCode, String productName, Version version) {
        this.productCode = productCode;
        this.productName = productName;
        this.versionList.add(version);
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public ArrayList<Version> getVersionList() {
        return versionList;
    }

    public void setVersionList(ArrayList<Version> versionList) {
        this.versionList = versionList;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getKernelVersion() {
        return kernelVersion;
    }

    public void setKernelVersion(String kernelVersion) {
        this.kernelVersion = kernelVersion;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setVersion(Version version) {
        versionList.add(version);
    }

    public void removeVersion(String version) {
        int index = 0;
        for (Version versionToRemove :
            versionList) {
            if (versionToRemove.getVersion().equals(version)) {
                break;
            }
            index++;
        }
//        Version status=versionList.remove(index);
        versionList.remove(index);
//        System.out.println("Remove Version in Product   :"+productName+"version"+status.getVersion());
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public ArrayList<Version> getVersion() {
        return versionList;
    }

}
