package com.cool.server.controller.client;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.PostCreateDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.PostVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@Tag(name = "帖子接口", description = "帖子发布、列表、详情、点赞、收藏")
@RestController
@RequestMapping("/post")
public class PostClientController {
    
    private final PostService postService;

    public PostClientController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "发布帖子", description = "发布新帖子", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/create")
    public Long createPost(@RequestBody PostCreateDTO dto) {
        return postService.createPost(dto);
    }

    @Operation(summary = "帖子列表", description = "获取帖子列表，支持分页和筛选")
    @GetMapping("/list")
    public PageVO<PostVO> getPostList(PageQueryDTO dto) {
        return postService.getPostList(dto);
    }

    @Operation(summary = "帖子详情", description = "获取帖子详细信息")
    @GetMapping("/detail/{id}")
    public PostVO getPostDetail(@Parameter(description = "帖子ID") @PathVariable Long id) {
        return postService.getPostDetail(id);
    }

    @Operation(summary = "删除帖子", description = "删除自己的帖子", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @DeleteMapping("/delete/{id}")
    public void deletePost(@Parameter(description = "帖子ID") @PathVariable Long id) {
        postService.deletePost(id);
    }

    @Operation(summary = "点赞帖子", description = "为帖子点赞", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/like/{id}")
    public void likePost(@Parameter(description = "帖子ID") @PathVariable Long id) {
        postService.likePost(id);
    }

    @Operation(summary = "取消点赞", description = "取消帖子点赞", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @DeleteMapping("/unlike/{id}")
    public void unlikePost(@Parameter(description = "帖子ID") @PathVariable Long id) {
        postService.unlikePost(id);
    }

    @Operation(summary = "收藏帖子", description = "收藏帖子", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/collect/{id}")
    public void collectPost(@Parameter(description = "帖子ID") @PathVariable Long id) {
        postService.collectPost(id);
    }

    @Operation(summary = "取消收藏", description = "取消帖子收藏", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @DeleteMapping("/uncollect/{id}")
    public void uncollectPost(@Parameter(description = "帖子ID") @PathVariable Long id) {
        postService.uncollectPost(id);
    }
}
