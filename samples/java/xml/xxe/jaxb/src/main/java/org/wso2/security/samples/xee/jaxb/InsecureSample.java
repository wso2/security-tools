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

package org.wso2.security.samples.xee.jaxb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.security.samples.xee.jaxb.entity.Foo;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;




/**
 * InsecureSample without overiding the EntityExpansionLimit from xerces.util.SecurityManager
 * The default value for EntityExpansion limit from xerces.util.SecurityManager will be used
 * even an environment property is added for the EntityExpansionLimit.
 */

public class InsecureSample {
    private static final Log log = LogFactory.getLog(InsecureSample.class);
    private XMLReader xmlReader;

    private static void loadAssociationConfig() {

        try {
            JAXBContext jc = JAXBContext.newInstance(Foo.class);
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();
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
