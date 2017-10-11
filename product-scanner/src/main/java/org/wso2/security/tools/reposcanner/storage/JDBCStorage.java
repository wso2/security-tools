package org.wso2.security.tools.reposcanner.storage;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.wso2.security.tools.reposcanner.AppConfig;
import org.wso2.security.tools.reposcanner.entiry.Repo;
import org.wso2.security.tools.reposcanner.entiry.RepoArtifact;
import org.wso2.security.tools.reposcanner.entiry.RepoError;

import java.util.List;
import java.util.Properties;

/**
 * Created by ayoma on 4/13/17.
 */
public class JDBCStorage implements Storage {
    private static Logger log = Logger.getLogger(JDBCStorage.class.getName());
    private SessionFactory sessionFactory;

    public JDBCStorage(String driverName, String connectionUri, String username, char[] password, String hibernateDialect) {
        try {
            Properties properties = new Properties();
            properties.put("hibernate.connection.driver_class", driverName);
            properties.put("hibernate.connection.url", connectionUri);
            properties.put("hibernate.connection.username", username);
            properties.put("hibernate.connection.password", new String(password));
            properties.put("hibernate.dialect", hibernateDialect);
            if (AppConfig.isCreateDB()) {
                properties.put("hibernate.hbm2ddl.auto", "create");
            }
            if (AppConfig.isDebug()) {
                properties.put("hibernate.show_sql", "true");
                properties.put("hibernate.format_sql", "true");
            }

            Configuration configuration = new Configuration();
            configuration.addProperties(properties);

            configuration.addAnnotatedClass(Repo.class);
            configuration.addAnnotatedClass(RepoArtifact.class);
            configuration.addAnnotatedClass(RepoError.class);

            sessionFactory = configuration.buildSessionFactory();

            for (int i = 0; i < password.length; i++) {
                password[i] = ' ';
            }
        } catch (Throwable ex) {
            log.fatal("Failed to create sessionFactory object. Terminating..." + ex);
            throw ex;
        }
    }

    public synchronized boolean isRepoPresent(Repo repo) throws Exception {
        return !getRepoInfoList(repo.getUser(), repo.getRepositoryName(), repo.getTagName()).isEmpty();
    }

    @Override
    public synchronized boolean isArtifactPresent(Repo repo, String path) throws Exception {
        if (isRepoPresent(repo)) {
            List<Repo> repoList = getRepoInfoList(repo.getUser(), repo.getRepositoryName(), repo.getTagName());
            repo = repoList.get(0);

            Session session = sessionFactory.openSession();
            List results = null;
            try {
                String hql = "FROM org.wso2.security.tools.reposcanner.entiry.RepoArtifact A WHERE A.repo = :repo AND A.path = :path";
                Query query = session.createQuery(hql);
                query.setParameter("repo", repo);
                query.setParameter("path", path);
                results = query.list();
                if (results.size() > 1) {
                    log.warn("[Unexpected] Unexpected condition. Repo Info " + repo.getId() + ", Path \"" + path + "\" found multiple times");
                }
            } catch (Exception e) {
                throw e;
            } finally {
                session.close();
            }
            return !results.isEmpty();
        } else {
            return false;
        }
    }

    private synchronized List<Repo> getRepoInfoList(String user, String repositoryName, String tagName) throws Exception {
        Session session = sessionFactory.openSession();
        List results = null;
        try {
            String hql = "FROM org.wso2.security.tools.reposcanner.entiry.Repo R WHERE R.repositoryName = :repositoryName AND R.tagName = :tagName AND R.user = :user";
            Query query = session.createQuery(hql);
            query.setParameter("repositoryName", repositoryName);
            query.setParameter("tagName", tagName);
            query.setParameter("user", user);
            results = query.list();
            if (results.size() > 1) {
                log.warn("[Unexpected] Unexpected condition. User: " + user + ", Repo Name:" + repositoryName + ", Tag:" + tagName + " found multiple times");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            session.close();
        }
        return results;
    }

    public synchronized boolean persist(RepoArtifact repoArtifactInfo) throws Exception {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        try {
            List<Repo> repoList = getRepoInfoList(repoArtifactInfo.getRepo().getUser(), repoArtifactInfo.getRepo().getRepositoryName(), repoArtifactInfo.getRepo().getTagName());
            if (repoList.isEmpty()) {
                session.save(repoArtifactInfo.getRepo());
            } else {
                repoArtifactInfo.setRepo(repoList.get(0));
            }
            session.save(repoArtifactInfo);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
        return true;
    }

    @Override
    public synchronized boolean persistError(RepoError repoError) throws Exception {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        try {
            List<Repo> repoList = getRepoInfoList(repoError.getRepo().getUser(), repoError.getRepo().getRepositoryName(), repoError.getRepo().getTagName());
            if (repoList.isEmpty()) {
                session.save(repoError.getRepo());
            } else {
                repoError.setRepo(repoList.get(0));
            }
            session.save(repoError);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            session.close();
        }

        return true;
    }

    public void close() {
        sessionFactory.close();
    }

    @Override
    public boolean isErrorPresent(Repo repo, String path) throws Exception {
        if (isRepoPresent(repo)) {
            List<Repo> repoList = getRepoInfoList(repo.getUser(), repo.getRepositoryName(), repo.getTagName());
            repo = repoList.get(0);

            Session session = sessionFactory.openSession();
            List results = null;
            try {
                String hql = "FROM org.wso2.security.tools.reposcanner.entiry.RepoError A WHERE A.repo = :repo AND A.buildConfigLocation = :path";
                Query query = session.createQuery(hql);
                query.setParameter("repo", repo);
                query.setParameter("path", path);
                results = query.list();
                if (results.size() > 1) {
                    log.warn("[Unexpected] Unexpected condition. Repo Info " + repo.getId() + ", Path \"" + path + "\" found multiple times");
                }
            } catch (Exception e) {
                throw e;
            } finally {
                session.close();
            }
            return !results.isEmpty();
        } else {
            return false;
        }
    }
}
