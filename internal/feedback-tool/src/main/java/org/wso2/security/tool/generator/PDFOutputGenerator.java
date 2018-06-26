/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.security.tool.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * PDFOutputGenerator -- This class consists of functionality to generate an output PDF file by using the functionality
 * offered by the generators; HTMLOutputGenerator and PDFFromHTMLOutputGenerator.
 * The method convert() generates an output PDF file by first generating an HTML file by applying the uploaded data
 * to the uploaded template file using the functionality provided by class HTMLOutputGenerator. The generated HTML
 * file is then read and converted to a PDF file using the functionality provided by class PDFFromHTMLOutputGenerator.
 *
 * @author Arshika Mohottige
 */
public class PDFOutputGenerator implements OutputGenerator {

    private static final Logger log = LoggerFactory.getLogger(PDFOutputGenerator.class);
    private HTMLOutputGenerator htmlOutputGenerator;
    private PDFFromHTMLOutputGenerator pdfFromHTMLOutputGenerator;

    /**
     * Constructor to set the values of the reference variables htmlOutputGenerator and pdfFromHTMLOutputGenerator.
     *
     * @param htmlOutputGenerator        An instance of the class HTMLOutputGenerator.
     * @param pdfFromHTMLOutputGenerator An instance of the class PDFFromHTMLOutputGenerator.
     */
    public PDFOutputGenerator(HTMLOutputGenerator htmlOutputGenerator, PDFFromHTMLOutputGenerator
            pdfFromHTMLOutputGenerator) {
        this.htmlOutputGenerator = htmlOutputGenerator;
        this.pdfFromHTMLOutputGenerator = pdfFromHTMLOutputGenerator;
    }

    /**
     * Getter for the reference variable htmlOutputGenerator.
     *
     * @return returns the value of reference variable htmlOutputGenerator.
     */
    public HTMLOutputGenerator getHtmlOutputGenerator() {
        return htmlOutputGenerator;
    }

    /**
     * Getter for the value of reference variable pdfFromHTMLOutputGenerator.
     *
     * @return returns the value of the reference variable pdfFromHTMLOutputGenerator.
     */
    public PDFFromHTMLOutputGenerator getPdfFromHTMLOutputGenerator() {
        return pdfFromHTMLOutputGenerator;
    }

    /**
     * Generates an output PDF file by first generating an HTML file using an instance of class HTMLOutputGenerator
     * and then generating a PDF file using an instance of class PDFFromHTMLOutputGenerator.
     *
     * @param outputFilePath The output file path where the output pdf file is created.
     * @throws IOException If the exception is thrown by
     */
    @Override
    public void generate(String outputFilePath) throws IOException {
        if (this.getHtmlOutputGenerator() != null) {
            htmlOutputGenerator.generate(outputFilePath);
        } else {
            log.error("The value of the reference variable htmlOutputGenerator is null");
        }
        if (this.getPdfFromHTMLOutputGenerator() != null) {
            pdfFromHTMLOutputGenerator.generate(outputFilePath);
        } else {
            log.error("The value of the reference variable pdfFromHTMLOutputGenerator is null");
        }
    }
}
