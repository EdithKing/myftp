package com.edithking.myftp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Iterator;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileProperties {
    /**
     * 本地文件开头目录
     */
    private String localPath;
    /**
     * 远程主机开头目录
     */
    private String remotePath;
    /**
     * 远程主机用户名
     */
    private String remoteUser;
    /**
     * 远程主机密码
     */
    private String remotePassword;
    /**
     * 远程主机端口号
     */
    private Integer remotePort;
    /**
     * 远程主机Ip
     */
    private String remoteHost;
    /**
     * 文件路径替换
     */
    private HashMap<String, String> replacePath;
    /**
     * svn的url
     */
    private String svnUrl;
    /**
     * svn根路径
     */
    private String svnPath;
    /**
     * svn用户名
     */
    private String svnUsername;
    /**
     * svn密码
     */
    private String svnPassword;
    /**
     * svn最后版本
     */
    private Long lastestRevision;

    /**
     * svn功能
     */
    private Boolean svnClose;

    /**
     * 新增路径开头替换
     *
     * @param keyPath
     * @param valuePath
     * @return
     */
    public String addReplacePath(String keyPath, String valuePath) {
        if (replacePath == null) {
            replacePath = new HashMap<>();
        }
        replacePath.put(keyPath.trim(), valuePath.trim());
        return "SUCCESS";
    }

    /**
     * 将路径开头需要替换的进行替换，然后返回路径
     *
     * @param checkPath
     * @return
     */
    public String getReplacePath(String checkPath) {
        if (checkPath.startsWith("/")) {
            checkPath = checkPath.substring(1);
        }
        if (replacePath != null) {
            Iterator<String> iterator = replacePath.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (checkPath.contains(key)) {
                    checkPath = replacePath.get(key) + checkPath.substring(key.length());
                }
            }
        }
        if (checkPath.lastIndexOf("/") != -1) {
            return checkPath.substring(0, checkPath.lastIndexOf("/"));
        }
        return checkPath;
    }
}
