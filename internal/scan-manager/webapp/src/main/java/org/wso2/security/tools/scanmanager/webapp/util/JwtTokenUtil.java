/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.security.tools.scanmanager.webapp.util;

import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.HashMap;
import javax.xml.bind.DatatypeConverter;

/**
 * Utility class for JWT Token.
 */
public class JwtTokenUtil {

    private static JSONObject jwtPayload;

    /**
     * Validate the JWT token.
     *
     * @param jwtComponents array for JWT Token component (Header,Payload,Signature)
     * @param config        JWT configuration to validate
     * @throws ScanManagerWebappException when an error occurs while validating JWT token
     */
    public static void validateJWT(String[] jwtComponents, HashMap config) throws ScanManagerWebappException {
        jwtPayload = new JSONObject(getDecodedJWT(jwtComponents[1]));
        String keyStoreName = config.get(Constants.KEY_STORE_NAME).toString();
        String keyStorePwd = config.get(Constants.KEY_STORE_PWD).toString();
        String alias = config.get(Constants.ALIAS).toString();
        String algorithm = config.get(Constants.ALGORITHM).toString();

        validateMandatoryFields(jwtPayload);
        validateSignature(jwtComponents, keyStoreName, keyStorePwd, alias, algorithm);
        validateIssuer(jwtPayload, config.get(Constants.JWT_ISSUER).toString());
        validateExpirationTime(jwtPayload);
    }

    /**
     * Get username from email claim of the JWT token.
     *
     * @param jwtPayload JWT payload
     * @return the user email taken from the JWT token
     */
    public static String getUsernameFromJwt(String jwtPayload) {
        String jwtBody = getDecodedJWT(jwtPayload);
        JSONObject jsonClaimObject = new JSONObject(jwtBody);
        return jsonClaimObject.getString(Constants.EMAIL_CLAIM);
    }

    /**
     * Decode the JWT.
     *
     * @param jwtPayload JWT token's payload
     * @return decoded JWT payload
     */
    public static String getDecodedJWT(String jwtPayload) {
        if (jwtPayload != null) {
            byte[] decodedHeader = DatatypeConverter.parseBase64Binary(jwtPayload);
            return new String(decodedHeader, StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * Validate the mandatory fields of the JWT token.
     *
     * @param jwtPayload JWT payload
     * @throws ScanManagerWebappException when an error occurs while validating the mandatory fields of the JWT token
     */
    private static void validateMandatoryFields(JSONObject jwtPayload) throws ScanManagerWebappException {
        if (jwtPayload.getString(Constants.JWT_ISSUER).equals("") || jwtPayload.getString(Constants.JWT_SUBJECT)
                .equals("") || jwtPayload.getInt(Constants.JWT_EXPIRY) == 0) {
            throw new ScanManagerWebappException("Mandatory fields(Issuer, Subject or Expiration time) " +
                    "are empty in the given JSON Web Token.");
        }
    }

    /**
     * Validate the signature of the JWT token.
     *
     * @param jwtComponents JWT token components
     * @param keyStoreName  KeyStore name
     * @param keyStorePwd   KeyStore password
     * @param alias         Alias of the certificate related to JWT signing
     * @param algorithm     JWT token signing algorithm
     * @throws ScanManagerWebappException when an error occurs while validating the signature
     */
    private static void validateSignature(String[] jwtComponents, String keyStoreName, String keyStorePwd
            , String alias, String algorithm) throws ScanManagerWebappException {

        if (jwtComponents != null) {
            byte[] jwtSignature = DatatypeConverter.parseBase64Binary(jwtComponents[2]);
            ClassPathResource path = new ClassPathResource(keyStoreName);

            try (FileInputStream fileInputStream = new FileInputStream(path.getURL().getPath())) {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(fileInputStream, keyStorePwd.toCharArray());
                Certificate certificate = keyStore.getCertificate(alias);
                Signature signature = Signature.getInstance(algorithm);
                signature.initVerify(certificate);
                String jwtAssertion = jwtComponents[0] + "." + jwtComponents[1];
                signature.update(jwtAssertion.getBytes(StandardCharsets.UTF_8));

                if (!signature.verify(jwtSignature)) {
                    throw new ScanManagerWebappException("Invalid signature");
                }
            } catch (SignatureException | CertificateException | NoSuchAlgorithmException
                    | InvalidKeyException | IOException | KeyStoreException e) {
                throw new ScanManagerWebappException("Error occured while validating the JWT signature", e);
            }
        }
    }

    /**
     * Validate the JWT token issuer.
     *
     * @param jwtPayload JWT payload
     * @param issuer     value that should be the issuer of the JWT token
     * @throws ScanManagerWebappException when an error occurs while validating the JWT token issuer
     */
    private static void validateIssuer(JSONObject jwtPayload, String issuer) throws ScanManagerWebappException {
        if (!jwtPayload.getString(Constants.JWT_ISSUER).equals(issuer)) {
            throw new ScanManagerWebappException("JWT contained invalid issuer name : " + jwtPayload
                    .getString(Constants.JWT_ISSUER));
        }
    }

    /**
     * Validate the Expiry time of the JWT token.
     *
     * @param jwtPayload JWT payload
     * @throws ScanManagerWebappException when an error occurs while validating the Expiry time of the JWT token
     */
    private static void validateExpirationTime(JSONObject jwtPayload) throws ScanManagerWebappException {
        Object expTime = jwtPayload.get(Constants.JWT_EXPIRY);
        if (!(Long.parseLong(expTime.toString()) > System.currentTimeMillis())) {
            throw new ScanManagerWebappException("JWT token is expired");
        }
    }
}
