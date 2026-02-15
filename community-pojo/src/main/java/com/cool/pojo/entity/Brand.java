package com.cool.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Brand extends BaseEntity {
    
    private Long id;
    private String name;
    private String logo;
    private String description;
}
