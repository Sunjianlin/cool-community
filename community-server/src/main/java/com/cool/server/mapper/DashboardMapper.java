package com.cool.server.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DashboardMapper {
    
    Long countUsers();
    
    Long countPosts();
    
    Long countTopics();
    
    Long countProducts();
    
    Long countTodayPosts();
    
    Long countTodayUsers();
}
