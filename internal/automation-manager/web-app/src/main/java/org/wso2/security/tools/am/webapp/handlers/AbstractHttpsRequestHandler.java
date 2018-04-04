/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.am.webapp.handlers;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * The abstract class {@link AbstractHttpsRequestHandler} is to initiate a Https request by setting SSL context
 *
 */
public abstract class AbstractHttpsRequestHandler {

    private static final String trustStoreType = "JKS";
    private static final String trustManagerType = "SunX509";
    private static final String protocol = "TLSv1.2";
    private static final String trustStorePath = "org.wso2.security.tools.am.webapp/truststore.jks";
    private static final String trustStorePassword = "wso2carbon";
    private static KeyStore trustStore;
    protected static SSLSocketFactory sslSocketFactory;
    protected static boolean isInitialized = false;

    /**
     * Initiate a SSL socket factory for HTTPS request
     */
    protected static void init() {
        try {
            trustStore = KeyStore.getInstance(trustStoreType);
            InputStream inputStream = HttpsRequestHandler.class.getClassLoader().getResourceAsStream(trustStorePath);
            assert inputStream != null;
            trustStore.load(inputStream, trustStorePassword.toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerType);
            trustManagerFactory.init(trustStore);
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            sslSocketFactory = sslContext.getSocketFactory();
            isInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
