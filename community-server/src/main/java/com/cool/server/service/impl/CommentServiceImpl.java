package com.cool.server.service.impl;

import com.cool.common.constant.MessageConstant;
import com.cool.pojo.dto.CommentCreateDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.entity.Comment;
import com.cool.pojo.entity.Post;
import com.cool.pojo.entity.User;
import com.cool.pojo.vo.CommentVO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.CommentMapper;
import com.cool.server.mapper.PostMapper;
import com.cool.server.mapper.UserMapper;
import com.cool.server.service.CommentService;
import com.cool.server.service.producer.NotifyProducer;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    
    private final CommentMapper commentMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NotifyProducer notifyProducer;

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

        postMapper.addComment(dto.getPostId());
        
        commentMapper.insert(comment);
        
        Post post = postMapper.getById(dto.getPostId());
        User commenter = userMapper.getById(userId);
        
        if (post != null && commenter != null) {
            if (dto.getParentId() != null && dto.getParentId() > 0) {
                Comment parentComment = commentMapper.getById(dto.getParentId());
                if (parentComment != null && !parentComment.getUserId().equals(userId)) {
                    notifyProducer.sendCommentReplyNotify(
                            parentComment.getUserId(), 
                            userId, 
                            commenter.getNickname(), 
                            dto.getPostId(), 
                            post.getTitle(), 
                            comment.getId()
                    );
                }
            } else {
                if (!post.getUserId().equals(userId)) {
                    notifyProducer.sendPostCommentNotify(
                            post.getUserId(), 
                            userId, 
                            commenter.getNickname(), 
                            dto.getPostId(), 
                            post.getTitle()
                    );
                }
            }
        }
        
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
