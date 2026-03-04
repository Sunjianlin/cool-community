package com.cool.server.controller.client;

import com.cool.pojo.dto.CommentCreateDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.CommentVO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评论接口", description = "评论发布、列表、点赞")
@RestController
@RequestMapping("/comment")
public class CommentController {
    
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "发表评论", description = "为帖子发表评论", security = @SecurityRequirement(name = "Bearer"))
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public Long createComment(@RequestBody CommentCreateDTO commentCreateDTO) {
        return commentService.createComment(commentCreateDTO);
    }

    @Operation(summary = "评论列表", description = "获取帖子的评论列表")
    @GetMapping("/list/{postId}")
    public PageVO<CommentVO> getCommentList(
            @Parameter(description = "帖子ID") @PathVariable Long postId, 
            PageQueryDTO pageQueryDTO) {
        return commentService.getCommentList(postId, pageQueryDTO);
    }

    @Operation(summary = "删除评论", description = "删除自己的评论", security = @SecurityRequirement(name = "Bearer"))
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/{id}")
    public void deleteComment(@Parameter(description = "评论ID") @PathVariable Long id) {
        commentService.deleteComment(id);
    }

    @Operation(summary = "点赞评论", description = "为评论点赞", security = @SecurityRequirement(name = "Bearer"))
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/like/{id}")
    public void likeComment(@Parameter(description = "评论ID") @PathVariable Long id) {
        commentService.likeComment(id);
    }

    @Operation(summary = "取消点赞评论", description = "取消评论点赞", security = @SecurityRequirement(name = "Bearer"))
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/unlike/{id}")
    public void unlikeComment(@Parameter(description = "评论ID") @PathVariable Long id) {
        commentService.unlikeComment(id);
    }
}
