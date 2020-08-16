package com.cmall.manage.web.util;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class PmsUploadUtil {
    public static String uploadImage(MultipartFile multipartFile) {

        String imgUrl = "http://192.168.233.100";

        //上传图片到服务器

        //配置fdfs的全局链接地址
        String tracker = PmsUploadUtil.class.getResource("/tracker.conf").getPath();
        try {
            ClientGlobal.init(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TrackerClient trackerClient = new TrackerClient();

        //获得一个trackerServer的实例
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getTrackerServer();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //通过tracker获得一个Storage链接客户端
        StorageClient storageClient = new StorageClient(trackerServer, null);


        try {

            byte[] bytes = multipartFile.getBytes();  //获得上传的二进制对象


            //获得文件后缀名
            String originalFilename = multipartFile.getOriginalFilename();//a.jpg
            int i = originalFilename.lastIndexOf(".");
            String setName = originalFilename.substring(i + 1);//截取最后一个小数点后面的字符

            String[] uploadInfos = storageClient.upload_file(bytes, setName, null);

            for (String uploadInfo : uploadInfos) {

                imgUrl += "/" + uploadInfo;

            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return imgUrl;
    }
}
