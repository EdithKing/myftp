package com.edithking.myftp.service.impl;

import com.edithking.myftp.entity.FileOperation;
import com.edithking.myftp.entity.FileProperties;
import com.edithking.myftp.service.FileReadService;
import com.edithking.myftp.service.FileWriteService;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class FileWriteServiceImpl implements FileWriteService {

    private FileProperties fileProperties;
    @Getter
    @Setter
    @Autowired
    private FileReadService fileReadService;
    private ChannelSftp channelSftp;
    private Channel channel;

    private FileProperties readProperties() {
        FileProperties file = new FileProperties();
        try {
            File curAllInOneProjectFile = new File(System.getProperty("user.dir"));
            File parentFile = curAllInOneProjectFile.getParentFile();
            String parentPath = parentFile + "/";
            InputStreamReader fileInputStream = new InputStreamReader(new FileInputStream("file.properties"), "UTF-8");
            Properties properties = new Properties();
            properties.load(fileInputStream);
            file.setLocalPath(properties.getProperty("localPath"));
            file.setRemoteUser(properties.getProperty("remoteUser"));
            file.setRemotePassword(properties.getProperty("remotePassword"));
            file.setRemotePath(properties.getProperty("remotePath"));
            file.setRemotePort(Integer.valueOf(properties.getProperty("remotePort")));
            file.setRemoteHost(properties.getProperty("remoteHost"));
            fileReadService.setFileContext(properties.getProperty("fileContext"));
            log.info("配置文件读取完成，配置信息如下" + file);
            this.fileProperties = file;
        } catch (Exception e) {
            log.error("配置文件读取有误", e);
        }
        return file;
    }


    @Override
    public String writeFile() {
        FileProperties fileProperties = readProperties();
        if (fileProperties != null) {
            List<FileOperation> fileOperations = fileReadService.getAllFileOperations();
            login();
            if (channelSftp != null) {
                fileOperations.forEach(e -> {
                    try {
                        String localFileName = fileProperties.getLocalPath() + "\\" + e.getFileName();
                        String remotePathTemp = fileProperties.getRemotePath() + "/" + e.getFileName();
                        String remotePathFile = remotePathTemp.replace("\\", "/");
                        String remotePath = remotePathFile.substring(0, remotePathFile.lastIndexOf("/"));
                        remotePathFile = remotePathFile.trim();
                        remotePath = remotePath.trim();
                        if (e.getTypeId() == 2) {
                            channelSftp.rm(remotePathFile);
                            log.info("文件删除成功:" + remotePathFile);
                        } else if (e.getTypeId() == 3) {
                            if (!isDirExist(channelSftp, remotePath)) {
                                channelSftp.put(localFileName, remotePath);
                            }
                            log.info("文件更新成功:" + remotePathFile);
                        } else if (e.getTypeId() == 1) {
                            if (!isDirExist(channelSftp, remotePath)) {
                                log.info("目录不存在，新建一个目录");
                                mkdir_P(channelSftp, remotePath);
                            }
                            if (isDirExist(channelSftp, remotePath)) {
                                channelSftp.put(localFileName, remotePath);
                                log.info("文件新增成功:" + remotePathFile);
                            }
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        log.error("文件上传失败", fileProperties.getLocalPath() + e.getFileName() + "错误原因", exception);
                    }
                });
                log.info("文件传输执行完成");
                channelSftp.disconnect();
            }
        }
        return "SUCCESS";
    }

    private boolean isDirExist(ChannelSftp channelSftp, String remotePath) {
        boolean flag = true;
        try {
            if (!channelSftp.stat(remotePath).isDir()) {
                return true;
            }
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    private void mkdir_P(ChannelSftp channelSftp, String remotePath) {
        String paths[] = remotePath.split("/");
        String temp = "";
        try {
            for (int i = 0; i < paths.length; i++) {
                if (temp.equals("/")) {
                    temp = temp + paths[i];
                } else {
                    temp = temp + "/" + paths[i];
                }
                if (!isDirExist(channelSftp, temp)) {
                    channelSftp.mkdir(temp);
                }
            }
        } catch (Exception e) {
            log.error("创建 " + temp + " 目录失败", e);
        }
    }

    private void login() {
        try {
            JSch jSch = new JSch();
            Session session = jSch.getSession(fileProperties.getRemoteUser(), fileProperties.getRemoteHost(), fileProperties.getRemotePort());
            session.setPassword(fileProperties.getRemotePassword());
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            session.connect();
            log.info("Session connected!");
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            log.info("Channel connected!");
        } catch (Exception e) {
            log.error("远程主机登录异常", e);
        }
    }

}
