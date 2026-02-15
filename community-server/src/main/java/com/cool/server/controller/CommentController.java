package com.cool.server.controller;

import com.cool.pojo.dto.CommentCreateDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.CommentVO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.CommentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {
    
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/create")
    @RequireLogin
    public Long createComment(@RequestBody CommentCreateDTO commentCreateDTO) {
        return commentService.createComment(commentCreateDTO);
    }

    @GetMapping("/list/{postId}")
    public PageVO<CommentVO> getCommentList(@PathVariable Long postId, PageQueryDTO pageQueryDTO) {
        return commentService.getCommentList(postId, pageQueryDTO);
    }

    @DeleteMapping("/delete/{id}")
    @RequireLogin
    public void deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
    }

    @PostMapping("/like/{id}")
    @RequireLogin
    public void likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
    }

    @DeleteMapping("/unlike/{id}")
    @RequireLogin
    public void unlikeComment(@PathVariable Long id) {
        commentService.unlikeComment(id);
    }
}
