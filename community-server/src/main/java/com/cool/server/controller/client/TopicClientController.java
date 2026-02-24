package com.cool.server.controller.client;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.TopicCreateDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.TopicVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@Tag(name = "话题接口", description = "话题列表、详情、关注")
@RestController
@RequestMapping("/topic")
public class TopicClientController {
    
    private final TopicService topicService;

    public TopicClientController(TopicService topicService) {
        this.topicService = topicService;
    }

    @Operation(summary = "话题列表", description = "获取话题列表，支持分页")
    @GetMapping("/list")
    public PageVO<TopicVO> getTopicList(PageQueryDTO dto) {
        return topicService.getTopicList(dto);
    }

    @Operation(summary = "热门话题", description = "获取热门话题列表")
    @GetMapping("/hot")
    public PageVO<TopicVO> getHotTopics(PageQueryDTO dto) {
        return topicService.getHotTopics(dto);
    }

    @Operation(summary = "话题详情", description = "获取话题详细信息")
    @GetMapping("/detail/{id}")
    public TopicVO getTopicDetail(@Parameter(description = "话题ID") @PathVariable Long id) {
        return topicService.getTopicDetail(id);
    }

    @Operation(summary = "关注话题", description = "关注指定话题", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/follow/{id}")
    public void followTopic(@Parameter(description = "话题ID") @PathVariable Long id) {
        topicService.followTopic(id);
    }

    @Operation(summary = "取消关注话题", description = "取消关注指定话题", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @DeleteMapping("/unfollow/{id}")
    public void unfollowTopic(@Parameter(description = "话题ID") @PathVariable Long id) {
        topicService.unfollowTopic(id);
    }
}
