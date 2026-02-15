package com.cool.server.service.impl;

import com.cool.common.constant.MessageConstant;
import com.cool.pojo.dto.CommentCreateDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.entity.Comment;
import com.cool.pojo.vo.CommentVO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.CommentMapper;
import com.cool.server.service.CommentService;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    
    private final CommentMapper commentMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public CommentServiceImpl(CommentMapper commentMapper, StringRedisTemplate stringRedisTemplate) {
        this.commentMapper = commentMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Long createComment(CommentCreateDTO dto) {
        Long userId = BaseContext.getCurrentId();
        
        Comment comment = new Comment();
        BeanUtil.copyProperties(dto, comment);
        comment.setUserId(userId);
        comment.setLikeCount(0);
        comment.setStatus(1);
        
        commentMapper.insert(comment);
        return comment.getId();
    }

    @Override
    public PageVO<CommentVO> getCommentList(Long postId, PageQueryDTO dto) {
        Long total = commentMapper.countByPostId(postId);
        List<CommentVO> comments = commentMapper.listByPostId(postId, dto);
        
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            comments.forEach(comment -> {
                String key = "comment:like:" + comment.getId() + ":" + userId;
                String isLiked = stringRedisTemplate.opsForValue().get(key);
                comment.setIsLiked(isLiked != null);
            });
        }
        
        return PageVO.of(comments, total, dto.getPage(), dto.getPageSize());
    }

    @Override
    public void deleteComment(Long id) {
        Long userId = BaseContext.getCurrentId();
        Comment comment = commentMapper.getById(id);
        if (comment == null) {
            throw new RuntimeException(MessageConstant.DATA_NOT_FOUND);
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException(MessageConstant.NO_PERMISSION);
        }
        commentMapper.deleteById(id);
    }

    @Override
    public void likeComment(Long id) {
        Long userId = BaseContext.getCurrentId();
        String key = "comment:like:" + id + ":" + userId;
        stringRedisTemplate.opsForValue().set(key, "1");
        commentMapper.incrementLikeCount(id);
    }

    @Override
    public void unlikeComment(Long id) {
        Long userId = BaseContext.getCurrentId();
        String key = "comment:like:" + id + ":" + userId;
        stringRedisTemplate.delete(key);
        commentMapper.decrementLikeCount(id);
    }
}
