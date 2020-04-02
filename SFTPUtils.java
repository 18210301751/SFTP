package org.transfar.freeflow.charging.util;

/**
 * @ClassName SFTPUtils
 * @Description TODO
 * @Author Liu.YuChong
 * @Date 2019/10/22 15:17
 * @Version 1.0
 */

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

/**
 * 类说明 sftp工具类
 */
@Component
@ConfigurationProperties(prefix = "sftp")
public class SFTPUtils {
    private transient Logger log = LoggerFactory.getLogger(this.getClass());

    private ChannelSftp sftp;

    private Session session;
    /** SFTP 登录用户名*/
    private  String username;
    /** SFTP 登录密码*/
    private  String password;
    /** 私钥 */
    private String privateKey;
    /** SFTP 服务器地址IP地址*/
    private String host;
    /** SFTP 端口*/
    private int port;


    /**
     * 构造基于密码认证的sftp对象
     */
    /* SFTPUtils() {
        this.username = IpPortConstant.SFTP_USERNAME;
        this.password = IpPortConstant.SFTP_PASSWORD;
        this.host =IpPortConstant.SFTP_HOST;
        this.port =IpPortConstant.SFTP_PORT ;
    }*/
    public SFTPUtils(){}
    /**
     * 构造基于秘钥认证的sftp对象
     */
    public SFTPUtils(String username, String host, int port, String privateKey) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.privateKey = privateKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 连接sftp服务器
     */
    public void login(){
        try {

            JSch jsch = new JSch();
            /*if (privateKey != null) {
                jsch.addIdentity(privateKey);// 设置私钥
            }*/

            session = jsch.getSession(username, host, port);

            if (password != null) {
                session.setPassword(password);
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");

            session.setConfig(config);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();

            sftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接 server
     */
    public void logout(){
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
            }
        }
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }


    /**
     * 将输入流的数据上传到sftp作为文件。文件完整路径=basePath+directory
     * @param basePath  服务器的基础路径
     * @param directory  上传到该目录
     * @param sftpFileName  sftp端文件名
     * @param in   输入流
     */
    public OutputStream upload(String basePath,String directory, String sftpFileName) throws SftpException {
        try {
            sftp.cd(basePath);
            sftp.cd(directory);
        } catch (SftpException e) {
            //目录不存在，则创建文件夹
            String [] dirs=directory.split("/");
            String tempPath=basePath;
            for(String dir:dirs){
            if(null== dir || "".equals(dir)) continue;
            tempPath+="/"+dir;
            try{
                sftp.cd(tempPath);
            }catch(SftpException ex){
                sftp.mkdir(tempPath);
                sftp.cd(tempPath);
            }
        }
        }
        OutputStream o=sftp.put(sftpFileName);  //上传文件
        System.out.println("上传"+sftpFileName+"成功()");
        return o;
    }
    public void upload(String basePath,String directory, String sftpFileName, InputStream input) throws SftpException {
        try {
            sftp.cd(basePath);
            sftp.cd(directory);
        } catch (SftpException e) {
            //目录不存在，则创建文件夹
            String [] dirs=directory.split("/");
            String tempPath=basePath;
            for(String dir:dirs){
                if(null== dir || "".equals(dir)) continue;
                tempPath+="/"+dir;
                try{
                    sftp.cd(tempPath);
                }catch(SftpException ex){
                    sftp.mkdir(tempPath);
                    sftp.cd(tempPath);
                }
            }
        }
        sftp.put(input,sftpFileName);  //上传文件
        System.out.println("上传"+sftpFileName+"成功()");
    }
    /**
     * 将文件夹上传到sftp。ftp文件完整路径=basePath+directory
     * @param basePath 服务器的基础路径
     * @param directory 上传到该目录
     * @param localDirectory 本地完整文件夹路径
     */
    public void uploadFiles(String basePath,String directory,String localDirectory) throws SftpException{
        try {
            sftp.cd(basePath);
            sftp.cd(directory);
        } catch (SftpException e) {
            //目录不存在，则创建文件夹
            String [] dirs=directory.split("/");
            String tempPath=basePath;
            for(String dir:dirs){
                if(null== dir || "".equals(dir)) continue;
                tempPath+="/"+dir;
                try{
                    sftp.cd(tempPath);
                }catch(SftpException ex){
                    sftp.mkdir(tempPath);
                    sftp.cd(tempPath);
                }
            }
        }
        File file = new File(localDirectory);
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                try {
                    FileInputStream in = new FileInputStream(list[i]);
                    sftp.put(in, list[i].getName());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("上传文件夹成功");
        }
        // 读入文件流
//        sftp.put(localDirectory,basePath+directory);
    }

    /**
     * 下载文件。
     * @param directory 下载目录
     * @param downloadFile 下载的文件
     * @param saveFile 存在本地的路径
     */
    public void download(String directory, String downloadFile, String saveFile) throws SftpException, FileNotFoundException{
        if (directory != null && !"".equals(directory)) {
            sftp.cd(directory);
        }
        File file = new File(saveFile);
        sftp.get(downloadFile, new FileOutputStream(file));
    }

    public InputStream download(String directory) throws SftpException{

        return sftp.get(directory);
    }

    /**
     * 下载文件
     * @param directory 下载目录
     * @param downloadFile 下载的文件名
     * @return 字节数组
     */
    /*public byte[] download(String directory, String downloadFile) throws SftpException, IOException{
        if (directory != null && !"".equals(directory)) {
            sftp.cd(directory);
        }
//        InputStream is = sftp.get(downloadFile);

//        byte[] fileData = IOUtils.toByteArray(is);

        return fileData;
    }
*/

    /**
     * 删除文件
     * @param directory 要删除文件所在目录
     * @param deleteFile 要删除的文件
     */
    public void delete(String directory, String deleteFile) throws SftpException{
        sftp.cd(directory);
        sftp.rm(deleteFile);
    }


    /**
     * 列出目录下的文件
     * @param directory 要列出的目录
     * @param sftp
     */
    public Vector<?> listFiles(String directory) throws SftpException {
        return sftp.ls(directory);
    }

 /*   //上传文件测试
    public static void main(String[] args) throws SftpException, IOException {
        SFTPUtils sftp = new SFTPUtils();
        sftp.login();
        File file = new File("D:\\data\\a.txt");
        InputStream is = new FileInputStream(file);

        sftp.upload("/data/sftp/mysftp/upload","/temp", "123.txt", is);
        sftp.logout();


    }*/
}



