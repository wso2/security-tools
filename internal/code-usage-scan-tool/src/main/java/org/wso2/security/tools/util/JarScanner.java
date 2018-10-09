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

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.exception.ScanToolException;
import org.wso2.security.tools.model.MethodReference;
import org.wso2.security.tools.visitor.ClassVisitorImpl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * JarScanner - Scans the jar files provided by the user and populates the database with the method references inside
 * the class files.
 */
public class JarScanner {

    private static final Logger log = LoggerFactory.getLogger(JarScanner.class);
    private String dataFilePath;
    private String product;
    private int version;

    public static ArrayList<MethodReference> methodReferences;

    /**
     * Constructor to set the values of the variables dataFilePath, product and version.
     *
     * @param dataFilePath path where the jar file is saved.
     * @param product      product name.
     * @param version      product version.
     */
    public JarScanner(String dataFilePath, String product, int version) {
        this.dataFilePath = dataFilePath;
        this.product = product;
        this.version = version;
    }

    /**
     * Goes through the entries with the .class extension and extracts the method
     * instructions (method invocations) inside the class.
     *
     * @throws ScanToolException thrown if an exception is thrown inside the method.
     */
    public void scan() throws ScanToolException {

        try (JarFile jarFile = new JarFile(dataFilePath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                methodReferences = new ArrayList<>();
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    InputStream stream = new BufferedInputStream(jarFile.getInputStream(entry), 1024);
                    ClassReader reader = new ClassReader(stream);
                    ClassVisitorImpl cv = new ClassVisitorImpl();
                    reader.accept(cv, 0);
                    stream.close();
                }
            }
            new DatabaseUtils().insertIntoDatabase(methodReferences);
        } catch (IOException e) {
          throw new ScanToolException("IOException was thrown while scanning the jar file.", e);
        }
        log.debug("Successful");
    }

}
