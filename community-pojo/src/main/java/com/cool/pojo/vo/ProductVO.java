package com.cool.pojo.vo;

import lombok.Data;

@Data
public class ProductVO {
    
    private Long id;
    private String name;
    private String description;
    private String image;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private String price;
    private String specs;
    private Integer reviewCount;
    private Double avgRating;
}
