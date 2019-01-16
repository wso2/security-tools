package org.wso2.security.tools.scanmanager.service;

import org.wso2.security.tools.scanmanager.model.Scan;
import org.wso2.security.tools.scanmanager.model.Scanner;

import java.util.List;
/**
 * The class {@code ScannerService} is the servise class that manage the methods of the
 * scans.
 */
public interface ScanService {
    boolean persist(Scan scan);

    Scan getScan(Integer scanId);

    List<Scanner> getScanners();
}
