package com.cool.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductCategory extends BaseEntity {
    
    private Long id;
    private String name;
    private Long parentId;
    private Integer sort;
}
