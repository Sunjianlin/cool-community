package com.cool.server.controller.client;

import com.cool.server.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件接口", description = "文件上传")
@RestController
@RequestMapping("/file")
public class FileController {
    
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "上传单个文件", description = "上传单个文件，返回文件URL")
    @PostMapping("/upload")
    public String uploadFile(@Parameter(description = "文件") @RequestParam("file") MultipartFile file) {
        return fileService.uploadFile(file);
    }

    @Operation(summary = "上传多张图片", description = "批量上传图片，返回图片URL数组")
    @PostMapping("/upload/images")
    public String[] uploadImages(@Parameter(description = "图片文件数组") @RequestParam("files") MultipartFile[] files) {
        return fileService.uploadImages(files);
    }
}
