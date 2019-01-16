package org.wso2.security.tools.scanmanager.service;

import org.wso2.security.tools.scanmanager.model.Log;

/**
 * The class {@code LogService} is the service class that manage the methods of the
 * Scan logs.
 */
public interface LogService {
    boolean persist(Log log);
}
