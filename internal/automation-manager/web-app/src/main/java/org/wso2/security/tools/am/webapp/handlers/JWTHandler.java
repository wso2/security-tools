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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

/**
 * This class provides utility methods to handle JWT
 */
@SuppressWarnings("unused")
public class JWTHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(JWTHandler.class);

    /**
     * The main contract of this method is to validate a given JWT
     *
     * @param signedJWTAsString A signed JWT
     * @return Whether the token is valid
     * @throws IOException              If an exception is produced by failed or interrupted I/O operations
     * @throws CertificateException     If a certificate error occurs
     * @throws NoSuchAlgorithmException This exception is thrown when a particular cryptographic algorithm is
     *                                  requested but is not available in the environment
     * @throws JOSEException            Javascript Object Signing and Encryption (JOSE) exception
     * @throws KeyStoreException        This is the generic KeyStore exception
     * @throws ParseException           If an error has been reached unexpectedly while parsing
     */
    public static boolean validateToken(String signedJWTAsString) throws IOException, CertificateException,
            NoSuchAlgorithmException, JOSEException, KeyStoreException, ParseException {

        RSAPublicKey publicKey;
        InputStream file = ClassLoader.getSystemResourceAsStream("wso2carbon.jks");
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(file, "wso2carbon".toCharArray());
        String alias = "wso2carbon";
        // Get certificate of public key
        Certificate cert = keystore.getCertificate(alias);
        // Get public key
        publicKey = (RSAPublicKey) cert.getPublicKey();
        SignedJWT signedJWT = SignedJWT.parse(signedJWTAsString);
        JWSVerifier verifier = new RSASSAVerifier(publicKey);

        if (signedJWT.verify(verifier)) {
            LOGGER.info("Signature is Valid");
            return true;
        } else {
            LOGGER.error("Signature is NOT Valid");
        }
        return false;
    }

    /**
     * Extract email address from a JWT
     *
     * @param signedJWTAsString A signed JWT to be extracted
     * @return Extracted email value from JWT
     * @throws ParseException If an error has been reached unexpectedly
     *                        while parsing
     */
    public static String extractEmailFromJWT(String signedJWTAsString) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(signedJWTAsString);
        String jsonString = signedJWT.getPayload().toString();
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getString("sub");
    }
}
