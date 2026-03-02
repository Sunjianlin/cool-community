package com.cool.server.service;

import com.cool.pojo.entity.UserBackground;
import java.util.List;

/**
 * 用户背景图服务接口
 */
public interface UserBackgroundService {
    
    /**
     * 获取用户的背景图列表
     */
    List<UserBackground> getBackgroundList(Long userId);
    
    /**
     * 获取用户当前使用的背景图
     */
    UserBackground getCurrentBackground(Long userId);
    
    /**
     * 设置当前背景图
     */
    void setCurrentBackground(Long userId, Long backgroundId);
    
    /**
     * 添加背景图
     */
    void addBackground(Long userId, String backgroundImage);
}
