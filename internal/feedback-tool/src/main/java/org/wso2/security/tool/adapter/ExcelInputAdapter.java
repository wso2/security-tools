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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tool.exception.FeedbackToolException;
import org.wso2.security.tool.util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * ExcelInputAdapter -- This class exclusively consists of the functionality to convert the data in the data file
 * uploaded with the .xlsx extension to the JSON format.The method convert() in this class
 * returns a JSONObject containing all the data in the uploaded data file.
 */
public class ExcelInputAdapter implements InputAdapter {

    private static final Logger log = LoggerFactory.getLogger(ExcelInputAdapter.class);
    private String logInfo;

    /**
     * Converts the data in the files with .xlsx extension to the JSON format.
     * A workbook is created from the the excel file (.xlsx) and while iterating through the sheets in the workbook;
     * the data is read and  set in to a JSONObject. The JSONObject returned by the method contains an array of
     * row objects corresponding to each row in the workbook. A row object contains values of each cell in a given row,
     * with key values starting from letter 'A'.
     *
     * @param dataFilePath The path where the data file uploaded is saved.
     * @return returns the JSON object that contains all the data in the .xlsx file.
     * @throws FeedbackToolException If the .xlsx file is not found in the given path or due to an error in
     *                               parsing the data in the data file.
     */
    @Override
    public JSONObject convert(String dataFilePath) throws FeedbackToolException {

        // JSONObject to hold the array of row objects
        JSONObject dataJSONObject = new JSONObject();
        try {
            Workbook workbook = WorkbookFactory.create(new File(dataFilePath));
            logInfo = "Workbook has " + workbook.getNumberOfSheets() + " sheets";
            log.info(logInfo);

            Iterator<Sheet> sheetIterator = workbook.sheetIterator();

            // JSONArray to hold all the row objects
            JSONArray rowsJSONArray = new JSONArray();
            while (sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();
                logInfo = "Sheet: " + sheet.getSheetName() + " has " + sheet.getNumMergedRegions()
                        + " merged regions";
                log.info(logInfo);

                DataFormatter dataFormatter = new DataFormatter();

                logInfo = "Iterating over Rows and Columns using for-each loop";
                log.info(logInfo);
                for (Row row : sheet) {

                    // JSONObject to hold the data in the cells of a given row
                    JSONObject rowJSONObject = new JSONObject();

                    char keyLetter = 'A';
                    for (Cell cell : row) {
                        String cellValue = dataFormatter.formatCellValue(cell);
                        rowJSONObject.put(keyLetter, cellValue);
                        ++keyLetter;
                    }
                    rowsJSONArray.add(rowJSONObject);
                }
            }
            dataJSONObject.put(Constants.JSON_DATA_OBJECT, rowsJSONArray);
        } catch (InvalidFormatException e) {
            throw new FeedbackToolException("Error in parsing the data file uploaded", e);
        } catch (IOException e) {
            throw new FeedbackToolException("Data file was not found in the specified location", e);
        }
        return dataJSONObject;
    }

}
