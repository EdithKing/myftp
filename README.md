# myftp
个人实现本地以及主机文件替换和删除

### 文件传输工具ftp



1. 下载源码后，通过maven打包成jar包

2. 在target目录下新增一个file.properties文件

   file.properties内容如下

   ```properties
   #本地文件所放路径
   localPath:C:/
   #目标主机用户名
   remoteUser:
   #目标主机密码
   remotePassword:
   #目标主机文件所放目录
   remotePath:
   #目标主机的ftp端口
   remotePort:
   #目标主机ip
   remoteHost:
   #所需要上传的文件列表(默认同一目录下的fileConext.txt)
   fileContext:fileConext.txt
   #目录名替换(本地为A，目标目录是B)
   replacePath:A=B,A1=B1
   
   ```

3. 需要上传的文件列表：（默认fileConext.txt）

   fileConext.txt内容,类型AUD，A新增，U更新，D删除，文件可以使用绝对路径，也可以使用localPath下的相对路径，绝对路径必须与localPath一致，如果目标目录文件夹不存在，会自动创建文件夹。路径以/作为分割

   ```txt
   AC:/test.txt
   Atest.txt
   ```

4. 运行jar包：java -jar myftp-1.0.jar



