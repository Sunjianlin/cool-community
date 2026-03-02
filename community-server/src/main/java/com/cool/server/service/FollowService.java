package com.cool.server.service;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.UserVO;

public interface FollowService {
    
    // 关注类型常量
    int TYPE_USER = 0;
    int TYPE_TOPIC = 1;
    int TYPE_PRODUCT = 2;
    
    /**
     * 关注用户/话题/产品
     * @param targetId 目标ID
     * @param targetType 目标类型
     */
    void follow(Long targetId, int targetType);
    
    /**
     * 取消关注
     * @param targetId 目标ID
     * @param targetType 目标类型
     */
    void unfollow(Long targetId, int targetType);
    
    /**
     * 检查是否已关注
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 是否已关注
     */
    boolean isFollowing(Long userId, Long targetId, int targetType);
    
    /**
     * 获取关注列表
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param dto 分页参数
     * @return 关注列表
     */
    PageVO<?> getFollowingList(Long userId, int targetType, PageQueryDTO dto);
    
    /**
     * 获取粉丝/关注者列表
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param dto 分页参数
     * @return 粉丝/关注者列表
     */
    PageVO<?> getFollowerList(Long targetId, int targetType, PageQueryDTO dto);
    
    /**
     * 获取关注数量
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 关注数量
     */
    Long getFollowCount(Long targetId, int targetType);
    
    /**
     * 获取被关注数量
     * @param userId 用户ID
     * @param targetType 目标类型
     * @return 被关注数量
     */
    Long getFollowerCount(Long userId, int targetType);
}
