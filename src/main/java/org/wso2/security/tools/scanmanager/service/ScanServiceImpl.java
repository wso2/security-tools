package org.wso2.security.tools.scanmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wso2.security.tools.scanmanager.dao.ScanDAO;
import org.wso2.security.tools.scanmanager.model.Scan;
import org.wso2.security.tools.scanmanager.model.Scanner;

import java.util.List;

import static org.wso2.security.tools.scanmanager.config.StartUpInit.scanManagerConfiguration;

/**
 * The class {@code } is the service class that manage the method implementations of the
 * Scans.
 */
@Service
public class ScanServiceImpl implements ScanService {

    @Autowired
    ScanDAO scanDAO;

    @Override
    @Transactional
    public boolean persist(Scan scan) {
        return scanDAO.persist(scan);
    }

    @Override
    @Transactional
    public Scan getScan(Integer scanId) {
        return scanDAO.getScan(scanId);
    }


    @Override
    public List<Scanner> getScanners() {
        return scanManagerConfiguration.getScanners();
    }
}
