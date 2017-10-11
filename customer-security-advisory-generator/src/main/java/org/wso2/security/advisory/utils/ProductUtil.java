package org.wso2.security.advisory.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.wso2.security.advisory.beans.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductUtil {

    public ProductUtil() {

    }

    /**
     * This method parse all released products from a given xml file.
     *
     * @param filePath is the path to xml file which contains all released products.
     */
    public List<Product> loadReleasedProducts(String filePath) {

        ArrayList<Product> productsList = new ArrayList<Product>();

        try {
            File fXmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = getSecuredDocumentBuilder();

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("Product");

            for (int i = 0; i < nList.getLength(); i++) {

                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE && "Product".equals(nNode.getNodeName())) {

                    Element eElement = (Element) nNode;
                    Product product = new Product();
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
//                    LocalDateTime dateTime =LocalDateTime.parse(eElement.getElementsByTagName("releaseDate").item(0).getTextContent());
                    product.setProductName(eElement.getElementsByTagName("name").item(0).getTextContent());
                    product.setProductCode(eElement.getElementsByTagName("codeName").item(0).getTextContent());
                    product.setVersion(new Version(eElement.getElementsByTagName("version").item(0).getTextContent(), "true".equals(eElement.getElementsByTagName("isWumSupported").item(0).getTextContent()), "true".equals(eElement.getElementsByTagName("isPatchSupported").item(0).getTextContent())));
                        product.getVersion().get(0).setReleaseDate(dateFormat.parse(eElement.getElementsByTagName("releaseDate").item(0).getTextContent()));
//                    product.getVersion().get(0).setReleaseDate(dateTime);
//                    System.out.println(LocalDateTime.parse(eElement.getElementsByTagName("releaseDate").item(0).getTextContent())+"  "+product.getReleaseDate());
                    product.setPlatformVersion(eElement.getElementsByTagName("platformVersion").item(0).getTextContent());
                    product.setKernelVersion(eElement.getElementsByTagName("kernelVersion").item(0).getTextContent());
                    productsList.add(product);


                }
            }

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return productsList;
    }

    public ArrayList<Product> getProductsOfKernel(String kernelVersion) {

        ArrayList<Product> productsList = new ArrayList<Product>();

        //TODO: complete

        return productsList;
    }

    public ArrayList<Product> getProductsOfPlatform(String platformVersion) {

        ArrayList<Product> productsList = new ArrayList<Product>();

        //TODO: complete

        return productsList;

    }

    private DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        return dbf;
    }
}
