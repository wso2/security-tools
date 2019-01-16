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

package org.wso2.security.tools.scanner.scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.eclipse.jgit.api.Git;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.scanner.config.ConfigurationReader;
import org.wso2.security.tools.scanner.exception.ScannerException;
import org.wso2.security.tools.scanner.handler.FileHandler;
import org.wso2.security.tools.scanner.handler.GitHandler;
import org.wso2.security.tools.scanner.handler.MavenHandler;
import org.wso2.security.tools.scanner.utils.ScannerConstants;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Observable;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Abstract class for the Scanner interface.
 */
public abstract class AbstractScanner extends Observable implements Scanner {
    static File workingDirectory = null;
    private static final Log log = LogFactory.getLog(AbstractScanner.class);
    private static final int ENTITY_EXPANSION_LIMIT = 0;

    public AbstractScanner() {
    }

    /**
     * Upload Multipart Zip file.
     *
     * @param zipFile file to upload
     * @param file    file to be created after the uploading
     * @return whether the file upload success
     * @throws ScannerException
     */
    public boolean uploadZipFile(MultipartFile zipFile, File file) throws ScannerException {
        boolean success = false;
        String zipFileName = zipFile.getOriginalFilename();

        if (file.exists() || file.mkdir()) {
            String fileUploadPath = ConfigurationReader.getConfigProperty(ScannerConstants.
                    DEFAULT_PRODUCT_PATH) + File.separator + zipFileName;
            success = FileHandler.uploadFile(zipFile, fileUploadPath);
            log.info("File successfully uploaded");
        }

        return success;
    }

    /**
     * Extract the provided zip file to a pre defined location.
     *
     * @param file File that need to extract
     * @return Extracted folder name
     * @throws ScannerException
     */
    public String extractZipFile(File file) throws ScannerException {
        try {
            return FileHandler.extractZipFile(ConfigurationReader.getConfigProperty(
                    ScannerConstants.DEFAULT_PRODUCT_PATH) + File.separator + file.getName());
        } catch (ScannerException e) {
            throw new ScannerException("Error occurred while extracting zip file.", e);
        } catch (IOException e) {
            throw new ScannerException("Error occurred while extracting zip file.", e);
        }
    }

    /**
     * Zip the working directory.
     *
     * @throws ScannerException
     */
    public void createZipFile() throws ScannerException {

        FileHandler.zipFiles(workingDirectory);
    }

    /**
     * Convert the given xmlFile to a XML Document.
     *
     * @param xmlFile file need to be converted
     * @return Converted Node list from the XML file
     * @throws ScannerException
     */
    public NodeList convertXMLFileToDocument(File xmlFile) throws ScannerException {
        DocumentBuilderFactory dbFactory = getSecuredDocumentBuilderFactory();
        DocumentBuilder dBuilder;
        Document doc;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile);
        } catch (ParserConfigurationException e) {
            throw new ScannerException("Parse Configuration Exception while parsing XML to document .", e);
        } catch (SAXException e) {
            throw new ScannerException("Parse Exception while parsing XML to document .", e);
        } catch (IOException e) {
            throw new ScannerException("IO Exception while parsing XML to document .", e);
        }
        doc.getDocumentElement().normalize();

        return doc.getElementsByTagName("format");
    }

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
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.
                    EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.
                    EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.
                    LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + Constants.
                    EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " + Constants.
                    EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE +
                    " or secure-processing.");
        }
        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY,
                securityManager);
        return dbf;
    }


    /**
     * Git clone a given github repository.
     *
     * @param gitUrl URL to repository needs to clone
     * @param branch branch to clone
     * @return whether the git clone success
     * @throws ScannerException
     */
    public boolean gitProductClone(String gitUrl, String branch) throws ScannerException {
        Git git;

        try {
            git = GitHandler.gitClone(gitUrl, ConfigurationReader.getConfigProperty
                    (ScannerConstants.DEFAULT_GIT_PRODUCT_PATH), branch);
            return GitHandler.hasAtLeastOneReference(git.getRepository());
        } catch (ScannerException e) {
            throw new ScannerException("Error occured while cloning the product repository. ", e);
        }
    }

    /**
     * Git check out to a specific branch.
     *
     * @param branch the branch to be checked out
     * @return whether git check out success
     * @throws ScannerException
     */
    public boolean gitCheckout(String branch) throws ScannerException {
        try {
            return GitHandler.gitCheckout(ConfigurationReader.getConfigProperty(ScannerConstants.
                            DEFAULT_GIT_PRODUCT_PATH),
                    branch);
        } catch (ScannerException e) {
            throw new ScannerException("Error occured while checkout the git branch of the product repository", e);
        }
    }

    /**
     * Build the product by running maven command.
     *
     * @return whether product build success
     * @throws ScannerException
     */
    public boolean mvnBuildProduct() throws ScannerException {
        try {
            MavenHandler.runMavenCommand(ConfigurationReader.getConfigProperty(
                    ScannerConstants.DEFAULT_GIT_PRODUCT_PATH),
                    ScannerConstants.MVN_BUILD_COMMAND);
            return true;
        } catch (ScannerException e) {
            throw new ScannerException("Error occured while building product. ", e);
        }
    }
}
