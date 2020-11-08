package com.edithking.myftp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileProperties {
    private String localPath;
    private String remotePath;
    private String remoteUser;
    private String remotePassword;
    private Integer remotePort;
    private String remoteHost;
}
