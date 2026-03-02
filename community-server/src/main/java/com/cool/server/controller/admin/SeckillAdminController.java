package com.cool.server.controller.admin;

import com.cool.common.properties.AliOssProperties;
import com.cool.common.utils.AliOssUtil;
import com.cool.pojo.entity.SeckillActivity;
import com.cool.server.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 秒杀活动管理控制器
 */
@Tag(name = "秒杀活动管理", description = "秒杀活动管理接口")
@RestController
@RequestMapping("/admin/seckill")
public class SeckillAdminController {
    
    @Autowired
    private SeckillService seckillService;
    
    @Autowired
    private AliOssProperties aliOssProperties;
    
    @Operation(summary = "上传背景图", security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/upload")
    public Map<String, Object> uploadBackgroundImage(
            @Parameter(description = "背景图文件") @RequestParam("file") MultipartFile file) {
        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "seckill/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + suffix;
            
            // 上传到阿里云OSS
            String fileUrl = AliOssUtil.upload(
                    aliOssProperties.getEndpoint(),
                    aliOssProperties.getAccessKeyId(),
                    aliOssProperties.getAccessKeySecret(),
                    aliOssProperties.getBucketName(),
                    fileName,
                    file.getInputStream()
            );
            
            return Map.of("code", 200, "data", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Map.of("code", 500, "message", "上传失败");
        }
    }
    
    @Operation(summary = "创建活动", security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/create")
    public Map<String, Object> createActivity(@RequestBody SeckillActivity activity) {
        seckillService.createActivity(activity);
        return Map.of("code", 200, "message", "创建成功");
    }
    
    @Operation(summary = "更新活动", security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/update")
    public Map<String, Object> updateActivity(@RequestBody SeckillActivity activity) {
        seckillService.updateActivity(activity);
        return Map.of("code", 200, "message", "更新成功");
    }
    
    @Operation(summary = "删除活动", security = @SecurityRequirement(name = "Bearer"))
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteActivity(
            @Parameter(description = "活动ID") @PathVariable Long id) {
        seckillService.deleteActivity(id);
        return Map.of("code", 200, "message", "删除成功");
    }
    
    @Operation(summary = "获取活动列表", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/list")
    public Map<String, Object> getActivityList() {
        List<SeckillActivity> activities = seckillService.getActivityList();
        return Map.of("code", 200, "data", activities);
    }
}
