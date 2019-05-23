/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.security.tools.scanmanager.webapp.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.scanmanager.webapp.config.ScanManagerWebappConfiguration;
import org.wso2.security.tools.scanmanager.webapp.exception.ScanManagerWebappException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.security.tools.scanmanager.webapp.util.Constants.FTP_SCAN_DATA_DIRECTORY_NAME;

/**
 * Utility class for scan manager webapp.
 */
public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static final String STRICT_HOST_KEY_CHECKING_CONFIG_NAME = "StrictHostKeyChecking";
    private static final String PREFERRED_AUTHENTICATIONS_CONFIG_NAME = "PreferredAuthentications";

    private FTPUtil() {
    }

    /**
     * Download files from FTP server
     *
     * @param remoteFileLocation location of the files in the FTP
     * @param outputLocation     location of the output file
     * @throws ScanManagerWebappException when an error occurs while downloading from the FTP server
     */
    public static void downloadFromFTP(String remoteFileLocation, String outputLocation)
            throws ScanManagerWebappException {
        ChannelSftp channelSftp = null;
        Channel channel;
        Session session = null;
        BufferedInputStream destFileBufferedInputStream = null;
        char[] ftpPassword = null;
        File file = new File(outputLocation);
        File remoteFile = new File(remoteFileLocation);
        JSch jsch = new JSch();

        byte[] buffer = new byte[1024];
        int readCount;
        try (OutputStream destFileOutputStream = new FileOutputStream(file);
             BufferedOutputStream destFileBufferedOutputStream = new BufferedOutputStream(destFileOutputStream)) {
            String ftpHost = ScanManagerWebappConfiguration.getInstance().getFtpHost();
            String ftpUsername = ScanManagerWebappConfiguration.getInstance().getFtpUsername();
            ftpPassword = ScanManagerWebappConfiguration.getInstance().getFtpPassword();
            int ftpPort = ScanManagerWebappConfiguration.getInstance().getFtpPort();
            session = jsch.getSession(ftpUsername, ftpHost, ftpPort);
            session.setConfig(STRICT_HOST_KEY_CHECKING_CONFIG_NAME, "no");
            session.setConfig(PREFERRED_AUTHENTICATIONS_CONFIG_NAME, "password");
            session.setPassword(toBytes(ftpPassword));
            session.connect();

            channel = session.openChannel("sftp");
            channelSftp = (ChannelSftp) channel;
            channelSftp.connect();
            channelSftp.cd(remoteFile.getParent());
            destFileBufferedInputStream = new BufferedInputStream(channelSftp.get(remoteFileLocation));
            while ((readCount = destFileBufferedInputStream.read(buffer)) > 0) {
                destFileBufferedOutputStream.write(buffer, 0, readCount);
            }
        } catch (IOException | JSchException | SftpException e) {
            throw new ScanManagerWebappException("Error occurred while downloading from FTP.", e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
            try {
                if (destFileBufferedInputStream != null) {
                    destFileBufferedInputStream.close();
                }
            } catch (IOException e) {
                logger.error("Error occurred while closing the buffered output stream from FTP", e);
            }
            cleanCharArrays(ftpPassword);
        }
    }

    /**
     * Upload files to FTP server.
     *
     * @param remoteScanDirectory destination file directory
     * @param storedFileMap       file map
     * @return a map containing the paths of the uploaded files
     * @throws ScanManagerWebappException when an error occurs when uploading files to FTP server
     */
    public static Map<String, String> uploadFilesToFTP(String remoteScanDirectory, Map<String, String> storedFileMap)
            throws ScanManagerWebappException {
        ChannelSftp channelSftp = null;
        Channel channel;
        Session session = null;
        char[] ftpPassword = null;
        Map<String, String> uploadedFileMetaData = new HashMap<>();
        JSch jsch = new JSch();

        try {
            String ftpHost = ScanManagerWebappConfiguration.getInstance().getFtpHost();
            String ftpUsername = ScanManagerWebappConfiguration.getInstance().getFtpUsername();
            ftpPassword = ScanManagerWebappConfiguration.getInstance().getFtpPassword();
            int ftpPort = ScanManagerWebappConfiguration.getInstance().getFtpPort();
            session = jsch.getSession(ftpUsername, ftpHost, ftpPort);
            session.setConfig(STRICT_HOST_KEY_CHECKING_CONFIG_NAME, "no");
            session.setConfig(PREFERRED_AUTHENTICATIONS_CONFIG_NAME, "password");
            session.setPassword(toBytes(ftpPassword));
            session.connect();

            channel = session.openChannel("sftp");
            channelSftp = (ChannelSftp) channel;
            channelSftp.connect();
            channelSftp.cd(ScanManagerWebappConfiguration.getInstance().getFtpBasePath() + File.separator +
                    FTP_SCAN_DATA_DIRECTORY_NAME);
            channelSftp.mkdir(remoteScanDirectory);
            channelSftp.cd(remoteScanDirectory);

            for (Map.Entry<String, String> fileEntry : storedFileMap.entrySet()) {
                String unifiedFileName = fileEntry.getKey() + "." +
                        FilenameUtils.getExtension(fileEntry.getValue());
                channelSftp.put(new FileInputStream(fileEntry.getValue()), unifiedFileName);
                uploadedFileMetaData.put(fileEntry.getKey(),
                        ScanManagerWebappConfiguration.getInstance().getFtpBasePath() + File.separator +
                                FTP_SCAN_DATA_DIRECTORY_NAME + File.separator + remoteScanDirectory + File.separator +
                                unifiedFileName);
            }
        } catch (IOException | JSchException | SftpException e) {
            throw new ScanManagerWebappException("Error occurred while uploading files to FTP.", e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
            cleanCharArrays(ftpPassword);
        }
        return uploadedFileMetaData;
    }

    /**
     * Flush char arrays that stores sensitive information.
     *
     * @param charArray char array to be flushed
     */
    private static void cleanCharArrays(char[] charArray) {
        if (charArray != null) {
            for (int i = 0; i < charArray.length; i++) {
                charArray[i] = '\0';
            }
        }
    }

    private static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }
}
