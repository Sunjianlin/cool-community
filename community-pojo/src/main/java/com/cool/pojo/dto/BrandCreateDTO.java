package com.cool.pojo.dto;

import lombok.Data;

@Data
public class BrandCreateDTO {
    
    private Long id;
    private String name;
    private String logo;
    private String description;
}
