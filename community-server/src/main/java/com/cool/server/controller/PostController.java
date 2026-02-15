package com.cool.server.controller;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.PostCreateDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.PostVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.PostService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
public class PostController {
    
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/create")
    @RequireLogin
    public Long createPost(@RequestBody PostCreateDTO dto) {
        return postService.createPost(dto);
    }

    @GetMapping("/list")
    public PageVO<PostVO> getPostList(PageQueryDTO dto) {
        return postService.getPostList(dto);
    }

    @GetMapping("/detail/{id}")
    public PostVO getPostDetail(@PathVariable Long id) {
        return postService.getPostDetail(id);
    }

    @DeleteMapping("/delete/{id}")
    @RequireLogin
    public void deletePost(@PathVariable Long id) {
        postService.deletePost(id);
    }

    @PostMapping("/like/{id}")
    @RequireLogin
    public void likePost(@PathVariable Long id) {
        postService.likePost(id);
    }

    @DeleteMapping("/unlike/{id}")
    @RequireLogin
    public void unlikePost(@PathVariable Long id) {
        postService.unlikePost(id);
    }

    @PostMapping("/collect/{id}")
    @RequireLogin
    public void collectPost(@PathVariable Long id) {
        postService.collectPost(id);
    }

    @DeleteMapping("/uncollect/{id}")
    @RequireLogin
    public void uncollectPost(@PathVariable Long id) {
        postService.uncollectPost(id);
    }

    @PostMapping("/approve/{id}")
    @RequireLogin
    public void approvePost(@PathVariable Long id) {
        postService.approvePost(id);
    }

    @PostMapping("/reject/{id}")
    @RequireLogin
    public void rejectPost(@PathVariable Long id) {
        postService.rejectPost(id);
    }

    @PostMapping("/top/{id}")
    @RequireLogin
    public void setTop(@PathVariable Long id) {
        postService.setTop(id);
    }

    @DeleteMapping("/top/{id}")
    @RequireLogin
    public void cancelTop(@PathVariable Long id) {
        postService.cancelTop(id);
    }

    @PostMapping("/essence/{id}")
    @RequireLogin
    public void setEssence(@PathVariable Long id) {
        postService.setEssence(id);
    }

    @DeleteMapping("/essence/{id}")
    @RequireLogin
    public void cancelEssence(@PathVariable Long id) {
        postService.cancelEssence(id);
    }
}
