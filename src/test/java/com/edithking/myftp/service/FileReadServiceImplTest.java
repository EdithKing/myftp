package com.edithking.myftp.service;

import com.edithking.myftp.entity.FileOperation;
import com.edithking.myftp.service.impl.FileReadServiceImpl;
import com.edithking.myftp.service.impl.FileWriteServiceImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class FileReadServiceImplTest {

    private FileReadServiceImpl fileReadService;

    private FileWriteServiceImpl fileWriteService;


    @Test
    public void getAllFileOperations() {
        fileWriteService = new FileWriteServiceImpl();
        fileReadService = new FileReadServiceImpl();
        fileWriteService.setFileReadService(fileReadService);
        fileWriteService.readProperties();
        List<FileOperation> fileOperations = fileReadService.getAllFileOperations();
        fileOperations.forEach(e ->{
            System.out.println(e);
        });
    }
}
