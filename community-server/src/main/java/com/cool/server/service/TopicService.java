package com.cool.server.service;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.TopicCreateDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.TopicVO;

public interface TopicService {
    
    Long createTopic(TopicCreateDTO dto);

    void updateTopic(TopicCreateDTO dto);

    void deleteTopic(Long id);

    PageVO<TopicVO> getTopicList(PageQueryDTO dto);

    PageVO<TopicVO> getHotTopics(PageQueryDTO dto);

    TopicVO getTopicDetail(Long id);

    void followTopic(Long id);

    void unfollowTopic(Long id);

    void setHot(Long id, boolean isHot);
}
