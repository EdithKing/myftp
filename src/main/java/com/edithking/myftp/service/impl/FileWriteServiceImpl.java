package com.edithking.myftp.service.impl;

import com.edithking.myftp.entity.FileOperation;
import com.edithking.myftp.entity.FileProperties;
import com.edithking.myftp.enumentity.CommonType;
import com.edithking.myftp.service.FileReadService;
import com.edithking.myftp.service.FileWriteService;
import com.edithking.myftp.util.JschUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

@Slf4j
@Service
/**
 * 文件传输操作类
 */
public class FileWriteServiceImpl implements FileWriteService {


    @Autowired
    private FileReadService fileReadService;

    @Autowired
    private FileProperties fileProperties;

    @Autowired
    private JschUtil jschUtil;


    /**
     * 对文件路径开头需要替换的进行替换
     * @param remotePath
     * @return
     */
    private String replacePath(String remotePath) {
        Map<String, String> replaceMap = fileProperties.getReplacePath();
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (remotePath.startsWith(key)) {
                remotePath = remotePath.replace(key, value);
            }
        }
        return remotePath;
    }

    /**
     * 传输文件
     *
     * @return
     */
    @Override
    public String writeFile() {
        if (fileProperties != null) {
            List<FileOperation> fileOperations = fileReadService.getAllFileOperations();
            if (null == fileOperations || fileOperations.size() == 0) {
                log.info("获取需要合并的文件内容为空,本次合并结束");
                return CommonType.OK;
            }
            ChannelSftp channelSftp = jschUtil.login();
            int fileCount = 0;
            int dirCount = 0;
            if (channelSftp != null) {
                for (int i = 0; i < fileOperations.size(); i++) {
                    FileOperation fileOperation = fileOperations.get(i);
                    String localFile = fileProperties.getLocalPath() + fileOperation.getFileName();
                    String remoteFile = fileProperties.getRemotePath() + replacePath(fileOperation.getFileName());
                    remoteFile = remoteFile.replaceAll(Matcher.quoteReplacement(CommonType.BACKSLASH), CommonType.SLASH);
                    File file = new File(localFile);
                    String fileName = file.getName();
                    String remotePath = remoteFile;
                    localFile = localFile.trim();
                    remoteFile = remoteFile.trim();
                    remotePath = remotePath.trim();
                    fileName = fileName.trim();
                    if (-1 != fileName.indexOf(CommonType.DIAN)) {
                        remotePath = remoteFile.substring(0, remoteFile.length() - fileName.length());
                    }

                    if (1 == fileOperation.getTypeId() || 3 == fileOperation.getTypeId()) {
                        if (!jschUtil.isDirExist(channelSftp, remotePath)) {
                            jschUtil.mkdir_P(channelSftp, remotePath);
                        }
                        if (-1 != fileName.indexOf(CommonType.DIAN)) {
                            try {
                                if(file.exists()) {
                                    channelSftp.put(localFile, remotePath);
                                    fileCount++;
                                    log.info("文件传输成功:" + remoteFile);
                                }else {
                                    log.error("文件不存在,忽略传送,文件名:",localFile);
                                }
                            } catch (SftpException e) {
                                log.error("文件上传失败", fileProperties.getLocalPath() + fileOperation.getFileName() + "错误原因", e);
                            }
                        } else {
                            dirCount++;
                        }
                    } else if (2 == fileOperation.getTypeId()) {
                        if (-1 == fileName.indexOf(CommonType.DIAN)) {
                            jschUtil.deleteDir(channelSftp, remotePath);
                            dirCount++;
                            log.info("删除文件夹完成," + remotePath);
                        } else {
                            System.out.println(remoteFile);
                            if (jschUtil.checkFileExist(channelSftp, remoteFile)) {
                                try {
                                    channelSftp.rm(remoteFile);
                                    fileCount++;
                                    log.info("文件已删除:"+ remoteFile);
                                } catch (SftpException e) {
                                    log.error("删除文件有误,文件为:" + remoteFile, e);
                                }
                            }
                        }
                    }
                }
                jschUtil.logout(channelSftp);
                log.info("文件传输执行完成");
                int fail = fileOperations.size() - fileCount - dirCount;
                log.info(" [操作文件总数:" + fileOperations.size() + " ,成功操作文件数目:" + fileCount + " , 文件夹数目:" + dirCount
                        + " , 失败操作数目:" + fail + " ] ");
            }
        }
        return "SUCCESS";
    }
}
