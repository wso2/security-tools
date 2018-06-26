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

import java.io.IOException;

/**
 * InputAdapter -- This interface exclusively consists of methods that must be implemented by all the input adapters.
 * Implementation of method convert() should offer functionality to convert the uploaded
 * data file in a given format to the JSON format.
 *
 * @author Arshika Mohottige
 */
public interface InputAdapter {

    /**
     * Converts the data in the data files uploaded; to the JSON format.
     * The "data" JSONObject will hold the values of all the data contained in the uploaded data file.
     *
     * @param dataFilePath The path where the data file uploaded by the client is saved.
     * @return returns the JSON object that contains all the data in the data file.
     * @throws IOException If the data file is not found in the given path.
     */
    JSONObject convert(String dataFilePath) throws IOException;

}
