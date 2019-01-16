package org.wso2.security.tools.scanmanager.dao;

import org.wso2.security.tools.scanmanager.model.Log;

/**
 * The class {@code LogDAO} is the DAO class that manage the persistance methods of the
 * Scan logs.
 */
public interface LogDAO {
    boolean persist(Log log);
}
