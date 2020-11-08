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

import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;

@Slf4j
@Service
public class FileWriteServiceImpl implements FileWriteService {

    private FileProperties fileProperties;
    @Getter
    @Setter
    @Autowired
    private FileReadServiceImpl fileReadService;
    private ChannelSftp channelSftp;
    private Channel channel;

    public FileProperties readProperties() {
        FileProperties file = new FileProperties();
        try {
            FileInputStream fileInputStream = new FileInputStream("file.properties");
            Properties properties = new Properties();
            properties.load(fileInputStream);
            file.setLocalPath(properties.getProperty("localPath"));
            file.setRemoteUser(properties.getProperty("remoteUser"));
            file.setRemotePassword(properties.getProperty("remotePassword"));
            file.setRemotePath(properties.getProperty("remotePath"));
            file.setRemotePort(Integer.valueOf(properties.getProperty("remotePort")));
            file.setRemoteHost(properties.getProperty("remoteHost"));
            fileReadService.setFileContext(properties.getProperty("fileContext"));
            System.out.println(file);
            log.info("配置文件读取完成，配置信息如下",file);
            this.fileProperties = file;
        }catch (Exception e){
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
                        channelSftp = (ChannelSftp) channel;
                        String localFileName = fileProperties.getLocalPath() + e.getFileName();
                        String remotePath = null;
                        if(e.getTypeId() == 2){
                            remotePath = fileProperties.getRemotePath()  + e.getFileName();
                            channelSftp.rm(remotePath.replace("/","\\"));
                            log.info("文件删除成功",localFileName);
                        }else {
                            remotePath = fileProperties.getRemotePath() + fileProperties.getRemotePath();
                            channelSftp.put(localFileName,remotePath.replace("/","\\"));
                            log.info("文件上传成功", localFileName);
                        }
                    }catch (Exception exception){
                        log.error("文件上传失败",fileProperties.getLocalPath() + e.getFileName() + "错误原因",exception);
                    }
                    });
                }
            }
        return "文件传输执行完成";
    }

    public void login(){
        try{
        JSch jSch = new JSch();
        Session session = jSch.getSession(fileProperties.getRemoteHost(),fileProperties.getRemoteUser(),fileProperties.getRemotePort());
        session.setPassword(fileProperties.getRemotePassword());
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            session.connect();
            log.debug("Session connected!");
            channel = session.openChannel("sftp");
            channel.connect();
            log.debug("Channel connected!");
        }catch (Exception e){
            log.error("远程主机登录异常",e);
        }
    }

}
