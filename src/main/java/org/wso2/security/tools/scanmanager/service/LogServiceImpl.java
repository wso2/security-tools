package org.wso2.security.tools.scanmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wso2.security.tools.scanmanager.dao.LogDAO;
import org.wso2.security.tools.scanmanager.model.Log;

/**
 * The class {@code LogService} is the service class that manage the method implementations of the
 * Scan logs.
 */

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    LogDAO logDAO;

    @Override
    @Transactional
    public boolean persist(Log log) {
        return logDAO.persist(log);
    }
}
