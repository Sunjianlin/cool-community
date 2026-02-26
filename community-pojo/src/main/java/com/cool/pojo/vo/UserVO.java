package com.cool.pojo.vo;

import lombok.Data;

@Data
public class UserVO {
    
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String bio;
    private Integer gender;
    private Integer status;
    private Integer role;
    private String roleName;
    private Long followingCount;
    private Long followerCount;
    private Integer postCount;
    private Integer likeCount;
    private Boolean isFollowing;
    private Integer onlineStatus;
}
