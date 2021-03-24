package com.edithking.myftp.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

import com.edithking.myftp.entity.FileOperation;
import com.edithking.myftp.entity.FileProperties;
import com.edithking.myftp.enumentity.CommonType;

import lombok.extern.slf4j.Slf4j;

/**
 * svn工具类
 */
@Slf4j
@Component
public class SvnUtil {
	@Autowired
	private FileProperties fileProperties;
	@Autowired
	private FileOperationUtil fileOperationUtil;

	public List<FileOperation> getFileOperationToSvn() {
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();
		FSRepositoryFactory.setup();
		List<FileOperation> fileOperations = new ArrayList<>();
		try {
			SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(fileProperties.getSvnUrl()));
			ISVNAuthenticationManager authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(
					fileProperties.getSvnUsername(), fileProperties.getSvnPassword());
			repository.setAuthenticationManager(authenticationManager);
			log.info("svn认证通过");
			Collection logEntries = null;
			Long lastRevision = repository.getLatestRevision();
			Long startRevision = null;
			if (null != fileProperties.getLastestRevision()) {
				startRevision = fileProperties.getLastestRevision();
			} else {
				startRevision = 0L;
			}
			logEntries = repository.log(new String[] { "" }, null, startRevision, lastRevision, true, true);
			logEntries.forEach(v -> {
				SVNLogEntry logEntry = (SVNLogEntry) v;
				if (logEntry.getChangedPaths().size() > 0) {
					Set changedPathsSet = logEntry.getChangedPaths().keySet();
					changedPathsSet.forEach(s -> {
						SVNLogEntryPath svnLogEntryPath = logEntry.getChangedPaths().get(s);
						String fileContent = svnLogEntryPath.getType()
								+ svnLogEntryPath.getPath().replaceFirst(fileProperties.getSvnPath(), "");
						FileOperation fileOperation = fileOperationUtil.getFileOperation(fileContent);
						if (null != fileOperation) {
							fileOperations.add(fileOperationUtil.getFileOperation(fileContent));
						}
					});
				}
			});
			log.info("svn内容获取完成");
			if (fileOperations.size() != 0) {
				OutputStreamWriter outputStreamWriter = null;
				try {
					outputStreamWriter = new OutputStreamWriter(new FileOutputStream(CommonType.LASTFILE), "UTF-8");
					outputStreamWriter.write(lastRevision.toString());
				} catch (Exception e) {
					log.error("更新上次传递svn版本的配置内容失败" + e.getMessage());
				} finally {
					if (null != outputStreamWriter) {
						try {
							outputStreamWriter.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (SVNException e) {
			log.error("svn获取更新记录有问题:" + e.getMessage());
		}
		return fileOperations;
	}
}
