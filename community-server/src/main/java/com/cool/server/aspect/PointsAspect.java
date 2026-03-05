package com.cool.server.aspect;

import com.cool.common.constant.PointsConstant;
import com.cool.common.enumeration.SeckillResult;
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
        if (BaseContext.getCurrentId() != null) {
            userPointsService.addPoints(PointsConstant.POST_CREATE_POINTS, PointsConstant.TYPE_POST_CREATE);
        }
    }

    /**
     * 评论后增加积分
     */
    @AfterReturning("commentCreatePointcut()")
    public void addPointsAfterCommentCreate(JoinPoint joinPoint) {
        if (BaseContext.getCurrentId() != null) {
            userPointsService.addPoints(PointsConstant.COMMENT_CREATE_POINTS, PointsConstant.TYPE_COMMENT_CREATE);
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
                // 临时设置目标用户ID到BaseContext，以便在addPoints方法中获取
                Long originalUserId = BaseContext.getCurrentId();
                BaseContext.setCurrentId(targetId);
                try {
                    userPointsService.addPoints(PointsConstant.LIKE_POINTS, PointsConstant.TYPE_BE_LIKED);
                } finally {
                    // 恢复原始用户ID
                    BaseContext.setCurrentId(originalUserId);
                }
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
                // 临时设置目标用户ID到BaseContext，以便在addPoints方法中获取
                Long originalUserId = BaseContext.getCurrentId();
                BaseContext.setCurrentId(followId);
                try {
                    userPointsService.addPoints(PointsConstant.BE_FOLLOWED_POINTS, PointsConstant.TYPE_BE_FOLLOWED);
                } finally {
                    // 恢复原始用户ID
                    BaseContext.setCurrentId(originalUserId);
                }
            }
        }
    }

    /**
     * 产品评论后增加积分
     */
    @AfterReturning("productReviewPointcut()")
    public void addPointsAfterProductReview(JoinPoint joinPoint) {
        if (BaseContext.getCurrentId() != null) {
            userPointsService.addPoints(PointsConstant.PRODUCT_REVIEW_POINTS, PointsConstant.TYPE_PRODUCT_REVIEW);
        }
    }

    /**
     * 秒杀成功后增加积分
     */
    @AfterReturning(value = "seckillSuccessPointcut()", returning = "result")
    public void addPointsAfterSeckillSuccess(JoinPoint joinPoint, Object result) {
        // 检查秒杀是否成功
        if (result instanceof SeckillResult) {
            SeckillResult seckillResult = (SeckillResult) result;
            if (seckillResult.isSuccess()) {
                Object[] args = joinPoint.getArgs();
                if (args.length >= 1) {
                    Long userId = (Long) args[0];
                    if (userId != null) {
                        Long originalUserId = BaseContext.getCurrentId();
                        BaseContext.setCurrentId(userId);
                        try {
                            userPointsService.addPoints(PointsConstant.SECKILL_SUCCESS_POINTS, PointsConstant.TYPE_SECKILL_SUCCESS);
                        } finally {
                            BaseContext.setCurrentId(originalUserId);
                        }
                    }
                }
            }
        }
    }
}
