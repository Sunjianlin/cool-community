package com.cool.pojo.document;

import lombok.Data;

import java.util.Date;

@Data
public class UserDocument {
    
    private Long id;
    
    private String username;
    
    private String nickname;
    
    private String avatar;
    
    private String bio;
    
    private Integer gender;
    
    private Integer status;
    
    private Integer followerCount;
    
    private Integer postCount;
    
    private Date createTime;
}
