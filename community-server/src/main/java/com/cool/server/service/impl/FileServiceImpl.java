package com.cool.server.service.impl;

import com.cool.common.constant.MessageConstant;
import com.cool.common.properties.AliOssProperties;
import com.cool.common.utils.AliOssUtil;
import com.cool.server.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    
    private final AliOssProperties aliOssProperties;

    public FileServiceImpl(AliOssProperties aliOssProperties) {
        this.aliOssProperties = aliOssProperties;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            return AliOssUtil.uploadImage(
                    aliOssProperties.getEndpoint(),
                    aliOssProperties.getAccessKeyId(),
                    aliOssProperties.getAccessKeySecret(),
                    aliOssProperties.getBucketName(),
                    file.getOriginalFilename(),
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException(MessageConstant.UPLOAD_FAILED);
        }
    }

    @Override
    public String[] uploadImages(MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = uploadFile(file);
            urls.add(url);
        }
        return urls.toArray(new String[0]);
    }
}
