package com.cool.server.service.impl;

import com.cool.pojo.entity.UserBackground;
import com.cool.server.mapper.UserBackgroundMapper;
import com.cool.server.service.UserBackgroundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户背景图服务实现类
 */
@Service
public class UserBackgroundServiceImpl implements UserBackgroundService {
    
    private static final Logger log = LoggerFactory.getLogger(UserBackgroundServiceImpl.class);
    
    @Autowired
    private UserBackgroundMapper backgroundMapper;
    
    @Override
    public List<UserBackground> getBackgroundList(Long userId) {
        return backgroundMapper.selectByUserId(userId);
    }
    
    @Override
    public UserBackground getCurrentBackground(Long userId) {
        return backgroundMapper.selectCurrentByUserId(userId);
    }
    
    @Override
    @Transactional
    public void setCurrentBackground(Long userId, Long backgroundId) {
        try {
            // 1. 将所有背景图设置为非当前
            backgroundMapper.updateAllToNonCurrent(userId);
            
            // 2. 将指定背景图设置为当前
            backgroundMapper.updateToCurrent(backgroundId, userId);
            
            log.info("设置用户背景图: {}, {}", userId, backgroundId);
        } catch (Exception e) {
            log.error("设置用户背景图失败: {}", e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void addBackground(Long userId, String backgroundImage) {
        UserBackground background = new UserBackground();
        background.setUserId(userId);
        background.setBackgroundImage(backgroundImage);
        background.setIsCurrent(0); // 默认为非当前
        background.setAcquireTime(LocalDateTime.now());
        background.setCreateTime(LocalDateTime.now());
        
        backgroundMapper.insert(background);
        log.info("添加用户背景图: {}, {}", userId, backgroundImage);
    }
}
