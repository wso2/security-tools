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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Jackson2Helper;
import com.github.jknack.handlebars.JsonNodeValueResolver;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tool.util.Constants;
import org.wso2.security.tool.util.FileHandler;

import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * HTMLOutputGenerator -- This class consists of functionality to generate an output HTML file by applying
 * the input data which is converted to the JSON format; to the handlebars template.
 * Which allows the client to review the file before generating the PDF.
 * The generated HTML file is created at the specified output file path location.
 *
 * @author Arshika Mohottige
 */
public class HTMLOutputGenerator implements OutputGenerator {

    private static final Logger log = LoggerFactory.getLogger(HTMLOutputGenerator.class);
    private String templateFileName;
    private JSONObject dataJSONObject;
    private String hbsFileDirectory;

    /**
     * Constructor to set the values of the variables hbsFilePath, templateFileName and the dataJSONObject.
     *
     * @param hbsFileDirectory The directory where the uploaded template file is saved.
     * @param dataJSONObject   The dataJSONObject the data file after converting to the JSON format.
     * @param templateFileName The name of the uploaded template file (.hbs).
     */
    public HTMLOutputGenerator(JSONObject dataJSONObject, String hbsFileDirectory, String templateFileName) {
        this.dataJSONObject = dataJSONObject;
        this.hbsFileDirectory = hbsFileDirectory;
        this.templateFileName = templateFileName;
    }

    /**
     * Getter for the variable hbsFileDirectory.
     *
     * @return the value of the variable hbsFileDirectory.
     */
    public String getHbsFileDirectory() {
        return hbsFileDirectory;
    }

    /**
     * Getter for the variable dataJSONObject.
     *
     * @return the value of the variable dataJSONObject.
     */
    public JSONObject getDataJSONObject() {
        return dataJSONObject;
    }

    /**
     * Getter for the variable templateFileName.
     *
     * @return the value of the variable templateFileName.
     */
    public String getTemplateFileName() {
        return templateFileName;
    }


    /**
     * Generates an output HTML file by applying the input data in the JSON format to the handlebars template.
     * The uploaded template file is loaded, compiled and the data JSONObject is then applied to the compiled template.
     * The resulting template is assigned to a String object which is then passed to the writeToFile method of the
     * util class FileHandler.
     *
     * @param outputFilePath The output file path where the output html file is created.
     * @throws IOException If the readTree() method throws IOException due to missing input while handling tree model
     *                     nodes with Jackson.
     */
    public void generate(String outputFilePath) throws IOException {
        Template template;
        Context context;
        try {
            // Handling tree model nodes with Jackson.
            JsonNode jsonNode = new ObjectMapper().readTree(this.getDataJSONObject().toJSONString());
            Handlebars handlebars = new Handlebars();
            handlebars.registerHelper("json", Jackson2Helper.INSTANCE);

            context = Context
                    .newBuilder(jsonNode)
                    .resolver(JsonNodeValueResolver.INSTANCE,
                            JavaBeanValueResolver.INSTANCE,
                            FieldValueResolver.INSTANCE,
                            MapValueResolver.INSTANCE,
                            MethodValueResolver.INSTANCE
                    )
                    .build();

            /**
             * Loads template files from the specified file path. The base directory path must be specified at the
             *             creation time; which then serves as the template repository.
             */
            TemplateLoader loader = new FileTemplateLoader(this.getHbsFileDirectory(), ".hbs");
            handlebars = new Handlebars(loader);

            template = handlebars.compile(this.getTemplateFileName());
            String html = template.apply(context);

            FileHandler.writeToFile(html, System.getProperty("java.io.tmpdir") +
                    Constants.OUTPUT_HTML_FILE);
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException was thrown while compiling the template file; " + e.getMessage(), e);
        }
    }

}
