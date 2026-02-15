package com.cool.server.controller;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.TopicCreateDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.TopicVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.TopicService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topic")
public class TopicController {
    
    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @PostMapping("/create")
    @RequireLogin
    public Long createTopic(@RequestBody TopicCreateDTO dto) {
        return topicService.createTopic(dto);
    }

    @PutMapping("/update")
    @RequireLogin
    public void updateTopic(@RequestBody TopicCreateDTO dto) {
        topicService.updateTopic(dto);
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public void deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
    }

    @GetMapping("/list")
    public PageVO<TopicVO> getTopicList(PageQueryDTO dto) {
        return topicService.getTopicList(dto);
    }

    @GetMapping("/hot")
    public PageVO<TopicVO> getHotTopics(PageQueryDTO dto) {
        return topicService.getHotTopics(dto);
    }

    @GetMapping("/detail/{id}")
    public TopicVO getTopicDetail(@PathVariable Long id) {
        return topicService.getTopicDetail(id);
    }

    @PostMapping("/follow/{id}")
    @RequireLogin
    public void followTopic(@PathVariable Long id) {
        topicService.followTopic(id);
    }

    @DeleteMapping("/unfollow/{id}")
    @RequireLogin
    public void unfollowTopic(@PathVariable Long id) {
        topicService.unfollowTopic(id);
    }
}
