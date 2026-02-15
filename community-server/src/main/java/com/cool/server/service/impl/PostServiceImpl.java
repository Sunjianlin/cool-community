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
import com.cool.server.service.PostService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {
    
    private final PostMapper postMapper;
    private final StringRedisTemplate stringRedisTemplate;

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
        post.setStatus(PostStatusConstant.PUBLISHED);
        post.setIsTop(0);
        post.setIsEssence(0);
        
        postMapper.insert(post);
        return post.getId();
    }

    @Override
    public PageVO<PostVO> getPostList(PageQueryDTO dto) {
        Long total = postMapper.count(dto);
        List<PostVO> posts = postMapper.list(dto);
        
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            posts.forEach(post -> {
                setLikeAndCollectStatus(post, userId);
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
        
        postMapper.incrementViewCount(id);
        
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            setLikeAndCollectStatus(post, userId);
        }
        
        post.setStatusName(PostStatusConstant.getStatusName(post.getStatus()));
        
        return post;
    }

    private void setLikeAndCollectStatus(PostVO post, Long userId) {
        String likeKey = RedisConstant.POST_LIKE_KEY + post.getId() + ":" + userId;
        String isLiked = stringRedisTemplate.opsForValue().get(likeKey);
        post.setIsLiked(isLiked != null);
        
        String collectKey = RedisConstant.POST_COLLECT_KEY + post.getId() + ":" + userId;
        String isCollected = stringRedisTemplate.opsForValue().get(collectKey);
        post.setIsCollected(isCollected != null);
    }

    @Override
    public void deletePost(Long id) {
        Long userId = BaseContext.getCurrentId();
        Post post = postMapper.getById(id);
        if (post == null) {
            throw new RuntimeException(MessageConstant.DATA_NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException(MessageConstant.NO_PERMISSION);
        }
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
