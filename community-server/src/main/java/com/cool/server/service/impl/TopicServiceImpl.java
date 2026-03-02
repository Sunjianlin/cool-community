package com.cool.server.service.impl;

import com.cool.common.constant.MessageConstant;
import com.cool.common.constant.RedisConstant;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.TopicCreateDTO;
import com.cool.pojo.entity.Topic;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.TopicVO;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.TopicMapper;
import com.cool.server.service.FollowService;
import com.cool.server.service.TopicService;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class TopicServiceImpl implements TopicService {
    
    private final TopicMapper topicMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final FollowService followService;

    public TopicServiceImpl(TopicMapper topicMapper, StringRedisTemplate stringRedisTemplate, FollowService followService) {
        this.topicMapper = topicMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.followService = followService;
    }

    @Override
    public Long createTopic(TopicCreateDTO dto) {
        Topic topic = new Topic();
        BeanUtil.copyProperties(dto, topic);
        topic.setFollowCount(0);
        topic.setPostCount(0);
        topic.setStatus(1);
        topic.setIsHot(0);
        
        topicMapper.insert(topic);
        return topic.getId();
    }

    @Override
    public void updateTopic(TopicCreateDTO dto) {
        Topic topic = new Topic();
        BeanUtil.copyProperties(dto, topic);
        topicMapper.update(topic);
    }

    @Override
    public void deleteTopic(Long id) {
        topicMapper.deleteById(id);
    }

    @Override
    public PageVO<TopicVO> getTopicList(PageQueryDTO dto) {
        Long total = topicMapper.count(dto);
        List<TopicVO> topics = topicMapper.list(dto);
        
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            topics.forEach(topic -> {
                String key = RedisConstant.TOPIC_FOLLOW_KEY + topic.getId() + ":" + userId;
                String isFollowed = stringRedisTemplate.opsForValue().get(key);
                topic.setIsFollowed(isFollowed != null);
            });
        }
        
        return PageVO.of(topics, total, dto.getPage(), dto.getPageSize());
    }

    @Override
    public PageVO<TopicVO> getHotTopics(PageQueryDTO dto) {
        List<TopicVO> topics = topicMapper.listHot(dto);
        Long total = topicMapper.countHot();
        
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            topics.forEach(topic -> {
                String key = RedisConstant.TOPIC_FOLLOW_KEY + topic.getId() + ":" + userId;
                String isFollowed = stringRedisTemplate.opsForValue().get(key);
                topic.setIsFollowed(isFollowed != null);
            });
        }
        
        return PageVO.of(topics, total, dto.getPage(), dto.getPageSize());
    }

    @Override
    public TopicVO getTopicDetail(Long id) {
        TopicVO topic = topicMapper.getDetailById(id);
        if (topic == null) {
            throw new RuntimeException(MessageConstant.DATA_NOT_FOUND);
        }
        
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            boolean isFollowed = followService.isFollowing(userId, id, FollowService.TYPE_TOPIC);
            topic.setIsFollowed(isFollowed);
        }
        
        return topic;
    }

    @Override
    public void followTopic(Long id) {
        followService.follow(id, FollowService.TYPE_TOPIC);
    }

    @Override
    public void unfollowTopic(Long id) {
        followService.unfollow(id, FollowService.TYPE_TOPIC);
    }

    @Override
    public void setHot(Long id, boolean isHot) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setIsHot(isHot ? 1 : 0);
        topicMapper.update(topic);
    }
}
