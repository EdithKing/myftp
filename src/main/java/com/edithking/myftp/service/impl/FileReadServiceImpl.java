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
public class FileReadServiceImpl implements FileReadService {

    private List<FileOperation> fileOperations = new ArrayList<>();
    @Getter
    @Setter
    private String fileContext;

    @Override
    public List<FileOperation> getAllFileOperations() {
        fileOperations.clear();
        if(fileContext != null) {
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
        }else {
            log.error("需要上传的内容的文件为空，",fileContext);
            return null;
        }
    }
    private FileOperation getFileOperation(String fileContext){
        System.out.println(fileContext);
        Character type = fileContext.charAt(0);
        System.out.println(fileContext.charAt(0));
        System.out.println(type);
        switch (type){
           case 'A': return new FileOperation(OperationType.ADD.typeId, fileContext.substring(1));
           case 'U': return new FileOperation(OperationType.UPDATE.typeId,fileContext.substring(1));
           case 'D': return new FileOperation(OperationType.DELETE.typeId, fileContext.substring(1));
            default:
                return new FileOperation(4,"空");
        }
    }
}
