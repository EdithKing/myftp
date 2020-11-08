package com.edithking.myftp;

import com.edithking.myftp.service.FileWriteService;
import com.edithking.myftp.service.impl.FileWriteServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyftpApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyftpApplication.class, args);

    }

}
