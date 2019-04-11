package org.wso2.security.tools.scanmanager.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wso2.security.tools.scanmanager.model.Log;
import org.wso2.security.tools.scanmanager.model.Scan;
import org.wso2.security.tools.scanmanager.model.ScanStatus;
import org.wso2.security.tools.scanmanager.model.ScanStatusUpdateObject;
import org.wso2.security.tools.scanmanager.service.LogService;
import org.wso2.security.tools.scanmanager.service.ScanService;

import java.sql.Timestamp;

/**
 * The class {@code ScannerEngineController} is the web controller which defines the routines for managing
 * scans and combine scan containers to scan manager.
 */
@Controller
@RequestMapping("callback")
public class CallbackController {

    private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);

    @Autowired
    LogService logService;

    @PostMapping(value = "persist-scan-log")
    public void persistScanLog(@RequestBody Log log) {

        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            log.setTimeStamp(timestamp);
            logService.persist(log);
        } catch (Exception ex) {
            logger.error("Error occurred while persisting the scan log", ex);
        }
    }

    @Autowired
    ScanService scanService;

    @PostMapping(value = "update-scan-status")
    @ResponseBody
    public void updateScanStatus(@RequestBody ScanStatusUpdateObject scanStatusUpdateObject) {
        try {
            String scanStatus = ScanStatus.valueOf(scanStatusUpdateObject.getScanStatus()).toString();
            Scan scan = scanService.getScan(scanStatusUpdateObject.getScanId());
            scan.setStatus(scanStatus);

            if (scanStatus.equals(ScanStatus.RUNNING.toString())) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                scan.setStartTimestamp(timestamp);
            } else if (scanStatus.equals(ScanStatus.SUBMITTED.toString())) {
                scan.setActualScannerId(scanStatusUpdateObject.getActualScannerId());
            }

            scanService.persist(scan);
        } catch (IllegalArgumentException ex) {
            logger.error("Invalid Scan Status is provided", ex);
        } catch (Exception ex) {
            logger.error("Error occurred while updating scan status", ex);
        }
    }
}
