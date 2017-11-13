package com.gofobao.framework.helper;

import com.jcraft.jsch.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

@Component
@Slf4j
public class FtpHelper {
    @Value("${jixin.ftp-port}")
    Integer ftpPort;

    @Value("${jixin.ftp-host}")
    String ftpHost;

    @Value("${jixin.ftp-username}")
    String ftpUsername;

    @Value("${jixin.ftp-password}")
    String ftpPassword;

    // 文件根目录
    static final String FILE_ROOT = "C2P";

    @Value("${jixin.save-file-path}")
    String saveFileUrl;

    /**
     * 获取文件路劲
     *
     * @param dateStr
     * @return
     */
    public String getFileDir(@NonNull String dateStr) {
        Date date = DateHelper.stringToDate(dateStr, DateHelper.DATE_FORMAT_YMD_NUM);
        int year = DateHelper.getYear(date);
        int month = DateHelper.getMonth(date);
        int day = DateHelper.getDayOfMonth(date);
        return String.format("/%s/%s/%s/%s", FILE_ROOT, year, month, day);
    }

    /**
     * ftp 下载
     *
     * @param dir      文件目录
     * @param fileName 文件名称
     * @return
     */
    public boolean download(String dir, String fileName) {
        log.info("ftp 下载日期路径");
        FTPClient client = new FTPClient();
        try {
            client.connect(ftpHost, ftpPort);
            boolean flag = client.login(ftpUsername, ftpPassword);
            if (!flag) {
                log.warn("======================================");
                log.warn("// FTP 账号密码错误");
                log.warn("======================================");
                return false;
            }
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);

            // 更改目录
            client.changeWorkingDirectory(dir);
            //文件在本地的存放地址
            File newFile = new File(saveFileUrl);
            if (!newFile.exists() && !newFile.isDirectory()) {
                newFile.mkdirs();
            }
            String saveFile = String.format("%s%s%s", saveFileUrl, File.separator, fileName);
            File file = new File(saveFile);
            if (file.exists()) {
                return true;
            }
            try (OutputStream outputStream = new FileOutputStream(saveFile);) {
                boolean downloadState = client.retrieveFile(fileName, outputStream);
                if (!downloadState) {
                    log.warn(String.format("FTP 文件下载失败: %s", fileName));
                    return false;
                }
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                log.error("SFTP文件下载异常", e);
            }

        } catch (Exception e) {
            log.info("ftp 下载异常", e);
        } finally {
            if (client.isConnected()) {
                try {
                    client.logout();
                    client.disconnect();
                } catch (Exception e) {
                    log.error("FTP 文件关闭失败", e);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 安全链接
     *
     * @param dir      目录
     * @param fileName 文件名
     * @return
     */
    public boolean downloadBySecurity(String dir, String fileName) {
        log.info("===========================================");
        log.info("文件路径" + dir);
        log.info("文件名称" + fileName);
        log.info("===========================================");

        Session session = null;
        Channel channel = null;
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(ftpUsername, ftpHost, ftpPort);
            if (session == null) {
                log.error("文件链接异常");
                return false;
            }
            //设置登陆主机的密码
            session.setPassword(ftpPassword);
            //设置第一次登陆的时候提示，可选值：(ask | yes | no)
            session.setConfig("StrictHostKeyChecking", "no");
            //设置登陆超时时间
            session.connect(30000);
            channel = (Channel) session.openChannel("sftp");

            channel.connect(1000);
            ChannelSftp sftp = (ChannelSftp) channel;
            sftp.cd(dir);

            File newFile = new File(saveFileUrl);
            if (!newFile.exists() && !newFile.isDirectory()) {
                newFile.mkdirs();
            }
            String saveFile = String.format("%s%s%s", saveFileUrl, File.separator, fileName);
            File file = new File(saveFile);
            if (file.exists()) {
                return true;
            }
            try (OutputStream outputStream = new FileOutputStream(saveFile)) {
                String path = String.format("%s/%s", dir, fileName);
                sftp.get(path, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                log.error("SFTP文件下载异常", e);
            } finally {
                sftp.disconnect();
            }
            return true;
        } catch (Exception e) {
            log.error("SFTP文件异常", e);
            return false;
        } finally {
            if (!ObjectUtils.isEmpty(session)) {
                session.disconnect();
                channel.disconnect();
            }
        }
    }

}
