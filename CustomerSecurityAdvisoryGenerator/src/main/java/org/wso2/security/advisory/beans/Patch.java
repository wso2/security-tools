package org.wso2.security.advisory.beans;

import com.google.gson.annotations.SerializedName;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;

public class Patch {

    private final static Logger logger = Logger.getLogger(Patch.class);

    @SerializedName("version_state")
    boolean versionState=true;

    @SerializedName("patch-name")
    private String name;

    @SerializedName("state")
    private String state=".";

    private ArrayList<String> supportJIRAs;
    private String platform;
    private String patchLifeCycleState;
    private String issueType;
    private ArrayList<String> developedBy;
    private String lifeCycle;
    private String zipLocation;
    private Date releasedOn;
    private String releasedTimestamp;
    private boolean isBackportPatch;
    private ArrayList<String> qaedBy;
    private String client;
    private ArrayList<String> applicableProducts;
    private String secuityAdvisoryName;
    private ArrayList<String> jarsIncluded;

    public Patch(String name) {
        this.name = name;

    }
    public Patch(String name,boolean versionState) {
        this.name = name;
        this.versionState=versionState;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getSupportJIRAs() {
        return supportJIRAs;
    }

    public void setSupportJIRAs(ArrayList<String> supportJIRAs) {
        this.supportJIRAs = supportJIRAs;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPatchLifeCycleState() {
        return patchLifeCycleState;
    }

    public void setPatchLifeCycleState(String patchLifeCycleState) {
        this.patchLifeCycleState = patchLifeCycleState;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public ArrayList<String> getDevelopedBy() {
        return developedBy;
    }

    public void setDevelopedBy(ArrayList<String> developedBy) {
        this.developedBy = developedBy;
    }

    public String getLifeCycle() {
        return lifeCycle;
    }

    public void setLifeCycle(String lifeCycle) {
        this.lifeCycle = lifeCycle;
    }

    public String getZipLocation() {
        return zipLocation;
    }

    public void setZipLocation(String zipLocation) {
        this.zipLocation = zipLocation;
    }

    public Date getReleasedOn() {
        return releasedOn;
    }

    public void setReleasedOn(Date releasedOn) {
        this.releasedOn = releasedOn;
    }

    public String getReleasedTimestamp() {
        return releasedTimestamp;
    }

    public void setReleasedTimestamp(String releasedTimestamp) {
        this.releasedTimestamp = releasedTimestamp;
    }

    public boolean isBackportPatch() {
        return isBackportPatch;
    }

    public void setBackportPatch(boolean backportPatch) {
        isBackportPatch = backportPatch;
    }

    public ArrayList<String> getQaedBy() {
        return qaedBy;
    }

    public void setQaedBy(ArrayList<String> qaedBy) {
        this.qaedBy = qaedBy;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public ArrayList<String> getApplicableProducts() {
        return applicableProducts;
    }

    public void setApplicableProducts(ArrayList<String> applicableProducts) {
        this.applicableProducts = applicableProducts;
    }

    public String getSecuityAdvisoryName() {
        return secuityAdvisoryName;
    }

    public void setSecuityAdvisoryName(String secuityAdvisoryName) {
        this.secuityAdvisoryName = secuityAdvisoryName;
    }

    public ArrayList<String> getJarsIncluded() {
        return jarsIncluded;
    }

    public void setJarsIncluded(ArrayList<String> jarsIncluded) {
        this.jarsIncluded = jarsIncluded;
    }


    public void displayPatchDetails() {


        logger.info("Name : " + name);
        logger.info("Platform : " + platform);
        logger.info("Issue Type : " + issueType);
        logger.info("Client : " + client);
        logger.info("Security Advisory Name : " + secuityAdvisoryName);
        logger.info("Backport Patch : " + String.valueOf(isBackportPatch));
        logger.info("ZIP File Location : " + zipLocation);
        logger.info("Life Cycle : " + lifeCycle);
        logger.info("Patch Life Cycle State : " + patchLifeCycleState);
        logger.info("Released On : " + String.valueOf(releasedOn));
        logger.info("Released Timestamp : " + releasedTimestamp);

        for (String supportJira : supportJIRAs) {
            logger.info("Support JIRA : " + supportJira);
        }

        for (String developer : developedBy) {
            logger.info("Developer : " + developer);
        }

        for (String qa : qaedBy) {
            logger.info("QAed By : " + qa);
        }

        for (String applicableProduct : applicableProducts) {
            logger.info("Applicable Product : " + applicableProduct);
        }

        for (String jarIncluded : jarsIncluded) {
            logger.info("JAR Included : " + jarIncluded);
        }
    }

}
