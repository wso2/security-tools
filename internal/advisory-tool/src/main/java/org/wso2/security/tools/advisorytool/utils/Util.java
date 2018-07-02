/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.security.tools.advisorytool.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.wso2.security.tools.advisorytool.data.ProductDataHolder;
import org.wso2.security.tools.advisorytool.exeption.AdvisoryToolException;
import org.wso2.security.tools.advisorytool.model.Header;
import org.wso2.security.tools.advisorytool.model.Product;
import org.wso2.security.tools.advisorytool.model.Version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the utility class.
 */
public class Util {

    private Util() {
    }

    private static final Logger logger = Logger.getLogger(Util.class);

    /**
     * This method is used to initiate an HTTP connection and retrieve the response.
     *
     * @param url
     * @param
     * @return
     */
    public static StringBuilder httpConnection(String url, List<Header> headerList) throws AdvisoryToolException {
        HttpResponse response = null;
        StringBuilder result = new StringBuilder();
        String line = "";

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            for (Header header : headerList) {
                request.addHeader(header.getHeaderName(), header.getHeaderValue());
            }
            response = client.execute(request);
        } catch (IOException e) {
            throw new AdvisoryToolException("Connection to the URL " + url + " failed", e);
        }

        try (BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), "UTF-8"))) {
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            throw new AdvisoryToolException("Error occured while reading the response from " + url, e);
        }
        return result;
    }

    /**
     * Generate the product and version string for a given kernel version.
     *
     * @param kernelVersion
     * @return list of product and version strings (e.g., WSO2 API Manager 2.1.0)
     */
    public static List<String> getApplicableProductsForAKernel(String kernelVersion) {
        List<String> applicableProductAndVersionStringList = new ArrayList<>();

        for (Product product : ProductDataHolder.getInstance().getProductList()) {
            for (Version version : product.getVersionList()) {
                if (version.getKernelVersionNumber().equals(kernelVersion)
                        && !applicableProductAndVersionStringList.contains(product
                        .getName() + " " + version.getVersionNumber())) {
                    applicableProductAndVersionStringList.add(product.getName()
                            + " " + version.getVersionNumber());

                    if (logger.isDebugEnabled()) {
                        logger.debug("Product Version String : "
                                + product.getName() + " "
                                + version.getVersionNumber());
                    }
                }
            }
        }
        return applicableProductAndVersionStringList;
    }
}
