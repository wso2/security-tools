/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package xxe.jaxb;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.util.SecurityManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import xxe.entity.Foo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;


/**
 * DemoWithSecurityManager with overiding the EntityExpansionLimit from xerces.util.SecurityManager
 */

public class DemoWithSecurityManager {
    private static final Log log = LogFactory.getLog(DemoWithSecurityManager.class);

    private static void loadAssociationConfig() {

        try {
            JAXBContext jc = JAXBContext.newInstance(Foo.class);
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser saxParser = spf.newSAXParser();
            SecurityManager securityManager = new SecurityManager();
            securityManager.setEntityExpansionLimit(1);
            saxParser.setProperty("http://apache.org/xml/properties/security-manager", securityManager);
            XMLReader xmlReader = saxParser.getXMLReader();
            InputSource inputSource =
                    new InputSource(new InputStreamReader
                            (new FileInputStream("src/main/resources/input.xml") , "UTF-8"));
            SAXSource source = new SAXSource(xmlReader, inputSource);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            Foo foo = (Foo) unmarshaller.unmarshal(source);
            log.info(foo.getValue());
        } catch (FileNotFoundException e) {
            log.error("Failed to find the input.xml", e);
        } catch (ParserConfigurationException | SAXException | JAXBException e) {
            log.error("Failed to parse the input.xml", e);
        } catch (UnsupportedEncodingException e) {
           log.error("Encoding type is not supported");
        }
    }

    public static void main(String[] args) {
        loadAssociationConfig();
    }
}
