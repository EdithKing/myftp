package com.edithking.myftp.init;

import com.edithking.myftp.entity.FileProperties;
import com.edithking.myftp.enumentity.CommonType;
import com.edithking.myftp.service.FileWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Matcher;

@Component
/**
 * 初始化启动服务
 */
@Slf4j
public class FileWirteStart implements ApplicationRunner {
	@Autowired
	private FileWriteService fileWriteService;

	Properties props = System.getProperties();
	String osName = props.getProperty("os.name");

	/**
	 * 初始化配置文件内容
	 * 
	 * @return
	 */
	@Bean
	public FileProperties getFileProperties() {
		FileProperties fileProperties = new FileProperties();
		InputStreamReader fileInputStream = null;
		BufferedReader br = null;
		try {
			fileInputStream = new InputStreamReader(new FileInputStream(CommonType.FILEPROPERTIES), "UTF-8");
			Properties properties = new Properties();
			properties.load(fileInputStream);
			String path = properties.getProperty("localPath");
			if (!path.endsWith(CommonType.SLASH) && !path.endsWith(CommonType.BACKSLASH)) {
				path = path + CommonType.SLASH;
			}
			if (osName.startsWith(CommonType.WINDOW)) {
				path = path.replaceAll(CommonType.SLASH, Matcher.quoteReplacement(CommonType.BACKSLASH));
			} else if (osName.startsWith(CommonType.LINUX)) {
				path = path.replaceAll(Matcher.quoteReplacement(CommonType.BACKSLASH), CommonType.SLASH);
			}
			fileProperties.setLocalPath(path);
			fileProperties.setRemoteUser(properties.getProperty("remoteUser"));
			fileProperties.setRemotePassword(properties.getProperty("remotePassword"));
			String remotePath = properties.getProperty("remotePath");
			if (!remotePath.endsWith(CommonType.SLASH)) {
				remotePath = remotePath + CommonType.SLASH;
			}
			fileProperties.setRemotePath(remotePath);
			fileProperties.setRemotePort(Integer.valueOf(properties.getProperty("remotePort")));
			fileProperties.setRemoteHost(properties.getProperty("remoteHost"));
			fileProperties.setSvnUrl(properties.getProperty("svnUrl"));
			fileProperties.setSvnPath(properties.getProperty("svnPath"));
			fileProperties.setSvnUsername(properties.getProperty("svnUsername"));
			fileProperties.setSvnPassword(properties.getProperty("svnPassword"));
			fileProperties.setSvnClose(Boolean.valueOf(properties.getProperty("svnClose")));
			if (null != properties.getProperty("replacePath")) {
				readReplacePath(fileProperties, properties.getProperty("replacePath"));
			}
			File file = new File(CommonType.LASTFILE);
			if (file.exists()) {
				fileInputStream = new InputStreamReader(new FileInputStream(CommonType.LASTFILE), "UTF-8");
				br = new BufferedReader(fileInputStream);
				String lastestRevision = br.readLine();
				if (null != lastestRevision && lastestRevision.length() > 0) {
					fileProperties.setLastestRevision(Long.valueOf(lastestRevision));
				}
			} else {
				fileProperties.setLastestRevision(0L);
			}
			log.info("配置文件读取完成，配置信息如下" + fileProperties);
		} catch (Exception e) {
			log.error("配置文件读取有误", e);
		} finally {
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != fileInputStream) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fileProperties;
	}

	/**
	 * 对文件路径开头需要替换的进行替换
	 *
	 * @param file
	 * @param replacePath
	 */
	private void readReplacePath(FileProperties file, String replacePath) {
		String[] replaces = replacePath.split(CommonType.COMMA);
		for (int i = 0; i < replaces.length; i++) {
			Integer size = replaces[i].indexOf("=");
			String key = replaces[i].substring(0, size);
			String value = replaces[i].substring(size + 1);
			if (osName.startsWith(CommonType.WINDOW)) {
				key = key.replaceAll(CommonType.SLASH, Matcher.quoteReplacement(CommonType.BACKSLASH));
			} else if (osName.startsWith(CommonType.LINUX)) {
				key = key.replaceAll(Matcher.quoteReplacement(CommonType.BACKSLASH), CommonType.SLASH);
			}
			value.replaceAll(Matcher.quoteReplacement(CommonType.BACKSLASH), CommonType.SLASH);
			file.addReplacePath(key, value);
		}
	}

	/**
	 * 启动上传文件服务操作，完成后结束
	 *
	 * @param args
	 */
	@Override
	public void run(ApplicationArguments args) {
		if (fileWriteService.writeFile().equals(CommonType.OK)) {
			System.exit(1);
		}
	}
}
