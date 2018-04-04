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

package org.wso2.security.tools.defectdojo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {
    public static void main(String[] args) {
        DataBase dataBaseConnection = new DataBase();
        dataBaseConnection.getDBConnection();
    }

    public Connection getDBConnection() {
        Connection conn = null;
        try {
            String username = "root";
            String password = "1234";

            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DefectDojoFinal", "root", "1234");

                return conn;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
        }
        return conn;
    }

    public boolean closeConnection(Connection dbConnection) {

        try {
            dbConnection.close();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
