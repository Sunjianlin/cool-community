package com.cool.server.aspect;

import com.cool.common.constant.PointsConstant;
import com.cool.server.context.BaseContext;
import com.cool.server.service.UserPointsService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 积分切面类
 */
@Aspect
@Component
public class PointsAspect {

    @Autowired
    private UserPointsService userPointsService;

    /**
     * 定义发帖切点
     */
    @Pointcut("execution(* com.cool.server.service.impl.PostServiceImpl.createPost(..))")
    public void postCreatePointcut() {}

    /**
     * 定义评论切点
     */
    @Pointcut("execution(* com.cool.server.service.impl.CommentServiceImpl.createComment(..))")
    public void commentCreatePointcut() {}

    /**
     * 定义点赞切点
     */
    @Pointcut("execution(* com.cool.server.service.impl.*ServiceImpl.*Like(..))")
    public void likePointcut() {}

    /**
     * 定义关注切点
     */
    @Pointcut("execution(* com.cool.server.service.impl.*ServiceImpl.*follow(..))")
    public void followPointcut() {}

    /**
     * 定义产品评论切点
     */
    @Pointcut("execution(* com.cool.server.service.impl.*ServiceImpl.*Review(..))")
    public void productReviewPointcut() {}

    /**
     * 定义秒杀成功切点
     */
    @Pointcut("execution(* com.cool.server.service.impl.*ServiceImpl.*Seckill(..))")
    public void seckillSuccessPointcut() {}

    /**
     * 发帖后增加积分
     */
    @AfterReturning("postCreatePointcut()")
    public void addPointsAfterPostCreate(JoinPoint joinPoint) {
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            userPointsService.addPoints(userId, PointsConstant.POST_CREATE_POINTS, "POST_CREATE", "发布帖子获得积分");
        }
    }

    /**
     * 评论后增加积分
     */
    @AfterReturning("commentCreatePointcut()")
    public void addPointsAfterCommentCreate(JoinPoint joinPoint) {
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            userPointsService.addPoints(userId, PointsConstant.COMMENT_CREATE_POINTS, "COMMENT_CREATE", "发表评论获得积分");
        }
    }

    /**
     * 点赞后增加积分（给被点赞的用户）
     */
    @AfterReturning("likePointcut()")
    public void addPointsAfterLike(JoinPoint joinPoint) {
        // 这里需要根据具体的方法参数获取被点赞的用户ID
        // 假设方法参数为 (Long userId, Long targetId, Integer type)
        Object[] args = joinPoint.getArgs();
        if (args.length >= 2) {
            Long targetId = (Long) args[1];
            if (targetId != null) {
                userPointsService.addPoints(targetId, PointsConstant.LIKE_POINTS, "BE_LIKED", "被点赞获得积分");
            }
        }
    }

    /**
     * 关注后增加积分（给被关注的用户）
     */
    @AfterReturning("followPointcut()")
    public void addPointsAfterFollow(JoinPoint joinPoint) {
        // 这里需要根据具体的方法参数获取被关注的用户ID
        // 假设方法参数为 (Long userId, Long followId, Integer type)
        Object[] args = joinPoint.getArgs();
        if (args.length >= 2) {
            Long followId = (Long) args[1];
            if (followId != null) {
                userPointsService.addPoints(followId, PointsConstant.BE_FOLLOWED_POINTS, "BE_FOLLOWED", "被关注获得积分");
            }
        }
    }

    /**
     * 产品评论后增加积分
     */
    @AfterReturning("productReviewPointcut()")
    public void addPointsAfterProductReview(JoinPoint joinPoint) {
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            userPointsService.addPoints(userId, PointsConstant.PRODUCT_REVIEW_POINTS, "PRODUCT_REVIEW", "评论产品获得积分");
        }
    }

    /**
     * 秒杀成功后增加积分
     */
    @AfterReturning(value = "seckillSuccessPointcut()", returning = "result")
    public void addPointsAfterSeckillSuccess(JoinPoint joinPoint, Object result) {
        // 检查秒杀是否成功
        if (result != null && (Boolean) result) {
            Object[] args = joinPoint.getArgs();
            if (args.length >= 1) {
                Long userId = (Long) args[0];
                if (userId != null) {
                    userPointsService.addPoints(userId, PointsConstant.SECKILL_SUCCESS_POINTS, "SECKILL_SUCCESS", "秒杀成功获得积分");
                }
            }
        }
    }
}
