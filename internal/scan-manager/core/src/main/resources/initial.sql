/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

CREATE TABLE IF NOT EXISTS SCANNER(
            SCANNER_ID VARCHAR(255),
            SCANNER_NAME VARCHAR(256),
            SCANNER_TYPE VARCHAR (50),
            SCANNER_IMAGE VARCHAR (100),
            PRIMARY KEY (SCANNER_ID)
)ENGINE INNODB;

CREATE TABLE IF NOT EXISTS USER(
            ID INT(10) NOT NULL AUTO_INCREMENT,
            USERNAME VARCHAR(256),
            EMAIL VARCHAR(256),
            PRIMARY KEY (ID),
            CONSTRAINT USER_KEY UNIQUE (USERNAME, EMAIL)
)ENGINE INNODB;

CREATE TABLE IF NOT EXISTS SCANNER_FIELD(
            ID BIGINT NOT NULL AUTO_INCREMENT,
            FIELD_ID VARCHAR(255),
            SCANNER_ID VARCHAR(256),
            FIELD_NAME VARCHAR (256),
            FIELD_DESCRIPTION VARCHAR (1024),
            FIELD_TYPE VARCHAR (50),
            FIELD_ORDER INT (50),
            IS_REQUIRED VARCHAR (50),
            PRIMARY KEY (ID),
            FOREIGN KEY (SCANNER_ID) REFERENCES SCANNER(SCANNER_ID),
            CONSTRAINT FIELD_KEY UNIQUE (FIELD_ID,SCANNER_ID)
)ENGINE INNODB;

CREATE TABLE IF NOT EXISTS SCANNER_APP(
            ID BIGINT NOT NULL AUTO_INCREMENT,
            APP_ID VARCHAR(255),
            APP_NAME VARCHAR(256),
            SCANNER_ID VARCHAR (256),
            PRODUCT VARCHAR (50),
            PRIMARY KEY (ID),
            FOREIGN KEY (SCANNER_ID) REFERENCES SCANNER(SCANNER_ID),
            CONSTRAINT SCANNER_APP_KEY UNIQUE (APP_ID,SCANNER_ID)
)ENGINE INNODB;

CREATE TABLE IF NOT EXISTS SCAN(
            JOB_ID VARCHAR (255),
            SCAN_NAME VARCHAR (255),
            SCAN_DESCRIPTION VARCHAR (1024),
            SCANNER_ID VARCHAR(255),
            STATUS VARCHAR(50),
            PRIORITY INT (10),
            PRODUCT VARCHAR(50),
            TYPE VARCHAR(50),
            USER_ID INT(10),
            SUBMITTED_TIMESTAMP DATETIME,
            STARTED_TIMESTAMP DATETIME,
            REPORT_PATH VARCHAR(256),
            SCANNER_SCAN_ID VARCHAR(256),
            SCANNER_APP_ID VARCHAR(256),
            PRIMARY KEY (JOB_ID),
            FOREIGN KEY (SCANNER_ID) REFERENCES SCANNER(SCANNER_ID),
            FOREIGN KEY (USER_ID) REFERENCES USER(ID)
)ENGINE INNODB;

CREATE TABLE IF NOT EXISTS SCAN_FILE(
            ID BIGINT NOT NULL AUTO_INCREMENT,
            JOB_ID VARCHAR(255),
            SCAN_FILE_NAME VARCHAR(256),
            SCAN_FILE_LOCATION VARCHAR (256),
            PRIMARY KEY (ID),
            FOREIGN KEY (JOB_ID) REFERENCES SCAN(JOB_ID) ,
            CONSTRAINT SCAN_FILE_KEY UNIQUE (JOB_ID,SCAN_FILE_NAME)
)ENGINE INNODB;

CREATE TABLE IF NOT EXISTS SCAN_PROPERTY(
            ID BIGINT NOT NULL AUTO_INCREMENT,
            JOB_ID VARCHAR(255),
            PROPERTY_NAME VARCHAR(256),
            PROPERTY_VALUE VARCHAR (256),
            PRIMARY KEY (ID),
            FOREIGN KEY (JOB_ID) REFERENCES SCAN(JOB_ID),
            CONSTRAINT SCAN_PROPERTY_KEY UNIQUE (JOB_ID,PROPERTY_NAME)
)ENGINE INNODB;

CREATE TABLE IF NOT EXISTS LOG(
            ID BIGINT NOT NULL AUTO_INCREMENT,
            JOB_ID VARCHAR(255),
            TYPE VARCHAR(256),
            TIME_CREATED DATETIME NOT NULL,
            MESSAGE VARCHAR (2058),
            PRIMARY KEY (ID),
            FOREIGN KEY (JOB_ID) REFERENCES SCAN(JOB_ID)
)ENGINE INNODB;
