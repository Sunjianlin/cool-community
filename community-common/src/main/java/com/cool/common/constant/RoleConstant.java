package com.cool.common.constant;

public class RoleConstant {
    public static final Integer USER = 0;
    public static final Integer MODERATOR = 1;
    public static final Integer ADMIN = 2;
    public static final Integer SYSTEM = 3;
    
    public static final String[] ROLE_NAMES = {"普通用户", "版主", "管理员", "系统管理员"};
    
    public static String getRoleName(Integer role) {
        if (role == null || role < 0 || role >= ROLE_NAMES.length) {
            return "未知角色";
        }
        return ROLE_NAMES[role];
    }
}
