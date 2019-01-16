/*
 *  Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.security.tools.scanner.qualys.qualys_scanner.handler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.security.tools.scanner.config.ConfigurationReader;
import org.wso2.security.tools.scanner.exception.ScannerException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.wso2.security.tools.scanner.scanner.AbstractScanner.getSecuredDocumentBuilderFactory;


/**
 * The class {@code XmlFileHandler} is to handle xml file uploading.
 * Since Qualys backend expects a XML file containing application details, this class is to create the
 * corresponding xml file as a String.
 */
public class XmlFileHandler {

    private XmlFileHandler() {

    }

    /**
     * Get the current date.
     *
     * @return formatted date and time
     */
    private static String getDate() {
        Date date = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss");

        return ft.format(date);
    }

    /**
     * Creates a xml String with the given application ID.
     *
     * @param appId Application ID
     * @return XML string
     * @throws ScannerException
     */
    public static String xmlFileBuilder(String appId) throws ScannerException {
        String output;
        String date = getDate();

        try {
            DocumentBuilderFactory dbf = getSecuredDocumentBuilderFactory();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("ServiceRequest");
            doc.appendChild(root);

            Element data = doc.createElement("data");
            root.appendChild(data);

            Element WasScan = doc.createElement("WasScan");
            data.appendChild(WasScan);

            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode("New Discovery scan launch from API " + date));
            WasScan.appendChild(name);

            Element type = doc.createElement("type");
            type.appendChild(doc.createTextNode(ConfigurationReader.getConfigProperty("scanner_type")));
            WasScan.appendChild(type);

            Element target = doc.createElement("target");
            WasScan.appendChild(target);

            Element webApp = doc.createElement("webApp");
            target.appendChild(webApp);

            Element id = doc.createElement("id");
            id.appendChild(doc.createTextNode(String.valueOf(appId)));
            webApp.appendChild(id);

            Element scannerAppliance = doc.createElement("scannerAppliance");
            target.appendChild(scannerAppliance);

            Element sApplianceType = doc.createElement("type");
            sApplianceType.appendChild(doc.createTextNode(ConfigurationReader.getConfigProperty
                    ("scanner_appliance_type")));
            scannerAppliance.appendChild(sApplianceType);

            Element profile = doc.createElement("profile");
            WasScan.appendChild(profile);

            Element profileId = doc.createElement("id");
            profileId.appendChild(doc.createTextNode(ConfigurationReader.getConfigProperty("option_profile_id")));
            profile.appendChild(profileId);

            StringWriter stringWriter = buildSecureStringWriter(doc);
            output = stringWriter.getBuffer().toString();

            return output;

        } catch (ParserConfigurationException e) {
            throw new ScannerException("Error while parsing the XML!", e);
        }
    }

    /**
     * Build a secure String writer.
     *
     * @param doc Document that needs to be converted to String
     * @return StringWriter
     * @throws ScannerException
     */
    private static StringWriter buildSecureStringWriter(Document doc) throws ScannerException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer;

        } catch (TransformerConfigurationException e) {
            throw new ScannerException("Configuration error!", e);
        } catch (TransformerException e) {
            throw new ScannerException("Error when transforming data!", e);
        }
    }
}
