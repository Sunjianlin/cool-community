package com.cool.server.controller.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cool.pojo.Result;
import org.springframework.web.bind.annotation.*;


import com.cool.pojo.dto.NotificationSummaryDTO;
import com.cool.pojo.vo.*;
import com.cool.server.service.MessageCenterService;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/message-center")
@RequiredArgsConstructor
@Tag(name = "消息中心接口")
public class MessageCenterController {

    private final MessageCenterService messageCenterService;

    @GetMapping("/summary")
    @Operation(summary = "获取消息汇总")
    public Result<NotificationSummaryDTO> getNotificationSummary(Claims claims) {
        Long userId = Long.parseLong(claims.get("id").toString());
        NotificationSummaryDTO summary = messageCenterService.getNotificationSummary(userId);
        return Result.success(summary);
    }

    @GetMapping("/comment")
    @Operation(summary = "获取评论通知列表（包含评论回复和帖子评论）")
    public Result<Map<String, Object>> getCommentNotifications(
            Claims claims,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = Long.parseLong(claims.get("id").toString());
        List<CommentReplyNotificationVO> replyList = messageCenterService.getCommentReplyNotifications(userId, page, pageSize);
        List<PostCommentNotificationVO> commentList = messageCenterService.getPostCommentNotifications(userId, page, pageSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("replyList", replyList);
        result.put("commentList", commentList);
        result.put("current", page);
        result.put("size", pageSize);
        return Result.success(result);
    }

    @GetMapping("/like")
    @Operation(summary = "获取点赞通知列表")
    public Result<Map<String, Object>> getLikeNotifications(
            Claims claims,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = Long.parseLong(claims.get("id").toString());
        List<LikeNotificationVO> list = messageCenterService.getLikeNotifications(userId, page, pageSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("current", page);
        result.put("size", pageSize);
        return Result.success(result);
    }

    @GetMapping("/follow")
    @Operation(summary = "获取关注通知列表")
    public Result<Map<String, Object>> getFollowNotifications(
            Claims claims,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = Long.parseLong(claims.get("id").toString());
        List<FollowNotificationVO> list = messageCenterService.getFollowNotifications(userId, page, pageSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("current", page);
        result.put("size", pageSize);
        return Result.success(result);
    }

    @GetMapping("/system")
    @Operation(summary = "获取系统通知列表")
    public Result<Map<String, Object>> getSystemNotifications(
            Claims claims,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = Long.parseLong(claims.get("id").toString());
        List<SystemNotificationVO> list = messageCenterService.getSystemNotifications(userId, page, pageSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("current", page);
        result.put("size", pageSize);
        return Result.success(result);
    }

    @PutMapping("/comment-reply/{id}/read")
    @Operation(summary = "标记评论回复通知已读")
    public Result<Void> markCommentReplyAsRead(Claims claims, @PathVariable Long id) {
        Long userId = Long.parseLong(claims.get("id").toString());
        messageCenterService.markCommentReplyAsRead(userId, id);
        return Result.success();
    }

    @PutMapping("/post-comment/{id}/read")
    @Operation(summary = "标记帖子评论通知已读")
    public Result<Void> markPostCommentAsRead(Claims claims, @PathVariable Long id) {
        Long userId = Long.parseLong(claims.get("id").toString());
        messageCenterService.markPostCommentAsRead(userId, id);
        return Result.success();
    }

    @PutMapping("/comment/read-all")
    @Operation(summary = "标记所有评论通知已读")
    public Result<Void> markAllCommentAsRead(Claims claims) {
        Long userId = Long.parseLong(claims.get("id").toString());
        messageCenterService.markAllCommentReplyAsRead(userId);
        messageCenterService.markAllPostCommentAsRead(userId);
        return Result.success();
    }

    @PutMapping("/like/{id}/read")
    @Operation(summary = "标记点赞通知已读")
    public Result<Void> markLikeAsRead(Claims claims, @PathVariable Long id) {
        Long userId = Long.parseLong(claims.get("id").toString());
        messageCenterService.markLikeAsRead(userId, id);
        return Result.success();
    }

    @PutMapping("/like/read-all")
    @Operation(summary = "标记所有点赞通知已读")
    public Result<Void> markAllLikeAsRead(Claims claims) {
        Long userId = Long.parseLong(claims.get("id").toString());
        messageCenterService.markAllLikeAsRead(userId);
        return Result.success();
    }

    @PutMapping("/follow/{id}/read")
    @Operation(summary = "标记关注通知已读")
    public Result<Void> markFollowAsRead(Claims claims, @PathVariable Long id) {
        Long userId = Long.parseLong(claims.get("id").toString());
        messageCenterService.markFollowAsRead(userId, id);
        return Result.success();
    }

    @PutMapping("/follow/read-all")
    @Operation(summary = "标记所有关注通知已读")
    public Result<Void> markAllFollowAsRead(Claims claims) {
        Long userId = Long.parseLong(claims.get("id").toString());
        messageCenterService.markAllFollowAsRead(userId);
        return Result.success();
    }

    @PutMapping("/system/{id}/read")
    @Operation(summary = "标记系统通知已读")
    public Result<Void> markSystemAsRead(Claims claims, @PathVariable Long id) {
        Long userId = Long.parseLong(claims.get("id").toString());
        messageCenterService.markSystemAsRead(userId, id);
        return Result.success();
    }

    @PutMapping("/system/read-all")
    @Operation(summary = "标记所有系统通知已读")
    public Result<Void> markAllSystemAsRead(Claims claims) {
        Long userId = Long.parseLong(claims.get("id").toString());
        messageCenterService.markAllSystemAsRead(userId);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "标记所有通知已读")
    public Result<Void> markAllAsRead(Claims claims) {
        Long userId = Long.parseLong(claims.get("id").toString());
        messageCenterService.markAllAsRead(userId);
        return Result.success();
    }
}
