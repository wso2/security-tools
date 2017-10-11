package org.wso2.security.advisory.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lowagie.text.DocumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.msf4j.template.MustacheTemplateEngine;
import org.wso2.security.advisory.exception.AdvisoryException;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;
import org.xml.sax.SAXException;

import org.wso2.security.advisory.beans.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.wso2.security.advisory.utils.PdfUtil.HTML_TEMPLATE;

/**
 * This class will handle all the PDF generation related tasks.
 */
public class SecurityAdvisoryGenerator {

    private static TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {
    };

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        SecurityAdvisoryGenerator securityAdvisoryGenerator = new SecurityAdvisoryGenerator();
        Pdf pdf = new Pdf();

        securityAdvisoryGenerator.buildPdfFromXml(pdf);
        pdf.setAffectedWUMProducts();
        securityAdvisoryGenerator.generateGsonString(pdf);
    }

    /**
     * This method populates the html from the template using the provided data.
     *
     * @param pdfInfoMap Map which contains the basic pdf details to be generated
     * @throws AdvisoryException If the html string is {@code null}
     */
    public String populateHTML(Map<String, Object> pdfInfoMap, String templateName) throws AdvisoryException {

        String html = MustacheTemplateEngine.instance().render(templateName, pdfInfoMap);
        if (html == null) {
            throw new AdvisoryException("HTML generation failed.");
        }
        return html;
    }

    /**
     * This method creates the PDF.
     *
     * @param htmlString   This contains the generated html string
     * @param tempFilePath The temp file path which will be used to create the temp PDF file
     * @throws AdvisoryException If the PDF creation fails
     */
    public void createPDF(String htmlString, String tempFilePath) throws AdvisoryException {
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(htmlString.getBytes(StandardCharsets.UTF_8));
            Document document = XMLResource.load(byteArrayInputStream).getDocument();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(document, "/");
            renderer.layout();
            try (FileOutputStream fileOutputStream = new FileOutputStream(tempFilePath)) {
                renderer.createPDF(fileOutputStream);
            }
        } catch (DocumentException | IOException e) {
            throw new AdvisoryException("PDF creation failed: ", e);
        } finally {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException ignore) {
                    //Ignored. At this point, the file is saved and the GC will remove the object later because there
                    //are no references to it.
                }
            }
        }
    }

    /**
     * This method generate htmlString for pdf and send to createPdf method.
     *
     * @param pdf is to be generated
     */
    private void generateGsonString(Pdf pdf) {
        Gson gson = new Gson();
        File tempFile;

        String templateName = HTML_TEMPLATE;

        try {
            String jsonString = gson.toJson(pdf);
            Map<String, Object> pdfInfoMap = gson.fromJson(jsonString, typeToken.getType());
            String htmlString = populateHTML(pdfInfoMap, templateName);
            tempFile = File.createTempFile("temp", Long.toString(System.nanoTime()));

            createPDF(htmlString, tempFile.toString());

        } catch (AdvisoryException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method generate a xml file for the given PDF.
     *
     * @param pdf contains the details for the xml to be generated
     */
    public void generateXMLFileForPdf(Pdf pdf) {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            ArrayList<Product> pdfAllAffectedProducts = pdf.getAllAffectedProducts();

            //for output to file, console
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            //create rot element as pdf
            Element rootElement = doc.createElement("Pdf");
            doc.appendChild(rootElement);

            //add advisory pdf details into the root element
            rootElement.appendChild(getProductElements(doc, "Title", pdf.getTitle()));
            rootElement.appendChild(getProductElements(doc, "Severity", pdf.getSeverity()));
            rootElement.appendChild(getProductElements(doc, "Score", pdf.getScore()));
            rootElement.appendChild(getProductElements(doc, "Overview", pdf.getOverview()));
            rootElement.appendChild(getProductElements(doc, "Description", pdf.getDescription()));
            rootElement.appendChild(getProductElements(doc, "Impact", pdf.getImpact()));
            rootElement.appendChild(getProductElements(doc, "Solution", pdf.getSolution()));
            rootElement.appendChild(getProductElements(doc, "PublicDisclosure", pdf.getPublicDisclosure()));
            rootElement.appendChild(getProductElements(doc, "Notes", pdf.getNotes()));

            Element allAffectedProducts = doc.createElement("AllAffectedProducts");
            rootElement.appendChild(allAffectedProducts);

            for (Product product :
                pdfAllAffectedProducts) {
                allAffectedProducts.appendChild(getProducts(doc, product.getProductCode(), product.getProductName(), product.getVersion()));
//                Product p= (Product) getProducts(doc, product.getProductCode(), product.getProductName(), product.getVersion());

            }
//
            rootElement.appendChild(getProductElements(doc, "Thanks", pdf.getThanks()));

            //for pretty print
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);

            //write to console or file
            StreamResult console = new StreamResult(System.out);
            System.out.println("Enter the path to store the Pdf.xml file : ( src/main/resources ) ");
            String pdfXmlFilePath = scanner.nextLine();
            StreamResult file = new StreamResult(new File(pdfXmlFilePath + "/pdf.xml"));

            //write data
//            transformer.transform(source, console);
            transformer.transform(source, file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method gives the Product to build the xml file for pdf to the document builder.
     *
     * @param doc         is the Document to build the xml file.
     * @param productCode is the ProductCode of the Product that want to build in xml file.
     * @param productName is the productName of the Product that want to build in xml file.
     * @param versionList is the versionList of the Product that want to build in xml file.
     */
    public Node getProducts(Document doc, String productCode, String productName, ArrayList<Version> versionList) {

        Element product = doc.createElement("Product");
        Element versionListElement = doc.createElement("versionList");

        product.appendChild(getProductElements(doc, "productCode", productCode));
        product.appendChild(getProductElements(doc, "productName", productName));

        for (Version version :
            versionList) {

            Node versionNode = doc.createElement("Version");
            versionListElement.appendChild(versionNode);

            versionNode.appendChild(getProductElements(doc, "versionName", version.getVersion()));
            versionNode.appendChild(getProductElements(doc, "isWumSupported", String.valueOf(version.isWumSupported())));
            versionNode.appendChild(getProductElements(doc, "isPatchSupported", String.valueOf(version.isPatchSupported())));

            Element patchList = doc.createElement("patchNumberList");
            versionNode.appendChild(patchList);

            for (Patch patch :
                version.getPatchList()) {
                patchList.appendChild(getProductElements(doc, "patch", patch.getName()));
                System.out.println(productCode+"\t"+productName+"\t"+version.getVersion()+"\t"+patch.getName());
            }
        }

        product.appendChild(versionListElement);
        return product;
    }

    /**
     * This method gives the Product Child elements to build the xml file for pdf to the document builder.
     *
     * @param doc   is the Document to build the xml file.
     * @param name  is the tag that should be in the xml file.
     * @param value is the value for the given tag name.
     */
    private static Node getProductElements(Document doc, String name, String value) {

        Element node = doc.createElement(name);

        node.appendChild(doc.createTextNode(value));

        return node;
    }

    /**
     * This method generate the Pdf from the given xml file.
     *
     * @param pdf is the Document to build the xml file.
     */
    public void buildPdfFromXml(Pdf pdf) {

        System.out.print("Enter the reference path to XML file ( ex:- src/main/resources/pdf.xml ) : ");
        String xmlFilePath = scanner.nextLine();

        String filePath = xmlFilePath;
        File xmlFile = new File(filePath);

        ArrayList<Product> allAffectedProductsList = new ArrayList<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try {

            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            doc.getDocumentElement().normalize();

            NodeList nodelist = doc.getElementsByTagName("Pdf");
            Node node = nodelist.item(0);

            Element element = (Element) node;

            pdf.setTitle(getTagValue("Title", element, 0));
            pdf.setSeverity(getTagValue("Severity", element, 0));
            pdf.setScore(getTagValue("Score", element, 0));
            pdf.setOverview(getTagValue("Overview", element, 0));
            pdf.setDescription(getTagValue("Description", element, 0));
            pdf.setImpact(getTagValue("Impact", element, 0));
            pdf.setSolution(getTagValue("Solution", element, 0));
            pdf.setNotes(getTagValue("Notes", element, 0));

            NodeList allAffectedProductsNodeList = doc.getElementsByTagName("AllAffectedProducts");

            Element a = (Element) allAffectedProductsNodeList.item(0);

            for (int i = 0; i < a.getElementsByTagName("productName").getLength(); i++) {
                allAffectedProductsList.add(getProduct(allAffectedProductsNodeList.item(0), i));
            }
            pdf.setAffectedProducts(allAffectedProductsList);
            pdf.setAllAffectedProducts(allAffectedProductsList);

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method parse the Product element from the given xml file for building the pdf.
     *
     * @param node  is the element node that need to parse into a Product.
     * @param index is the index of the Product from the product list.
     */
    private static Product getProduct(Node node, int index) {

        Element element = (Element) node;
        ArrayList<Version> versionList = new ArrayList<>();

        Element c = (Element) element.getElementsByTagName("versionList").item(index);

        for (int i = 0; i < c.getElementsByTagName("Version").getLength(); i++) {
            versionList.add(getVersion(element.getElementsByTagName("versionList").item(index), i));
        }

        Product product = new Product(getTagValue("productCode", element, index), getTagValue("productName", element, index), versionList);

        return product;
    }

    /**
     * This method parse the Version element of Product from the given xml file for building the pdf.
     *
     * @param node  is the element node that need to parse into a Version.
     * @param index is the index of the Version from the version list.
     */
    private static Version getVersion(Node node, int index) {

        Version version = null;

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            version = new Version(getTagValue("versionName", element, index), Boolean.valueOf(getTagValue("isWumSupported", element, index)), Boolean.getBoolean(getTagValue("isPatchSupported", element, index)));
            Element c = (Element) element.getElementsByTagName("patchNumberList").item(index);

            for (int h = 0; h < c.getElementsByTagName("patch").getLength(); h++) {
                if(version.getPatchList().size() == 0) {
                    version.setPatchList(new Patch(getTagValue("patch", c, h)));
                }
                else {
                    version.setPatchList(new Patch(getTagValue("patch", c, h),false));
                }
            }
        }

        return version;
    }

    /**
     * This method parse the element of Product for a given tag name within a given element.
     *
     * @param tag     that is want to parse from xml for a given element.
     * @param element is the place where tag exists.
     * @param index   is the index of the Version of Product from the version list.
     */
    private static String getTagValue(String tag, Element element, int index) {

        NodeList nodeList = element.getElementsByTagName(tag).item(index).getChildNodes();
        Node node = nodeList.item(0);

        return node.getNodeValue();
    }

}