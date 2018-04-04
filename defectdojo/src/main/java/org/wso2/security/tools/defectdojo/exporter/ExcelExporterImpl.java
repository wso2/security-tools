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

package org.wso2.security.tools.defectdojo.exporter;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.wso2.security.tools.defectdojo.database.DataBase;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ExcelExporterImpl implements ExcelExporter {
    DataBase dataBase = new DataBase();

    public void createExcel(int testId, String outputFile) {
        Connection dbConnection = dataBase.getDBConnection();
        try {

            if (outputFile.isEmpty())
                outputFile = "src/main/resources/Files/Test.xlsx";

            FileOutputStream fos = new FileOutputStream(outputFile);
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("FirstSheet");

            PreparedStatement preparedStatement = dbConnection.prepareStatement("SELECT * FROM `dojo_finding` WHERE test_id=?");

            preparedStatement.setInt(1, testId);

            int rowNumber = 0;
            ResultSet rs = preparedStatement.executeQuery();

            int i = 0;
            while (rs.next()) {
                FileOutputStream fileOut = new FileOutputStream(outputFile);
                if (i == 0) {
                    HSSFRow rowHeader = sheet.createRow((short) rowNumber);
                    Cell header0 = rowHeader.createCell(0);
                    header0.setCellValue("Issue_Id");
                    Cell header1 = rowHeader.createCell(1);
                    header1.setCellValue("title");
                    Cell header2 = rowHeader.createCell(2);
                    header2.setCellValue("cwe");
                    Cell header3 = rowHeader.createCell(3);
                    header3.setCellValue("url");
                    Cell header4 = rowHeader.createCell(4);
                    header4.setCellValue("severity");
                    Cell header5 = rowHeader.createCell(5);
                    header5.setCellValue("description");
                    Cell header6 = rowHeader.createCell(6);
                    header6.setCellValue("mitigation");
                    Cell header7 = rowHeader.createCell(7);
                    header7.setCellValue("impact");
                    Cell header8 = rowHeader.createCell(8);
                    header8.setCellValue("line_number");
                    Cell header9 = rowHeader.createCell(9);
                    header9.setCellValue("sourcefile");
                    Cell header10 = rowHeader.createCell(10);
                    header10.setCellValue("sourcefilepath");
                    i++;
                }

                HSSFRow row = sheet.createRow((short) rowNumber);

                Cell cell0 = row.createCell(0);
                int id = rs.getInt(1);
                cell0.setCellValue(id);

                Cell cell1 = row.createCell(1);
                String title = rs.getString(2);
                cell1.setCellValue(title);

                Cell cell2 = row.createCell(2);
                String cwe = rs.getString(4);
                cell2.setCellValue(cwe);

                Cell cell3 = row.createCell(3);
                String url = rs.getString(5);
                cell3.setCellValue(url);

                Cell cell4 = row.createCell(4);
                String severity = rs.getString(6);
                cell4.setCellValue(severity);

                Cell cell5 = row.createCell(5);
                String description = rs.getString(7);
                cell5.setCellValue(description);

                Cell cell6 = row.createCell(6);
                String mitigation = rs.getString(8);
                cell6.setCellValue(mitigation);

                Cell cell7 = row.createCell(6);
                String impact = rs.getString(9);
                cell7.setCellValue(impact);

                Cell cell8 = row.createCell(7);
                String line_number = rs.getString(29);
                cell8.setCellValue(line_number);
                System.out.println(rs.getString(29));

                Cell cell9 = row.createCell(8);
                String sourcefile = rs.getString(30);
                cell9.setCellValue(sourcefile);
                System.out.println(rs.getString(30));

                Cell cell10 = row.createCell(9);
                String sourcefilePath = rs.getString(31);
                cell10.setCellValue(sourcefilePath);
                System.out.println(rs.getString(31));

                Cell cell11 = row.createCell(10);
                String functionPrototype = rs.getString(34);
                cell11.setCellValue(functionPrototype);

                Cell cell12 = row.createCell(11);
                String issueId = rs.getString(35);
                cell12.setCellValue(issueId);

                PreparedStatement preparedStatement1 = dbConnection.prepareStatement("SELECT notes_id FROM dojo_finding_notes " +
                        "WHERE EXISTS (SELECT id FROM dojo_finding WHERE title=? and sourcefile=? and line_number=? and test_id <> ? " +
                        "and dojo_finding_notes.finding_id=dojo_finding.id)");

                preparedStatement1.setString(1, title);
                preparedStatement1.setString(2, sourcefile);
                preparedStatement1.setString(3, line_number);
                preparedStatement1.setInt(4, testId);

                ResultSet rs1 = preparedStatement1.executeQuery();
                int cellIndex = 12;

                while (rs1.next()) {

                    int noteId = rs1.getInt(1);
                    PreparedStatement preparedStatement2 = dbConnection.prepareStatement("SELECT entry FROM dojo_notes WHERE id=?");
                    preparedStatement2.setInt(1, noteId);
                    ResultSet rs2 = preparedStatement2.executeQuery();

                    while (rs2.next()) {
                        Cell cell13 = row.createCell(cellIndex++);

                        cell13.setCellValue(rs2.getString(1));
                    }
                }

                rowNumber++;
                workbook.write(fileOut);
                fileOut.close();
            }

            System.out.println("Your excel file has been generated!");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dataBase.closeConnection(dbConnection);
        }
    }
}