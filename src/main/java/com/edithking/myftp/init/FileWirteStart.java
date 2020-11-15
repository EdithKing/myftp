package com.edithking.myftp.init;

import com.edithking.myftp.service.FileWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
/**
 * 初始化启动服务
 */
public class FileWirteStart implements ApplicationRunner {
    @Autowired
    private FileWriteService fileWriteService;

    /**
     * 启动上传文件服务操作，完成后结束
     *
     * @param args
     */
    @Override
    public void run(ApplicationArguments args) {
        if (fileWriteService.writeFile().equals("SUCCESS")) {
            System.exit(1);
        }
    }
}
