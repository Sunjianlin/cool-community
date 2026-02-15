package com.cool.server.controller;

import com.cool.server.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {
    
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        return fileService.uploadFile(file);
    }

    @PostMapping("/upload/images")
    public String[] uploadImages(@RequestParam("files") MultipartFile[] files) {
        return fileService.uploadImages(files);
    }
}
