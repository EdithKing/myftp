package com.edithking.myftp.util;

import com.edithking.myftp.entity.FileProperties;
import com.edithking.myftp.enumentity.CommonType;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.Vector;
/**
 * 目标主机操作工具类
 */
@Slf4j
@Component
public class JschUtil {

    @Autowired
    private FileProperties fileProperties;

    /**
     * 登录远程主机
     */
    public ChannelSftp login() {
        ChannelSftp channelSftp = null;
        try {
            JSch jSch = new JSch();
            Session session = jSch.getSession(fileProperties.getRemoteUser(), fileProperties.getRemoteHost(),
                    fileProperties.getRemotePort());
            session.setPassword(fileProperties.getRemotePassword());
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            session.connect();
            log.info("Session connected!");
            Channel channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            log.info("Channel connected!");
        } catch (Exception e) {
            log.error("远程主机登录异常", e);
        }
        return channelSftp;
    }


    /**
     * 删除文件夹以及文件
     * @param channelSftp
     * @param path
     * @return
     */
    public boolean deleteDir(ChannelSftp channelSftp,String path) {
        if (path.equals(CommonType.DIAN) || path.equals(CommonType.DIAN2)) {
            return true;
        }
        try {
            if (isDirExist(channelSftp,path)) {
                Vector<ChannelSftp.LsEntry> vector = channelSftp.ls(path);
                for (int i = 0; i < vector.size(); i++) {
                    if (!vector.get(i).getFilename().equals(CommonType.DIAN) && !vector.get(i).getFilename().equals(CommonType.DIAN2)) {
                        if (checkFileExist(channelSftp,path + CommonType.SLASH + vector.get(i).getFilename())) {
                            channelSftp.rm(path + CommonType.SLASH + vector.get(i).getFilename());
                        } else {
                            deleteDir(channelSftp,path + CommonType.SLASH + vector.get(i).getFilename());
                        }
                    }
                }
                channelSftp.rmdir(path);
            }
            return true;
        } catch (Exception e) {
            log.error("删除文件有误,文件名为:"+path + e.getMessage());
        }
        return false;
    }


    /**
     * 退出
     * @param channelSftp
     */
    public void logout(ChannelSftp channelSftp) {
        if(null != channelSftp){
            channelSftp.disconnect();
        }
    }

    /**
     * 判断远程主机文件是否存在
     *
     * @param remotePathFile
     * @return
     */
    public Boolean checkFileExist(ChannelSftp channelSftp,String remotePathFile) {
        try {
            if (channelSftp.stat(remotePathFile).isReg()) {
                return true;
            }
        } catch (Exception e) {
           log.info("检查文件存在异常" + e.getMessage());
        }
        return false;
    }

    /**
     * 层次创建远程目录
     *
     * @param remotePath
     */
    public void mkdir_P(ChannelSftp channelSftp,String remotePath) {
        String paths[] = remotePath.split(CommonType.SLASH);
        String temp = "";
        try {
            for (int i = 0; i < paths.length; i++) {
                if (temp.equals(CommonType.SLASH)) {
                    temp = temp + paths[i];
                } else {
                    temp = temp + CommonType.SLASH + paths[i];
                }
                if (!isDirExist(channelSftp,temp)) {
                    log.info("创建目录" + temp);
                    channelSftp.mkdir(temp);
                }
            }
        } catch (Exception e) {
            log.error("创建 " + temp + " 目录失败", e);
        }
    }

    /**
     * 判断远程主机目录是否存在
     *
     * @param remotePath
     * @return
     */
    public boolean isDirExist(ChannelSftp channelSftp,String remotePath) {
        if (remotePath.indexOf(CommonType.DIAN) != -1 && -1 != remotePath.lastIndexOf(CommonType.SLASH)){
            remotePath = remotePath.substring(0,remotePath.lastIndexOf(CommonType.SLASH));
        }
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
}
