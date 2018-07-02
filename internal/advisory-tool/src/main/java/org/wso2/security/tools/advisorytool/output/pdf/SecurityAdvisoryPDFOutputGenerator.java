/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.security.tools.advisorytool.output.pdf;

import com.lowagie.text.DocumentException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.wso2.security.tools.advisorytool.exeption.AdvisoryToolException;
import org.wso2.security.tools.advisorytool.model.SecurityAdvisory;
import org.wso2.security.tools.advisorytool.output.html.SecurityAdvisoryHTMLOutputGenerator;
import org.wso2.security.tools.advisorytool.utils.Constants;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This class generates the security advisory PDF.
 */
public class SecurityAdvisoryPDFOutputGenerator extends SecurityAdvisoryHTMLOutputGenerator {

    private static final Logger logger = Logger.getLogger(SecurityAdvisoryPDFOutputGenerator.class);

    @Override
    public boolean isAdvisoryGenerateFromFile() {
        return false;
    }

    @Override
    public void generate(SecurityAdvisory securityAdvisory) throws AdvisoryToolException {

        logger.info("Security Advisory PDF generation started");
        File outputFile = new File(Constants.SECURITY_ADVISORY_OUTPUT_DIRECTORY
                + File.separator + "pdf" + File.separator + securityAdvisory.getName() + ".pdf");

        File outputDirectory = new File(outputFile.getParent());
        outputDirectory.mkdirs();
        if (!outputDirectory.exists()) {
            throw new AdvisoryToolException("Unable to create the directory " + outputDirectory);
        }

        String securityAdvisoryHTML = generateAdvisoryHTML(securityAdvisory);
        createPDFFromHTML(securityAdvisoryHTML, outputFile.toString());
        logger.info("Security Advisory PDF generation completed");
    }

    /**
     * This method is used to generate the PDF from the given html string.
     *
     * @param htmlString
     * @param outputFilePath
     */
    protected void createPDFFromHTML(String htmlString, String outputFilePath) throws AdvisoryToolException {

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(htmlString
                .getBytes(StandardCharsets.UTF_8))) {
            Document document = XMLResource.load(byteArrayInputStream).getDocument();
            createPDF(document, outputFilePath);
        } catch (IOException e) {
            throw new AdvisoryToolException("Failed to generate the Security Advisory PDF.", e);
        }
    }

    /**
     * Generates the PDF for a given Document.
     * @param document
     * @param outputFilePath
     * @throws AdvisoryToolException
     */
    private void createPDF(Document document, String outputFilePath) throws AdvisoryToolException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(document, "/");
        renderer.layout();
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath)) {
            renderer.createPDF(fileOutputStream);
        } catch (DocumentException | IOException e) {
            throw new AdvisoryToolException("Failed to generate the Security Advisory PDF.", e);
        }
    }
}
