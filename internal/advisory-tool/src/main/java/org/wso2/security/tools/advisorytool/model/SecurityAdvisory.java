/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.security.tools.advisorytool.model;

import com.google.gson.annotations.SerializedName;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Security Advisory.
 */
@XmlRootElement
public class SecurityAdvisory {

    @SerializedName("advisory-title")
    private String title;

    @SerializedName("advisory-name")
    private String name;

    @SerializedName("advisory-date")
    private String date;

    @SerializedName("severity")
    private String severity;

    @SerializedName("score")
    private String score;

    @SerializedName("overview")
    private String overview;

    @SerializedName("description")
    private String description;

    @SerializedName("impact")
    private String impact;

    @SerializedName("solution")
    private String solution;

    @SerializedName("public-disclosure")
    private String publicDisclosure;

    @SerializedName("notes")
    private String notes;

    @SerializedName("credits")
    private String credits;

    @SerializedName("applicable-patches")
    private List<Patch> applicablePatchList = new ArrayList<>();

    @SerializedName("affected-all-products")
    private List<Product> affectedAllProducts = new ArrayList<>();

    @SerializedName("affected-patch-products")
    private List<Product> affectedPatchProducts = new ArrayList<>();

    @SerializedName("affected-wum-products")
    private List<Product> affectedWUMProducts = new ArrayList<>();

    @SerializedName("thanks")
    private String thanks;

    @SerializedName("all-affected-products")
    private List<Product> allAffectedProducts = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getOverview() {
        return overview;
    }

    public String getDescription() {
        return description;
    }

    public List<Product> getAffectedPatchProducts() {
        return affectedPatchProducts;
    }

    public void setAffectedPatchProducts(List<Product> affectedPatchProducts) {
        this.affectedPatchProducts = affectedPatchProducts;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getPublicDisclosure() {
        return publicDisclosure;
    }

    public void setPublicDisclosure(String publicDisclosure) {
        this.publicDisclosure = publicDisclosure;
    }

    public List<Patch> getApplicablePatchList() {
        return applicablePatchList;
    }

    public void setApplicablePatchList(List<Patch> applicablePatchList) {
        this.applicablePatchList = applicablePatchList;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public List<Product> getAffectedAllProducts() {
        return affectedAllProducts;
    }

    public void setAffectedAllProducts(List<Product> affectedAllProducts) {
        this.affectedAllProducts = affectedAllProducts;
    }

    public List<Product> getAffectedWUMProducts() {
        return affectedWUMProducts;
    }

    public void setAffectedWUMProducts(List<Product> affectedWUMProducts) {
        this.affectedWUMProducts = affectedWUMProducts;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public void setThanks(String thanks) {
        this.thanks = thanks;
    }

    public String getThanks() {
        return thanks;
    }

    public List<Product> getAllAffectedProducts() {
        return allAffectedProducts;
    }
}
