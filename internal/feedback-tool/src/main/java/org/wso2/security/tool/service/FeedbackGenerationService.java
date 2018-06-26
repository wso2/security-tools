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
package org.wso2.security.tool.service;


import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.security.tool.adapter.InputAdapter;
import org.wso2.security.tool.generator.HTMLOutputGenerator;
import org.wso2.security.tool.generator.OutputGenerator;
import org.wso2.security.tool.generator.PDFFromHTMLOutputGenerator;
import org.wso2.security.tool.generator.PDFOutputGenerator;
import org.wso2.security.tool.util.Constants;
import org.wso2.security.tool.util.FileHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Customer Feedback Generation Service. -------------------------------------------------------------------------------
 * FeedbackGenerationService - This class consists of functionality to accept uploaded files by the clients and
 * to generate the requested output file. The data files uploaded are converted to JSON objects; prior to applying
 * the data to the template file (.hbs). The operations supported are generateHTML(), generatePDF() and
 * generate PDFFromHTML() which will instantiate the corresponding generators and delegate the functionality.
 *
 * @author Arshika Mohottige
 */
@Path("/security-feedback")
public class FeedbackGenerationService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackGenerationService.class);
    private OutputGenerator outputGenerator = null;
    private InputAdapter adapter = null;

    /**
     * Generates an output HTML file by applying the input data to the handlebars template.
     * The input data file and the template file are saved. The data file is converted in to a JSONObject to contain
     * all the data in the data file. The template file is compiled and the data is applied to the compiled template
     * file. The resulting template file is then generated as an HTML file.
     *
     * @param hbsFileInfo         The file details of the template file uploaded.
     * @param hbsFileInputStream  The template file InputStream.
     * @param dataFileInfo        The file details of the data file uploaded.
     * @param dataFileInputStream The data file InputStream.
     * @return returns a response object from the current ResponseBuilder with its associated metadata.
     */
    @POST
    @Path("/generate-html")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("text/html")
    public Response generateHTML(@FormDataParam("hbs") FileInfo hbsFileInfo,
                                 @FormDataParam("hbs") InputStream hbsFileInputStream,
                                 @FormDataParam("data") FileInfo dataFileInfo,
                                 @FormDataParam("data") InputStream dataFileInputStream) {
        String dataFilePath;
        JSONObject dataJSONObject = null;

        // Saving the uploaded handlebars template file
        try {
            FileHandler.saveUploadedFile(hbsFileInputStream, hbsFileInfo);
        } catch (IOException e) {
            log.error("Error occurred while saving the template file: " + hbsFileInfo.getFileName()
                    + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            IOUtils.closeQuietly(hbsFileInputStream);
        }

        // Saving the uploaded data (.xls/.xml) file
        try {
            dataFilePath = FileHandler.saveUploadedFile(dataFileInputStream, dataFileInfo);
        } catch (IOException e) {
            log.error("Error occured while saving the data file: " + dataFileInfo.getFileName() + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            IOUtils.closeQuietly(dataFileInputStream);
        }

        // Converting the uploaded data to a JSON object
        try {
            adapter = FileHandler.getAdapter(dataFileInfo);
            dataJSONObject = adapter.convert(dataFilePath);
        } catch (IOException e) {
            log.error("Error occurred while converting data in the file: " + dataFileInfo.getFileName()
                    + e.getMessage(), e);
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            log.error("Error occurred while obtaining the InputAdapter; " + e.getMessage(), e);
        }

        // Generating the output html page by combining the JSON data and the handlebars template
        outputGenerator = new HTMLOutputGenerator(dataJSONObject, System.getProperty("java.io.tmpdir"),
                hbsFileInfo.getFileName());
        try {
            outputGenerator.generate(System.getProperty("java.io.tmpdir"));
        } catch (IOException e) {
            log.error("Error occurred while generating the output HTML file; " + e.getMessage(), e);
        }

        File file = new File(System.getProperty("java.io.tmpdir") + Constants.OUTPUT_HTML_FILE);
        Response.ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition",
                "attachment; filename=feedback-output.html");
        return response.build();
    }

    /**
     * Generates an output PDF file by reading an HTML file.
     * The input html file is saved. The html file is then read by the PDFFromHTMLGenerator and converted to
     * a html string which is then generated as a PDF file.
     *
     * @param htmlFileInfo        The file details of the html file uploaded.
     * @param htmlFileInputStream The html file InputStream.
     * @return returns a response object from the current ResponseBuilder with its associated metadata.
     */
    @POST
    @Path("/generate-pdf-from-html")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/pdf")
    public Response generatePDFFromHTML(@FormDataParam("html") FileInfo htmlFileInfo,
                                        @FormDataParam("html") InputStream htmlFileInputStream) {
        String htmlFilePath;

        // Saving the uploaded handlebars template file
        try {
            htmlFilePath = FileHandler.saveUploadedFile(htmlFileInputStream, htmlFileInfo);
        } catch (IOException e) {
            log.error("Error occurred while saving the html file: " + htmlFileInfo.getFileName() + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            IOUtils.closeQuietly(htmlFileInputStream);
        }

        // Instantiating outputGenerator
        outputGenerator = new PDFFromHTMLOutputGenerator(htmlFilePath);

        // Generating the output pdf page
        try {
            outputGenerator.generate(System.getProperty("java.io.tmpdir"));
        } catch (IOException e) {
            log.error("Error occurred while generating the output PDF file; " + e.getMessage(), e);
        }
        File file = new File(System.getProperty("java.io.tmpdir") + Constants.OUTPUT_PDF_FILE);
        Response.ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition",
                "attachment; filename=feedback-output.pdf");
        return response.build();
    }

    /**
     * Generates an output PDF file by first generating an HTMl file by applying the data to the template file
     * and then converting that html file to pdf.
     * The PDFOutputGenerator uses instances of HTMLOutputGenerator and PDFFromHTMLOutputGenerator to first generate
     * an html file and then convert that html to a pdf file.
     *
     * @param hbsFileInfo         The file details of the template file uploaded.
     * @param hbsFileInputStream  The template file InputStream.
     * @param dataFileInfo        The file details of the data file uploaded.
     * @param dataFileInputStream The data file InputStream.
     * @return returns a response object from the current ResponseBuilder with its associated metadata.
     */
    @POST
    @Path("/generate-pdf")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/pdf")
    public Response generatePDF(@FormDataParam("hbs") FileInfo hbsFileInfo,
                                @FormDataParam("hbs") InputStream hbsFileInputStream,
                                @FormDataParam("data") FileInfo dataFileInfo,
                                @FormDataParam("data") InputStream dataFileInputStream) {
        String dataFilePath;
        JSONObject dataJSONObject = null;

        // Saving the uploaded handlebars template file
        try {
            FileHandler.saveUploadedFile(hbsFileInputStream, hbsFileInfo);
        } catch (IOException e) {
            log.error("Error occurred while saving the template file: " + hbsFileInfo.getFileName()
                    + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            IOUtils.closeQuietly(hbsFileInputStream);
        }

        // Saving the uploaded data (.xls/.xml) file
        try {
            dataFilePath = FileHandler.saveUploadedFile(dataFileInputStream, dataFileInfo);
        } catch (IOException e) {
            log.error("Error occurred while saving the data file: " + dataFileInfo.getFileName() + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            IOUtils.closeQuietly(dataFileInputStream);
        }

        // Converting the uploaded data to a JSON object
        try {
            adapter = FileHandler.getAdapter(dataFileInfo);
            dataJSONObject = adapter.convert(dataFilePath);
        } catch (IOException e) {
            log.error("Error occurred while converting data in the file: " + dataFileInfo.getFileName()
                    + e.getMessage(), e);
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            log.error("Error occurred while obtaining the InputAdapter; " + e.getMessage(), e);
        }

        // Instantiating outputGenerator
        outputGenerator = new PDFOutputGenerator(
                new HTMLOutputGenerator(dataJSONObject, System.getProperty("java.io.tmpdir"),
                        hbsFileInfo.getFileName()),
                new PDFFromHTMLOutputGenerator(System.getProperty("java.io.tmpdir")
                        + Constants.OUTPUT_HTML_FILE)
        );

        // Generating the output pdf page
        try {
            outputGenerator.generate(System.getProperty("java.io.tmpdir"));
        } catch (IOException e) {
            log.error("Error occurred while generating the output PDF file; " + e.getMessage(), e);
        }
        File file = new File(System.getProperty("java.io.tmpdir") + Constants.OUTPUT_PDF_FILE);
        Response.ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition",
                "attachment; filename=feedback-output.pdf");
        return response.build();
    }
}
