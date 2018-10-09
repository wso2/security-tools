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

package org.wso2.security.tools.util;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.model.MethodReference;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * DatabaseUtils - Handles all the database  related functionality.
 */
public class DatabaseUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
    private static final String USERNAME = "mgdb";
    private static final String PASSWORD = "1234";

    /**
     * Connect to MongoDB without security.
     *
     * @return mongodb database client.
     */
    private static MongoClient getMongoClient_1() {
        MongoClient mongoClient = new MongoClient(Constants.HOST, Constants.PORT);
        return mongoClient;
    }

    /**
     * Connect to the DB MongoDB with security.
     *
     * @return mongodb database client.
     */
    private static MongoClient getMongoClient_2() {
        MongoCredential credential = MongoCredential.createMongoCRCredential(
                USERNAME, Constants.DB_NAME, PASSWORD.toCharArray());
        MongoClient mongoClient = new MongoClient(
                new ServerAddress(Constants.HOST, Constants.PORT), Arrays.asList(credential));
        return mongoClient;
    }

    /**
     * Connect to the DB MongoDB without security.
     *
     * @return mongodb database client.
     */
    public static MongoClient getMongoClient() {
        return getMongoClient_1();
        // You can replace by getMongoClient_2 ()
        // In case of connection to MongoDB need security.
    }

    /**
     * Finds the method usages of a given method.
     *
     * @param productName the product in which the method usage is searched for.
     * @param version     version of the product.
     * @param method      name of the method.
     * @param owner       owner of the method (class in which the method is declared in).
     */
    public void find(String productName, int version, String method, String owner) {

        // To connect to mongodb server
        MongoClient mongoClient = DatabaseUtils.getMongoClient();

        // Connecting to the database
        DB db = mongoClient.getDB(Constants.DB_NAME);
        DBCollection dept = db.getCollection("Usages");
        DBObject where = getWhereClause_1(method, owner);

        // Query to get the method usages
        DBCursor cursor = dept.find(where);
        int i = 1;
        while (cursor.hasNext()) {
            System.out.println("Match: " + i);
            System.out.println(cursor.next());
            i++;
        }
        log.debug("Querying successful");
    }

    /**
     * Inserts the extracted method usages into the database.
     *
     * @param methodReferences method usages to be inserted to the database.
     */
    public void insertIntoDatabase(ArrayList<MethodReference> methodReferences) {

        // To connect to mongodb server
        MongoClient mongoClient = DatabaseUtils.getMongoClient();

        // Connecting to the databases
        DB db = mongoClient.getDB(Constants.DB_NAME);

        // Get the Collection with name Usages
        // Not necessarily this 'Collection' must exist in the DB.
        DBCollection usages = db.getCollection("Usages");

        for (MethodReference ref : methodReferences) {
            BasicDBObject obj = new BasicDBObject();
            obj.append("method_name", ref.getMethodName());
            obj.append("owner_class", ref.getParentClass());
            obj.append("usage_class", ref.getUsageClass());
            obj.append("usage_method", ref.getUsageMethod());
            obj.append("line_number", ref.getUsageLineNumber());
            usages.insert(obj);
        }

    }

    /**
     * Building the JSON result.
     *
     * @param methodName
     * @param className
     * @return
     */
    private static DBObject getWhereClause_1(String methodName, String className) {
        BasicDBObjectBuilder whereBuilder = BasicDBObjectBuilder.start();

        whereBuilder.append("method_name", methodName);
        whereBuilder.append("owner_class", className);
        DBObject where = whereBuilder.get();
        System.out.println(where.toString());
        return where;
    }
}
