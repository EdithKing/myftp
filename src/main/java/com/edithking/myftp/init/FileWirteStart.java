package com.edithking.myftp.init;

import com.edithking.myftp.service.FileReadService;
import com.edithking.myftp.service.FileWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class FileWirteStart implements ApplicationRunner {

    @Autowired
    private FileReadService fileReadService;
    @Autowired
    private FileWriteService fileWriteService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        fileWriteService.writeFile();
    }
}
