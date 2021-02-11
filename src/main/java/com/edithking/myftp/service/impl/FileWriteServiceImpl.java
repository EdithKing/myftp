package com.edithking.myftp.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edithking.myftp.entity.FileOperation;
import com.edithking.myftp.entity.FileProperties;
import com.edithking.myftp.service.FileReadService;
import com.edithking.myftp.service.FileWriteService;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
/**
 * 文件传输操作类
 */
public class FileWriteServiceImpl implements FileWriteService {

	private FileProperties fileProperties;
	@Getter
	@Setter
	@Autowired
	private FileReadService fileReadService;
	private ChannelSftp channelSftp;
	private Channel channel;

	/**
	 * 获取当前项目路径下的file.properties文件，并对其内容进行解析，赋值给FileProperties对象
	 *
	 * @return
	 */
	private FileProperties readProperties() {
		FileProperties file = new FileProperties();
		try {
			InputStreamReader fileInputStream = new InputStreamReader(new FileInputStream("file.properties"), "UTF-8");
			Properties properties = new Properties();
			properties.load(fileInputStream);
			file.setLocalPath(properties.getProperty("localPath"));
			file.setRemoteUser(properties.getProperty("remoteUser"));
			file.setRemotePassword(properties.getProperty("remotePassword"));
			file.setRemotePath(properties.getProperty("remotePath"));
			file.setRemotePort(Integer.valueOf(properties.getProperty("remotePort")));
			file.setRemoteHost(properties.getProperty("remoteHost"));
			if (null != properties.getProperty("replacePath")) {
				readReplacePath(file, properties.getProperty("replacePath"));
			}
			fileReadService.setFileContext(properties.getProperty("fileContext"));
			log.info("配置文件读取完成，配置信息如下" + file);
			this.fileProperties = file;
		} catch (Exception e) {
			log.error("配置文件读取有误", e);
		}
		return file;
	}

	/**
	 * 对文件路径开头需要替换的进行替换
	 *
	 * @param file
	 * @param replacePath
	 */
	private void readReplacePath(FileProperties file, String replacePath) {
		String[] replaces = replacePath.split(",");
		for (int i = 0; i < replaces.length; i++) {
			Integer size = replaces[i].indexOf("=");
			String key = replaces[i].substring(0, size);
			String value = replaces[i].substring(size + 1);
			file.addReplacePath(key, value);
		}
	}

	/**
	 * 传输文件
	 *
	 * @return
	 */
	@Override
	public String writeFile() {
		FileProperties fileProperties = readProperties();
		Properties props = System.getProperties();
		String osName = props.getProperty("os.name");
		if (fileProperties != null) {
			List<FileOperation> fileOperations = fileReadService.getAllFileOperations();
			login();
			int result = 0;
			int filedir = 0;
			if (channelSftp != null) {
				for (int i = 0; i < fileOperations.size(); i++) {
					FileOperation e = fileOperations.get(i);
					try {
						// 本地文件绝对路径or相对路径
						String localFileName = e.getFileName();
						if (!localFileName.startsWith(fileProperties.getLocalPath())) {
							localFileName = fileProperties.getLocalPath() + "/" + localFileName;
						}
						if (osName.startsWith("Window")) {
							localFileName = localFileName.replaceAll("/", "\\");
						} else if (osName.startsWith("Linux")) {
							localFileName = localFileName.replaceAll("\\", "/");
						}
						// 将文件绝对路径开头去掉
						String fileName = localFileName.replace(fileProperties.getLocalPath(), "");
						// 远程主机是linux
						String remotePathTemp = fileName.replace("\\", "/");
						// 经过替换后的远程文件目录路径，未含文件名，未包含file.properties中的远程文件目录
						String remotePath = fileProperties.getReplacePath(remotePathTemp);
						if (remotePathTemp.indexOf(".") != -1) {
							// 远程文件相对目录 = 远程目录加文件名
							remotePathTemp = remotePath + remotePathTemp.substring(remotePathTemp.lastIndexOf("/"));
						}
						// 远程文件绝对目录
						String remotePathFile = fileProperties.getRemotePath() + "/" + remotePathTemp;
						remotePath = fileProperties.getRemotePath() + "/" + remotePath;
						// 消除fileContext.txt的文件名中空格
						localFileName = localFileName.trim();
						remotePathFile = remotePathFile.trim();
						remotePath = remotePath.trim();
						// 删除文件以及文件夹
						if (e.getTypeId() == 2) {
							if (remotePathFile.equals(remotePath)) {
								deleteDir(remotePath);
								result++;
							} else if (checkFileExist(remotePathFile)) {
								channelSftp.rm(remotePathFile);
								log.info("文件删除成功:" + remotePathFile);
								result++;
							} else {
								log.info("文件或者文件夹不存在，不能删除:" + remotePathFile);
							}
						} else if (e.getTypeId() == 3 || e.getTypeId() == 1) {
							if (!isDirExist(remotePath)) {
								log.info("目录不存在，新建一个目录" + remotePath);
								mkdir_P(remotePath);
							}
							File localFile = new File(localFileName);
							if (localFile.isFile()) {
								channelSftp.put(localFileName, remotePath);
								result++;
							} else {
								filedir++;
							}
							log.info("文件更新成功:" + remotePathFile);
						}
					} catch (Exception exception) {
						log.error("文件上传失败", fileProperties.getLocalPath() + e.getFileName() + "错误原因", exception);
					}
				}
				log.info("文件传输执行完成");
				int fail = fileOperations.size() - result - filedir;
				log.info(" [操作文件总数:" + fileOperations.size() + " ,成功操作文件数目:" + result + " , 文件夹数目:" + filedir
						+ " , 失败操作数目:" + fail + " ] ");
				channelSftp.disconnect();
			}
		}
		return "SUCCESS";
	}

	private boolean deleteDir(String path) {
		try {
			if (isDirExist(path)) {
				Vector<LsEntry> vector = channelSftp.ls(path);
				for (int i = 0; i < vector.size(); i++) {
					deleteDir(vector.get(i).getFilename());
				}
				channelSftp.rmdir(path);
			} else {
				channelSftp.rm(path);
			}
			return true;
		} catch (Exception e) {
			log.error("删除文件有误:" + e.getMessage());
		}
		return false;
	}

	/**
	 * 判断远程主机文件是否存在
	 *
	 * @param remotePathFile
	 * @return
	 */
	private Boolean checkFileExist(String remotePathFile) {
		Integer lastSize = remotePathFile.lastIndexOf("/");
		String path = remotePathFile.substring(0, lastSize);
		String fileName = remotePathFile.substring(lastSize + 1);
		if (isDirExist(path)) {
			try {
				Vector vector = channelSftp.ls(path);
				for (int i = 0; i < vector.size(); i++) {
					if (vector.get(i).toString().indexOf(fileName) != -1) {
						return true;
					}
				}
			} catch (Exception e) {
				log.error("删除文件检测文件是否存在出现异常" + e);
			}
		}
		return false;
	}

	/**
	 * 判断远程主机目录是否存在
	 *
	 * @param remotePath
	 * @return
	 */
	private boolean isDirExist(String remotePath) {
		boolean flag = true;
		try {
			if (!channelSftp.stat(remotePath).isDir()) {
				return true;
			}
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 层次创建远程目录
	 *
	 * @param remotePath
	 */
	private void mkdir_P(String remotePath) {
		String paths[] = remotePath.split("/");
		String temp = "";
		try {
			for (int i = 0; i < paths.length; i++) {
				if (temp.equals("/")) {
					temp = temp + paths[i];
				} else {
					temp = temp + "/" + paths[i];
				}
				if (!isDirExist(temp)) {
					log.info("创建目录" + temp);
					channelSftp.mkdir(temp);
				}
			}
		} catch (Exception e) {
			log.error("创建 " + temp + " 目录失败", e);
		}
	}

	/**
	 * 登录远程主机
	 */
	private void login() {
		try {
			JSch jSch = new JSch();
			Session session = jSch.getSession(fileProperties.getRemoteUser(), fileProperties.getRemoteHost(),
					fileProperties.getRemotePort());
			session.setPassword(fileProperties.getRemotePassword());
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			session.setConfig(sshConfig);
			session.connect();
			log.info("Session connected!");
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;
			log.info("Channel connected!");
		} catch (Exception e) {
			log.error("远程主机登录异常", e);
		}
	}

}
