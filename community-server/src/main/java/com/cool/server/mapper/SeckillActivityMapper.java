package com.cool.server.mapper;

import com.cool.pojo.entity.SeckillActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * 秒杀活动Mapper
 */
@Mapper
public interface SeckillActivityMapper {
    
    /**
     * 根据ID查询
     */
    SeckillActivity selectById(Long id);
    
    /**
     * 根据日期查询活动
     */
    List<SeckillActivity> selectByDate(LocalDate date);
    
    /**
     * 查询次日活动
     */
    SeckillActivity selectNextDayActivity();
    
    /**
     * 插入活动
     */
    void insert(SeckillActivity activity);
    
    /**
     * 更新活动
     */
    void update(SeckillActivity activity);
    
    /**
     * 删除活动
     */
    void delete(Long id);
    
    /**
     * 查询活动列表
     */
    List<SeckillActivity> list();
}
