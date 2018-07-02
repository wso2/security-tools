package org.wso2.security.tools.advisorytool.output.html;

import org.wso2.security.tools.advisorytool.exeption.AdvisoryToolException;
import org.wso2.security.tools.advisorytool.model.SecurityAdvisory;
import org.wso2.security.tools.advisorytool.utils.Constants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * This class generates the security advisory HTML from XML.
 */
public class SecurityAdvisoryHTMLOutputGeneratorFromXML extends SecurityAdvisoryHTMLOutputGenerator{

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(SecurityAdvisoryHTMLOutputGeneratorFromXML.class);

    @Override
    public boolean isAdvisoryGenerateFromFile() {
        return true;
    }

    @Override
    public void generate(SecurityAdvisory securityAdvisory) throws AdvisoryToolException {

        logger.info("Security Advisory HTML generation from XML started");
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
            throw new AdvisoryToolException("Failed to generate the security advisory HTML " +
                    "from the security advisory XML", e);
        }
    }
}
