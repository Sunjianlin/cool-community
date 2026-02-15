package com.cool.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class AliOssUtil {
    
    public static String upload(String endpoint, String accessKeyId, String accessKeySecret, 
                                String bucketName, String objectName, InputStream inputStream) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(inputStream.available());
            PutObjectResult result = ossClient.putObject(bucketName, objectName, inputStream, metadata);
            if (result != null) {
                return "https://" + bucketName + "." + endpoint + "/" + objectName;
            }
        } catch (IOException e) {
            throw new RuntimeException("OSS上传失败", e);
        } finally {
            ossClient.shutdown();
        }
        return null;
    }

    public static String generateObjectName(String originalFilename) {
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + suffix;
    }

    public static String uploadImage(String endpoint, String accessKeyId, String accessKeySecret,
                                     String bucketName, String originalFilename, byte[] bytes) {
        String objectName = "images/" + generateObjectName(originalFilename);
        return upload(endpoint, accessKeyId, accessKeySecret, bucketName, objectName, new ByteArrayInputStream(bytes));
    }

    public static String uploadAvatar(String endpoint, String accessKeyId, String accessKeySecret,
                                      String bucketName, String originalFilename, byte[] bytes) {
        String objectName = "avatar/" + generateObjectName(originalFilename);
        return upload(endpoint, accessKeyId, accessKeySecret, bucketName, objectName, new ByteArrayInputStream(bytes));
    }
}
