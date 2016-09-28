/*
 *
 *  * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.security.tools.zap.ext.zapwso2jiraplugin;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.extension.report.ReportGenerator;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.extension.XmlReporterExtension;
import org.zaproxy.zap.utils.XMLStringUtil;
import org.zaproxy.zap.view.ScanPanel;

/*
* This class is used to create the report after a scan, Here methods are copied and modified from package
* org.parosproxy.paros.extension.report; class ReportLastScan
**/

public class LastScanReport {

    public String generateReport(StringBuilder report) throws Exception {
        report.append("<?xml version=\"1.0\"?>");
        report.append("<OWASPZAPReport version=\"").append(Constant.PROGRAM_VERSION).append("\" generated=\"")
                .append(ReportGenerator.getCurrentDateTimeString()).append("\">\r\n");
        siteXML(report);
        report.append("</OWASPZAPReport>");
        return report.toString();
    }

    private void siteXML(StringBuilder report) {
        SiteMap siteMap = Model.getSingleton().getSession().getSiteTree();
        SiteNode root = (SiteNode) siteMap.getRoot();
        int siteNumber = root.getChildCount();
        for (int i = 0; i < siteNumber; i++) {
            SiteNode site = (SiteNode) root.getChildAt(i);
            String siteName = ScanPanel.cleanSiteName(site, true);
            String[] hostAndPort = siteName.split(":");
            boolean isSSL = (site.getNodeName().startsWith("https"));
            String siteStart = "<site name=\"" + XMLStringUtil.escapeControlChrs(site.getNodeName()) + "\"" +
                    " host=\"" + XMLStringUtil.escapeControlChrs(hostAndPort[0]) + "\"" +
                    " port=\"" + XMLStringUtil.escapeControlChrs(hostAndPort[1]) + "\"" +
                    " ssl=\"" + String.valueOf(isSSL) + "\"" +
                    ">";
            StringBuilder extensionsXML = getExtensionsXML(site);
            String siteEnd = "</site>";
            report.append(siteStart);
            report.append(extensionsXML);
            report.append(siteEnd);
        }
    }

    public StringBuilder getExtensionsXML(SiteNode site) {
        StringBuilder extensionXml = new StringBuilder();
        ExtensionLoader loader = Control.getSingleton().getExtensionLoader();
        int extensionCount = loader.getExtensionCount();
        for (int i = 0; i < extensionCount; i++) {
            Extension extension = loader.getExtension(i);
            if (extension instanceof XmlReporterExtension) {
                extensionXml.append(((XmlReporterExtension) extension).getXml(site));
            }
        }
        return extensionXml;
    }
}
