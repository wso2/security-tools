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

package org.wso2.security.tools.service;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.security.tools.exception.ScanToolException;
import org.wso2.security.tools.util.DatabaseUtils;
import org.wso2.security.tools.util.FileHandler;
import org.wso2.security.tools.util.JarScanner;

import java.io.InputStream;
import java.net.UnknownHostException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * Method Usage Finder Service. ----------------------------------------------------------------------------------------
 * MethodReferenceFinderService - This class consists of functionality to accept uploaded jar files by the clients and
 * populate the database with all the method references inside the class files of the given jar. Also it offers an
 * endpoint to obtain the usages of a given method.
 */
@Path("/usage-service")
public class MethodReferenceFinderService {

    private static final Logger log = LoggerFactory.getLogger(MethodReferenceFinderService.class);

    @POST
    @Path("/populate-data")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String populateData(@FormDataParam("jar") FileInfo jarFileInfo,
                                 @FormDataParam("jar") InputStream jarFileInputStream,
                                 @FormDataParam("productName") String productName,
                                 @FormDataParam("productVersion") int version) {

        String dataFilePath = "Data File Path Not Found";

        try {
            FileHandler.saveUploadedFile(jarFileInputStream, jarFileInfo);
            dataFilePath = FileHandler.saveUploadedFile(jarFileInputStream, jarFileInfo);
            // the data file path variable with the datafile path should be used instead of the hard coded string.
            JarScanner jarScanner = new JarScanner("target/scan-tool-0.1-SNAPSHOT.jar"
                    , productName, version);
            jarScanner.scan();
        } catch (ScanToolException e) {
            log.error("Error occurred while scanning the jar file", e);
        } finally {
            IOUtils.closeQuietly(jarFileInputStream);
        }
       return dataFilePath;
    }

    @POST
    @Path("/query-data")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String queryData(@FormDataParam("method") String method,
                               @FormDataParam("owner") String owner,
                               @FormDataParam("productName") String productName,
                               @FormDataParam("productVersion") int version) throws UnknownHostException {

        new DatabaseUtils().find(productName, version, method, owner);
        return "Done";
    }


}
