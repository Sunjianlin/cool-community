package com.cool.pojo.vo;

import lombok.Data;

@Data
public class DashboardVO {
    private Long userCount;
    private Long postCount;
    private Long topicCount;
    private Long productCount;
    private Long todayPostCount;
    private Long todayUserCount;
}
