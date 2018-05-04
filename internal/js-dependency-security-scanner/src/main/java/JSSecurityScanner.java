/*
 *   Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

import org.apache.log4j.Logger;
import org.wso2.security.tools.scanner.dependency.js.postprocessor.VulnerabilityReportManager;
import org.wso2.security.tools.scanner.dependency.js.preprocessor.PreProcessor;
import org.wso2.security.tools.scanner.dependency.js.scanmanager.Executor;
import org.wso2.security.tools.scanner.dependency.js.scanmanager.RetireJsExecutor;

import java.util.HashMap;


/**
 * Main class for JSSecurity Scanner. This class is main entry point for JSSecurity scanner.
 * This class call fallowing functions :
 * 1. Start pre processing : It is responsible for parse configuration files, download product packs and
 * package.json files if required and Unzip product packs.
 * 2. Perform scanning process : Run retire.js scanner against root directory of each products.
 * 3. Expose generated issue reports by uploading reports to security artifacts.
 * create JIRA issue ticket if there are any known vulnerabilities found in report.
 */
public class JSSecurityScanner {

    private static final Logger log = Logger.getLogger(JSSecurityScanner.class);

    public static void main(String[] args) {

        //Instance to perform pre processing
        PreProcessor preProcessor = new PreProcessor();

        //Instance to perform retire.js scan for supported product list
        Executor retireJsExecutor = new RetireJsExecutor();

        //Instance to perform outputPublisher
        VulnerabilityReportManager outputController = new VulnerabilityReportManager();

        //call pre processing
        log.info("----------------------------------------------------------------------------------------------");
        log.info("[JS_SEC_DAILY_SCAN] START");
        HashMap<String, String> productMapper = preProcessor.startPreprocessing();
        log.info("[JS_SEC_DAILY_SCAN] Product packs are downloaded successfully and ready to execute scanning " +
                "tool.");
        log.info("----------------------------------------------------------------------------------------------");
        if (productMapper.size() > 0) {
            HashMap<String, String> responseMapper = retireJsExecutor.startScanner(productMapper);
            log.info("[JS_SEC_DAILY_SCAN] Scan execution finished");
            log.info("----------------------------------------------------------------------------------------------");
            log.info("[JS_SEC_DAILY_SCAN] Starts publishing the report ");
            outputController.controlReportManager(responseMapper);
            log.info("----------------------------------------------------------------------------------------------");
            log.info("[JS_SEC_DAILY_SCAN] END");
        }
    }

}
