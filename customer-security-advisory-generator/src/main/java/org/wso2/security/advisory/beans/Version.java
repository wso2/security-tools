package org.wso2.security.advisory.beans;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nadeeshani on 8/4/17.
 */
public class Version {

    @SerializedName("version")
    String version;

    @SerializedName("version-patch")
    List<Patch> patchList = new ArrayList<Patch>();

    @SerializedName("is-wum-supported")
    private boolean isWumSupported;

    @SerializedName("is-patch-supported")
    private boolean isPatchSupported;

    @SerializedName("expanded-versions")
    private int expandedVersions;

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    private Date releaseDate;

    public Version(String version) {
        this.version = version;
    }

    public Version(String version, boolean isWumSupported, boolean isPatchSupported) {
        this.version = version;
        this.isWumSupported = isWumSupported;
        this.isPatchSupported = isPatchSupported;
    }

    public boolean isWumSupported() {
        return isWumSupported;
    }

    public void setWumSupported(boolean wumSupported) {
        isWumSupported = wumSupported;
    }

    public boolean isPatchSupported() {
        return isPatchSupported;
    }

    public void setPatchSupported(boolean patchSupported) {
        isPatchSupported = patchSupported;
    }

    public List<Patch> getPatchList() {
        return patchList;
    }

    public void setPatchList(Patch patch) {
        this.patchList.add(patch);
        expandedVersions=patchList.size();
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

}
