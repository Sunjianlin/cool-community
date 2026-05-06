package com.cool.server.service;

import com.cool.pojo.dto.SearchDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.PostVO;
import com.cool.pojo.vo.SearchResultVO;
import com.cool.pojo.vo.TopicVO;
import com.cool.pojo.vo.UserVO;

import java.util.List;

public interface SearchService {
    
    SearchResultVO searchAll(SearchDTO searchDTO);
    
    PageVO<PostVO> searchPosts(SearchDTO searchDTO);
    
    PageVO<UserVO> searchUsers(SearchDTO searchDTO);
    
    PageVO<TopicVO> searchTopics(SearchDTO searchDTO);
    
    List<String> getSuggestions(String prefix);
    
    List<String> getHotKeywords();
    
    void syncPostToEs(Long postId);
    
    void syncUserToEs(Long userId);
    
    void syncTopicToEs(Long topicId);
    
    void syncAllData();
}
