package com.cool.server.controller.admin;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.TopicCreateDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.TopicVO;
import com.cool.server.annotation.RequireAdmin;
import com.cool.server.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-话题管理", description = "话题管理相关接口，需要管理员权限")
@RestController
@RequestMapping("/admin/topic")
public class AdminTopicController {
    
    private final TopicService topicService;

    public AdminTopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @Operation(summary = "话题列表", description = "获取话题列表，支持分页", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @GetMapping("/list")
    public PageVO<TopicVO> getTopicList(PageQueryDTO dto) {
        return topicService.getTopicList(dto);
    }

    @Operation(summary = "创建话题", description = "创建新话题", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PostMapping("/create")
    public Long createTopic(@RequestBody TopicCreateDTO dto) {
        return topicService.createTopic(dto);
    }

    @Operation(summary = "更新话题", description = "更新话题信息", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PutMapping("/update")
    public void updateTopic(@RequestBody TopicCreateDTO dto) {
        topicService.updateTopic(dto);
    }

    @Operation(summary = "删除话题", description = "删除话题", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @DeleteMapping("/{id}")
    public void deleteTopic(@Parameter(description = "话题ID") @PathVariable Long id) {
        topicService.deleteTopic(id);
    }

    @Operation(summary = "设为热门", description = "设置话题为热门", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PostMapping("/setHot/{id}")
    public void setHot(@Parameter(description = "话题ID") @PathVariable Long id) {
        topicService.setHot(id, true);
    }

    @Operation(summary = "取消热门", description = "取消话题热门", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @DeleteMapping("/setHot/{id}")
    public void cancelHot(@Parameter(description = "话题ID") @PathVariable Long id) {
        topicService.setHot(id, false);
    }
}
