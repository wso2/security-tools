package org.wso2.security.advisory.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.security.advisory.beans.Patch;
import org.wso2.security.advisory.exception.AdvisoryException;
import org.wso2.security.advisory.handler.SecurityAdvisoryGenerator;
import org.wso2.security.advisory.beans.*;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * This is the manager class which handles HTML generation from the template and PDF creation.
 */
public class PdfUtil {

    private static TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {
    };
    private static final Log log = LogFactory.getLog(PdfUtil.class);

    private List<Product> releasedProductList;
    private static Scanner scanner = new Scanner(System.in);
    public static String HTML_TEMPLATE="html_template.mustache";

    private String patchListApiUrl;
    private String patchDetailsApiUrl;
    private String advisoryDetailsApiUrl;
    private String patchListApiAuthenticationHeader;
    private String patchDetailsApiAuthenticationHeader;
    private String advisoryDetailsApiAuthenticationHeader;

    public static void main(String[] args) throws AdvisoryException {

        PdfUtil pdfUtil;
        System.out.println("------------------- Security Patch Release Automation Tool -------------------");
        pdfUtil = new PdfUtil();
        pdfUtil.getProperties();
        pdfUtil.getReleasedProducts();
        pdfUtil.process();

    }

    /**
     * This method get the access tokens and urls for calling PMT APIs from the pmtaccess property file
     *
     */
    public void getProperties (){

        try (InputStream in = new FileInputStream("src/main/resources/pmtaccess.properties")) {

            Properties properties = new Properties();
            properties.load(in);

            patchListApiUrl=properties.getProperty("patchListApiUrl");
            patchDetailsApiUrl=properties.getProperty("patchDetailsApiUrl");
            advisoryDetailsApiUrl=properties.getProperty("advisoryDetailsApiUrl");
            patchListApiAuthenticationHeader=properties.getProperty("patchListApiAuthenticationHeader");
            patchDetailsApiAuthenticationHeader=properties.getProperty("patchDetailsApiAuthenticationHeader");
            advisoryDetailsApiAuthenticationHeader=properties.getProperty("advisoryDetailsApiAuthenticationHeader");

        } catch (IOException e) {
            log.error("Error in loading property file.");
        }
    }

    /**
     * This method process all methods generate HTML and create the pdf.
     *
     */
    public void process() {

        SecurityAdvisoryGenerator securityAdvisoryGenerator = new SecurityAdvisoryGenerator();
        Pdf pdf = new Pdf();
        String[] patchList;

        System.out.print("Enter advisory number ( ex:- 2017-0262 ) : ");
        String advisoryNumber = scanner.nextLine();

        patchList = getPatchListForAdvisory(advisoryNumber);
        pdf.setAffectedProducts(getAffectedProductsFromPatch(patchList));
        pdf.setAllAffectedProducts(getAffectedProductsFromPatch(patchList));
        getPdfDetailsFromAdvisory(pdf, advisoryNumber);
        pdf.setAffectedWUMProducts();

        securityAdvisoryGenerator.generateXMLFileForPdf(pdf);
    }

    /**
     * This method retrieve details of the security advisory pdf from the PMT for a given advisory number.
     *
     * @param pdf            is to filled with the PMT details.
     * @param advisoryNumber is advisory number that want to create pdf for.
     */
    public void getPdfDetailsFromAdvisory(Pdf pdf, String advisoryNumber) {

        Gson gson = new Gson();
        HttpResponse response;

        String url = advisoryDetailsApiUrl;
        try {
            url=url.concat(advisoryNumber);

            response = getConnection(url, "Advisory");

            BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = null;

            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            JsonElement json = gson.fromJson(String.valueOf(result), JsonElement.class);
            String jsonInString = gson.toJson(json);

            ArrayList<String> advisoryDetails = new ArrayList<String>();
            JSONArray jsonMainArr = new JSONArray(jsonInString);
            JSONArray ob=null;
            JSONObject jo=null;
            for (int i = 0; i < jsonMainArr.length(); i++) {
                jo = jsonMainArr.getJSONObject(i);
                ob = jo.getJSONArray("value");
                advisoryDetails.add(ob.get(0).toString());
            }
//            jo.get("name");

            pdf.setOverview(advisoryDetails.get(getIndexOfObjectFromJsonArray("overview_overview",jsonMainArr)));
            pdf.setSeverity(advisoryDetails.get(getIndexOfObjectFromJsonArray("overview_severity",jsonMainArr)));
            pdf.setDescription(advisoryDetails.get(getIndexOfObjectFromJsonArray("overview_description",jsonMainArr)));
            pdf.setImpact(advisoryDetails.get(getIndexOfObjectFromJsonArray("overview_impact",jsonMainArr)));
            pdf.setSolution(advisoryDetails.get(getIndexOfObjectFromJsonArray("overview_solution",jsonMainArr)));
            pdf.setNotes(advisoryDetails.get(getIndexOfObjectFromJsonArray("overview_note",jsonMainArr)));

        } catch (Exception e) {
            log.error("Error while calling to the api");
            e.printStackTrace();
        }
    }

    public int getIndexOfObjectFromJsonArray(String tag, JSONArray jsonArray) throws JSONException {
        JSONArray ob=null;
        JSONObject jo;
        for (int i = 0; i < jsonArray.length(); i++) {
            jo = jsonArray.getJSONObject(i);
            if(jo.get("name").equals(tag)){
                return i;
            }
        }
        return 0;
    }

    /**
     * This method retrieve related patch list  for a given advisory number.
     *
     * @param advisoryNumber is advisory number that want to create pdf for.
     */
    public String[] getPatchListForAdvisory(String advisoryNumber) {

        Gson gson = new Gson();
        String[] patchList = null;
        HttpResponse response = null;
        StringBuffer result = new StringBuffer();
        String line = "";
        String url = patchListApiUrl;
        try {
            url = url.concat(advisoryNumber);

            response = getConnection(url, "PatchList");

            BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            JsonElement json = gson.fromJson(String.valueOf(result), JsonElement.class);
            String jsonInString = gson.toJson(json);

            JSONObject jsonOb = new JSONObject(jsonInString);
            String patches = jsonOb.getString("patches");

            patchList = patches.split(",");

        } catch (Exception e) {
            log.error("Error while calling to the api");
            e.printStackTrace();
        }

        return patchList;
    }

    /**
     * This method retrieve affected products for a given patch list.
     *
     * @param patchList is the related patch list for a advisory.
     */
    public ArrayList<Product> getAffectedProductsFromPatch(String[] patchList) {
        ArrayList<Product> affectedProducts = null;

        Gson gson = new Gson();
        HttpResponse response = null;
        affectedProducts = new ArrayList<>();

        try {
            for (String patchNumber :
                patchList) {
                String url=patchDetailsApiUrl;
                url = url.concat(patchNumber);
                response = getConnection(url, "Patch");
                System.out.println(">>>>>>>>>>"+url);
                BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

                StringBuffer result = new StringBuffer();
                String line = "";

                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

                JsonElement json = gson.fromJson(String.valueOf(result), JsonElement.class);
                String jsonInString = gson.toJson(json);
                JSONArray jsonMainArr = new JSONArray(jsonInString);

                int jsonArrayIndex;

                for (jsonArrayIndex = 0; jsonArrayIndex < jsonMainArr.length(); jsonArrayIndex++) {
                    if (jsonMainArr.getJSONObject(jsonArrayIndex).getString("name").equals("overview_products")) {
                        break;
                    }
                }

                JSONObject jo = jsonMainArr.getJSONObject(jsonArrayIndex);
                JSONArray jsonArray = jo.getJSONArray("value");

                for (int j = 0; j < jsonArray.length(); j++) {

                    Object jsonObject = jsonArray.get(j);
                    String[] array = jsonObject.toString().split("(?=\\s\\d)");//|(?<=\d)(?=\D)

                    if("Carbon".equals(array[0])){
                        List<Product> productListOnKernelVersion;
                        productListOnKernelVersion= getProductListOnCarbonKernel(array[1].replace(" ", ""));
                        System.out.println("Carbon");
                        for (Product product:
                             productListOnKernelVersion) {
                             createAffectedProducts(affectedProducts,patchNumber,product.getProductName(),product.getVersion().get(0).getVersion());
                        }
                    }
                    else {
                        createAffectedProducts(affectedProducts,patchNumber,"WSO2 ".concat(array[0]),array[1]);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while calling to the api");
            e.printStackTrace();
        }

        return affectedProducts;
    }

    public void createAffectedProducts(ArrayList<Product> affectedProducts, String patchNumber, String productName,String productVersion){
        boolean isProductDuplicate = false;
        boolean isVersionDuplicate = false;
        int k;
        int j=0;
        Version version;

        for (k = 0; k < affectedProducts.size(); k++) {
            if (affectedProducts.get(k).getProductName().equals(productName)) {
                isProductDuplicate = true;
                List<Version> versionList=affectedProducts.get(k).getVersionList();
                for (j = 0; j < versionList.size(); j++) {
                    if(versionList.get(j).getVersion().equals(productVersion.replaceAll(" ",""))){
                        isVersionDuplicate=true;
                        break;
                    }
                }
                break;
            }
        }

        if(!isProductReleasedDateOld(productName,productVersion)) {
            System.out.println("True");
            if (isProductDuplicate) {
                if (isVersionDuplicate) {
                    affectedProducts.get(k).getVersionList().get(j).setPatchList(new Patch(patchNumber));
                } else {
                    version = new Version(productVersion.replace(" ", ""), isWumSupported(productName, productVersion), isPatchSupported(productName, productVersion));
                    version.setPatchList(new Patch(patchNumber));
                    affectedProducts.get(k).setVersion(version);
                }
            } else {
                version = new Version(productVersion.replace(" ", ""), isWumSupported(productName, productVersion), isPatchSupported(productName, productVersion));

                version.setPatchList(new Patch(patchNumber));
                Product product = new Product(getProductCode(productName), productName, version);
                affectedProducts.add(product);
            }
        }
    }

    public boolean isProductReleasedDateOld(String productName, String version){
        Date releasedDate=null;
        for (Product product :
            releasedProductList) {
            if(product.getProductName().equals(productName) && product.getVersion().get(0).getVersion().equals(version)){
                releasedDate=product.getVersion().get(0).getReleaseDate();
                System.out.println(productName+"      <<>>  "+version+" "+releasedDate.getYear()+" "+releasedDate.getMonth());
                if(releasedDate.getYear()<114) {
                        return true;
                }
            }
        }
        return false;
    }

    public List<Product> getProductListOnCarbonKernel(String kernelVersion){
        ArrayList<Product> productListOnKernelVersion= new ArrayList<>();
        for (Product product :
            releasedProductList) {

            if(product.getKernelVersion().equals(kernelVersion)) {
                productListOnKernelVersion.add(product);
            }
        }
        return productListOnKernelVersion;
    }

    /**
     * This method returns the http response from the pmt.
     *
     * @param url for the pmt api.
     * @param api gives the detail which api should be connected to in the PMT.
     */
    public HttpResponse getConnection(String url, String api) {

        HttpResponse response = null;

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = null;

            if (api == "Patch") {
                request = new HttpGet(url);
                request.addHeader("Authorization", patchDetailsApiAuthenticationHeader);
            } else if (api == "Advisory") {
                request = new HttpGet(url);
                request.addHeader("Authorization", advisoryDetailsApiAuthenticationHeader);
            } else if (api == "PatchList") {
                request = new HttpGet(url);
                request.addHeader("Authorization", patchListApiAuthenticationHeader);
            }
            response = client.execute(request);

        } catch (IOException e) {
            log.error("Error while authorizing the PMT API");
        }

        return response;
    }

    /**
     * This method return all the released products from a given file.
     */
    public void getReleasedProducts() {

        Scanner scanner = new Scanner(System.in);

        ProductUtil releaseMatrix = new ProductUtil();
        System.out.print("Enter file path to the Products.xml file: ");
        String productsFilePath = scanner.nextLine();
        releasedProductList = releaseMatrix.loadReleasedProducts(productsFilePath);

    }

    public boolean isWumSupported(String productName, String productVersion) {

        for (Product product :
            releasedProductList) {
            System.out.println(product.getProductName()+":           :"+productName);
            if (product.getProductName().equals(productName)) {

                for (Version version :
                    product.getVersionList()) {

                    if (version.getVersion().equals(productVersion.replaceAll(" ", ""))) {

                        if (version.isWumSupported()) {
                            return true;
                        }
                    }
                }

            }
        }

        return false;
    }

    public boolean isPatchSupported(String productName, String productVersion) {

        for (Product product :
            releasedProductList) {

            if (product.getProductName().equals(productName)) {

                for (Version version :
                    product.getVersionList()) {

                    if (version.getVersion().equals(productVersion.replaceAll(" ", ""))) {

                        if (version.isPatchSupported()) {
                            return true;
                        }
                    }
                }

            }
        }

        return false;
    }

    public String getProductCode(String productName) {

        for (Product product :
            releasedProductList) {
//            System.out.println(product.getProductName()+":  <<>> :WSO2 "+productName);

            if (product.getProductName().equals(productName)) {
                return product.getProductCode();
            }
        }

        return null;
    }
}