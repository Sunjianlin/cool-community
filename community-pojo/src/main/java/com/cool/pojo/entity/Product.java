package com.cool.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {
    
    private Long id;
    private String name;
    private String description;
    private String image;
    private String brand;
    private Long categoryId;
    private String price;
    private String specs;
    private Integer reviewCount;
    private Integer followCount;
    private Integer status;
}
