package org.wso2.security.tools.scanmanager.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.wso2.security.tools.scanmanager.controller.ScanController;
import org.wso2.security.tools.scanmanager.model.Log;
/**
 * The class {@code LogDAOImpl} is the DAO class that manage the persistence method implementations of the,
 * Scan logs.
 */
@Repository
public class LogDAOImpl implements LogDAO {
    private static final Logger logger = LoggerFactory.getLogger(ScanController.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public boolean persist(Log log) {
        boolean status = true;
        try {
            Session currentSession = sessionFactory.getCurrentSession();
            currentSession.saveOrUpdate(log);
            logger.debug("log is successfully persists");
        } catch (Exception ex) {
            //needs to handle exception
            logger.error("Error occurred while persisting logs from DAO", ex);
            status = false;
        }
        return status;
    }
}
