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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.wso2.security.tools.advisorytool.config.Configuration;
import org.wso2.security.tools.advisorytool.data.ProductDataHolder;
import org.wso2.security.tools.advisorytool.exeption.AdvisoryToolException;
import org.wso2.security.tools.advisorytool.model.Patch;
import org.wso2.security.tools.advisorytool.model.Platform;
import org.wso2.security.tools.advisorytool.model.Product;
import org.wso2.security.tools.advisorytool.model.SecurityAdvisory;
import org.wso2.security.tools.advisorytool.model.Version;
import org.wso2.security.tools.advisorytool.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class builds the customer security advisory object.
 */
public class CustomerSecurityAdvisoryBuilder extends SecurityAdvisoryBuilder {

    private static final Logger logger = Logger.getLogger(CustomerSecurityAdvisoryBuilder.class);
    protected SecurityAdvisory securityAdvisory = new SecurityAdvisory();

    public void setAdvisoryData(SecurityAdvisory securityAdvisory) throws AdvisoryToolException {

        SecurityAdvisory securityAdvisoryDataFromPMT = null;
        try {
            this.securityAdvisory = securityAdvisory;

            //getting the advisory details from the PMT.
            securityAdvisoryDataFromPMT = getAdvisoryData(securityAdvisory.getName());

            if (securityAdvisoryDataFromPMT != null) {
                if (StringUtils.isEmpty(securityAdvisory.getDescription())) {
                    this.securityAdvisory.setDescription(securityAdvisoryDataFromPMT.getDescription());
                }

                if (StringUtils.isEmpty(securityAdvisory.getImpact())) {
                    this.securityAdvisory.setImpact(securityAdvisoryDataFromPMT.getImpact());
                }

                if (StringUtils.isEmpty(securityAdvisory.getOverview())) {
                    this.securityAdvisory.setOverview(securityAdvisoryDataFromPMT.getOverview());
                }

                if (StringUtils.isEmpty(securityAdvisory.getNotes())) {
                    this.securityAdvisory.setNotes(securityAdvisoryDataFromPMT.getNotes());
                }

                if (StringUtils.isEmpty(securityAdvisory.getSeverity())) {
                    this.securityAdvisory.setSeverity(securityAdvisoryDataFromPMT.getSeverity());
                }

                if (StringUtils.isEmpty(securityAdvisory.getScore())) {
                    this.securityAdvisory.setScore(securityAdvisoryDataFromPMT.getScore());
                }

                if (StringUtils.isEmpty(securityAdvisory.getSolution())) {
                    this.securityAdvisory.setSolution(securityAdvisoryDataFromPMT.getSolution());
                }

                if (StringUtils.isEmpty(securityAdvisory.getCredits())) {
                    this.securityAdvisory.setCredits(securityAdvisoryDataFromPMT.getCredits());
                }
            }

        } catch (AdvisoryToolException e) {
            throw new AdvisoryToolException("Error occurred while building the advisory details.", e);
        }
    }

    public void buildAffectedProductsList() throws AdvisoryToolException {

        List<Patch> patchListForAdvisory = getApplicablePatchListForAdvisory(securityAdvisory
                .getName());

        if (patchListForAdvisory.isEmpty()) {
            throw new AdvisoryToolException("Applicable patch list for the Security Advisory "
                    + securityAdvisory.getName() + " is empty.");
        }

        List<Product> affectedProductList = new ArrayList<>();
        List<Product> releasedProductList = ProductDataHolder.getInstance().getProductList();

        //affected product map is built with the affected product and version strings
        // (e.g., WSO2 API Manager 2.1.0) in the patch object.
        Map<String, Map<String, Version>> affectedProductMap =
                generateAffectedProductMapFromApplicablePatches(patchListForAdvisory);

        //iterate through the product map and create the product objects list.
        Iterator it = affectedProductMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            for (Product releasedProduct : releasedProductList) {
                if (releasedProduct.getName().equals(pair.getKey())) {

                    Product affectedProduct = new Product();

                    //getting the product details from the released product list.
                    affectedProduct.setName(releasedProduct.getName());
                    affectedProduct.setCodeName(releasedProduct.getCodeName());

                    Map<String, Version> affectedVersionMap = (Map<String, Version>) pair.getValue();
                    Iterator versionIterator = affectedVersionMap.entrySet().iterator();
                    List<Version> affectedVersionList = new ArrayList<>();

                    while (versionIterator.hasNext()) {
                        Map.Entry versionPair = (Map.Entry) versionIterator.next();
                        affectedVersionList.add((Version) versionPair.getValue());
                        versionIterator.remove();
                    }

                    affectedVersionList.sort(Comparator.comparing(Version::getVersionNumber));
                    affectedProduct.setVersionList(affectedVersionList);
                    affectedProductList.add(affectedProduct);
                }
            }
            it.remove();
        }

        affectedProductList.sort(Comparator.comparing(Product::getName));
        securityAdvisory.setAffectedAllProducts(affectedProductList);
    }

    public void buildAdvisory() throws AdvisoryToolException {

        List<String> affectedPatchNames = new ArrayList<>();
        List<Patch> supportedPatchListForAdvisory = new ArrayList<>();

        //creating the list of patch supported products.
        List<Product> affectedPatchSupportedProducts = getPatchSupportedProducts(securityAdvisory
                .getAffectedAllProducts());
        securityAdvisory.setAffectedPatchProducts(affectedPatchSupportedProducts);

        //generate the patch details file to be used to upload the patches to the customer patch locations.
        generatePatchUploadDataFile(securityAdvisory.getName(), affectedPatchSupportedProducts);

        //creating the list of WUM supported products.
        List<Product> affectedWUMProductsList = getWUMProductsFromAffectedProducts(securityAdvisory
                .getAffectedAllProducts());
        securityAdvisory.setAffectedWUMProducts(affectedWUMProductsList);

        //creating the patch list with the customer patch download link.
        for (Product affectedPatchSupportedProduct : affectedPatchSupportedProducts) {
            for (Version affectedPatchSupportedVersion : affectedPatchSupportedProduct
                    .getVersionList()) {
                for (String patchName : affectedPatchSupportedVersion.getPatchNamesList()) {

                    for (Platform platform : Configuration.getInstance().getPlatforms()) {

                        if (affectedPatchSupportedVersion.getPlatformVersionNumber().equals(platform
                                .getVersionNumber())) {

                            Patch patch = new Patch();
                            patch.setName(patchName);

                            //building the customer patch download URL.
                            String url = Configuration.getInstance().getPatchZIPCustomerLocation()
                                    + platform.getPatchDirectoryName() + "/Product/"
                                    + affectedPatchSupportedProduct.getCodeName() + " "
                                    + affectedPatchSupportedVersion.getVersionNumber()
                                    + "/" + patchName.replace(" ", "")
                                    + ".zip";

                            patch.setZipLocation(url);

                            if (!affectedPatchNames.contains(patchName)) {
                                affectedPatchNames.add(patchName);
                                supportedPatchListForAdvisory.add(patch);
                            }
                        }
                    }
                }
            }

        }

        supportedPatchListForAdvisory.sort(Comparator.comparing(Patch::getName));
        securityAdvisory.setApplicablePatchList(supportedPatchListForAdvisory);

    }

    public SecurityAdvisory getSecurityAdvisory() {
        return securityAdvisory;
    }

    /**
     * Generate a products map using the product and version strings (e.g., WSO2 API Manager 2.1.0)
     * in the given patch object.
     *
     * @param applicablePatchList
     * @return A map with the product name as the key and a another version map as the value.
     * The version map has the version name as the key and Version object as the value.
     */
    private Map<String, Map<String, Version>> generateAffectedProductMapFromApplicablePatches(
            List<Patch> applicablePatchList) {

        Map<String, Map<String, Version>> affectedProductMap = new HashMap<>();

        for (Patch patch : applicablePatchList) {
            for (String affectedProductAndVersionString : patch.getApplicableProductAndVersionStrings()) {

                //separating the product and version from the product and version string.
                String[] array = affectedProductAndVersionString.split("(?=\\s\\d)");
                String productName = array[0].trim();
                String versionNumber = array[1].trim();

                //building the version object with the details in the released product list and
                //the given patch as an applicable patch.
                Version currentAffectedVersion = buildAffectedVersion(productName, versionNumber,
                        patch.getName());

                if (currentAffectedVersion != null) {
                    if (affectedProductMap.containsKey(productName)) {
                        if (affectedProductMap.get(productName).containsKey(currentAffectedVersion
                                .getVersionNumber())) {

                            //if the product map contains the affected version, add this patch in
                            //to the applicable patch list.
                            affectedProductMap.get(productName).get(versionNumber).getPatchNamesList()
                                    .addAll(currentAffectedVersion.getPatchNamesList());
                        } else {
                            //else add the complete version object in to the product map
                            affectedProductMap.get(productName).put(versionNumber,
                                    currentAffectedVersion);
                        }
                    } else {

                        //if the product map doesn't contain the current product name as a key,
                        // create a new version map and add under the current product name.
                        Map<String, Version> affectedVersionMap = new HashMap<>();
                        affectedVersionMap.put(versionNumber, currentAffectedVersion);
                        affectedProductMap.put(productName, affectedVersionMap);
                    }
                }
            }
        }
        return affectedProductMap;
    }

    /**
     * Creating a Version object with the required details from the released products list and
     * adding the given patch name as an applicable patch.
     *
     * @param productName
     * @param versionNumber
     * @param patchName
     * @return
     */
    private Version buildAffectedVersion(String productName, String versionNumber, String patchName) {

        DateTime dateTime = new DateTime();
        boolean hasAMatchingProduct = false;
        boolean hasAMatchingVersion = false;
        boolean hasAnApplicableVersion = false;

        List<Product> releasedProductList = ProductDataHolder.getInstance().getProductList();
        Version affectedVersion = new Version();

        for (Product releasedProduct : releasedProductList) {
            if (releasedProduct.getName().equals(productName)) {
                hasAMatchingProduct = true;
                for (Version releasedVersion : releasedProduct.getVersionList()) {
                    if (releasedVersion.getVersionNumber().equals(versionNumber)) {
                        hasAMatchingVersion = true;
                        DateTime productDate = new DateTime(releasedVersion.getReleasedDate());
                        Period period = new Period(productDate, dateTime);

                        //check if the product is within the supported period or whether it is deprecated.
                        if ((period.getYears() < Configuration.getInstance().getPatchSupportPeriod()) &&
                                !releasedVersion.isDeprecated()) {

                            hasAnApplicableVersion = true;
                            affectedVersion.getPatchNamesList().add(patchName);
                            affectedVersion.setVersionNumber(releasedVersion.getVersionNumber());
                            affectedVersion.setPublicSupported(releasedVersion.isPublicSupported());
                            affectedVersion.setPatchSupported(releasedVersion.isPatchSupported());
                            affectedVersion.setWumSupported(releasedVersion.isWumSupported());
                            affectedVersion.setReleasedDate(releasedVersion.getReleasedDate());
                            affectedVersion.setKernelVersionNumber(releasedVersion
                                    .getKernelVersionNumber());
                            affectedVersion.setPlatformVersionNumber(releasedVersion
                                    .getPlatformVersionNumber());
                        }
                        break;
                    }
                }
                break;
            }
        }

        if (!hasAMatchingVersion) {
            logger.warn("Unable to find the version " + versionNumber + " for the product "
                    + productName + " in the Products list");

        }

        if (!hasAMatchingProduct) {
            logger.warn("Unable to find the product " + productName + " in the Products list");
        }

        if (hasAnApplicableVersion) {

            //if the released product list contains a version with the given details and
            // within the supported period.
            return affectedVersion;
        } else {
            return null;
        }
    }

    /**
     * Generate the patch upload data file to be used in uploading the patches to the customer
     * accessible location.
     *
     * @param securityAdvisoryName
     * @param affectedPatchSupportedProductList
     * @throws AdvisoryToolException
     */
    private void generatePatchUploadDataFile(String securityAdvisoryName,
                                             List<Product> affectedPatchSupportedProductList)
            throws AdvisoryToolException {

        logger.info("Generating patch upload data file.");
        File outputFile = new File(Constants.SECURITY_ADVISORY_OUTPUT_DIRECTORY + File.separator
                + "csv" + File.separator + securityAdvisoryName + ".csv");

         File outputDirectory = new File(outputFile.getParent());
         outputDirectory.mkdirs();
        if (!outputDirectory.exists()) {
            throw new AdvisoryToolException("Unable to create the directory " + outputDirectory);
        }

        try (PrintWriter pw = new PrintWriter(outputFile, "UTF-8")) {
            for (Product product : affectedPatchSupportedProductList) {
                for (Version version : product.getVersionList()) {
                    for (String patchName : version.getPatchNamesList()) {
                        pw.write(product.getCodeName() + ";" + version.getVersionNumber()
                                + ";" + patchName + "\n");
                    }
                }
            }

        } catch (IOException e) {
            throw new AdvisoryToolException("Error occurred while generating the patch upload csv", e);
        }

    }
}
