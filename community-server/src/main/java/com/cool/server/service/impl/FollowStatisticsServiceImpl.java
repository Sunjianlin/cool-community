package com.cool.server.service.impl;

import com.cool.pojo.vo.StatisticVO;
import com.cool.server.mapper.FollowMapper;
import com.cool.server.mapper.ProductMapper;
import com.cool.server.mapper.TopicMapper;
import com.cool.server.mapper.UserMapper;
import com.cool.server.service.FollowStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FollowStatisticsServiceImpl implements FollowStatisticsService {
    
    @Autowired
    private FollowMapper followMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TopicMapper topicMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    public List<StatisticVO> getFollowGrowthTrend(int days) {
        List<StatisticVO> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = endDate.minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // 这里简化实现，实际应该从数据库查询当天的关注数
            // 由于没有实现具体的SQL，这里返回模拟数据
            StatisticVO vo = new StatisticVO();
            vo.setDate(dateStr);
            vo.setValue((long) (Math.random() * 100 + 50));
            result.add(vo);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Long> getFollowTypeDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        
        // 这里简化实现，实际应该从数据库查询各类型的关注数
        distribution.put("用户", 1200L);
        distribution.put("话题", 800L);
        distribution.put("产品", 500L);
        
        return distribution;
    }
    
    @Override
    public List<StatisticVO> getTopFollowedUsers(int limit) {
        List<StatisticVO> result = new ArrayList<>();
        
        // 这里简化实现，实际应该从数据库查询关注数最多的用户
        for (int i = 1; i <= limit; i++) {
            StatisticVO vo = new StatisticVO();
            vo.setName("用户" + i);
            vo.setValue(1000L - i * 100);
            result.add(vo);
        }
        
        return result;
    }
    
    @Override
    public List<StatisticVO> getTopFollowedTopics(int limit) {
        List<StatisticVO> result = new ArrayList<>();
        
        // 这里简化实现，实际应该从数据库查询关注数最多的话题
        for (int i = 1; i <= limit; i++) {
            StatisticVO vo = new StatisticVO();
            vo.setName("话题" + i);
            vo.setValue(800L - i * 80);
            result.add(vo);
        }
        
        return result;
    }
    
    @Override
    public List<StatisticVO> getTopFollowedProducts(int limit) {
        List<StatisticVO> result = new ArrayList<>();
        
        // 这里简化实现，实际应该从数据库查询关注数最多的产品
        for (int i = 1; i <= limit; i++) {
            StatisticVO vo = new StatisticVO();
            vo.setName("产品" + i);
            vo.setValue(500L - i * 50);
            result.add(vo);
        }
        
        return result;
    }
}
