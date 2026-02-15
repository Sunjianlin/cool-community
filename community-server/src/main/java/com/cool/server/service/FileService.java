package com.cool.server.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    
    String uploadFile(MultipartFile file);

    String[] uploadImages(MultipartFile[] files);
}
