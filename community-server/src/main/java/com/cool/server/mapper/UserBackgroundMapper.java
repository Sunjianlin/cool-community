package com.cool.server.mapper;

import com.cool.pojo.entity.UserBackground;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户背景图Mapper
 */
@Mapper
public interface UserBackgroundMapper {
    
    /**
     * 插入背景图
     */
    void insert(UserBackground background);
    
    /**
     * 查询用户的背景图列表
     */
    List<UserBackground> selectByUserId(Long userId);
    
    /**
     * 查询用户当前使用的背景图
     */
    UserBackground selectCurrentByUserId(Long userId);
    
    /**
     * 将用户所有背景图设置为非当前
     */
    void updateAllToNonCurrent(Long userId);
    
    /**
     * 将指定背景图设置为当前
     */
    void updateToCurrent(@Param("id") Long id, @Param("userId") Long userId);
}
