package com.edithking.myftp.service.impl;

import com.edithking.myftp.entity.FileOperation;
import com.edithking.myftp.enumentity.OperationType;
import com.edithking.myftp.service.FileReadService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 获取fileContext 文件中的内容
     * 格式: 文件传输类型(A,U,D)以及文件在本地的绝对路径去掉本地文件开头目录
     *
     * @return
     */
    @Override
    public List<FileOperation> getAllFileOperations() {
        fileOperations.clear();
        if (fileContext != null) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(fileContext));
                String context = null;
                while ((context = bufferedReader.readLine()) != null) {
                    fileOperations.add(getFileOperation(context));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return fileOperations;
        } else {
            log.error("需要上传的内容的文件为空，", fileContext);
            return null;
        }
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
                log.error("文件路径没有按照规定的开头,U更新,A新增,D删除");
                return null;
        }
    }
}
