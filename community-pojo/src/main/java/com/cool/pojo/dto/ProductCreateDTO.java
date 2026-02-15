package com.cool.pojo.dto;

import lombok.Data;

@Data
public class ProductCreateDTO {
    
    private Long id;
    private String name;
    private String description;
    private String image;
    private String brand;
    private Long categoryId;
    private String price;
    private String specs;
}
