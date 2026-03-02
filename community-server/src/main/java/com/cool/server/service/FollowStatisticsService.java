package com.cool.server.service;

import com.cool.pojo.vo.StatisticVO;

import java.util.List;
import java.util.Map;

public interface FollowStatisticsService {
    
    List<StatisticVO> getFollowGrowthTrend(int days);
    Map<String, Long> getFollowTypeDistribution();
    List<StatisticVO> getTopFollowedUsers(int limit);
    List<StatisticVO> getTopFollowedTopics(int limit);
    List<StatisticVO> getTopFollowedProducts(int limit);
}
