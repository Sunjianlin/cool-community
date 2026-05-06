package com.cool.pojo.document;

import lombok.Data;

import java.util.Date;

@Data
public class TopicDocument {
    
    private Long id;
    
    private String name;
    
    private String nameSuggest;
    
    private String description;
    
    private String icon;
    
    private String category;
    
    private Integer followCount;
    
    private Integer postCount;
    
    private Integer status;
    
    private Boolean isHot;
    
    private Date createTime;
}
