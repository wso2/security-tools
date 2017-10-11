package org.wso2.security.tools.reposcanner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.log4j.Logger;
import org.wso2.security.tools.reposcanner.scanner.GitHubRepoScanner;
import org.wso2.security.tools.reposcanner.scanner.RepoScanner;
import org.wso2.security.tools.reposcanner.storage.JDBCStorage;
import org.wso2.security.tools.reposcanner.storage.Storage;

/**
 * Created by ayoma on 4/14/17.
 */
public class Main {
    private static Logger log = Logger.getLogger(Main.class.getName());

    @Parameter(names = {"-git.oauth2"}, description = "OAuth token used to access GitHub", password = true, order = 1)
    private String gitOAuth2Token;

    @Parameter(names = {"-git.users"}, description = "Comma separated list of GitHub user accounts to scan", order = 2)
    private String gitUserAccounts;

    @Parameter(names = {"-maven.home"}, description = "Maven home (if environment variables are not set)", order = 3)
    private String mavenHome;

    @Parameter(names = {"-storage"}, description = "Storage used in storing final results (Options: JDBC) (Default: JDBC)", order = 4)
    private String storageType;

    @Parameter(names = {"-jdbc.driver"}, description = "Database driver class (Default: com.mysql.jdbc.Driver)", order = 5)
    private String databaseDriver;

    @Parameter(names = {"-jdbc.url"}, description = "Database connection URL (Default: jdbc:mysql://localhost/RepoScanner)", order = 6)
    private String databaseUrl;

    @Parameter(names = {"-jdbc.username"}, description = "Database username (Default: root)", order = 7)
    private String databaseUsername;

    @Parameter(names = {"-jdbc.password"}, description = "Database password", password = true, order = 8)
    private String databasePassword;

    @Parameter(names = {"-jdbc.dialect"}, description = "Database Hibernate dialect (Default: org.hibernate.dialect.MySQLDialect)", order = 9)
    private String databaseHibernateDialect;

    @Parameter(names = {"-verbose", "-v"}, description = "Verbose output", order = 10)
    private boolean verbose;

    @Parameter(names = {"-debug", "-d"}, description = "Verbose + Debug output for debugging requirements", order = 11)
    private boolean debug;

    @Parameter(names = {"--help", "-help", "-?"}, help = true, order = 12)
    private boolean help;

    @Parameter(names = {"-jdbc.create"}, description = "Drop and create JDBC tables", order = 13)
    private boolean databaseCreate;

    @Parameter(names = {"-rescan"}, description = "Rescan repo-tag combinaions even if they are already indexed. (Default: false)", order = 16)
    private boolean rescan;

    @Parameter(names = {"-downloadMaster"}, description = "Download master branches of all repositories locally", order = 17)
    private boolean downloadMaster;

    @Parameter(names = {"-downloadTags"}, description = "Download all tags of all repositories locally", order = 18)
    private boolean downloadTags;

    @Parameter(names = {"-skipScan"}, description = "Skip scanning process. Usable when -downloadMaster and -downloadTag options are required without scanning.", order = 18)
    private boolean skipScan;

    public static void main(String[] args) throws Exception {
        Main main = new Main();

        log.info("-------------------------------------------------");
        log.info("-----                                       -----");
        log.info("-----          Repository Scanner           -----");
        log.info("-----                                       -----");
        log.info("-------------------------------------------------");

        JCommander jCommander = new JCommander(main, args);
        jCommander.setProgramName("Repo Scanner");

        if (main.help) {
            jCommander.usage();
            return;
        }

        if (main.databaseDriver == null || main.databaseDriver.length() == 0) {
            main.databaseDriver = "com.mysql.jdbc.Driver";
        }
        if (main.databaseUrl == null || main.databaseUrl.length() == 0) {
            main.databaseUrl = "jdbc:mysql://localhost/RepoScanner";
        }
        if (main.databaseUsername == null || main.databaseUsername.length() == 0) {
            main.databaseUsername = "root";
        }
        if (main.databaseHibernateDialect == null || main.databaseHibernateDialect.length() == 0) {
            main.databaseHibernateDialect = "org.hibernate.dialect.MySQLDialect";
        }
        if (main.storageType == null || main.storageType.length() == 0) {
            main.storageType = "JDBC";
        }

        main.start(jCommander);
    }

    public void start(JCommander jCommander) {
        if (gitUserAccounts != null) {
            for (String user : gitUserAccounts.split(",")) {
                AppConfig.addGithubAccount(user);
            }
        }

        AppConfig.setMavenHome(mavenHome);
        AppConfig.setVerbose(verbose);
        if (debug) {
            AppConfig.setVerbose(true);
            AppConfig.setDebug(true);
        }
        AppConfig.setCreateDB(databaseCreate);
        AppConfig.setRescanRepos(rescan);
        AppConfig.setDownloadMaster(downloadMaster);
        AppConfig.setDownloadTags(downloadTags);
        AppConfig.setSkipScan(skipScan);

        if (rescan) {
            log.warn("Full repository re-scan in progress. Scan will take additional time.");
        }

        Storage storage = null;
        if (!AppConfig.isSkipScan()) {
            if (storageType.equals("JDBC")) {
                if (databaseDriver == null || databaseUrl == null || databaseUsername == null || databasePassword == null || databaseHibernateDialect == null) {
                    log.error("JDBC parameters are not properly set (All -jdbc parameters are required). Terminating...");
                    jCommander.usage();
                    return;
                }
                storage = new JDBCStorage(databaseDriver, databaseUrl, databaseUsername, databasePassword.toCharArray(), databaseHibernateDialect);
            } else {
                log.error("No valid storage option selected. Terminating...");
                jCommander.usage();
                return;
            }
        } else {
            log.warn("[Skipping] SkipScan parameter is set. Skipping database connection initialization.");
        }

        if (gitOAuth2Token != null) {
            try {
                RepoScanner scanner = new GitHubRepoScanner(gitOAuth2Token.toCharArray());
                scanner.scan(storage);

                //Any other scanners should be added here to make sure storage is closed only after all the scanners are complete

                log.info("Scanning complete. Terminating...");
            } catch (Exception e) {
                log.fatal("Exception occured during scanning process. Terminating...", e);
            } finally {
                storage.close();
            }
        } else {
            log.error("No scanning parameters are set. Please use \"-git.oauth2\" parameter to set OAuth credentials to access GitHub account. Terminating...");
            jCommander.usage();
            storage.close();
        }
    }
}