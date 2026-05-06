package com.cool.pojo.dto;

import lombok.Data;

@Data
public class SearchDTO {
    private String keyword;
    private Integer page = 1;
    private Integer pageSize = 10;
    private String type = "all";
}
