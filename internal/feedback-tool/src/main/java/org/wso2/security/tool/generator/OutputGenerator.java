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

import org.wso2.security.tool.exception.FeedbackToolException;

/**
 * OutputGenerator -- The common interface implemented by all output generators.
 * Implementation of the method generate() should offer functionality to generate a file of the given output file type.
 */
public interface OutputGenerator {

    /**
     * Generates a specific file by the output generators (HTML/ PDF).
     * Converts the given template file (.hbs) and the data in JSON format to a desired output file format (HTML/PDF).
     *
     * @param outputFilePath The output file path where the output file is created.
     * @throws FeedbackToolException If an Exception is thrown inside the method implementation.
     */
    void generate(String outputFilePath) throws FeedbackToolException;

}
