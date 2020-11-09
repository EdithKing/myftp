package com.edithking.myftp.service;


import com.edithking.myftp.entity.FileOperation;

import java.util.List;

public interface FileReadService {

    List<FileOperation> getAllFileOperations();

    String getFileContext();

    void setFileContext(String fileContext);
}
