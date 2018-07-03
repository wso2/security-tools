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
package org.wso2.security.tools.advisorytool.builders;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.security.tools.advisorytool.config.Configuration;
import org.wso2.security.tools.advisorytool.exeption.AdvisoryToolException;
import org.wso2.security.tools.advisorytool.model.Header;
import org.wso2.security.tools.advisorytool.model.Patch;
import org.wso2.security.tools.advisorytool.model.Product;
import org.wso2.security.tools.advisorytool.model.SecurityAdvisory;
import org.wso2.security.tools.advisorytool.model.Version;
import org.wso2.security.tools.advisorytool.utils.Constants;
import org.wso2.security.tools.advisorytool.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * SecurityAdvisoryBuilder class to build the security advisory object.
 */
public abstract class SecurityAdvisoryBuilder {
    private static final Logger logger = Logger.getLogger(SecurityAdvisoryBuilder.class);

    /**
     * Filling the advisory data from the PMT.
     *
     * @param securityAdvisory
     * @throws AdvisoryToolException
     */
    public abstract void setAdvisoryData(SecurityAdvisory securityAdvisory) throws AdvisoryToolException;

    /**
     * Creating the affected products list.
     *
     * @throws AdvisoryToolException
     */
    public abstract void buildAffectedProductsList() throws AdvisoryToolException;

    /**
     * Populating the advisory object.
     *
     * @throws AdvisoryToolException
     */
    public abstract void buildAdvisory() throws AdvisoryToolException;

    public abstract SecurityAdvisory getSecurityAdvisory();

    /**
     * Get patch details from the PMT.
     *
     * @param patchName
     * @return
     */
    protected Patch getPatchDetails(String patchName) throws AdvisoryToolException {
        int jsonMainArrayIndex;
        StringBuilder result = null;
        Patch patch = new Patch();
        List<String> applicableProductAndVersionStringList = new ArrayList<>();

        String url = Configuration.getInstance().getPatchDetailsAPI();
        url = url.concat(patchName);
        ArrayList<Header> headerList = new ArrayList<>();
        headerList.add(new Header(Constants.AUTHORIZATION_HEADER_NAME, Configuration.getInstance()
                .getPatchDetailsAPIAuthHeader()));
        try {
            result = Util.httpConnection(url, headerList);

            if (String.valueOf(result).contains("Resource not found")) {
                throw new AdvisoryToolException("Patch " + patchName + " cannot be found in the PMT.");
            }

            JSONArray jsonMainArr = new JSONArray(String.valueOf(result));
            for (jsonMainArrayIndex = 0; jsonMainArrayIndex < jsonMainArr.length();
                 jsonMainArrayIndex++) {
                String jsonObjectString = jsonMainArr.getJSONObject(jsonMainArrayIndex)
                        .getString(Constants.NAME_OBJECT_STRING);

                //checking whether the patch is in the released state.
                if (Constants.PATCH_LIFECYCLE_STATE.equals(jsonObjectString)) {
                    JSONObject jsonObject = jsonMainArr.getJSONObject(jsonMainArrayIndex);
                    JSONArray jsonArray = jsonObject.getJSONArray(Constants.VALUE_OBJECT_STRING);

                    for (int jsonArrayIndex = 0; jsonArrayIndex < jsonArray.length();
                         jsonArrayIndex++) {
                        if (!jsonArray.get(jsonArrayIndex).toString()
                                .contains(Constants.PATCH_LIFECYCLE_RELEASED_STATE)) {

                            //if the patch is not in the released state.
                            logger.warn("Patch " + patchName + " is in the state of "
                                    + jsonArray.get(jsonArrayIndex).toString());
                            return null;
                        }

                    }
                }

                if (Constants.APPLICABLE_PRODUCTS.equals(jsonObjectString)) {
                    JSONObject jsonObject = jsonMainArr.getJSONObject(jsonMainArrayIndex);
                    JSONArray jsonArray = jsonObject.getJSONArray(Constants.VALUE_OBJECT_STRING);
                    for (int jsonArrayIndex = 0; jsonArrayIndex < jsonArray.length();
                         jsonArrayIndex++) {
                        String[] array = jsonArray.get(jsonArrayIndex).toString()
                                .split("(?=\\s\\d)");
                        if (!"Carbon".equals(array[0].trim())) {

                            //WSO2 prefix will get appended to the product names returned from the PMT
                            // as we maintain products in our Products.xml with the WSO2 prefix.
                            if (!applicableProductAndVersionStringList.contains("WSO2 ".concat(jsonArray
                                    .get(jsonArrayIndex).toString().trim()))) {
                                applicableProductAndVersionStringList.add("WSO2 ".concat(jsonArray
                                        .get(jsonArrayIndex).toString().trim()));
                            }

                        } else {

                            //If the applicable product is a kernel version, the relevant products
                            // included in the that kernel version are searched from the Products.xml
                            applicableProductAndVersionStringList
                                    .addAll(Util.getApplicableProductsForAKernel(array[1].trim()));
                        }
                    }
                }
            }
            patch.setName(patchName);
            patch.setApplicableProductAndVersionStrings(applicableProductAndVersionStringList);
        } catch (JSONException e) {
            throw new AdvisoryToolException("Error occurred while retrieving the patch details from PMT.", e);
        }
        return patch;
    }

    /**
     * Get the advisory data (e.g., description, impact, etc) from the PMT.
     *
     * @param advisoryName
     * @return
     */
    protected SecurityAdvisory getAdvisoryData(String advisoryName) throws AdvisoryToolException {
        SecurityAdvisory securityAdvisory = new SecurityAdvisory();
        StringBuilder result;

        String url = Configuration.getInstance().getAdvisoryDetailsAPI();
        url = url.concat(advisoryName);
        ArrayList<Header> headerList = new ArrayList<>();
        headerList.add(new Header(Constants.AUTHORIZATION_HEADER_NAME, Configuration.getInstance()
                .getAdvisoryDetailsAPIAuthHeader()));
        try {
            result = Util.httpConnection(url, headerList);
            if (String.valueOf(result).contains("Resource not found")) {
                throw new AdvisoryToolException("The advisory name " + advisoryName
                        + " cannot be found in the PMT.");
            }
        } catch (AdvisoryToolException e) {
            throw new AdvisoryToolException("Error occurred while receiving the " + advisoryName +
                    " advisory details from the PMT", e);
        }

        ArrayList<String> advisoryDetails = new ArrayList<>();
        try {

            JSONArray jsonMainArr = new JSONArray(String.valueOf(result));
            JSONArray jsonArray;
            JSONObject jsonObject;
            for (int i = 0; i < jsonMainArr.length(); i++) {
                jsonObject = jsonMainArr.getJSONObject(i);
                jsonArray = jsonObject.getJSONArray("value");
                advisoryDetails.add(jsonArray.get(0).toString());
            }

            securityAdvisory.setName(advisoryName);

            if (getIndexOfObjectFromJsonArray(Constants.OVERVIEW, jsonMainArr) != -1) {
                securityAdvisory.setOverview(advisoryDetails
                        .get(getIndexOfObjectFromJsonArray(Constants.OVERVIEW, jsonMainArr)));
            }

            if (getIndexOfObjectFromJsonArray(Constants.SEVERITY, jsonMainArr) != -1) {
                securityAdvisory.setSeverity(advisoryDetails
                        .get(getIndexOfObjectFromJsonArray(Constants.SEVERITY, jsonMainArr)));
            }

            if (getIndexOfObjectFromJsonArray(Constants.DESCRIPTION, jsonMainArr) != -1) {
                securityAdvisory.setDescription(advisoryDetails
                        .get(getIndexOfObjectFromJsonArray(Constants.DESCRIPTION, jsonMainArr)));
            }

            if (getIndexOfObjectFromJsonArray(Constants.IMPACT, jsonMainArr) != -1) {
                securityAdvisory.setImpact(advisoryDetails
                        .get(getIndexOfObjectFromJsonArray(Constants.IMPACT, jsonMainArr)));
            }

            if (getIndexOfObjectFromJsonArray(Constants.SOLUTION, jsonMainArr) != -1) {
                securityAdvisory.setSolution(advisoryDetails
                        .get(getIndexOfObjectFromJsonArray(Constants.SOLUTION, jsonMainArr)));
            }

            if (getIndexOfObjectFromJsonArray(Constants.NOTE, jsonMainArr) != -1) {
                securityAdvisory.setNotes(advisoryDetails
                        .get(getIndexOfObjectFromJsonArray(Constants.NOTE, jsonMainArr)));
            }

            if (getIndexOfObjectFromJsonArray(Constants.CVSS_SCORE, jsonMainArr) != -1) {
                securityAdvisory.setScore(advisoryDetails
                        .get(getIndexOfObjectFromJsonArray(Constants.CVSS_SCORE, jsonMainArr)));
            }
        } catch (JSONException e) {
            throw new AdvisoryToolException("Error occurred while setting the advisory data from PMT", e);
        }
        return securityAdvisory;
    }

    /**
     * Get the array index for the given tag.
     *
     * @param tag
     * @param jsonArray
     * @return
     * @throws JSONException
     */
    private int getIndexOfObjectFromJsonArray(String tag, JSONArray jsonArray) throws JSONException {
        JSONObject jo;
        for (int i = 0; i < jsonArray.length(); i++) {
            jo = jsonArray.getJSONObject(i);
            if (jo.get("name").equals(tag)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * This method is used to get the applicable patch list for a given advisory.
     *
     * @param advisoryName
     * @return
     */
    protected List<Patch> getApplicablePatchListForAdvisory(String advisoryName)
            throws AdvisoryToolException {
        List<Patch> applicablePatchList = new ArrayList<>();
        List<String> patchNamesListForAdvisory;

        patchNamesListForAdvisory = getPatchNamesListForAdvisory(advisoryName);
        for (String patchName : patchNamesListForAdvisory) {
            Patch patch = getPatchDetails(patchName);
            if (patch != null) {
                applicablePatchList.add(patch);
            }
        }
        return applicablePatchList;
    }

    /**
     * This method retrieves the related names list for a given advisory number.
     *
     * @param advisoryName
     */
    protected List<String> getPatchNamesListForAdvisory(String advisoryName)
            throws AdvisoryToolException {
        String[] patchArray;
        List<String> patchList = new ArrayList<>();
        StringBuilder result = null;

        String url = Configuration.getInstance().getPatchListAPI();
        url = url.concat(advisoryName);
        ArrayList<Header> headerList = new ArrayList<>();
        headerList.add(new Header(Constants.AUTHORIZATION_HEADER_NAME, Configuration.getInstance()
                .getPatchListAPIAuthHeader()));
        try {
            result = Util.httpConnection(url, headerList);
            JSONObject jsonOb = new JSONObject(String.valueOf(result));
            String patches = jsonOb.getString(Constants.PATCHES_OBJECT_STRING);

            //During an error, the message is returned as the patches array value instead
            // of the patch list.
            if ("Couldn't find any patches in the PMT".equals(patches)) {
                throw new AdvisoryToolException("Couldn't find any patches in the PMT");
            }

            patchArray = patches.split(",");
            for (String patchName : patchArray) {
                if (patchName.startsWith(Constants.PATCH_NAME_PREFIX)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("the patch " + patchName + " is applicable for the advisory "
                                + advisoryName);
                    }
                    patchList.add(patchName);
                } else {
                    throw new AdvisoryToolException("Invalid Patch name " + patchName);
                }
            }
        } catch (JSONException e) {
            throw new AdvisoryToolException("Error occurred while retrieving the patch list " +
                    "for an advisory", e);
        }
        return patchList;
    }

    /**
     * This method is used to extract the WUM supported products list from the affected products list.
     *
     * @param affectedProductsList
     * @return
     */
    protected List<Product> getWUMProductsFromAffectedProducts(List<Product> affectedProductsList) {
        List<Product> affectedWUMProductsList = new ArrayList<>();
        List<Version> affectedWUMVersionList = new ArrayList<>();

        logger.debug("Extracting the affected WUM supported products");
        for (Product affectedProduct : affectedProductsList) {
            Product affectedWUMProduct = new Product();
            for (Version affectedVersion : affectedProduct.getVersionList()) {
                if (affectedVersion.isWumSupported()) {
                    affectedWUMVersionList.add(affectedVersion);
                }
            }

            if (!affectedWUMVersionList.isEmpty()) {
                affectedWUMProduct.setName(affectedProduct.getName());
                affectedWUMProduct.setCodeName(affectedProduct.getCodeName());
                affectedWUMProduct.getVersionList().addAll(affectedWUMVersionList);
                affectedWUMProductsList.add(affectedWUMProduct);
                if (logger.isDebugEnabled()) {
                    logger.debug("Product name : " + affectedProduct.getName());
                    logger.debug("Product version list");

                    for (Version version : affectedWUMVersionList) {
                        logger.debug("Version : " + version.getVersionNumber());
                    }
                }
                affectedWUMVersionList.clear();
            }
        }
        return affectedWUMProductsList;
    }

    /**
     * This method is used to extract the Patch supported products list from the affected products list.
     *
     * @param affectedProductsList
     * @return
     */
    protected List<Product> getPatchSupportedProducts(List<Product> affectedProductsList) {
        List<Product> affectedPatchSupportedProductsList = new ArrayList<>();
        List<Version> affectedPatchSupportedVersionList = new ArrayList<>();
        Product affectedPatchSupportedProduct;

        logger.debug("Extracting the affected patch supported products");
        for (Product affectedProduct : affectedProductsList) {
            affectedPatchSupportedProduct = new Product();
            for (Version affectedVersion : affectedProduct.getVersionList()) {
                if (affectedVersion.isPatchSupported()) {
                    affectedPatchSupportedVersionList.add(affectedVersion);
                }
            }

            if (!affectedPatchSupportedVersionList.isEmpty()) {
                affectedPatchSupportedProduct.setName(affectedProduct.getName());
                affectedPatchSupportedProduct.setCodeName(affectedProduct.getCodeName());
                affectedPatchSupportedProduct.getVersionList()
                        .addAll(affectedPatchSupportedVersionList);
                affectedPatchSupportedProductsList.add(affectedPatchSupportedProduct);

                if (logger.isDebugEnabled()) {
                    logger.debug("Product name : " + affectedProduct.getName());
                    logger.debug("Product version list");

                    for (Version version : affectedPatchSupportedVersionList) {
                        logger.debug("Version : " + version.getVersionNumber());
                    }
                }
                affectedPatchSupportedVersionList.clear();
            }
        }
        return affectedPatchSupportedProductsList;
    }
}
