package org.wso2.security.advisory.beans;

import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.*;

/**
 * This is used to populate the Pdf object from the data received from the PMT API.
 */
public class Pdf {

    @SerializedName("pdf-title")
    private String title = "Security Update for Multiple WSO2 Products ";

    @SerializedName("pdf-name")
    private String name;

    @SerializedName("pdf-date")
    private Date date;

    @SerializedName("severity")
    private String severity = "Low";

    @SerializedName("score")
    private String score = "6.3 (CVSS:3.0/AV:N/AC:L/PR:L/UI:R/S:U/C:H/I:N/A:L)";

    @SerializedName("overview")
    private String overview;

    @SerializedName("description")
    private String description;

    @SerializedName("impact")
    private String impact;

    @SerializedName("solution")
    private String solution;

    @SerializedName("public-disclosure")
    private String publicDisclosure = "Pub";

    @SerializedName("notes")
    private String notes;

    @SerializedName("affected-products")
    private ArrayList<Product> affectedProducts = new ArrayList<>();

    @SerializedName("affected-wum-products")
    private ArrayList<Product> affectedWUMProducts = new ArrayList<>();

    @SerializedName("thanks")
    private String thanks = "Thanks, WSO2 Team.";

    @SerializedName("all-affected-products")
    private ArrayList<Product> allAffectedProducts = new ArrayList<>();

    public Pdf() {
    }

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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

    public String getDescription() throws IOException {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImpact() throws IOException {
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

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public ArrayList<Product> getAffectedProducts() {
        return affectedProducts;
    }

    public void setAffectedProducts(ArrayList<Product> affectedProducts) {
        sortVersionList(affectedProducts);
        this.affectedProducts = affectedProducts;
    }

    /**
     * This method sort the product list according to the product name ascending order.
     */
    public void sortVersionList(ArrayList<Product> productList) {
        sortProductList(productList);
        for (Product product :
            productList) {
            Collections.sort(product.getVersion(), (Version first, Version second) -> Integer.compare(Integer.parseInt(second.getVersion().replaceAll("\\.", "")), Integer.parseInt(first.getVersion().replaceAll("\\.", ""))));
        }

    }
    /**
     * This method sort the product list according to the product name ascending order.
     */
    public void sortProductList(ArrayList<Product> productList) {
        Collections.sort(productList, Comparator.comparing(Product::getProductName));

    }

    public void setThanks(String thanks) {
        this.thanks = thanks;
    }

    public String getThanks() {
        return thanks;
    }

    public ArrayList<Product> getAllAffectedProducts() {
        return allAffectedProducts;
    }

    public void setAllAffectedProducts(ArrayList<Product> affectedProducts) {
        sortVersionList(affectedProducts);
        this.allAffectedProducts = affectedProducts;
    }

    /**
     * This method check all the affected products and separate the Wum products to build the xml file for pdf to the document builder.
     */
    public void setAffectedWUMProducts() {

        ArrayList<Product> productsToRemove = new ArrayList<>();

        for (Product product :
            affectedProducts) {

            ArrayList<Version> versionsToRemove = new ArrayList<>();

            for (Version version :
                product.getVersion()) {

                if (version.isWumSupported()) {

                    Product affectedWUMProduct;
                    boolean duplicate = false;
                    int indexOfWumProduct;

                    for (indexOfWumProduct = 0; indexOfWumProduct < affectedWUMProducts.size(); indexOfWumProduct++) {

                        if (affectedWUMProducts.get(indexOfWumProduct).getProductName().equals(product.getProductName())) {
                            duplicate = true;
                            break;
                        }
                    }

                    if (duplicate) {
                        affectedWUMProducts.get(indexOfWumProduct).setVersion(version);
                        versionsToRemove.add(version);
                    } else {
                        affectedWUMProduct = new Product(product.getProductCode(), product.getProductName(), version);
                        affectedWUMProducts.add(affectedWUMProduct);

                        if (!version.isPatchSupported()) {
                            versionsToRemove.add(version);
                        }
                    }
                }
            }

            for (Version version :
                versionsToRemove) {
                product.removeVersion(version.getVersion());
            }
        }

        for (Product product :
            affectedProducts) {

            if (product.getVersion().size() == 0) {
                productsToRemove.add(product);
            }
        }

        for (Product product :
            productsToRemove) {
            affectedProducts.remove(product);
        }
    }

}
