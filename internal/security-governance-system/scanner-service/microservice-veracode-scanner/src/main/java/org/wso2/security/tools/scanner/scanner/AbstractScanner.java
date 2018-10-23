/*
 *  Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.eclipse.jgit.api.Git;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.scanner.Constants;
import org.wso2.security.tools.scanner.config.ConfigurationReader;
import org.wso2.security.tools.scanner.config.VeracodeScannerConfiguration;
import org.wso2.security.tools.scanner.exception.ScannerException;
import org.wso2.security.tools.scanner.handler.FileHandler;
import org.wso2.security.tools.scanner.handler.GitHandler;
import org.wso2.security.tools.scanner.handler.MavenHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Observable;

/**
 * Abstract class for the Scanner interface
 */
public abstract class AbstractScanner extends Observable implements Scanner {
    static File workingDirectory = null;
    private static final Log log = LogFactory.getLog(AbstractScanner.class);

    public AbstractScanner() {
    }

    /**
     * Upload Multipart Zip file
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
            String fileUploadPath = ConfigurationReader.getConfigProperty(Constants.DEFAULT_PRODUCT_PATH) +
                    File.separator + zipFileName;
            success = FileHandler.uploadFile(zipFile, fileUploadPath);
            log.info("File successfully uploaded");
        }

        return success;
    }

    /**
     * Extract the provided zip file to a pre defined location
     *
     * @param file File that need to extract
     * @return Extracted folder name
     * @throws ScannerException
     */
    public String extractZipFile(File file) throws ScannerException {
        try {
            return FileHandler.extractZipFile(ConfigurationReader.getConfigProperty(
                    Constants.DEFAULT_PRODUCT_PATH) + File.separator + file.getName());
        } catch (ScannerException e) {
            throw new ScannerException("Error occured while extracting zip file.", e);
        }
    }

    /**
     * Zip the working directory
     *
     * @throws ScannerException
     */
    public void createZipFile() throws ScannerException {

        FileHandler.zipFiles(workingDirectory);
    }

    /**
     * Convert the given xmlFile to a XML Document
     *
     * @param xmlFile file need to be converted
     * @return Converted Node list from the XML file
     * @throws ScannerException
     */
    private NodeList convertXMLFileToDocument(File xmlFile) throws ScannerException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        String FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
        DocumentBuilder dBuilder;
        Document doc;

        try {
            dbFactory.setFeature(FEATURE, true);
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile);
        } catch (ParserConfigurationException e) {
            throw new ScannerException("Error while parsing XML to document .", e);
        } catch (SAXException e) {
            throw new ScannerException("Error while parsing XML to document .", e);
        } catch (IOException e) {
            throw new ScannerException("Error while parsing XML to document .", e);
        }
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("format");

        return nList;
    }

    /**
     * Filter the file list that matches with the pattern in a file
     *
     * @param filePath Directory path that contains the jar list, which needs to check the matching patterns
     * @throws ScannerException
     */
    public void getRequiredFiles(String filePath) throws ScannerException {
        File dir = new File(filePath);
        File[] files = dir.listFiles();
        File patternXmlFile = new File(ConfigurationReader.getConfigProperty(Constants.JAR_FILTER_PATTERN_FILE_PATH));

        NodeList nodeList = convertXMLFileToDocument(patternXmlFile);

        for (File file : files) {
            if (file.isFile()) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    Element element = (Element) node;
                    String prefix = "";
                    String suffix = "";

                    if (element.getElementsByTagName(Constants.PREFIX).item(0) != null) {
                        prefix = String.valueOf(element.getElementsByTagName(Constants.PREFIX).
                                item(0).getChildNodes().item(0).getTextContent());
                    }

                    if (element.getElementsByTagName(Constants.SUFFIX).item(0) != null) {
                        suffix = String.valueOf(element.getElementsByTagName(Constants.SUFFIX)
                                .item(0).getChildNodes().item(0).getTextContent());
                    }

                    if (file.getName().endsWith(suffix) && file.getName().startsWith(prefix)) {
                        try {
                            File destFile = new File(workingDirectory + File.separator + file.getName());
                            Files.copy(file.getAbsoluteFile().toPath(), destFile.toPath());
                        } catch (IOException e) {
                            throw new ScannerException("Error occurred while copying the file to the working" +
                                    " directory", e);
                        }
                    }
                }
            } else if (file.isDirectory()) {
                getRequiredFiles(file.getAbsolutePath());
            }
        }
    }

    /**
     * Git clone a given github repository
     *
     * @param gitUrl URL to repository needs to clone
     * @param branch branch to clone
     * @return whether the git clone success
     * @throws ScannerException
     */
    public boolean GitProductClone(String gitUrl, String branch) throws ScannerException {

        Git git;
        try {
            git = GitHandler.gitClone(gitUrl, VeracodeScannerConfiguration.getInstance().getGitUsername(),
                    VeracodeScannerConfiguration.getInstance().getGitPassword(),
                    ConfigurationReader.getConfigProperty(Constants.DEFAULT_GIT_PRODUCT_PATH), branch);

            return GitHandler.hasAtLeastOneReference(git.getRepository());
        } catch (ScannerException e) {
            throw new ScannerException("Error occured while cloning the product repository. ", e);
        }
    }

    /**
     * Git check out to a specific branch
     *
     * @param branch the branch to be checked out
     * @return whether git check out success
     * @throws ScannerException
     */
    public boolean GitCheckoutClone(String branch) throws ScannerException {

        try {
            return GitHandler.gitCheckout(ConfigurationReader.getConfigProperty(Constants.DEFAULT_GIT_PRODUCT_PATH),
                    branch);
        } catch (ScannerException e) {
            throw new ScannerException("Error occured while checkout the git branch of the product repository", e);
        }
    }

    /**
     * Build the product by running maven command
     *
     * @return whether product build success
     * @throws ScannerException
     */
    public boolean mvnBuildProduct() throws ScannerException {
        try {
            MavenHandler.runMavenCommand(ConfigurationReader.getConfigProperty(
                    Constants.DEFAULT_GIT_PRODUCT_PATH) + File.separator + Constants.POM_FILE,
                    Constants.MVN_BUILD_COMMAND);
            return true;
        } catch (ScannerException e) {
            throw new ScannerException("Error occured while building product. ", e);
        }
    }

}