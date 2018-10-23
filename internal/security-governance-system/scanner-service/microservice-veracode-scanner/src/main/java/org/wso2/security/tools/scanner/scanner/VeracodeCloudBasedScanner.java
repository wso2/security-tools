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

import com.veracode.apiwrapper.AbstractAPIWrapper;
import com.veracode.apiwrapper.cli.VeracodeCommand;
import com.veracode.apiwrapper.wrappers.ResultsAPIWrapper;
import com.veracode.apiwrapper.wrappers.UploadAPIWrapper;
import com.veracode.parser.util.XmlUtils;
import com.veracode.util.lang.StringUtility;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.security.tools.scanner.Constants;
import org.wso2.security.tools.scanner.config.ConfigurationReader;
import org.wso2.security.tools.scanner.config.VeracodeScannerConfiguration;
import org.wso2.security.tools.scanner.exception.ScannerException;
import org.wso2.security.tools.scanner.handler.FileHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("VeracodeCloudBasedScannerImpl")
public class VeracodeCloudBasedScanner extends AbstractScanner {

    private ResultsAPIWrapper resultsAPIWrapper;
    private VeracodeCommand.Options options;
    //    private PrintStream outputWriter;
    private PrintStream logWriter;
    private UploadAPIWrapper uploadAPIWrapper;
    private static final Log log = LogFactory.getLog(VeracodeCloudBasedScanner.class);

    /**
     * Initialising the Veracode configurations
     *
     * @throws ScannerException
     */
    private static void loadConfiguration() throws ScannerException {
        ConfigurationReader.loadConfiguration();

        VeracodeScannerConfiguration.getInstance().setVeracodeUsername(
                ConfigurationReader.getConfigProperty(Constants.VERACODE_USERNAME));
        VeracodeScannerConfiguration.getInstance().setVeracodePassword(
                ConfigurationReader.getConfigProperty(Constants.VERACODE_PASSWORD));
        VeracodeScannerConfiguration.getInstance().setOutputFolderPath(
                ConfigurationReader.getConfigProperty(Constants.VERACODE_OUTPUT_FOLDER_PATH));
        VeracodeScannerConfiguration.getInstance().setOutputFilePath(ConfigurationReader.getConfigProperty(
                Constants.VERACODE_OUTPUT_FOLDER_PATH) + File.separator + Constants.VERACODE_OUTPUT_FILE_NAME);
        VeracodeScannerConfiguration.getInstance().setLogFilePath(ConfigurationReader.
                getConfigProperty(Constants.VERACODE_LOG_FILE_PATH));
        VeracodeScannerConfiguration.getInstance().setGitUsername(ConfigurationReader.
                getConfigProperty(Constants.GIT_USERNAME));
        VeracodeScannerConfiguration.getInstance().setGitPassword(ConfigurationReader.
                getConfigProperty(Constants.GIT_PASSWORD));
        VeracodeScannerConfiguration.getInstance().setScannerClass(ConfigurationReader.
                getConfigProperty(Constants.SCANNER_BEAN_CLASS_NAME));
        VeracodeScannerConfiguration.getInstance().setProductPathForZipFileUpload(ConfigurationReader.
                getConfigProperty(Constants.DEFAULT_PRODUCT_PATH));
        VeracodeScannerConfiguration.getInstance().setProductPathForGitClone(ConfigurationReader.
                getConfigProperty(Constants.DEFAULT_GIT_PRODUCT_PATH));
    }

    /**
     * Convert a given XML to XML Document
     *
     * @param xmlStr XML string that needs to convert
     * @return converted XML Document
     * @throws ScannerException
     */
    private static Document convertStringToDocument(String xmlStr) throws ScannerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        String FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
        DocumentBuilder builder;
        try {
            factory.setFeature(FEATURE, true);
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
            return doc;
        } catch (ParserConfigurationException e) {
            throw new ScannerException("Error parsing the String to XML", e);
        } catch (SAXException e) {
            throw new ScannerException("Error parsing the String to XML", e);
        } catch (IOException e) {
            throw new ScannerException("Error parsing the String to XML", e);
        }
    }

    /**
     * Initialising the Veracode Wrapper options
     *
     * @throws ScannerException
     */
    public void init() throws ScannerException {

        loadConfiguration();

        options = new VeracodeCommand.Options();
        options._vuser = VeracodeScannerConfiguration.getInstance().getVeracodeUsername();
        options._vpassword = VeracodeScannerConfiguration.getInstance().getVeracodePassword();
        options._output_folderpath = VeracodeScannerConfiguration.getInstance().getOutputFolderPath();
        options._output_filepath = VeracodeScannerConfiguration.getInstance().getOutputFilePath();
        options._log_filepath = VeracodeScannerConfiguration.getInstance().getLogFilePath();
        getUploadAPIWrapper(options);
        getResultAPIWrapper(options);
    }

    /**
     * Build the upload wrap for Upload API
     *
     * @param options represents the Veracode credentials and other required configurations
     * @return Upload API Wrapper
     * @throws ScannerException
     */
    private UploadAPIWrapper getUploadAPIWrapper(VeracodeCommand.Options options) throws ScannerException {
        uploadAPIWrapper = new UploadAPIWrapper();
        this.setUpWrapperCredentials(uploadAPIWrapper, options);

        return uploadAPIWrapper;
    }

    /**
     * Build the upload wrap for Result API
     *
     * @param options represents the Veracode credentials and other required configurations
     * @return
     * @throws ScannerException
     */
    private ResultsAPIWrapper getResultAPIWrapper(VeracodeCommand.Options options) throws ScannerException {
        resultsAPIWrapper = new ResultsAPIWrapper();
        this.setUpWrapperCredentials(resultsAPIWrapper, options);

        return resultsAPIWrapper;
    }

    /**
     * Run the scan using product zip file
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return the final report location or the Scan Status if failed
     * @throws ScannerException
     */
    @Override
    public String runScanUsingProductZip(ScannerRequestObject scannerRequestObject) throws ScannerException {
        if (options == null) {
            init();
        }

        String app_id = scannerRequestObject.getApp_id();
        String filePath = scannerRequestObject.getFilePath();
        File file = new File(filePath);
        String folderName = extractZipFile(file);

        FileHandler.DownloadProductFromFTP(scannerRequestObject.getFtpLocation(), scannerRequestObject.getProductName());

        return runScan(scannerRequestObject, folderName);
    }

    /**
     * Initiate the scan in the Veracode
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @param folderName           that product pack locates
     * @return the final report location or the Scan Status if failed
     * @throws ScannerException
     */
    private String runScan(ScannerRequestObject scannerRequestObject, String folderName) throws ScannerException {
        deleteLastScan(scannerRequestObject);

        String app_id = scannerRequestObject.getApp_id();
        workingDirectory = new File(folderName + Constants.WORK_DIRECTORY_SUFIX);

        try {
            workingDirectory.mkdirs();
        } catch (Exception e) {
            throw new ScannerException("Error while creating the Working Directory. ", e);
        }

        getRequiredFiles(folderName);
        createZipFile();

        if (uploadFileToVeracode(workingDirectory + Constants.ZIP_FILE_EXTENSION, app_id)) {
            if (beginPreScan(app_id)) {
                boolean status = beginScan(app_id);

                if (status) {
                    try {
                        TimeUnit.HOURS.sleep(4);
                        scannerRequestObject.setApp_id(app_id);
                        ScannerStatus scannerStatus = getLastScanStatus(scannerRequestObject);

                        while (!scannerStatus.equals(ScannerStatus.COMPLETED)) {
                            TimeUnit.MINUTES.sleep(10);
                            scannerStatus = getLastScanStatus(scannerRequestObject);
                        }

                        if (detailedReportPdf(scannerRequestObject)) {
                            return options._output_filepath;
                        }
                    } catch (InterruptedException e) {
                        throw new ScannerException("Error occured while thread sleeps. ", e);
                    }
                }
            }
        }
        return String.valueOf(getLastScanStatus(scannerRequestObject));
    }

    /**
     * Run the scan using product github location
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return the final report location or the Scan Status if failed
     * @throws ScannerException
     */
    @Override
    public String runScanUsingProductGitURL(ScannerRequestObject scannerRequestObject) throws ScannerException {
        if (options == null) {
            init();
        }

        String gitUrl = scannerRequestObject.getGitHubLoacation();
        String app_id = scannerRequestObject.getApp_id();
        String branch = scannerRequestObject.getGitBranch();
        File productFile = new File(VeracodeScannerConfiguration.getInstance().
                getProductPathForGitClone());

        GitProductClone(gitUrl, branch);
        GitCheckoutClone(branch);
        mvnBuildProduct();

        return runScan(scannerRequestObject, productFile.getAbsolutePath());
    }

    /**
     * Begin pre scan in Veracode before submit to the Scan
     *
     * @param app_id application id that need to start the pre scan
     * @return whether stating the pre scan success
     * @throws ScannerException
     */
    private boolean beginPreScan(String app_id) throws ScannerException {
        String result;

        try {
            result = uploadAPIWrapper.beginPreScan(
                    app_id, null,
                    "true", "true");
            boolean isValidXML = this.isValidXML(result);

            return isValidXML ? true : false;
        } catch (IOException e) {
            throw new ScannerException("Error while starting the pre scan .", e);
        }
    }

    /**
     * Begin scan in Veracode
     *
     * @param app_id application id that need to start the scan
     * @return whether stating the pre scan success
     * @throws ScannerException
     */
    private boolean beginScan(String app_id) throws ScannerException {
        String result;

        try {
            result = uploadAPIWrapper.beginScan(app_id, "all", "true");
        } catch (IOException e) {
            throw new ScannerException("IOException occured while starting the scan. ", e);
        }

        boolean isValidXML = this.isValidXML(result);

        return isValidXML ? true : false;
    }

    /**
     * Upload scan pack to the Veracode
     *
     * @param zipFile the product pack to scan
     * @param app_id  application id that is needed to upload
     * @return
     * @throws ScannerException
     */
    private boolean uploadFileToVeracode(String zipFile, String app_id) throws ScannerException {
        try {
            String result = uploadAPIWrapper.uploadFile(app_id, zipFile);
            boolean isValidXML = this.isValidXML(result);

            return isValidXML ? true : false;
        } catch (IOException e) {
            throw new ScannerException("IOException occured while uploading the scan pack to Veracode. ", e);
        }
    }

    /**
     * Set the Veracode credentials to the Veracode API wrappers
     *
     * @param wrapper Warpper that needs to be set credentials
     * @param options represents the Veracode credentials and other required configurations
     * @throws ScannerException
     */
    private void setUpWrapperCredentials(AbstractAPIWrapper wrapper, VeracodeCommand.Options options)
            throws ScannerException {
        String apiID = options._vid;
        String user;

        if (StringUtility.isNullOrEmpty(apiID)) {
            user = options._vuser;
            String pass = options._vpassword;

            if (StringUtility.isNullOrEmpty(user) && !StringUtility.isNullOrEmpty(options._api1)) {
                user = this.decodeB64(options._api1);
            }

            if (StringUtility.isNullOrEmpty(pass) && !StringUtility.isNullOrEmpty(options._api2)) {
                pass = this.decodeB64(options._api2);
            }

            wrapper.setUpCredentials(user, pass);
        } else {
            user = options._vkey;
            wrapper.setUpApiCredentials(apiID, user);
        }
    }

    /**
     * Do base64 decode for a given encoded string
     *
     * @param b64EncodedString encoded string that need to decode
     * @return decoded string
     * @throws ScannerException
     */
    private String decodeB64(String b64EncodedString) throws ScannerException {
        try {
            return new String(DatatypeConverter.parseBase64Binary(b64EncodedString), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ScannerException("Invalid base64-encoded String. ", e);
        }
    }

    /**
     * Check whether the provided string is a XML
     *
     * @param xmlResult XML String that need verify
     * @return whether the string is XML
     */
    private boolean isValidXML(String xmlResult) {
        xmlResult = XmlUtils.getDecodedXmlResponse(xmlResult, true);
        boolean isValidXML = false;
        String errorString = this.getErrorString(xmlResult);

        if (StringUtility.isNullOrEmpty(errorString) || errorString.contains("No modules selected")) {
            isValidXML = true;
        } else {
            log.info("Action : returned the following message:" + errorString);
        }

        return isValidXML;
    }

    /**
     * Filter out the error message from a given Veracode response XML
     *
     * @param xmlString XML string that contains the error message
     * @return the error message
     */
    private String getErrorString(String xmlString) {
        String errorString = null;
        StringBuilder builder = new StringBuilder();
        Pattern pattern = Pattern.compile("<error>(.*?)</error>");
        Matcher matcher = pattern.matcher(xmlString);
        while (matcher.find()) {
            builder.append(matcher.group(1) + "\r\n");
        }
        errorString = builder.toString();
        if (errorString.contains("\r\n")) {
            errorString = errorString.substring(0, builder.lastIndexOf("\r\n"));
        }
        return errorString;
    }

    /**
     * Returns the status of the last scan
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return Enum of the ScannerStatus
     */
    @Override
    public ScannerStatus getLastScanStatus(ScannerRequestObject scannerRequestObject) throws ScannerException {
        String status = null;
        String result;

        if (options == null) {
            init();
        }
        try {
            result = uploadAPIWrapper.getBuildInfo(scannerRequestObject.getApp_id());
        } catch (IOException e) {
            throw new ScannerException("Error while getting scan details of the application : "
                    + scannerRequestObject.getApp_id(), e);
        }

        boolean isValidXML = this.isValidXML(result);

        if (isValidXML) {
            status = getScanStatusForAGivenAppId(result);
        }

        if (status.equals("Not Submitted to Engine") || status.equals("Submitted to Engine") ||
                status.equals("Scan in Progress") || status.equals("Pending Internal Review")
                || status.equals("Pre-scan Submitted") || status.equals("Incomplete") ||
                status.equals("Pre-scan Success")) {
            return ScannerStatus.RUNNING;
        } else if (status.equals("Scan Canceled") || status.equals("Pre-scan Canceled")) {
            return ScannerStatus.CANCELED;
        } else if (status.equals("Pre-scan Failed")) {
            return ScannerStatus.FAILED;
        } else if (status.equals("Results Ready")) {
            return ScannerStatus.COMPLETED;
        }

        return null;
    }

    /**
     * Stop the last scan for a given application
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return whether delete scan operation success
     */
    @Override
    public boolean deleteLastScan(ScannerRequestObject scannerRequestObject) throws ScannerException {
        String result = null;

        if (options == null) {
            init();
        }

        try {
            result = uploadAPIWrapper.deleteBuild(scannerRequestObject.getApp_id());
        } catch (IOException e) {
            throw new ScannerException("Error occured while deleting the last scan of the application : "
                    + scannerRequestObject.getApp_id(), e);
        }

        boolean isValidXML = this.isValidXML(result);

        return isValidXML ? isValidXML : false;
    }

    /**
     * Controller method to stop the last scan for a given application
     *
     * @param scannerRequestObject Object that represent the required information for tha scanner operation
     * @return whether the report is downloaded
     */
    @Override
    public boolean detailedReportPdf(ScannerRequestObject scannerRequestObject) throws ScannerException {

        if (options == null) {
            init();
        }

        String build_id = getBuildIdForAGivenAppId(scannerRequestObject.getApp_id());
        byte[] result;

        try {
            result = resultsAPIWrapper.detailedReportPdf(build_id);
        } catch (IOException e) {
            throw new ScannerException("Error while downloading detail PDF report from the Veracode of the " +
                    "application " + scannerRequestObject.getApp_id(), e);
        }

        try {
            return printReport(result, options);
        } catch (Exception e) {
            throw new ScannerException("Error while printing the detail PDF report of the application "
                    + scannerRequestObject.getApp_id(), e);
        }
    }

    /**
     * Print the pdf report according to the Veracode response result report
     *
     * @param bytesResult Veracode response result
     * @param options     represents the Veracode credentials and other required configurations
     * @return whether report is successfully printed
     * @throws ScannerException
     */
    private boolean printReport(byte[] bytesResult, VeracodeCommand.Options options) throws ScannerException {
        byte[] pdfResult;
        pdfResult = bytesResult;
        boolean printStatus = false;

        if (pdfResult != null) {
            String filePath = options._output_filepath;

            if (!StringUtility.isNullOrEmpty(filePath)) {
                PrintStream writer = null;

                try {
                    writer = this.getPrintStream(new FileOutputStream(filePath));
                    writer.write(pdfResult, 0, pdfResult.length);
                } catch (Exception e) {
                    throw new ScannerException("Error while printing the report. ", e);
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
                printStatus = true;
            } else {
                log.error("Resulted in the following error(s): Output filepath is missing.");
            }
        } else {
            log.error("Unable to retrieve data from byte stream. ");
        }

        return printStatus;
    }

    /**
     * Create log writer
     *
     * @param options represents the Veracode credentials and other required configurations
     * @throws ScannerException
     */
    private void createLogWriter(VeracodeCommand.Options options) throws ScannerException {
        try {
            this.logWriter = new PrintStream(new FileOutputStream(options._log_filepath, true));
        } catch (FileNotFoundException e) {
            throw new ScannerException("Log file for Veracode Scanner does not found .", e);
        }

        this.logWriteLine(StringUtility.repeatChar('-', 80));
        this.logWriteLine(VeracodeCommand.getVersionString());
        this.logWriteLine(StringUtility.repeatChar('-', 80));
    }

    /**
     * Write logs
     *
     * @param text line to be written
     */
    private void logWriteLine(String text) {
        if (this.logWriter != null && !StringUtility.isNullOrEmpty(text)) {
            this.logWriter.println(text);
            this.logWriter.flush();
        }
    }

    /**
     * Create PrintStream
     *
     * @param ioOutput OutputStream that needs to build the printStream
     * @return build printStream
     * @throws ScannerException
     */
    private PrintStream getPrintStream(OutputStream ioOutput) throws ScannerException {
        PrintStream printStream = null;

        try {
            return printStream = new PrintStream(ioOutput, true, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new ScannerException("Unsupported encoding format with " + StandardCharsets.UTF_8.name(), e);
        }
    }

    /**
     * Get the scan status of a given application
     *
     * @param result Result that we got from the Veracode
     * @return the Scan's status
     * @throws ScannerException
     */
    private String getScanStatusForAGivenAppId(String result) throws ScannerException {
        String status = null;
        NodeList nodeList;
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr;

        Document doc = convertStringToDocument(result);

        try {
            expr = xpath.compile("//buildinfo/build/analysis_unit[@status]");
        } catch (XPathExpressionException e) {
            throw new ScannerException("Error occured while building the status XPath of the scan. ", e);
        }

        try {
            nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new ScannerException("Error occured while accessing the XPAth value for the status of " +
                    "the scan. ", e);
        }

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentItem = nodeList.item(i);
            status = currentItem.getAttributes().getNamedItem("status").getNodeValue();
        }

        return status;
    }

    /**
     * Get the last scan's unique Id of a given application
     *
     * @param app_id application id that is needed to upload
     * @return the buildId of the scan
     * @throws ScannerException
     */
    private String getBuildIdForAGivenAppId(String app_id) throws ScannerException {
        String build_id = null;
        String apiResult;
        NodeList nl;
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr;

        try {
            apiResult = uploadAPIWrapper.getBuildInfo(app_id);
        } catch (IOException e) {
            throw new ScannerException("IOException occured while getting scan info from Veracode for the " +
                    "application : " + app_id, e);
        }

        Document doc = convertStringToDocument(apiResult);

        try {
            expr = xpath.compile("//buildinfo/build[@build_id]");
        } catch (XPathExpressionException e) {
            throw new ScannerException("Error occured while building the buildID XPath of the scan for the " +
                    "application : " + app_id, e);
        }

        try {
            nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new ScannerException("Error while accessing the Xpath in the scan status results xml : " +
                    "" + app_id, e);
        }

        for (int i = 0; i < nl.getLength(); i++) {
            Node currentItem = nl.item(i);
            build_id = currentItem.getAttributes().getNamedItem("build_id").getNodeValue();
        }

        return build_id;
    }

    /**
     * Get the pre scan results from the Veracode
     *
     * @param app_id application id that is needed to upload
     * @return the Veracode result
     * @throws ScannerException
     */
    private String getPreScanResults(String app_id) throws ScannerException {

        if (options == null) {
            init();
        }

        String result = null;
        try {
            result = uploadAPIWrapper.getPreScanResults(app_id);
        } catch (IOException e) {
            throw new ScannerException("Error while getting pre scan results for application : " + app_id, e);
        }
        boolean isValidXML = this.isValidXML(result);

        return isValidXML ? result : null;
    }

    /**
     * Reupload product pack for scanning
     *
     * @param app_id application id that is needed to upload
     * @return the reesponse from the Veracode
     * @throws ScannerException
     */
    private String updateBuild(String app_id) throws ScannerException {

        if (options == null) {
            init();
        }

        String result = null;
        try {
            result = uploadAPIWrapper.updateBuild(app_id);
        } catch (IOException e) {
            throw new ScannerException("Error while updating the scan component lis of last scan : " + app_id, e);
        }
        boolean isValidXML = this.isValidXML(result);

        return isValidXML ? result : null;
    }

}

