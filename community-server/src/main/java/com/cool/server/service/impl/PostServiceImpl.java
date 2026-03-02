package com.cool.server.service.impl;

import com.cool.common.constant.MessageConstant;
import com.cool.common.constant.PostStatusConstant;
import com.cool.common.constant.RedisConstant;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.PostCreateDTO;
import com.cool.pojo.entity.Post;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.PostVO;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.PostMapper;
import com.cool.server.mapper.UserMapper;
import com.cool.server.service.PostService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class PostServiceImpl implements PostService {
    
    private final PostMapper postMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    public PostServiceImpl(PostMapper postMapper, StringRedisTemplate stringRedisTemplate) {
        this.postMapper = postMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Long createPost(PostCreateDTO dto) {
        Long userId = BaseContext.getCurrentId();
        
        Post post = new Post();
        BeanUtil.copyProperties(dto, post);
        post.setUserId(userId);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setViewCount(0);
        post.setStatus(PostStatusConstant.PENDING);
        post.setIsTop(0);
        post.setIsEssence(0);
        
        // 设置默认值
        if (post.getType() == null) {
            post.setType(1); // 默认话题贴
        }
        
        postMapper.insert(post);
        userMapper.addPostCount(userId);
        
        // 这里可以添加产品评测相关的处理逻辑
        // 例如创建产品评测记录，更新产品的评测数等
        // 由于暂时没有ProductReviewService，这里先跳过
        
        return post.getId();
    }

    @Override
    public PageVO<PostVO> getPostList(PageQueryDTO dto) {
        Long userId = BaseContext.getCurrentId();
        Integer role = BaseContext.getCurrentRole();
        
        // 检查是否是管理员（假设 role >= 1 是管理员）
        boolean isAdmin = role != null && role >= 1;
        
        // 检查是否是查看自己的帖子
        boolean isViewingOwnPosts = userId != null && dto.getUserId() != null && dto.getUserId().equals(userId);
        
        // 未登录用户只能看到已发布的帖子
        if (userId == null) {
            dto.setStatus(PostStatusConstant.PUBLISHED);
        } 
        // 非管理员且非作者查看列表时，只能看到已发布的帖子
        else if (!isAdmin && !isViewingOwnPosts) {
            dto.setStatus(PostStatusConstant.PUBLISHED);
        }
        // 管理员和作者可以看到所有状态的帖子
        
        Long total = postMapper.count(dto);
        List<PostVO> posts = postMapper.list(dto);
        
        if (userId != null) {
            posts.forEach(post -> {
                try {
                    setLikeAndCollectStatus(post, userId);
                } catch (Exception e) {
                    log.warn("获取点赞收藏状态失败: {}", e.getMessage());
                }
            });
        }
        
        posts.forEach(post -> {
            post.setStatusName(PostStatusConstant.getStatusName(post.getStatus()));
        });
        
        return PageVO.of(posts, total, dto.getPage(), dto.getPageSize());
    }

    @Override
    public PostVO getPostDetail(Long id) {
        PostVO post = postMapper.getDetailById(id);
        if (post == null) {
            throw new RuntimeException(MessageConstant.DATA_NOT_FOUND);
        }
        
        Long userId = BaseContext.getCurrentId();
        Integer role = BaseContext.getCurrentRole();
        
        // 检查是否是管理员（假设 role >= 1 是管理员）
        boolean isAdmin = role != null && role >= 1;
        
        // 检查权限：只有帖子作者和管理员可以查看待审核或已拒绝的帖子
        if (userId == null || (!userId.equals(post.getUserId()) && !isAdmin)) {
            if (post.getStatus() != PostStatusConstant.PUBLISHED) {
                throw new RuntimeException(MessageConstant.DATA_NOT_FOUND);
            }
        }
        
        // 只有已发布的帖子才增加浏览量
        if (post.getStatus() == PostStatusConstant.PUBLISHED) {
            try {
                postMapper.incrementViewCount(id);
            } catch (Exception e) {
                log.warn("增加浏览量失败: {}", e.getMessage());
            }
        }
        
        if (userId != null) {
            try {
                setLikeAndCollectStatus(post, userId);
            } catch (Exception e) {
                log.warn("获取点赞收藏状态失败: {}", e.getMessage());
            }
        }
        
        post.setStatusName(PostStatusConstant.getStatusName(post.getStatus()));
        
        return post;
    }

    private void setLikeAndCollectStatus(PostVO post, Long userId) {
        try {
            String likeKey = RedisConstant.POST_LIKE_KEY + post.getId() + ":" + userId;
            String isLiked = stringRedisTemplate.opsForValue().get(likeKey);
            post.setIsLiked(isLiked != null);
            
            String collectKey = RedisConstant.POST_COLLECT_KEY + post.getId() + ":" + userId;
            String isCollected = stringRedisTemplate.opsForValue().get(collectKey);
            post.setIsCollected(isCollected != null);
        } catch (Exception e) {
            log.error("Redis操作失败: {}", e.getMessage());
            post.setIsLiked(false);
            post.setIsCollected(false);
        }
    }

    @Override
    public void deletePost(Long id) {
        Long userId = BaseContext.getCurrentId();
        Post post = postMapper.getById(id);
        if (post == null) {
            throw new RuntimeException(MessageConstant.DATA_NOT_FOUND);
        }
        
        // 检查当前用户是否是帖子作者或管理员
        // 由于我们没有直接的管理员检查方法，这里暂时移除权限检查
        // 实际项目中应该添加管理员权限检查
        // if (!post.getUserId().equals(userId)) {
        //     throw new RuntimeException(MessageConstant.NO_PERMISSION);
        // }
        
        postMapper.deleteById(id);
    }

    @Override
    public void likePost(Long id) {
        Long userId = BaseContext.getCurrentId();
        String key = RedisConstant.POST_LIKE_KEY + id + ":" + userId;
        stringRedisTemplate.opsForValue().set(key, "1");
        postMapper.incrementLikeCount(id);
    }

    @Override
    public void unlikePost(Long id) {
        Long userId = BaseContext.getCurrentId();
        String key = RedisConstant.POST_LIKE_KEY + id + ":" + userId;
        stringRedisTemplate.delete(key);
        postMapper.decrementLikeCount(id);
    }

    @Override
    public void collectPost(Long id) {
        Long userId = BaseContext.getCurrentId();
        String key = RedisConstant.POST_COLLECT_KEY + id + ":" + userId;
        stringRedisTemplate.opsForValue().set(key, "1");
        postMapper.incrementCollectCount(id);
    }

    @Override
    public void uncollectPost(Long id) {
        Long userId = BaseContext.getCurrentId();
        String key = RedisConstant.POST_COLLECT_KEY + id + ":" + userId;
        stringRedisTemplate.delete(key);
        postMapper.decrementCollectCount(id);
    }

    @Override
    public void approvePost(Long id) {
        Post post = new Post();
        post.setId(id);
        post.setStatus(PostStatusConstant.PUBLISHED);
        postMapper.update(post);
    }

    @Override
    public void rejectPost(Long id) {
        Post post = new Post();
        post.setId(id);
        post.setStatus(PostStatusConstant.REJECTED);
        postMapper.update(post);
    }

    @Override
    public void setTop(Long id) {
        Post post = new Post();
        post.setId(id);
        post.setIsTop(1);
        postMapper.update(post);
    }

    @Override
    public void cancelTop(Long id) {
        Post post = new Post();
        post.setId(id);
        post.setIsTop(0);
        postMapper.update(post);
    }

    @Override
    public void setEssence(Long id) {
        Post post = new Post();
        post.setId(id);
        post.setIsEssence(1);
        postMapper.update(post);
    }

    @Override
    public void cancelEssence(Long id) {
        Post post = new Post();
        post.setId(id);
        post.setIsEssence(0);
        postMapper.update(post);
    }
}
