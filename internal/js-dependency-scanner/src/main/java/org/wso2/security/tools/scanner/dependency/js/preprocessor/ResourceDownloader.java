/*
 *
 *   Copyright (c) 2018, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.security.tools.scanner.dependency.js.preprocessor;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.wso2.security.tools.scanner.dependency.js.constants.JSScannerConstants;
import org.wso2.security.tools.scanner.dependency.js.exception.DownloaderException;
import org.wso2.security.tools.scanner.dependency.js.exception.FileHandlerException;
import org.wso2.security.tools.scanner.dependency.js.model.Product;
import org.wso2.security.tools.scanner.dependency.js.utils.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class for resource downloader. This has common methods which is needed to be check to download
 * relevant weekly release of products.
 */
public abstract class ResourceDownloader {

    private static final Logger log = Logger.getLogger(ResourceDownloader.class);

    /**
     * This method calculate and returns the no of days between published date of particular weekly release
     * and scan date (Current system date).
     *
     * @param releaseDate published date of particulae weekly release.
     * @return no of days between published date of particular weekly release
     * and scan date (Current system date)
     * @throws ParseException Exception occurred parsing the string to date format.
     */
    long getDateDiffFromLastWeeklyRelease(String releaseDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        LocalDateTime now = LocalDateTime.now();
        Date firstDate = sdf.parse(releaseDate);
        Date secondDate = sdf.parse(now.toString());

        long diffInMillis = Math.abs(secondDate.getTime() - firstDate.getTime());
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Check whether the given asset name is weekly release or not. It checks with the regular expression
     * which indicates the version pattern. (It checks whether the asset contains -x.x.x-).
     *
     * @param name Asset name
     * @return True if it is weekly release, False if it is not a weekly release.
     */
    boolean isWeeklyRelease(String name) {
        boolean isWeeklyRelease = false;
        Pattern pattern = Pattern.compile(JSScannerConstants.VERSION_REGEX);
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            isWeeklyRelease = true;
        }
        return isWeeklyRelease;
    }

    /**
     * Check whether the given asset name is GA release or not. It checks with the regular expression
     * which indicates the version pattern. (It checks whether the asset contains -x.x.x.).
     *
     * @param name asset name
     * @return True if it is weekly release, False if it is not a weekly release.
     */
    boolean isGARelease(String name) {
        boolean isGARelease = false;
        Pattern pattern = Pattern.compile((JSScannerConstants.GA_RELEASE_VERSION_REGEX));
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            isGARelease = true;
        }
        return isGARelease;
    }

    /**
     * Create Directory while downloading files
     *
     * @param dir Directory to be created
     * @throws DownloaderException Exception occurred while creating resource directory.
     */
    static void createResourceDirectory(File dir) throws DownloaderException {
        try {
            // If the file belongs to weekly release the existing weekly release folder should be deleted
            refreshWeeklyReleaseDirectory(dir);
            CommonUtils.createDirectory(dir);
            log.info("[JS_SEC_DAILY_SCAN]  " + "new weekly release is created successfully : "
                    + dir.getName());
        } catch (IOException | FileHandlerException e) {
            throw new DownloaderException("Error occurred in refreshing the weekly release directory : " +
                    dir.getAbsolutePath());
        }
    }

    /**
     * This method checks wheather weekly release is already exists and if so weekly release folder should be deleted.
     *
     * @param dir Directory to be checked.
     * @throws IOException Exception occurred while deleting the file.
     */
    private static void refreshWeeklyReleaseDirectory(File dir) throws IOException {
        if (dir.exists() && dir.getAbsolutePath().contains(JSScannerConstants.WEEKLY_RELEASE)) {
            FileUtils.deleteDirectory(dir);
            log.info("[JS_SEC_DAILY_SCAN]  " + "Existing weekly release is deleted successfully : "
                    + dir.getName());
        }
    }

    /**
     * Download product pack.
     *
     * @param product Repository Name.
     * @param path    Path where the downloaded pack to be placed.
     * @return Path of the zip file.
     * @throws DownloaderException exception occurred while downloading files.
     */
    public abstract List<String> downloadProductPack(Product product, String path) throws
            DownloaderException;

}
