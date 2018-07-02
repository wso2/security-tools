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

package org.wso2.security.tools.advisorytool.output.pdf;

import org.apache.log4j.Logger;
import org.wso2.security.tools.advisorytool.exeption.AdvisoryToolException;
import org.wso2.security.tools.advisorytool.model.SecurityAdvisory;
import org.wso2.security.tools.advisorytool.utils.Constants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * This class is used to generate the Security Advisory PDF from the Security Advisory XML file.
 */
public class SecurityAdvisoryPDFOutputGeneratorFromXML extends SecurityAdvisoryPDFOutputGenerator {

    private static final Logger logger = Logger.getLogger(SecurityAdvisoryPDFOutputGeneratorFromXML.class);

    @Override
    public boolean isAdvisoryGenerateFromFile() {
        return true;
    }

    @Override
    public void generate(SecurityAdvisory securityAdvisory) throws AdvisoryToolException {

        logger.info("Security Advisory PDF generation from XML started");
        File file = new File(Constants.SECURITY_ADVISORY_OUTPUT_DIRECTORY
                + File.separator + "xml" + File.separator + securityAdvisory.getName() + ".xml");
        if (!file.exists()) {
            throw new AdvisoryToolException("Failed to find the file " + file.getPath());
        }

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SecurityAdvisory.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            securityAdvisory = (SecurityAdvisory) jaxbUnmarshaller.unmarshal(file);
            super.generate(securityAdvisory);
        } catch (JAXBException e) {
            throw new AdvisoryToolException("Failed to generate the security advisory object " +
                    "from the security advisory XML", e);
        }
    }
}
