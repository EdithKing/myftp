package com.edithking.myftp.svn;

import com.edithking.myftp.entity.FileOperation;
import com.edithking.myftp.entity.FileProperties;
import com.edithking.myftp.enumentity.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
public class SvnUtil {

    public static List<FileOperation> getFileOperationToSvn(FileProperties fileProperties) {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
        List<FileOperation> fileOperations = new ArrayList<>();
        try {
            System.out.println(fileProperties);
            SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(fileProperties.getSvnUrl()));
            ISVNAuthenticationManager authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(fileProperties.getSvnUsername(), fileProperties.getSvnPassword());
            repository.setAuthenticationManager(authenticationManager);
            Collection logEntries = null;
            logEntries = repository.log(new String[]{""},null,fileProperties.getLatestRevision(),-1,true,true);
            logEntries.forEach(v->{
                SVNLogEntry logEntry = (SVNLogEntry) v;
                if(logEntry.getChangedPaths().size() > 0){
                    Set changedPathsSet = logEntry.getChangedPaths().keySet();
                    changedPathsSet.forEach(s ->{
                        SVNLogEntryPath svnLogEntryPath = logEntry.getChangedPaths().get(s);
                        if(svnLogEntryPath.getType() == 'U'){
                            fileOperations.add(new FileOperation(OperationType.UPDATE.typeId,svnLogEntryPath.getPath().replaceFirst(fileProperties.getSvnPath(),"")));
                        }else if(svnLogEntryPath.getType() == 'A'){
                            fileOperations.add(new FileOperation(OperationType.ADD.typeId,svnLogEntryPath.getPath().replaceFirst(fileProperties.getSvnPath(),"")));
                        }else if(svnLogEntryPath.getType() == 'D'){
                            fileOperations.add(new FileOperation(OperationType.DELETE.typeId,svnLogEntryPath.getPath().replaceFirst(fileProperties.getSvnPath(),"")));
                        }
                    });
                }
            });
            if(fileOperations.size() != 0){
                FileOutputStream fileInputStream = null;
                try {
                    fileInputStream = new FileOutputStream("last.properties");
                    Properties properties = new Properties();
                    properties.setProperty("latestRevision",repository.getLatestRevision()+"");
                    properties.store(fileInputStream, "");
                }catch (Exception e){
                    log.error("更新properties文件内容失败"+ e);
                }finally {
                    if(null != fileInputStream ) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }catch (SVNException e){
            log.error("svn获取更新记录有问题:" + e);
        }
        return fileOperations;
    }
}
