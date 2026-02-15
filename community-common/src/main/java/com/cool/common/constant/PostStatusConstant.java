package com.cool.common.constant;

public class PostStatusConstant {
    public static final Integer PENDING = 0;
    public static final Integer PUBLISHED = 1;
    public static final Integer REJECTED = 2;
    
    public static final String[] STATUS_NAMES = {"待审核", "已发布", "已拒绝"};
    
    public static String getStatusName(Integer status) {
        if (status == null || status < 0 || status >= STATUS_NAMES.length) {
            return "未知状态";
        }
        return STATUS_NAMES[status];
    }
}
