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

package org.wso2.security.samples.xee.dom;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * SecureSample to handle XXE
 */

public class InsecureSample {
    private static final Log log = LogFactory.getLog(SecureSample.class);

    private static final int ENTITY_EXPANSION_LIMIT = 0;


    public static void loadAssociationConfig() {
        String associationConfigFile = "src/main/resources/input.xml";
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(associationConfigFile));
            doc.getDocumentElement().normalize();
        } catch (FileNotFoundException e) {
            log.error("Failed to find the input.xml", e);
        } catch (ParserConfigurationException | SAXException e) {
            log.error("Failed to parse the input.xml", e);
        } catch (IOException e) {
            log.error("Error while reading the input.xml", e);
        }
    }

    public static void main(String[] args) {
        loadAssociationConfig();
    }


}
