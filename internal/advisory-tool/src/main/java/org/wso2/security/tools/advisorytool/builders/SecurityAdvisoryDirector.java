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
package org.wso2.security.tools.advisorytool.builders;

import org.apache.log4j.Logger;
import org.wso2.security.tools.advisorytool.exeption.AdvisoryToolException;
import org.wso2.security.tools.advisorytool.model.SecurityAdvisory;
import org.wso2.security.tools.advisorytool.output.SecurityAdvisoryOutputGenerator;

/**
 * SecurityAdvisoryDirector class to create a complete security advisory.
 */
public class SecurityAdvisoryDirector {
    private static Logger logger = Logger.getLogger(SecurityAdvisoryDirector.class);

    /**
     * Creating the complete security advisory.
     * @param builder
     * @param securityAdvisory
     * @param outputGenerator
     * @throws AdvisoryToolException
     */
    public void createSecurityAdvisory(SecurityAdvisoryBuilder builder,
                                       SecurityAdvisory securityAdvisory,
                                       SecurityAdvisoryOutputGenerator outputGenerator)
            throws AdvisoryToolException {
        if (securityAdvisory == null) {
            throw new AdvisoryToolException("Invalid security advisory found");
        }

        logger.info("Creating Security Advisory " + securityAdvisory.getName());
        try {
            if (!(outputGenerator.isAdvisoryGenerateFromFile())) {

                //build the advisory object by the data from the PMT only if the advisory data
                // is not being read from the file.
                builder.setAdvisoryData(securityAdvisory);
                builder.buildAffectedProductsList();
                builder.buildAdvisory();
                outputGenerator.generate(builder.getSecurityAdvisory());

            } else {
                outputGenerator.generate(securityAdvisory);
            }
        } catch (AdvisoryToolException e) {
            throw new AdvisoryToolException("Error occurred while creating the security advisory "
                    + securityAdvisory.getName(), e);
        }
    }
}
