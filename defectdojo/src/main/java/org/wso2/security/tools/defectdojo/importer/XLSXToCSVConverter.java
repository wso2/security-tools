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

package org.wso2.security.tools.defectdojo.importer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Iterator;
import java.util.Scanner;

public class XLSXToCSVConverter {

    private static void convertSelectedSheetInXLXSFileToCSV(File xlsxFile, int sheetIdx, File outputFile) throws Exception {

        FileInputStream fileInStream = new FileInputStream(xlsxFile);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outputFile);
            XSSFWorkbook workBook = new XSSFWorkbook(fileInStream);
            XSSFSheet selSheet = workBook.getSheetAt(sheetIdx);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            // Iterate through all the rows in the selected sheet
            Iterator<Row> rowIterator = selSheet.iterator();
            while (rowIterator.hasNext()) {
                StringBuffer sb = new StringBuffer();
                Row row = rowIterator.next();

                // Iterate through all the columns in the row and build "," separated string
                Iterator<Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (sb.length() != 0) {
                        sb.append(",");
                    }

                    switch (cell.getCellTypeEnum()) {
                        case STRING:
                            sb.append(cell.getStringCellValue().replaceAll(",", "/").replaceAll("\n", ""));
                            break;
                        case NUMERIC:
                            sb.append(cell.getNumericCellValue());
                            break;
                        case BOOLEAN:
                            sb.append(cell.getBooleanCellValue());
                            break;
                        default:
                    }
                }
                bw.write(sb.toString());
                bw.newLine();
            }
            bw.close();
            fos.close();
            workBook.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the output File Path for Exel:");
        String inputFilePath = scanner.next();

        System.out.println("Enter the output File Path for CSV file:");
        String outputFilePath = scanner.next();

        File inputFile;
        File outputFile;
        if (inputFilePath.isEmpty())
            inputFile = new File("src/main/resources/Files/ETA.xlsx");
        else
            inputFile = new File(inputFilePath);

        if (outputFilePath.isEmpty())
            outputFile = new File("src/main/resources/Files/output1.csv");
        else
            outputFile = new File(outputFilePath);

        int sheetIdx = 0; // 0 for first sheet

        convertSelectedSheetInXLXSFileToCSV(inputFile, sheetIdx, outputFile);
    }
}