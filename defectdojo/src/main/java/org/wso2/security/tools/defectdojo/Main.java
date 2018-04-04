/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.wso2.security.tools.defectdojo;

import org.wso2.security.tools.defectdojo.exporter.ExcelExporterImpl;
import org.wso2.security.tools.defectdojo.importer.ExcelImporterImpl;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ExcelExporterImpl excelExporter = new ExcelExporterImpl();
        ExcelImporterImpl excelImporter = new ExcelImporterImpl();
        Scanner scanner = new Scanner(System.in);

        if (args[0].equalsIgnoreCase("exporter")) {

            int testId=Integer.valueOf(args[1]);

            String outputFile = args[2];
            excelExporter.createExcel(testId, outputFile);

        } else if (args[0].equalsIgnoreCase("importer")) {

            String csvFilePath = args[1];
            excelImporter.getFindingDetailsFromCSV(csvFilePath);
        }

    }
}
