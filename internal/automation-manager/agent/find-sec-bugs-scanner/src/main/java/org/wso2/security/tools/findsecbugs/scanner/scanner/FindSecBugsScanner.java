/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.security.tools.findsecbugs.scanner.scanner;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.wso2.security.tools.findsecbugs.scanner.Constants;
import org.wso2.security.tools.findsecbugs.scanner.NotificationManager;
import org.wso2.security.tools.findsecbugs.scanner.exception.NotificationManagerException;
import org.wso2.security.tools.findsecbugs.scanner.handler.FileHandler;
import org.wso2.security.tools.findsecbugs.scanner.handler.MavenHandler;
import org.wso2.security.tools.findsecbugs.scanner.handler.XMLHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

/**
 * The class {@code FindSecBugsScanner} contains methods to run {@code FindSecBugs} scan and generate report
 */
public class FindSecBugsScanner {

    //FindSecBugs related
    private static final String FIND_BUGS_REPORT = "findbugsXml.xml";
    private static final String FINDBUGS_SECURITY_INCLUDE = "findbugs-security-include.xml";
    private static final String FINDBUGS_SECURITY_EXCLUDE = "findbugs-security-exclude.xml";
    //Maven Commands
    private static final String MVN_COMMAND_FIND_SEC_BUGS = "findbugs:findbugs";
    private static final String MVN_COMMAND_COMPILE = "compile";
    private static final Logger LOGGER = LoggerFactory.getLogger(FindSecBugsScanner.class);

    /**
     * The main contract of this method is to run the {@code FindSecBugs} scanning process
     * <p>Modifies the pom.xml file and add {@code FindBugs} plugin. Then build the product source code. Finally
     * find the generated reports, rename and move to a new folder</p>
     *
     * @throws MavenInvocationException
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public void runScan() throws MavenInvocationException, TransformerException, ParserConfigurationException,
            IOException, SAXException, NotificationManagerException {
        File reportsFolder = new File(Constants.REPORTS_FOLDER_PATH);
        LOGGER.info("FindSecBugs started");
        NotificationManager.notifyScanStatus("running");
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        createAndWriteToFindSecBugsIncludeFile(transformer);
        createAndWriteToFindSecBugsExcludeFile(transformer);
        appendFindSecBugsPlugin(transformer);
        MavenHandler.runMavenCommand(FindSecBugsScannerExecutor.getProductPath() + File.separator + Constants.POM_FILE,
                MVN_COMMAND_COMPILE);
        MavenHandler.runMavenCommand(FindSecBugsScannerExecutor.getProductPath() + File.separator + Constants.POM_FILE,
                MVN_COMMAND_FIND_SEC_BUGS);
        LOGGER.info("FindSecBugs scan completed");
        NotificationManager.notifyScanStatus("completed");
        if (reportsFolder.exists() || reportsFolder.mkdir()) {
            String reportsFolderPath = Constants.REPORTS_FOLDER_PATH + File.separator + Constants
                    .FIND_SEC_BUGS_REPORTS_FOLDER;
            FileHandler.findFilesRenameAndMoveToFolder(FindSecBugsScannerExecutor.getProductPath(), reportsFolderPath,
                    FIND_BUGS_REPORT);
            File fileToZip = new File(Constants.REPORTS_FOLDER_PATH);
            String destinationZipFilePath = Constants.REPORTS_FOLDER_PATH + Constants.ZIP_FILE_EXTENSION;
            FileHandler.zipFolder(fileToZip, fileToZip.getName(), destinationZipFilePath);
        }
    }

    private void createAndWriteToFindSecBugsIncludeFile(Transformer transformer) throws
            ParserConfigurationException, TransformerException {
        LOGGER.trace("Creating and writing to findbugs-security-include.xml file");
        File findSecBugsIncludeFile = new File(FindSecBugsScannerExecutor.getProductPath() + File.separator +
                FINDBUGS_SECURITY_INCLUDE);
        DocumentBuilder dBuilder;
        dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        assert dBuilder != null;
        Document findSecBugsIncludeDocument = dBuilder.newDocument();
        XMLHandler.writeFindSecBugsIncludeFile(findSecBugsIncludeDocument);
        DOMSource findSecBugsIncludeSource = new DOMSource(findSecBugsIncludeDocument);
        StreamResult findSecBugsIncludeResult = new StreamResult(findSecBugsIncludeFile);
        transformer.transform(findSecBugsIncludeSource, findSecBugsIncludeResult);
    }

    private void createAndWriteToFindSecBugsExcludeFile(Transformer transformer) throws ParserConfigurationException,
            TransformerException {
        LOGGER.trace("Creating and writing to findbugs-security-exclude.xml file");
        File findSecBugsExcludeFile = new File(FindSecBugsScannerExecutor.getProductPath() + File.separator +
                FINDBUGS_SECURITY_EXCLUDE);
        DocumentBuilder dBuilder;
        dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        assert dBuilder != null;
        Document findSecBugsExcludeDocument = dBuilder.newDocument();
        XMLHandler.writeFindSecBugsExcludeFile(findSecBugsExcludeDocument);
        DOMSource findSecBugsExcludeSource = new DOMSource(findSecBugsExcludeDocument);
        StreamResult findSecBugsExcludeResult = new StreamResult(findSecBugsExcludeFile);
        transformer.transform(findSecBugsExcludeSource, findSecBugsExcludeResult);
    }

    private void appendFindSecBugsPlugin(Transformer transformer) throws ParserConfigurationException, IOException,
            SAXException, TransformerException {
        LOGGER.trace("appending findsecbugs plugin to pom.xml file");
        File productPomFile = new File(FindSecBugsScannerExecutor.getProductPath() + File.separator + Constants
                .POM_FILE);
        DocumentBuilder dBuilder;
        dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document findBugsPluginDocument;
        findBugsPluginDocument = dBuilder.parse(productPomFile);
        findBugsPluginDocument = XMLHandler.appendFindBugsPlugin(findBugsPluginDocument.getDocumentElement(),
                findBugsPluginDocument);
        DOMSource findBugsPluginSource = new DOMSource(findBugsPluginDocument);
        StreamResult result = new StreamResult(FindSecBugsScannerExecutor.getProductPath() + File.separator +
                Constants.POM_FILE);
        transformer.transform(findBugsPluginSource, result);
    }
}
