package org.wso2.security.tools.scanmanager.dao;

import org.wso2.security.tools.scanmanager.model.Scan;
/**
 * The class {@code } is the DAO class that persist data on the SCAN table.
 */
public interface ScanDAO {
    
    boolean persist(Scan scan);
    
    Scan getScan(Integer scanId);
}
