# myftp
实现文件传输,可根据文件指定文件传输,根据svn更新记录传输

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
   #svn登陆路径
   svnUrl:
   #svn文件名路径
   svnPath:
   #svn的用户
   svnUsername:
   #svn的密码
   svnPassword:
   # 获取svn记录开关
   svnClose:false
   ```

3. 需要上传的文件列表：（默认fileConext.txt）

   fileConext.txt内容,类型AUD，A新增，U更新，D删除，文件可以使用绝对路径，也可以使用localPath下的相对路径，绝对路径必须与localPath一致，如果目标目录文件夹不存在，会自动创建文件夹。路径以/作为分割

   ```txt
   AC:/test.txt
   Atest.txt
   ```
4. 如果上传文件列表为空,获取svn更新记录开启,那么会从文件last.txt的所记录的最后一次上传的版本号(文件不存在默认为0,需要注意更新记录是否太多)为开始到最新的svn的更新的文件列表,然后将本地文件上传到对应主机目录,并且会更新last.txt文件,记录这次提交的版本号

5. 运行jar包：java -jar myftp-1.0.jar



