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

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to map the security advisory data.
 */
public class SecurityAdvisoryData {

    private String title;
    private String date;
    private String overview;
    private String description;
    private String impact;
    private String solution;
    private String notes;
    private String credits;
    private String publicDisclosure;
    private String severity;
    private String score;
    private String thanks;

    private List<SecurityAdvisory> advisories = new ArrayList<>();

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getThanks() {
        return thanks;
    }

    public void setThanks(String thanks) {
        this.thanks = thanks;
    }

    public String getPublicDisclosure() {
        return publicDisclosure;
    }

    public void setPublicDisclosure(String publicDisclosure) {
        this.publicDisclosure = publicDisclosure;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getDescription() {
        return description;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
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

    public void setScore(String cvssScore) {
        this.score = cvssScore;
    }

    public List<SecurityAdvisory> getAdvisories() {
        return advisories;
    }

    public void setAdvisories(List<SecurityAdvisory> advisories) {
        this.advisories = advisories;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
