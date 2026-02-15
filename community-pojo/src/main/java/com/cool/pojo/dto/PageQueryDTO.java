package com.cool.pojo.dto;

import lombok.Data;

@Data
public class PageQueryDTO {
    
    private Integer page = 1;
    private Integer pageSize = 10;
    private String keyword;
    private Long topicId;
    private Long userId;
    private Long categoryId;
    private String brand;
    private Integer status;

    public Integer getOffset() {
        return (page - 1) * pageSize;
    }
}
