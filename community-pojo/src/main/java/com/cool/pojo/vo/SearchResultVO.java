package com.cool.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchResultVO {
    private PageVO<PostVO> posts;
    private PageVO<UserVO> users;
    private PageVO<TopicVO> topics;
    private List<String> hotKeywords;
}
