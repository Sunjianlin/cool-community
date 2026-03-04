package com.cool.common.constant;

/**
 * 积分常量类
 */
public class PointsConstant {

    // 积分类型
    public static final int TYPE_INIT = 0; // 初始化
    public static final int TYPE_CHECKIN = 1; // 签到
    public static final int TYPE_POST_CREATE = 2; // 发帖
    public static final int TYPE_COMMENT_CREATE = 3; // 评论
    public static final int TYPE_BE_LIKED = 4; // 被点赞
    public static final int TYPE_BE_FOLLOWED = 5; // 被关注
    public static final int TYPE_PRODUCT_REVIEW = 6; // 产品评论
    public static final int TYPE_SECKILL_SUCCESS = 7; // 秒杀成功

    // 签到积分
    public static final int CHECKIN_POINTS = 10; // 基础签到积分
    public static final int CHECKIN_BONUS_3_DAYS = 5; // 连续3天额外奖励
    public static final int CHECKIN_BONUS_7_DAYS = 10; // 连续7天额外奖励
    public static final int CHECKIN_BONUS_30_DAYS = 20; // 连续30天额外奖励

    // 发帖积分
    public static final int POST_CREATE_POINTS = 5; // 发布帖子

    // 评论积分
    public static final int COMMENT_CREATE_POINTS = 2; // 发表评论

    // 点赞积分
    public static final int LIKE_POINTS = 1; // 获得点赞

    // 关注积分
    public static final int FOLLOW_POINTS = 1; // 关注用户
    public static final int BE_FOLLOWED_POINTS = 2; // 被关注

    // 产品评论积分
    public static final int PRODUCT_REVIEW_POINTS = 3; // 发表产品评论

    // 秒杀活动积分
    public static final int SECKILL_SUCCESS_POINTS = 10; // 秒杀成功
}
