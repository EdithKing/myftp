package com.edithking.myftp.util;

import com.edithking.myftp.entity.FileOperation;
import com.edithking.myftp.entity.FileProperties;
import com.edithking.myftp.enumentity.CommonType;
import com.edithking.myftp.enumentity.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * 文件内容获取工具类
 */
@Slf4j
@Component
public class FileOperationUtil {

    private static final String ADDED = "Added : ";
    private static final String UPDATE = "Updated : ";
    private static final String DELETE = "Deleted : ";

    @Autowired
    private FileProperties fileProperties;

    public List<FileOperation> getFileOperation() {
        List<FileOperation> fileOperations = new ArrayList<>();
        File file = new File(CommonType.FILECONTENT);
        BufferedReader bfReader = null;
        if(file.exists()) {
            try {
            	InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
                bfReader = new BufferedReader(isr);
                String str;
                while((str = bfReader.readLine()) != null){
                    FileOperation fileOperation = getFileOperation(str);
                    if(null != fileOperation) {
                        fileOperations.add(getFileOperation(str));
                    }
                }
            }catch (Exception e){
                log.error("读取配置文件" + file.getName() + "有误" + e.getMessage()) ;
            }finally {
                if(null != bfReader) {
                    try {
                        bfReader.close();
                    }catch (IOException e){
                        log.error("释放资源有误" + e.getMessage());
                    }
                }
            }
        }
        return fileOperations;
    }

    public FileOperation getFileOperation(String fileContext){
        if(-1 != fileContext.indexOf(".idea") || -1 !=  fileContext.indexOf(".svn") || -1 !=  fileContext.indexOf(".git")){
            return null;
        }
        Character type = fileContext.charAt(0);
        if(type == 'A') {
            return new FileOperation(OperationType.ADD.typeId, getRealFileName(fileContext.substring(1)));
        }else if(type == 'U' || type == 'M') {
            return new FileOperation(OperationType.UPDATE.typeId, getRealFileName(fileContext.substring(1)));
        }else if(type == 'D'){
                return new FileOperation(OperationType.DELETE.typeId, getRealFileName(fileContext.substring(1)));
        } else if (fileContext.startsWith(ADDED)) {
            return new FileOperation(OperationType.ADD.typeId, getRealFileName(fileContext.substring(ADDED.length())));
        }
        else if (fileContext.startsWith(UPDATE)) {
            return new FileOperation(OperationType.UPDATE.typeId, getRealFileName(fileContext.substring(UPDATE.length())));
        }
        else if (fileContext.startsWith(DELETE)) {
            return new FileOperation(OperationType.DELETE.typeId, getRealFileName(fileContext.substring(DELETE.length())));
        }
        return new FileOperation(OperationType.UPDATE.typeId, getRealFileName(fileContext));
    }

    private Properties props = System.getProperties();
    private String osName = props.getProperty("os.name");

    private String getRealFileName(String fileName){
        if (osName.startsWith(CommonType.WINDOW)) {
            fileName = fileName.replaceAll(CommonType.SLASH, Matcher.quoteReplacement(CommonType.BACKSLASH));
        } else if (osName.startsWith(CommonType.LINUX)) {
            fileName = fileName.replaceAll(Matcher.quoteReplacement(CommonType.BACKSLASH), CommonType.SLASH);
        }
        return fileName.replace(fileProperties.getLocalPath(),"");
    }
}
