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
package org.wso2.security.tools.scanmanager.scanners.veracode.util;

import com.veracode.apiwrapper.AbstractAPIWrapper;
import com.veracode.apiwrapper.cli.VeracodeCommand;
import com.veracode.apiwrapper.wrappers.ResultsAPIWrapper;
import com.veracode.apiwrapper.wrappers.UploadAPIWrapper;
import com.veracode.util.lang.StringUtility;
import org.wso2.security.tools.scanmanager.scanners.common.util.EncodeUtil;

import java.io.UnsupportedEncodingException;

/**
 * Util class to provide the Veracode API wrappers.
 */
public class VeracodeAPIUtil {
    private static VeracodeCommand.Options options;

    public static void setCredentials(VeracodeCommand.Options options) throws UnsupportedEncodingException {
        VeracodeAPIUtil.options = options;
    }

    /**
     * Build the upload wrap for Upload API.
     *
     * @return Veracode Upload API Wrapper
     * @throws UnsupportedEncodingException
     */
    public static UploadAPIWrapper getUploadAPIWrapper() throws UnsupportedEncodingException {
        UploadAPIWrapper uploadAPIWrapper = new UploadAPIWrapper();
        setUpWrapperCredentials(uploadAPIWrapper);

        return uploadAPIWrapper;
    }

    /**
     * Build the upload wrap for Result API.
     *
     * @return Veracode Result API Wrapper
     * @throws UnsupportedEncodingException
     */
    public static ResultsAPIWrapper getResultAPIWrapper() throws UnsupportedEncodingException {
        ResultsAPIWrapper resultsAPIWrapper = new ResultsAPIWrapper();
        setUpWrapperCredentials(resultsAPIWrapper);

        return resultsAPIWrapper;
    }

    /**
     * Set the Veracode credentials to the Veracode API wrappers.
     *
     * @param wrapper Wrapper that needs to be set credentials
     * @throws UnsupportedEncodingException
     */
    private static void setUpWrapperCredentials(AbstractAPIWrapper wrapper) throws UnsupportedEncodingException {
        String apiID = options._vid;
        String user;
        String pass;

        if (StringUtility.isNullOrEmpty(apiID)) {
            user = options._vuser;
            pass = options._vpassword;

            if (StringUtility.isNullOrEmpty(user) && !StringUtility.isNullOrEmpty(options._api1)) {
                user = EncodeUtil.decodeB64(options._api1);
            }

            if (StringUtility.isNullOrEmpty(pass) && !StringUtility.isNullOrEmpty(options._api2)) {
                pass = EncodeUtil.decodeB64(options._api2);
            }
            wrapper.setUpCredentials(user, pass);
        } else {
            wrapper.setUpApiCredentials(apiID, options._vkey);
        }
    }
}
