
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.security.tools.zap.ext.zapwso2jiraplugin;

import org.apache.log4j.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class JiraSecurityManager {

    private static final Logger log = Logger.getRootLogger();

    /**
     * Create DocumentBuilderFactory with the XXE and XEE prevention measurements.
     *
     * @return DocumentBuilderFactory instance
     */
    public static DocumentBuilderFactory getSecuredDocumentBuilderFactory() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        try {
            dbf.setFeature(
                    IssueCreatorConstants.SAX_FEATURE_PREFIX + IssueCreatorConstants.EXTERNAL_GENERAL_ENTITIES_FEATURE,
                    false);
            dbf.setFeature(IssueCreatorConstants.SAX_FEATURE_PREFIX
                    + IssueCreatorConstants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(
                    IssueCreatorConstants.XERCES_FEATURE_PREFIX + IssueCreatorConstants.LOAD_EXTERNAL_DTD_FEATURE,
                    false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + IssueCreatorConstants.EXTERNAL_GENERAL_ENTITIES_FEATURE
                    + " or " +
                    IssueCreatorConstants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or "
                    + IssueCreatorConstants.LOAD_EXTERNAL_DTD_FEATURE +
                    " or secure-processing.");
        }

        org.apache.xerces.util.SecurityManager securityManager = new org.apache.xerces.util.SecurityManager();
        securityManager.setEntityExpansionLimit(IssueCreatorConstants.ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(IssueCreatorConstants.XERCES_PROPERTY_PREFIX + IssueCreatorConstants.SECURITY_MANAGER_PROPERTY,
                securityManager);

        return dbf;
    }
}
