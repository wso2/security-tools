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
package org.wso2.security.tool.adapter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tool.exception.FeedbackToolException;

import java.io.FileReader;
import java.io.IOException;

/**
 * JSONInputAdapter -- This class consists of functionality to read the uploaded .json files and convert the data to a
 * JSONObject. The method convert() reads and converts the data in the .json file to a JSONObject and returns a
 * JSONObject that contains all the data in the input data file.
 *
 * @author Arshika Mohottige
 */
public class JSONInputAdapter implements InputAdapter {

    private static final Logger log = LoggerFactory.getLogger(JSONInputAdapter.class);

    /**
     * Reads the data in the uploaded .json files and convert the data into a JSONObject. The JSONObject is then
     * returned.
     *
     * @param dataFilePath The path where the data file uploaded by the client is saved.
     * @return returns the JSON object that contains all the data in the data file.
     * @throws FeedbackToolException If an Exception is thrown inside the method implementation.
     */
    @Override
    public JSONObject convert(String dataFilePath) throws FeedbackToolException {
        JSONParser parser = new JSONParser();
        Object object;
        try {
            object = parser.parse(new FileReader(dataFilePath));
            JSONObject jsonObject = (JSONObject) object;
            log.info("Returning the JSON object");
            return jsonObject;
        } catch (IOException e) {
            throw new FeedbackToolException("IOException was thrown while reading the uploaded JSON file" , e);
        } catch (ParseException e) {
            throw new FeedbackToolException("ParseException was thrown while parsing the data", e);
        }
    }
}
