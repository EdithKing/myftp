package com.edithking.myftp.service.impl;

import com.edithking.myftp.entity.FileOperation;
import com.edithking.myftp.entity.FileProperties;
import com.edithking.myftp.service.FileReadService;
import com.edithking.myftp.util.FileOperationUtil;
import com.edithking.myftp.util.SvnUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
/**
 * fileContext文件读取操作类
 */
public class FileReadServiceImpl implements FileReadService {

    @Autowired
    private FileOperationUtil fileOperationUtil;

    @Autowired
    private FileProperties fileProperties;

    @Autowired
    private SvnUtil svnUtil;

    /**
     * 获取fileContext 文件中的内容 格式: 文件传输类型(A,U,D)以及文件在本地的绝对路径去掉本地文件开头目录
     *
     * @return
     */
    @Override
    public List<FileOperation> getAllFileOperations() {
        List<FileOperation> fileOperations = fileOperationUtil.getFileOperation();
        if (fileOperations.size() != 0) {
            return fileOperations;
        } else if(!fileProperties.getSvnClose()){
            log.info("文件获取内容为空,从svn获取更新记录");
            fileOperations = svnUtil.getFileOperationToSvn();
            if (fileOperations.size() != 0) {
                return fileOperations;
            }
        }
        return null;
    }
}
