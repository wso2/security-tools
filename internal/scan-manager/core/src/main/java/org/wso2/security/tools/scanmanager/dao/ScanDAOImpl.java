package org.wso2.security.tools.scanmanager.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.wso2.security.tools.scanmanager.model.Scan;
/**
 * The class {@code LogDAOImpl} is the DAO implementation class that implements the persistence methods of the
 * Scan logs.
 */
@Repository
public class ScanDAOImpl implements ScanDAO {
    private static final Logger logger = LoggerFactory.getLogger(ScanDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public boolean persist(Scan scan) {
        Boolean saveFlag = true;
        try {
            Session currentSession = sessionFactory.getCurrentSession();
            //saving the data in the Database
            currentSession.saveOrUpdate(scan);
            logger.debug("scan is successfully persists");
        } catch (Exception ex) {
            //handle exception
            logger.error("Error occurred while persisting scan from DAO", ex);
            saveFlag = false;
        }
        return saveFlag;
    }

    @Override
    public Scan getScan(Integer scanId) {
        Scan scan;
        try {
            Session currentSession = sessionFactory.getCurrentSession();
            scan = (Scan) currentSession.get(Scan.class, scanId);
            logger.debug("scan is successfully retrieved");
        } catch (Exception ex) {
            //return the empty scan object
            logger.error("Error occurred while retrieving scan from DAO null scan object was returned", ex);
            scan = new Scan();
        }
        return scan;
    }
}
