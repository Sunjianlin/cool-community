package com.cool.server.service.impl;

import com.cool.pojo.vo.DashboardVO;
import com.cool.server.mapper.DashboardMapper;
import com.cool.server.service.DashboardService;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {
    
    private final DashboardMapper dashboardMapper;

    public DashboardServiceImpl(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public DashboardVO getStats() {
        DashboardVO vo = new DashboardVO();
        vo.setUserCount(dashboardMapper.countUsers());
        vo.setPostCount(dashboardMapper.countPosts());
        vo.setTopicCount(dashboardMapper.countTopics());
        vo.setProductCount(dashboardMapper.countProducts());
        vo.setTodayPostCount(dashboardMapper.countTodayPosts());
        vo.setTodayUserCount(dashboardMapper.countTodayUsers());
        return vo;
    }
}
