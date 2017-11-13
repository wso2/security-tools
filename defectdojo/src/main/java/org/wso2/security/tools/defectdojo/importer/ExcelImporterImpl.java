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

import org.wso2.security.tools.defectdojo.database.DataBase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ExcelImporterImpl implements ExcelImporter {
    DataBase dataBase = new DataBase();
    Scanner scanner = new Scanner(System.in);

    public String[] getFindingDetailsFromCSV(String csvFilePath) {

        String line;
        String cvsSplitBy = ",";

        if (csvFilePath.isEmpty())
            csvFilePath = "src/main/resources/Files/ex.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {

            while ((line = br.readLine()) != null) {

                String[] finding = line.split(cvsSplitBy);

                String findingId = finding[0];
                int noteId = createNote(finding[4]);

                updateNoteId(Integer.valueOf(findingId.split("\\.")[0]), noteId);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getFindingId(String title, String url, String lineNumber, String sourceFile) {

        Connection dbConnection = dataBase.getDBConnection();
        int index = 0;
        System.out.println(title + " " + lineNumber + " " + sourceFile);
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.
                    prepareStatement("SELECT id from dojo_finding WHERE  title=? and line_number=? and sourceFile=?");

            preparedStatement.setString(1, title);
            preparedStatement.setString(2, lineNumber);
            preparedStatement.setString(3, sourceFile);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {

                index = rs.getInt(1);
            }

            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dataBase.closeConnection(dbConnection);
        }
        return index;
    }

    public int createNote(String note) {

        int index = 0;
        Connection dbConnection = dataBase.getDBConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.
                    prepareStatement("INSERT INTO  dojo_notes(entry,date,author_id) VALUES (?,?,?) ");

            preparedStatement.setString(1, note);
            preparedStatement.setString(2, "2017-11-1");
            preparedStatement.setString(3, "1");

            preparedStatement.execute();
            preparedStatement.close();
            PreparedStatement preparedStatement1 = dbConnection.prepareStatement("SELECT id from dojo_notes WHERE entry=?");

            preparedStatement1.setString(1, note);

            ResultSet rs = preparedStatement1.executeQuery();

            while (rs.next()) {

                index = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dataBase.closeConnection(dbConnection);
        }

        return index;
    }

    public void updateNoteId(int findingId, int noteId) {

        Connection dbConnection = dataBase.getDBConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.
                    prepareStatement("INSERT dojo_finding_notes (finding_id,notes_id) VALUES (?,?) ");
            System.out.println(findingId);
            preparedStatement.setInt(1, findingId);
            preparedStatement.setInt(2, noteId);

            preparedStatement.execute();
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dataBase.closeConnection(dbConnection);
        }
    }
}