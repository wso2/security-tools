package org.wso2.security.tools.advisorytool.output.pdf;

import org.apache.log4j.Logger;
import org.wso2.security.tools.advisorytool.exeption.AdvisoryToolException;
import org.wso2.security.tools.advisorytool.model.SecurityAdvisory;
import org.wso2.security.tools.advisorytool.utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class generates the security advisory PDF from the security advisory HTML file.
 */
public class SecurityAdvisoryPDFOutputGeneratorFromHTML extends SecurityAdvisoryPDFOutputGenerator {

    private static final Logger logger = Logger.getLogger(SecurityAdvisoryPDFOutputGeneratorFromHTML.class);

    @Override
    public boolean isAdvisoryGenerateFromFile() {
        return true;
    }

    @Override
    public void generate(SecurityAdvisory securityAdvisory) throws AdvisoryToolException {

        logger.info("Security Advisory PDF generation from HTML started");
        File htmlFile = new File(Constants.SECURITY_ADVISORY_OUTPUT_DIRECTORY
                + File.separator + "html" + File.separator + securityAdvisory.getName() + ".html");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(htmlFile), "UTF-8"))) {

            File outputFile = new File(Constants.SECURITY_ADVISORY_OUTPUT_DIRECTORY
                    + File.separator + "pdf" + File.separator + securityAdvisory.getName() + ".pdf");

            File outputDirectory = new File(outputFile.getParent());
            outputDirectory.mkdirs();
            if (!outputDirectory.exists()) {
                throw new AdvisoryToolException("Unable to create the directory " + outputDirectory);
            }

            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String htmlString = sb.toString();
            createPDFFromHTML(htmlString, outputFile.toString());

            logger.info("Security Advisory PDF generation from HTML completed");
        } catch (IOException e) {
            throw new AdvisoryToolException("Failed to generate the security advisory object " +
                    "from the security advisory HTML", e);
        }
    }
}
