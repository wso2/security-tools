/*
 *  Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.security.tools.scanmanager.scanners.common.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.DatatypeConverter;

/**
 * Utility class for Encode handling.
 */
public class EncodeUtil {

    /**
     * Do base64 decode for a given encoded string.
     *
     * @param b64EncodedString encoded string that need to decode
     * @return decoded string
     * @throws UnsupportedEncodingException when decoding fails due to a unsupported encoding
     */
    public static String decodeB64(String b64EncodedString) throws UnsupportedEncodingException {
        return new String(DatatypeConverter.parseBase64Binary(b64EncodedString), StandardCharsets.UTF_8.name());
    }
}
