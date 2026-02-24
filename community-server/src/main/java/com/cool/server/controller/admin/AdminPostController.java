package com.cool.server.controller.admin;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.PostVO;
import com.cool.server.annotation.RequireAdmin;
import com.cool.server.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-帖子管理", description = "帖子管理相关接口，需要管理员权限")
@RestController
@RequestMapping("/admin/post")
public class AdminPostController {
    
    private final PostService postService;

    public AdminPostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "帖子列表", description = "获取帖子列表，支持分页", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @GetMapping("/list")
    public PageVO<PostVO> getPostList(PageQueryDTO dto) {
        return postService.getPostList(dto);
    }

    @Operation(summary = "审核通过", description = "审核通过帖子", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PostMapping("/approve/{id}")
    public void approvePost(@Parameter(description = "帖子ID") @PathVariable Long id) {
        postService.approvePost(id);
    }

    @Operation(summary = "审核拒绝", description = "审核拒绝帖子", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PostMapping("/reject/{id}")
    public void rejectPost(@Parameter(description = "帖子ID") @PathVariable Long id) {
        postService.rejectPost(id);
    }

    @Operation(summary = "置顶帖子", description = "设置帖子为置顶", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PostMapping("/top/{id}")
    public void setTop(@Parameter(description = "帖子ID") @PathVariable Long id) {
        postService.setTop(id);
    }

    @Operation(summary = "取消置顶", description = "取消帖子置顶", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @DeleteMapping("/top/{id}")
    public void cancelTop(@Parameter(description = "帖子ID") @PathVariable Long id) {
        postService.cancelTop(id);
    }

    @Operation(summary = "设为精华", description = "设置帖子为精华", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PostMapping("/essence/{id}")
    public void setEssence(@Parameter(description = "帖子ID") @PathVariable Long id) {
        postService.setEssence(id);
    }

    @Operation(summary = "取消精华", description = "取消帖子精华", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @DeleteMapping("/essence/{id}")
    public void cancelEssence(@Parameter(description = "帖子ID") @PathVariable Long id) {
        postService.cancelEssence(id);
    }

    @Operation(summary = "删除帖子", description = "删除帖子", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @DeleteMapping("/{id}")
    public void deletePost(@Parameter(description = "帖子ID") @PathVariable Long id) {
        postService.deletePost(id);
    }
}
