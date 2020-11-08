package com.edithking.myftp.init;

import com.edithking.myftp.service.impl.FileReadServiceImpl;
import com.edithking.myftp.service.impl.FileWriteServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class FileWirteStart implements ApplicationRunner {

    @Autowired
    private FileReadServiceImpl fileReadService;
    @Autowired
    private FileWriteServiceImpl fileWriteService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        fileWriteService.writeFile();
    }
}
