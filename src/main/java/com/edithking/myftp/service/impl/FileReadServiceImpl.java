package com.edithking.myftp.service.impl;

import com.edithking.myftp.entity.FileOperation;
import com.edithking.myftp.entity.FileProperties;
import com.edithking.myftp.enumentity.OperationType;
import com.edithking.myftp.service.FileReadService;
import com.edithking.myftp.svn.SvnUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
/**
 * fileContext文件读取操作类
 */
public class FileReadServiceImpl implements FileReadService {

    private List<FileOperation> fileOperations = new ArrayList<>();
    @Getter
    @Setter
    private String fileContext;

    @Getter
    @Setter
    private FileProperties fileProperties;

    /**
     * 获取fileContext 文件中的内容 格式: 文件传输类型(A,U,D)以及文件在本地的绝对路径去掉本地文件开头目录
     *
     * @return
     */
    @Override
    public List<FileOperation> getAllFileOperations() {
        fileOperations.clear();
        if (fileContext != null) {
            try {
                FileInputStream fis = new FileInputStream(fileContext);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(isr);
                String context = null;
                while ((context = bufferedReader.readLine()) != null) {
                    fileOperations.add(getFileOperation(context));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
           if(fileOperations.size() != 0){
               return fileOperations;
           }
        }
        System.out.println("ipm:" + fileProperties);
        log.info("从svn获取更新记录");
        fileOperations = SvnUtil.getFileOperationToSvn(fileProperties);
        if(fileOperations.size() != 0){
            return fileOperations;
        }
        return null;
    }

    /**
     * 创建对象并给文件属性以及文件名赋值
     *
     * @param fileContext
     * @return
     */
    private FileOperation getFileOperation(String fileContext) {
        Character type = fileContext.charAt(0);
        switch (type) {
            case 'A':
                return new FileOperation(OperationType.ADD.typeId, fileContext.substring(1));
            case 'U':
                return new FileOperation(OperationType.UPDATE.typeId, fileContext.substring(1));
            case 'D':
                return new FileOperation(OperationType.DELETE.typeId, fileContext.substring(1));
            default:
                break;
        }
        if (fileContext.startsWith("Added : ")) {
            return new FileOperation(OperationType.ADD.typeId, fileContext.substring("Added : ".length()));
        }
        if (fileContext.startsWith("Updated : ")) {
            return new FileOperation(OperationType.UPDATE.typeId, fileContext.substring("Updated : ".length()));
        }
        if (fileContext.startsWith("Deleted : ")) {
            return new FileOperation(OperationType.DELETE.typeId, fileContext.substring("Deleted : ".length()));
        }
        return new FileOperation(OperationType.UPDATE.typeId, fileContext);
    }
}
