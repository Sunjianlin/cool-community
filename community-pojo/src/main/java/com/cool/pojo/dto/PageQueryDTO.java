package com.cool.pojo.dto;

import lombok.Data;

@Data
public class PageQueryDTO {
    
    private Integer page = 1;
    private Integer pageSize = 10;
    private String keyword;
    private Long topicId;
    private Long userId;
    private Long productId;
    private Integer type;
    private Long categoryId;
    private String category;
    private String brand;
    private Integer status;
    private Integer role;
    private Integer offset;

    public Integer getOffset() {
        this.offset = (page - 1) * pageSize;
        return this.offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}
