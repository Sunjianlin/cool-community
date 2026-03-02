package com.cool.server.service.impl;

import com.cool.common.constant.MessageConstant;
import com.cool.common.constant.RedisConstant;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.TopicVO;
import com.cool.pojo.vo.UserVO;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.FollowMapper;
import com.cool.server.mapper.ProductMapper;
import com.cool.server.mapper.TopicMapper;
import com.cool.server.mapper.UserMapper;
import com.cool.server.service.FollowService;
import com.cool.server.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FollowServiceImpl implements FollowService {
    
    @Autowired
    private FollowMapper followMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TopicMapper topicMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Override
    @Transactional
    public void follow(Long targetId, int targetType) {
        Long userId = BaseContext.getCurrentId();
        
        // 检查目标是否存在
        checkTargetExists(targetId, targetType);
        
        // 检查是否已经关注
        if (isFollowing(userId, targetId, targetType)) {
            throw new RuntimeException("已经关注了该对象");
        }
        
        // 插入关注关系
        followMapper.insertFollow(userId, targetId, targetType);
        
        // 更新关注计数
        updateFollowCount(userId, targetId, targetType, true);
        
        // 缓存关注状态
        cacheFollowStatus(userId, targetId, targetType, true);
        
        // 发送关注通知（仅用户关注时）
        if (targetType == TYPE_USER) {
            notificationService.createFollowNotification(userId, targetId);
        }
        
        log.info("用户{}关注了{}:{} {}", userId, getTargetTypeName(targetType), targetType, targetId);
    }
    
    @Override
    @Transactional
    public void unfollow(Long targetId, int targetType) {
        Long userId = BaseContext.getCurrentId();
        
        // 检查是否已关注
        if (!isFollowing(userId, targetId, targetType)) {
            throw new RuntimeException("未关注该对象");
        }
        
        // 删除关注关系
        followMapper.deleteFollow(userId, targetId, targetType);
        
        // 更新关注计数
        updateFollowCount(userId, targetId, targetType, false);
        
        // 清除缓存
        clearFollowStatusCache(userId, targetId, targetType);
        
        log.info("用户{}取消关注了{}:{} {}", userId, getTargetTypeName(targetType), targetType, targetId);
    }
    
    @Override
    public boolean isFollowing(Long userId, Long targetId, int targetType) {
        // 先从缓存中查询
        String key = getFollowCacheKey(userId, targetId, targetType);
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return "1".equals(cached);
        }
        
        // 从数据库查询
        Integer count = followMapper.checkFollow(userId, targetId, targetType);
        boolean isFollowed = count != null && count > 0;
        
        // 缓存结果
        cacheFollowStatus(userId, targetId, targetType, isFollowed);
        
        return isFollowed;
    }
    
    @Override
    public PageVO<?> getFollowingList(Long userId, int targetType, PageQueryDTO dto) {
        int offset = (dto.getPage() - 1) * dto.getPageSize();
        List<?> list;
        Long total;
        
        switch (targetType) {
            case TYPE_USER:
                list = followMapper.getFollowingUserList(userId, offset, dto.getPageSize());
                total = followMapper.countFollows(userId, targetType);
                break;
            case TYPE_TOPIC:
                list = followMapper.getFollowingTopicList(userId, offset, dto.getPageSize());
                total = followMapper.countFollows(userId, targetType);
                break;
            case TYPE_PRODUCT:
                list = followMapper.getFollowingProductList(userId, offset, dto.getPageSize());
                total = followMapper.countFollows(userId, targetType);
                break;
            default:
                throw new RuntimeException("不支持的关注类型");
        }
        
        return PageVO.of(list, total, dto.getPage(), dto.getPageSize());
    }
    
    @Override
    public PageVO<?> getFollowerList(Long targetId, int targetType, PageQueryDTO dto) {
        int offset = (dto.getPage() - 1) * dto.getPageSize();
        List<?> list;
        Long total;
        
        switch (targetType) {
            case TYPE_USER:
                list = followMapper.getFollowerList(targetId, offset, dto.getPageSize());
                total = followMapper.countFollowers(targetId, targetType);
                break;
            case TYPE_TOPIC:
                list = followMapper.getTopicFollowerList(targetId, offset, dto.getPageSize());
                total = followMapper.countFollowers(targetId, targetType);
                break;
            case TYPE_PRODUCT:
                list = followMapper.getProductFollowerList(targetId, offset, dto.getPageSize());
                total = followMapper.countFollowers(targetId, targetType);
                break;
            default:
                throw new RuntimeException("不支持的关注类型");
        }
        
        return PageVO.of(list, total, dto.getPage(), dto.getPageSize());
    }
    
    @Override
    public Long getFollowCount(Long targetId, int targetType) {
        // 先从缓存查询
        String key = getFollowCountCacheKey(targetId, targetType);
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Long.parseLong(cached);
        }
        
        // 从数据库查询
        Long count;
        switch (targetType) {
            case TYPE_USER:
                count = userMapper.getFollowerCount(targetId);
                break;
            case TYPE_TOPIC:
                count = followMapper.countFollowers(targetId, targetType);
                break;
            case TYPE_PRODUCT:
                count = followMapper.countFollowers(targetId, targetType);
                break;
            default:
                throw new RuntimeException("不支持的关注类型");
        }
        
        // 缓存结果
        stringRedisTemplate.opsForValue().set(key, count.toString(), 1, TimeUnit.HOURS);
        
        return count;
    }
    
    @Override
    public Long getFollowerCount(Long userId, int targetType) {
        // 先从缓存查询
        String key = getFollowerCountCacheKey(userId, targetType);
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Long.parseLong(cached);
        }
        
        // 从数据库查询
        Long count = followMapper.countFollows(userId, targetType);
        
        // 缓存结果
        stringRedisTemplate.opsForValue().set(key, count.toString(), 1, TimeUnit.HOURS);
        
        return count;
    }
    
    /**
     * 检查目标是否存在
     */
    private void checkTargetExists(Long targetId, int targetType) {
        switch (targetType) {
            case TYPE_USER:
                if (userMapper.getById(targetId) == null) {
                    throw new RuntimeException(MessageConstant.USER_NOT_FOUND);
                }
                break;
            case TYPE_TOPIC:
                if (topicMapper.getById(targetId) == null) {
                    throw new RuntimeException("话题不存在");
                }
                break;
            case TYPE_PRODUCT:
                if (productMapper.getEntityById(targetId) == null) {
                    throw new RuntimeException("产品不存在");
                }
                break;
            default:
                throw new RuntimeException("不支持的关注类型");
        }
    }
    
    /**
     * 更新关注计数
     */
    private void updateFollowCount(Long userId, Long targetId, int targetType, boolean isFollow) {
        switch (targetType) {
            case TYPE_USER:
                if (isFollow) {
                    userMapper.incrementFollowingCount(userId);
                    userMapper.incrementFollowerCount(targetId);
                } else {
                    userMapper.decrementFollowingCount(userId);
                    userMapper.decrementFollowerCount(targetId);
                }
                // 清除缓存
                clearUserFollowCountCache(userId, targetId);
                break;
            case TYPE_TOPIC:
                if (isFollow) {
                    topicMapper.incrementFollowCount(targetId);
                } else {
                    topicMapper.decrementFollowCount(targetId);
                }
                // 清除缓存
                clearTopicFollowCountCache(targetId);
                break;
            case TYPE_PRODUCT:
                if (isFollow) {
                    productMapper.incrementFollowCount(targetId);
                } else {
                    productMapper.decrementFollowCount(targetId);
                }
                // 清除缓存
                clearProductFollowCountCache(targetId);
                break;
        }
    }
    
    /**
     * 缓存关注状态
     */
    private void cacheFollowStatus(Long userId, Long targetId, int targetType, boolean isFollowed) {
        String key = getFollowCacheKey(userId, targetId, targetType);
        stringRedisTemplate.opsForValue().set(key, isFollowed ? "1" : "0", 1, TimeUnit.HOURS);
    }
    
    /**
     * 清除关注状态缓存
     */
    private void clearFollowStatusCache(Long userId, Long targetId, int targetType) {
        String key = getFollowCacheKey(userId, targetId, targetType);
        stringRedisTemplate.delete(key);
    }
    
    /**
     * 清除用户关注计数缓存
     */
    private void clearUserFollowCountCache(Long userId, Long targetId) {
        stringRedisTemplate.delete(getFollowerCountCacheKey(userId, TYPE_USER));
        stringRedisTemplate.delete(getFollowCountCacheKey(targetId, TYPE_USER));
    }
    
    /**
     * 清除话题关注计数缓存
     */
    private void clearTopicFollowCountCache(Long topicId) {
        stringRedisTemplate.delete(getFollowCountCacheKey(topicId, TYPE_TOPIC));
    }
    
    /**
     * 清除产品关注计数缓存
     */
    private void clearProductFollowCountCache(Long productId) {
        stringRedisTemplate.delete(getFollowCountCacheKey(productId, TYPE_PRODUCT));
    }
    
    /**
     * 获取关注缓存键
     */
    private String getFollowCacheKey(Long userId, Long targetId, int targetType) {
        return RedisConstant.FOLLOW_KEY + userId + ":" + targetType + ":" + targetId;
    }
    
    /**
     * 获取关注数量缓存键
     */
    private String getFollowCountCacheKey(Long targetId, int targetType) {
        return RedisConstant.FOLLOW_COUNT_KEY + targetType + ":" + targetId;
    }
    
    /**
     * 获取被关注数量缓存键
     */
    private String getFollowerCountCacheKey(Long userId, int targetType) {
        return RedisConstant.FOLLOWER_COUNT_KEY + targetType + ":" + userId;
    }
    
    /**
     * 获取目标类型名称
     */
    private String getTargetTypeName(int targetType) {
        switch (targetType) {
            case TYPE_USER:
                return "用户";
            case TYPE_TOPIC:
                return "话题";
            case TYPE_PRODUCT:
                return "产品";
            default:
                return "未知";
        }
    }
}
